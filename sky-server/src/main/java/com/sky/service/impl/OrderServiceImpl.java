package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ParamEmptyException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPageVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderMapper orderMapper;


    @Autowired
    OrderDetailMapper orderDetailMapper;


    @Autowired
    AddressBookMapper addressBookMapper;

    @Autowired
    ShoppingCartMapper shoppingCartMapper;

    /**
     * 用户下单实现：需要从前端接收的数据中，来构造order表和orderDetail表的数据
     * 所以需要这两个的数据库操作接口
     * order表和 orderDetail是1 对多的关系，也就是说
     * order表中的一条数据对应这orderDetail的多条数据
     * 用现实世界理解是，一个订单有多个商品，商品信息保存在orderDetail中，订单信息保存在order中
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO dto) {

        //1.DTO校验过程(地址簿，购物车为空抛出业务异常)
        if (dto == null)
            throw new ParamEmptyException("前端传入对象为空");
        //获取地址簿id
        Long addressId = dto.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressId);
        if (addressBook == null)
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);

        //获取购物车信息
        Long currentUserId = BaseContext.getCurrentId();
        //构造cart对象
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(currentUserId);
        List<ShoppingCart> carts = shoppingCartMapper.list(cart); //查询当前用户id的购物车所有商品数据
        if (carts.isEmpty() || carts == null)
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);


        //2.构造订单类
        Orders order = new Orders();

        /*复制前端传来的信息：
         addressBookId,amount,deliveryStatus,estimatedDeliveryTime
         packAmount,pyMethod,Remark,tablewareNumber,tablewareStatus
         */
        BeanUtils.copyProperties(dto, order);
        order.setOrderTime(LocalDateTime.now()); //设置下单时间
        order.setUserId(currentUserId); //设置下单的用户id
        order.setNumber(String.valueOf(System.currentTimeMillis())); //设置订单号：根据指定规则生成:当前系统的时间戳
        //order.setPayStatus(Orders.UN_PAID); // 设置支付状态，默认未支付
        order.setPayStatus(Orders.PAID); //跳过支付过程，模拟已支付状态
        order.setStatus(Orders.TO_BE_CONFIRMED); //设置订单状态，待接单
        //手机号在AddressBook中有保存
        order.setPhone(addressBook.getPhone());
        order.setConsignee(addressBook.getConsignee()); //设置收货人
        order.setCancelTime(LocalDateTime.now().plusHours(1)); //一小时后自动取消,付款成功将这个值设置为null
        if(dto.getEstimatedDeliveryTime()==null){
            order.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(1)); //预计一小时后送达
        }

        if(dto.getTablewareStatus()==1)
            order.setTablewareNumber(carts.size()); //设置餐具数量，根据购物车的数量设置
        //3.向订单表插入一条订单
        orderMapper.insert(order);

        long orderId = order.getId();

        List<OrderDetail> orderDetails = new ArrayList<>();

        //向订单明细表插入多条数据， insertBatch(...) 这些数据是来自购物车
        for (ShoppingCart sc : carts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(sc, orderDetail); //将每条商品信息拷贝到detail对象里
            //设置订单id
            orderDetail.setOrderId(order.getId());
            orderDetails.add(orderDetail);
        }
        //批量插入订单明细数据
        orderDetailMapper.insertBatch(orderDetails);
        //插入完成后，该用户购物车数据清空....

        //清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(currentUserId);

        //封装VO对象
        OrderSubmitVO vo = OrderSubmitVO.builder()
                .id(order.getId()) //设置订单id
                .orderTime(order.getOrderTime())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .build();
        return vo;
    }

    /**
     * 订单支付接口
     */
    @Override
    @Transactional //会设计更新订单
    public OrderPaymentVO pay(OrdersPaymentDTO dto) {

        //根据订单号获取订单，随后更新
        Orders order = orderMapper.getByNumber(dto.getOrderNumber());
        if (order == null)
            throw new OrderBusinessException("未查询到该订单");

        //幂等：如果是已支付(待接单)，直接返回，不再处理
        if (order.getStatus() == Orders.TO_BE_CONFIRMED)
            return null;

        //更新订单状态： status:待接单  PayStatus: 已支付
        order.setPayStatus(Orders.PAID); //已支付
        order.setStatus(Orders.TO_BE_CONFIRMED); //待接单
        order.setCheckoutTime(LocalDateTime.now()); //付款时间
        order.setPayMethod(dto.getPayMethod()); //付款方式
        String address =  addressBookMapper.getByUserId(BaseContext.getCurrentId()).toString();
        order.setAddress(address);
        //update更新
        orderMapper.update(order);


        //TODO 4.通知商家(webSocket)

        //5. 构造vo对象返回：由于没有实际调用支付接口，这里使用mock数据返回
        OrderPaymentVO paymentVO = OrderPaymentVO.builder()
                .nonceStr(UUID.randomUUID().toString())     // 随机串，无所谓
                .timeStamp(String.valueOf(System.currentTimeMillis()))  // 当前时间戳
                .signType("RSA")                             // 固定值
                .packageStr("prepay_id=mock123")             // 假的 prepay_id
                .paySign("mock-sign")                        // 假签名
                .build();


        return paymentVO;
    }


    /**
     * 查询当前用户历史订单
     *
     * @return
     */
    public PageResult<OrderPageVO> findHistoryOrder(OrdersPageQueryDTO dto) {


        PageResult<OrderPageVO> resultVo = new PageResult<>(); //返回对象

        Long currentUserId = BaseContext.getCurrentId();

        //先分页查询订单，采用pageHelper
        PageHelper.startPage(dto.getPage(), dto.getPageSize()); //设置偏移量
        Page<Orders> ordersPage = orderMapper.getPage(dto,currentUserId); // Page属于PageHelper
        List<Orders> orders = ordersPage.getResult(); //获取到订单列表

        if (ordersPage.isEmpty())
        {
            //返回空结果
            resultVo.setRecords(new ArrayList<>());
            resultVo.setTotal(0);
            return resultVo;
        }

        //Record
        List<OrderPageVO> vos = new ArrayList<>();

        for (Orders order : orders) {
            OrderPageVO vo = new OrderPageVO();
            BeanUtils.copyProperties(order, vo); //复制order属性
            //查询这个订单的订单关系列表
            List<OrderDetail> detailList = orderDetailMapper.select(order);
            vo.setOrderDetailList(detailList); //设置该订单的订单关系列表
            vos.add(vo);
        }

        resultVo.setRecords(vos);
        resultVo.setTotal(ordersPage.getTotal());

        return resultVo;
    }


    @Override
    public OrderVO orderDetail(Long id) {
        //查询订单
        Orders  orders = orderMapper.selectById(id);
        if(orders==null)
            throw new OrderBusinessException("查无此单");

        //查询地址簿，根据用户id
        AddressBook addressBook = addressBookMapper.getByUserId(BaseContext.getCurrentId());

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(orders,vo);
        vo.setAddress(addressBook.toString()); //设置地址簿
        //根据订单id查询订单详细表
        List<OrderDetail> detailList = orderDetailMapper.select(orders);
        if(detailList==null || detailList.isEmpty())
            throw new OrderBusinessException("该订单的详细信息为空");
        vo.setOrderDetailList(detailList);
        StringBuilder orderDishes = new StringBuilder();


        //获取订单详细的信息，拼接字符串
        for(OrderDetail detail :detailList){
            orderDishes.append(detail.getName())
                    .append("x")
                    .append(detail.getName());
        }

        vo.setOrderDishes(orderDishes.toString());
        return vo;
    }


    /**
     * 取消订单 逻辑删除订单，即将订单状态设置为已删除
     * 避免真实删除订单，后续统计需要订单数据
     * @param id
     */
    @Override
    @Transactional
    public void cancelOrder(Long id) {

        //先利用订单id查询出订单
        Orders order = orderMapper.selectById(id);
        if(order==null)
            throw new OrderBusinessException("查无此订单");


        /*取消订单需要设置的字段:
        status : 订单状态设置为已取消(CANCELLED=6)
        cancelTime :订单取消时间
        cancelReason: 订单取消原因
        payStatus : 如果已支付(PAID)就设置已退款，如果(UN_PAID)未支付就保持
         */
        order.setStatus(Orders.CANCELLED); //订单状态设置为取消
        order.setCancelTime(LocalDateTime.now()); //订单取消时间
        order.setCancelReason("用户取消订单");
        if(order.getPayStatus().equals(Orders.PAID))
            order.setPayStatus(2); //支付状态设置为退款

        //更新订单
        orderMapper.update(order);
    }

    /**
     * 再来一单：
     * 场景：前端点击再来一单
     * 传入订单id，重新调用下单接口
     * @param id
     */
    @Override
    public void againOrder(Long id) {
        //查询旧订单数据
        Orders olderOrders = orderMapper.selectById(id);
        if(olderOrders==null)
            throw new OrderBusinessException("旧订单数据丢失");

        if (!olderOrders.getUserId().equals(BaseContext.getCurrentId()))
            throw new OrderBusinessException("无权操作该订单");

        //构建创建新订单需要的数据
        OrdersSubmitDTO dto = new OrdersSubmitDTO();

        //从旧订单中获取数据
        BeanUtils.copyProperties(olderOrders,dto);

        //获取旧订单明细表
        List<OrderDetail> detailList = orderDetailMapper.select(olderOrders);

        //清空购物车，放在和已选的叠加
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
        //获取当前用户id
        long currentUserId = BaseContext.getCurrentId();

        //构造新的购物车

        for (OrderDetail detail: detailList){
            ShoppingCart cart = new ShoppingCart();
            cart.setId(null); //防止旧明细id带入数据库，设置为null数据库自增
            cart.setUserId(currentUserId);
            cart.setName(detail.getName()); //设置名称
            cart.setDishId(detail.getDishId());
            cart.setDishFlavor(detail.getDishFlavor());
            cart.setNumber(detail.getNumber());
            cart.setAmount(detail.getAmount());
            cart.setSetmealId(detail.getSetmealId());
            cart.setCreateTime(LocalDateTime.now());
            cart.setImage(detail.getImage());
            //插入购物车表
            shoppingCartMapper.insert(cart);
        }
    }

    /**
     * 根据条件查询订单 时间复杂度log(n^2)
     * @param condition
     * @return
     */

    //TODO 待优化
    @Override
    public PageResult<OrderVO> pageQuery(OrdersPageQueryDTO condition) {

        //分页查询
        PageHelper.startPage(condition.getPage(),condition.getPageSize());
        Page<Orders> page = orderMapper.getPage(condition);
        List<OrderVO> vos  = new ArrayList<>();

        //根据查询出来的订单对象构造vo列表
        List<Orders> orders = page.getResult();
        //空结果提取返回
        if(orders==null ||orders.isEmpty())
            return new PageResult<>(page.getTotal(),vos);

        for(Orders o:orders){
            //根据用户id查询地址填入对象
            AddressBook address = addressBookMapper.getByUserId(o.getUserId()); //可优化对象
            o.setAddress(address.toString());
            StringBuilder orderDishes = new StringBuilder(); //菜品字符串构造器
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(o,vo); //复制查询出来的属性

            //根据订单查询对应菜品信息
            List<OrderDetail> detailList = orderDetailMapper.select(o); //多次数据库连接
            for(OrderDetail d:detailList){
                orderDishes.append(d.getName()+"x"+d.getNumber()+",");
            }
            vo.setOrderDishes(orderDishes.toString()); //设置菜品名
            vos.add(vo);
        }

        PageResult<OrderVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(vos);
        return result;
    }
}


/**
 * 优化版
 * @Override
 * public PageResult<OrderVO> pageQuery(OrdersPageQueryDTO condition) {
 *     PageHelper.startPage(condition.getPage(), condition.getPageSize());
 *     Page<Orders> page = orderMapper.getPage(condition);
 *     List<Orders> orders = page.getResult();
 *
 *     List<OrderVO> vos = new ArrayList<>();
 *
 *     // 空结果提前返回（避免下面查空 IN 列表）
 *     if (orders == null || orders.isEmpty()) {
 *         return new PageResult<>(page.getTotal(), vos);
 *     }
 *
 *     // 1. 一次查出所有订单的明细（解决 N+1）
 *     List<OrderDetail> allDetails = orderDetailMapper.selectByOrderIds(
 *         orders.stream().map(Orders::getId).collect(Collectors.toList()));
 *
 *     // 2. 按 orderId 分组
 *     Map<Long, List<OrderDetail>> detailMap = allDetails.stream()
 *         .collect(Collectors.groupingBy(OrderDetail::getOrderId));
 *
 *     for (Orders o : orders) {
 *         OrderVO vo = new OrderVO();
 *         BeanUtils.copyProperties(o, vo);
 *
 *         // 3. 空明细防御
 *         List<OrderDetail> detailList = detailMap.getOrDefault(o.getId(), Collections.emptyList());
 *
 *         // 4. 拼接加分隔符（顿号）
 *         String orderDishes = detailList.stream()
 *             .map(d -> d.getName() + "x" + d.getNumber())
 *             .collect(Collectors.joining("，"));
 *         vo.setOrderDishes(orderDishes);
 *
 *         vos.add(vo);
 *     }
 *     return new PageResult<>(page.getTotal(), vos);
 * }
 *
 */
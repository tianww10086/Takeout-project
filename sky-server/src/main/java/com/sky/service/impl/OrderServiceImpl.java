package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
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
import com.sky.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collector;
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
        //获取地址
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
        order.setPayStatus(Orders.UN_PAID); // 设置支付状态，默认未支付
        //order.setPayStatus(Orders.PAID);
        order.setStatus(Orders.PENDING_PAYMENT); //设置订单状态，待付款
        //手机号在AddressBook中有保存
        order.setAddressBookId(addressBook.getId());
        //设置收货地址
        order.setAddress(addressBook.toString());
        order.setPhone(addressBook.getPhone());
        order.setConsignee(addressBook.getConsignee()); //设置收货人
        order.setCancelTime(LocalDateTime.now().plusHours(1)); //一小时后自动取消,付款成功将这个值设置为null
        if(dto.getEstimatedDeliveryTime()==null){
            order.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(1)); //预计一小时后送达
        }

        //设置餐具数量：tablewareStatus==1 按餐量提供（数量=购物车商品种类数），否则不需要餐具
        if (dto.getTablewareStatus() != null && dto.getTablewareStatus() == 1) {
            order.setTablewareNumber(carts.size());
        } else {
            order.setTablewareNumber(0);
        }
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
        if (Objects.equals(order.getPayStatus(), Orders.PAID))
            return null;

        //更新订单状态： status:待接单  PayStatus: 已支付
        order.setPayStatus(Orders.PAID); //已支付
        order.setStatus(Orders.TO_BE_CONFIRMED); //待接单
        order.setCheckoutTime(LocalDateTime.now()); //付款时间
        order.setPayMethod(dto.getPayMethod()); //付款方式
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

    /**
     * 查看订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO orderDetail(Long id) {
        //查询订单
        Orders  orders = orderMapper.selectById(id);
        if(orders==null)
            throw new OrderBusinessException("查无此单");

        //查询地址簿，根据用户id
        AddressBook addressBook = addressBookMapper.getById(orders.getAddressBookId());

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(orders,vo);
        vo.setAddress(addressBook.toString()); //设置地址簿详细信息
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
                    .append(detail.getNumber())
                    .append(",");
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
     * 商家取消订单
     * @param dto
     */
    @Override
    @Transactional
    public void cancelOrder(OrdersCancelDTO dto) {
        //先利用订单id查询出订单
        Orders order = orderMapper.selectById(dto.getId());
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
        order.setCancelReason(dto.getCancelReason());
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

    //优化版，只做一次数据库连接
    @Override
    public PageResult<OrderVO> pageQuery(OrdersPageQueryDTO condition) {

        //分页查询设置limit?,?
        PageHelper.startPage(condition.getPage(),condition.getPageSize());
        Page<Orders> page = orderMapper.getPage(condition); //page:total,Result
        List<Orders> orders = page.getResult(); //获取分页查询出来的订单列表

        //存储分页查询出来的条目
        List<OrderVO> vos  = new ArrayList<>();
        //空结果提取返回
        if(orders==null ||orders.isEmpty())
            return new PageResult<>(page.getTotal(),vos);

        //先一次性查询出来所有的订单明细，先获取订单id列表
        /**
         * orders.stream() 获取包含订单列表对象的流
         * .map(Orders::getId) 生成一个流，里面包含每个对象的id
         * collect() 按照指定的规则返回一个集合
         * Collectors 指定规则
         * toList()返回集合
         * 总的来说，返回orders的订单id集合
         */
        List<Long> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());
        //传入Ids集合，查询出所有符合订单id的订单详细列表
        List<OrderDetail> detailList = orderDetailMapper.selectByOrderIds(orderIds); //一次数据库操作

        //根据订单id对detailList进行分组
        Map<Long,List<OrderDetail>> detailMap = detailList
                .stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        for(Orders o:orders){
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(o,vo);
            //空明细防御
            List<OrderDetail> details=detailMap.getOrDefault(o.getId(), Collections.emptyList());

            //4. 拼接菜品名
            String orderDishes = details.stream()
                    .map(d-> {
                        return d.getName()+"x"+d.getNumber();
                    }).collect(Collectors.joining(","));
            vo.setOrderDishes(orderDishes);

            vos.add(vo);
        }

        PageResult<OrderVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(vos);

        return result;
    }

    /**
     * 查询个订单状态统计：
     * 待接单数量toBeConfirmed 2
     * 派送中数量 deliveryInProgress 4
     * 待派送： confirmed 3
     * @return
     */
    @Override
    public OrderStatisticsVO orderStatistics() {

       OrderStatisticsVO vo = new OrderStatisticsVO();
       Integer tobeConfirmed= orderMapper.countByStatus(2);
       Integer confirmed= orderMapper.countByStatus(3);
       Integer deliveryInProgress= orderMapper.countByStatus(4);

       vo.setConfirmed(confirmed);
       vo.setToBeConfirmed(tobeConfirmed);
       vo.setDeliveryInProgress(deliveryInProgress);

       return vo;
    }

    /**
     * 接单实现 修改paystatus 和订单status
     * @param id
     */
    @Override
    @Transactional
    public void confirm(Long id) {
        Orders orders = orderMapper.selectById(id);
        orders.setStatus(Orders.CONFIRMED); //设置为已接单
        orderMapper.update(orders);
    }

    /**
     * 拒单
     */
    @Override
    @Transactional
    public void reject(OrdersRejectionDTO dto) {
        Orders o = orderMapper.selectById(dto.getId()); //先获取订单

        o.setStatus(Orders.CANCELLED); //订单状态设置为已取消
        o.setPayStatus(Orders.REFUND); //退款
        o.setRejectionReason(dto.getRejectionReason()); //设置拒单原因
        o.setCancelTime(LocalDateTime.now()); //订单取消时间
        o.setCancelReason("商家拒单"); //设置取消原因
        //更新订单状态
        orderMapper.update(o);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    @Transactional
    public void delivery(String id) {
        Long lId = Long.parseLong(id);
        Orders o = orderMapper.selectById(lId);
        Integer currentStatus = o.getStatus();
        Integer currentPayStatus = o.getPayStatus();
        //派送订单的前提是已接单和已付款，如果没有接单和付款就抛出异常
        if(!currentStatus.equals(Orders.CONFIRMED) && ! currentPayStatus.equals(Orders.PAID))
            throw new OrderBusinessException("该订单未接单");

        //将订单状态设置为派送中
        o.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(o);
    }


    /**
     * 完成订单
     */
    @Override
    @Transactional
    public void complete(String id){
        Long lId = Long.parseLong(id);
        Orders o = orderMapper.selectById(lId);
        Integer currentStatus = o.getStatus();
        Integer currentPayStatus = o.getPayStatus();
        //完成订单的前提是派送中
        if(!currentStatus.equals(Orders.DELIVERY_IN_PROGRESS) && ! currentPayStatus.equals(Orders.PAID))
            throw new OrderBusinessException("该订单未派送");

        //将订单状态设置为已完成
        o.setStatus(Orders.COMPLETED);
        //更新订单送达时间
        o.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(o);
    }
}



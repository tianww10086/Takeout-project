package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ParamEmptyException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
     *  order表和 orderDetail是1 对多的关系，也就是说
     *  order表中的一条数据对应这orderDetail的多条数据
     *  用现实世界理解是，一个订单有多个商品，商品信息保存在orderDetail中，订单信息保存在order中
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO dto) {

        //1.DTO校验过程(地址簿，购物车为空抛出业务异常)
        if(dto==null)
            throw new ParamEmptyException("前端传入对象为空");
        //获取地址簿id
        Long addressId = dto.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressId);
        if(addressBook==null)
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);

        //获取购物车信息
        Long currentUserId = BaseContext.getCurrentId();
        //构造cart对象
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(currentUserId);
        List<ShoppingCart> carts = shoppingCartMapper.list(cart); //查询当前用户id的购物车所有商品数据
        if(carts.isEmpty() ||carts==null)
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
        order.setNumber(String.valueOf( System.currentTimeMillis() ) ); //设置订单号：根据指定规则生成:当前系统的时间戳
        order.setPayStatus(Orders.UN_PAID); // 设置支付状态，默认未支付
        order.setStatus(Orders.PENDING_PAYMENT); //设置订单状态，默认待付款 PENDING_PAYMENT
        //手机号在AddressBook中有保存
        order.setPhone(addressBook.getPhone());
        order.setConsignee(addressBook.getConsignee()); //设置收货人

        //3.向订单表插入一条订单
        orderMapper.insert(order);

        long orderId = order.getId();

        List<OrderDetail> orderDetails = new ArrayList<>();

        //向订单明细表插入多条数据， insertBatch(...) 这些数据是来自购物车
        for(ShoppingCart sc: carts){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(sc,orderDetail); //将每条商品信息拷贝到detail对象里
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
        OrderSubmitVO vo =OrderSubmitVO.builder()
                .id(order.getId()) //设置订单id
                .orderTime(order.getOrderTime())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .build();
        return vo;
    }
}

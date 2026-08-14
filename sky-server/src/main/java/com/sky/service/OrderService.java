package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.*;
import org.springframework.core.annotation.Order;

public interface OrderService {
    /**
     * 用户下单
     * @param dto
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO dto);

    /**
     * 支付接口
     * @param dto
     * @return
     */
    OrderPaymentVO pay(OrdersPaymentDTO dto);

    /**
     * 查询用户历史订单
     * @param dto
     * @return
     */
    PageResult<OrderPageVO> findHistoryOrder(OrdersPageQueryDTO dto);

    /**
     * 查询订单详情接口
     * @param id
     * @return
     */
    OrderVO orderDetail(Long id);

    /**
     * 用户取消订单 根据订单id取消订单
     * @param id
     */
    void cancelOrder(Long id);

    /**
     * 商家取消订单
     */
    void cancelOrder(OrdersCancelDTO dto);

    /**
     * 再来一单
     * @param id
     */
    void againOrder(Long id);

    /**
     * 根据条件查询订单
     * @param condition
     * @return
     */
    PageResult<OrderVO> pageQuery(OrdersPageQueryDTO condition);

    /**
     * 查询个订单状态统计：
     * 待接单数量
     * 派送中数量
     * 待接待数量
     * @return
     */
    OrderStatisticsVO orderStatistics();

    /**
     * 接单
     * @param id
     */
    void confirm(Long id);


    /**
     * 拒单
     * @param dto
     */
    void reject(OrdersRejectionDTO dto);

    /**
     * 派送订单
     * @param id
     */
    void delivery(String id);

    /**
     * 完成订单
     * @param id
     */
    void complete(String id);

    void reminder(String id);
}

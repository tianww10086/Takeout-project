package com.sky.service;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPageVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
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
     * 根据订单id取消订单
     * @param id
     */
    void cancelOrder(Long id);

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
}

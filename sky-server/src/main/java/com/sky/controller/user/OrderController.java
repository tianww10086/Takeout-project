package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.exception.OrderBusinessException;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPageVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("UserOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户端订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    OrderService service;

    /**
     * 用户下单接口
     *
     * @param dto 包含用户下单时的信息
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单接口")
    public Result<OrderSubmitVO> Submit(@RequestBody OrdersSubmitDTO dto) {
        OrderSubmitVO vo = service.submitOrder(dto);

        return Result.success(vo);
    }

    /**
     * 订单支付接口 : 由于无商户资质，无法真实调用微信支付API接口
     * 这里前端发送数据后，后端直接返回支付成功
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付接口")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO dto) {
        if (dto == null)
            throw new OrderBusinessException("订单为空");
        OrderPaymentVO paymentVO = service.pay(dto);

        return Result.success(paymentVO);
    }

    /**
     * 查询历史订单接口 （分页查询）
     */
    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单接口")
    public Result<PageResult<OrderPageVO>> ordersQuery(OrdersPageQueryDTO dto) {
        if (dto == null)
            throw new OrderBusinessException("非法参数");
        PageResult<OrderPageVO> pages = service.pageQuery(dto);


        return Result.success(pages);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        if (id == null)
            throw new OrderBusinessException("空参null{id}异常:");
        OrderVO vo = service.orderDetail(id);

        return Result.success(vo);
    }


    /**
     * 取消订单(删除该订单)
     *
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("删除订单")
    public Result cancelOrder(@PathVariable String id) {
        if (id == null)
            throw new OrderBusinessException("空参null{id}异常:");
        Long idL = Long.parseLong(id);

        service.cancelOrder(idL);

        return Result.success();

    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result againOrder(@PathVariable String id){
        if (id == null)
            throw new OrderBusinessException("空参null{id}异常:");
        Long idL = Long.parseLong(id);

        service.againOrder(idL);

        return Result.success();
    }
}

package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.exception.OrderBusinessException;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
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
     * @param dto 包含用户下单时的信息
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单接口")
    public Result<OrderSubmitVO> Submit(@RequestBody OrdersSubmitDTO dto){
        OrderSubmitVO vo =  service.submitOrder(dto);

        return Result.success(vo);
    }

    /**
     * 订单支付接口 : 由于无商户资质，无法真实调用微信支付API接口
     * 这里前端发送数据后，后端直接返回支付成功
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付接口")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO dto){
        if(dto==null)
            throw new OrderBusinessException("订单为空");
        OrderPaymentVO paymentVO=  service.pay(dto);

        return Result.success(paymentVO);
    }
}

package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.exception.OrderBusinessException;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPageVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    OrderService service;
    /**
     * 订单搜索
     * @param dto 条件
     */
    @GetMapping("/conditionSearch")
    public Result<PageResult<OrderVO>> ordersByCondition(OrdersPageQueryDTO dto){
        if(dto==null)
            throw new OrderBusinessException("dto is null");

        PageResult<OrderVO> pageResult = service.pageQuery(dto);

        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO vo =  service.orderStatistics();
        return Result.success(vo);
    }

    /**
     * 查询订单详情
     * @param orderId
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查看订单详情")
    public Result<OrderVO> detail(@PathVariable("id") Long orderId){
        OrderVO vo =  service.orderDetail(orderId);

        return Result.success(vo);
    }

    /**
     * 接单 前端神了，一个参数也用body封
     * @param body
     * @return
     */
    @PutMapping("/confirm")
    public Result confirm(@RequestBody Map<String,Object> body){
        Long id = Long.valueOf(body.get("id").toString());
        service.confirm(id);

        return Result.success();
    }

    /**
     * 拒单
     * @param dto
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result reject(@RequestBody OrdersRejectionDTO dto){
        service.reject(dto);

        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO dto){
        service.cancelOrder(dto);

        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable String id){
        service.delivery(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable String id){
        service.complete(id);
        return Result.success();
    }
}

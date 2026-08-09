package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.exception.OrderBusinessException;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPageVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

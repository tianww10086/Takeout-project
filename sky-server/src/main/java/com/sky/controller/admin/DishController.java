package com.sky.controller.admin;

import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    DishService service;

    @GetMapping("/page")
    public Result<PageResult<DishPageVO>> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        // 要返回的数据有，总条数，菜品列表

        // 调用pageQuery服务获取菜品列表
        List<DishPageVO> list =  service.pageQuery(dishPageQueryDTO);
        //调用getCounts获取总条数
        long counts = service.getCounts(dishPageQueryDTO);
        //返回Result<PageResult<DishPageVO>>
        PageResult<DishPageVO> pageResult = new PageResult<>();
        pageResult.setTotal(counts);
        pageResult.setRecords(list);
        return Result.success(pageResult);
    }
}

package com.sky.controller.user;


import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.exception.ParamEmptyException;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "购物车相关接口")
public class ShoppingCartController {

    @Autowired
    ShoppingCartService shopService;

    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO dto){
        log.info("添加商品到购物车：{}",dto);
        shopService.addShoppingCart(dto);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list(){
        //查看当前用户的购物车里所有的商品
        log.info("查看购物车");
        List<ShoppingCart> carts=  shopService.showShoppingCart();
        return Result.success(carts);
    }

    //清空当前用户的购物车
    @DeleteMapping("/clean")
    public Result clean(){
        log.info("清空用户{}的购物车", BaseContext.getCurrentId());
        shopService.cleaShoppingCart();

        return Result.success();
    }

    //删除其中一个商品
    @PostMapping("/sub")
    public Result deleteItem(@RequestBody ShoppingCartDTO dto){
        if(dto==null)
            throw new ParamEmptyException("接收参数为null");
        log.info("删除商品{}"+dto);
        shopService.delete(dto);

        return Result.success();
    }
}

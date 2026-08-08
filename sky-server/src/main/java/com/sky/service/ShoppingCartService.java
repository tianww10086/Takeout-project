package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    void addShoppingCart(ShoppingCartDTO dto);

    /**
     * 查看购物车
     * @return
     */
    List<ShoppingCart> showShoppingCart();

    void cleaShoppingCart();

    //根据条件dto删除
    void delete(ShoppingCartDTO dto);
}

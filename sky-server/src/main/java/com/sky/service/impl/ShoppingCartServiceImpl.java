package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加购物车
     * @param dto
     */
    @Override
    @Transactional  //开启事务
    public void addShoppingCart(ShoppingCartDTO dto) {
        //先查询购物车表里有没有里有没有符合dto条件的数据，如果有，将对应的分数+1即可（更新操作）

        ShoppingCart cart = new ShoppingCart();
        BeanUtils.copyProperties(dto,cart); //属性引用
        cart.setUserId(BaseContext.getCurrentId()); //通过ThreadLocal获取线程局部变量

        List<ShoppingCart> list = shoppingCartMapper.list(cart);

        //如果查询不为空，只需要将分数+1即可。
        if(list!=null && list.size()>0){
            ShoppingCart goods = list.get(0); //通过上面cart条件查询出来的只可能有一条数据
            goods.setNumber(goods.getNumber()+1);
            //更新份数
            shoppingCartMapper.updateNumberById(goods);
        }else{
            //如果不存在，插入cart,插入之前 ，先设置cart其他属性

            //通过dish_id 或setmeal_id判断是菜品还是套餐
            if(cart.getDishId()!=null){
                //如果传入的是菜品，就查询菜品表，把相关属性填入
                //通过菜品id查询菜品信息：（name:菜品名，image:菜品图片路径,amount:菜品价格）
                Dish dish = dishMapper.findById(cart.getDishId());
                //把相关属性添加到购物车商品信息中
                cart.setName(dish.getName());
                cart.setImage(dish.getImage());
                cart.setAmount(dish.getPrice());

                //如果有菜品口味就设置菜品口味


            }else if(cart.getSetmealId()!=null){
                //与上面类似
                //如果传入的是套餐，就查询套餐表，把相关属性填入
                // (name:套餐名 ,image:套餐图片路径， amount:套餐价格)
                Setmeal setmeal = setmealMapper.select(Integer.parseInt(String.valueOf(cart.getSetmealId())));
                cart.setName(setmeal.getName());
                cart.setImage(setmeal.getImage());
                cart.setAmount(setmeal.getPrice());
            }
            //创建时间，现在
            cart.setCreateTime(LocalDateTime.now());
            cart.setNumber(1); //第一次插入数量固定为1
            shoppingCartMapper.insert(cart);
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        //查看当前用户的购物车数据
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(BaseContext.getCurrentId()); //设置用户id
        return shoppingCartMapper.list(cart);
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleaShoppingCart() {
        //删除当前用户的购物车数据
        Long currentUserId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(currentUserId);
    }
}

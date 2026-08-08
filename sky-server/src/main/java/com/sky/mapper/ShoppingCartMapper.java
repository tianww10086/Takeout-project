package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    //动态条件查询购物车数据
    List<ShoppingCart>  list(ShoppingCart cart);

    //根据购物车id更新份数+1
    @Update("""
        update shopping_cart set number =#{number} where id =#{id}
    """)
    void updateNumberById(ShoppingCart cart);

    //插入购物车的一个商品
    @Insert("""
    insert into shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) 
    values(#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{createTime})
""")
    void insert(ShoppingCart cart);

    @Delete("""
    delete from shopping_cart where user_id = #{currentUserId}
    """)
    void deleteByUserId(Long currentUserId);


    /**
     * 根据动态条件删除条目
     * @param cart
     */

    void delete(ShoppingCart cart);
}

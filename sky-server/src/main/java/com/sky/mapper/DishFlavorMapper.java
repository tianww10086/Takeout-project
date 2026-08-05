package com.sky.mapper;

import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入口味
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品id列表批量删除口味
     * @param ids
     */

    void deleteListById(List<Integer> ids);

    /**
     * 根据菜品id删除 口味
     * @param id
     */
    void  deleteByDishId(long id);

    /**
     * 根据菜品id查询出 口味列表
     * @param id 菜品id
     * @return
     */
    List<DishFlavor> findByDishId(Long id);
}

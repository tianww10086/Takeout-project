package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetmealDishMapper{

     void insertBatch(List<SetmealDish> setmealDishes);

    void deleteBatchBySetmealId(Long setmealId);

    List<SetmealDish> selectList(Integer id);

    void deleteBatchBySetmealIds(@Param("setmealIds") List<Long> setmealIds);
}

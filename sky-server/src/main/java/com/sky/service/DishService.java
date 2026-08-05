package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishPageVO;
import com.sky.vo.DishVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DishService {

    //分页查询接口，返回list
     List<DishPageVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 分页查询接口：查询总条目数
     * @param dishPageQueryDTO
     * @return
     */
     long getCounts(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 新增菜品 ，保存菜品的同时要保存对应的口味
     * @param dto
     */
     public void saveWithFlavor(DishDTO dto);


    /**
     * 批量删除菜品功能
     * @param ids
     */
    @Transactional
    void deleteWithFlavor(String ids);

    /**
     *
     * 修改菜品
     * @param dish
     */
    @Transactional
    void updateWithFlavor(DishDTO dish);

    /**
     * 根据id查询出前菜品及口味
     * @param id
     * @return
     */
    DishVO findByIdWithFlavor(Long id);


    void updateWithStatus(Integer status, Long id);

    List<Dish> listFindById(String categoryId);
}

package com.sky.service;


import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishPageVO;

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
}

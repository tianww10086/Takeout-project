package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    /**
     * 根据DTO保存套餐及套餐和对应菜品的关系（setmeal_dish表）
     * @param setmealDTO
     */
    void saveAndSetmealDish(SetmealDTO setmealDTO);

    /**
     * 根据dto修改套餐和 修改菜品的关系
     * @param dto
     */
    void updateAndSetmealDish(SetmealDTO dto);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    SetmealVO selectById(Integer id);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PageResult<Setmeal> pageQuery(SetmealPageQueryDTO dto);

    void deleteList(String ids);

    void onOff(Integer id, Integer status);
}

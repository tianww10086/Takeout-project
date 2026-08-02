package com.sky.service;

import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    /**
     * 获取总条目
     * @param dto
     * @return
     */
    Integer getCounts(CategoryPageQueryDTO dto);

    /**
     * 根据page和pageSize获取列表
     * @param dto
     * @return 该页条目和 总条目
     */
    PageResult<Category> pageQuery(CategoryPageQueryDTO dto);

    /**
     * 根据参数动态更新
     * @param c  参数
     * @return 受影响的值
     */
    long updateCategory(Category c);

    /**
     * 新增分类
     * @param c
     * @return
     */
    long addCategory(Category c);

    /**
     * 查询该名字是否不在数据库
     * @param name
     * @return true 存在，false不存在
     */
    boolean existByName(String name);
}

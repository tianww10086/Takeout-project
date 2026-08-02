package com.sky.mapper;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 分页查询，接受前端传输过来的数据查询
     */
    List<Category> pageQuery(CategoryPageQueryDTO queryDTO);

    /**
     * 查询总共有多少条数据
     */
    Integer getCounts(CategoryPageQueryDTO queryDTO);

    /**
     * 修改数据 ,根据主键id动态更新
     */
    Long updateCategory(Category c);

}

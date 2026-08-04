package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
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
    @AutoFill(OperationType.UPDATE)
    Long updateCategory(Category c);


    /**
     * 新增分类
     * @param c
     * @return
     */
    @AutoFill(OperationType.INSERT)
    long addCategory(Category c);

    /**
     * 根据分类名字查询分类
     * @param name
     * @return
     */
    Category getByName(String name);

    long delete(Integer id);

    List<Category> listByType(int iType);
}

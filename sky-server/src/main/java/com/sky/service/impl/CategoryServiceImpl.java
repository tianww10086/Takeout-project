package com.sky.service.impl;

import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    /**
     * 查询符合条件的条目数量
     * @param dto 该对象中包含查询条件
     * @return
     */
    @Override
    public Integer getCounts(CategoryPageQueryDTO dto) {
        return categoryMapper.getCounts(dto);
    }

    @Override
    public PageResult<Category> pageQuery(CategoryPageQueryDTO dto) {
        int page = dto.getPage();
        page = (page-1)*dto.getPageSize();
        dto.setPage(page);
        List<Category> list =categoryMapper.pageQuery(dto);
        PageResult<Category> pageResult = new PageResult<Category>();
        pageResult.setRecords(list);
        pageResult.setTotal(getCounts(dto));
        return pageResult;
    }

    /**
     * 根据参数动态更新
     * @param c  参数
     * @return 受影响的值
     */
    @Override
    public long updateCategory(Category c){
       return categoryMapper.updateCategory(c);
    }

    /**
     * 新增分类
     * @param c
     * @return
     */
    @Override
    public long addCategory(Category c) {
        return categoryMapper.addCategory(c);
    }

    /**
     * 查询名字在数据库中是否存在
     * @param name
     * @return
     */
    @Override
    public boolean existByName(String name) {
       Category c = categoryMapper.getByName(name);
       return c != null;
    }

    @Override
    public long delete(Integer id) {
        return categoryMapper.delete(id);
    }
<<<<<<< Updated upstream
=======

    @Override
    /**
     * 启用或禁用 服务
     */
    public void startOrStop(Integer status,Long id){
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();
        categoryMapper.updateCategory(category);
    }

    /**
     * 根据分类类型查询 分类列表
     * @param type
     * @return
     */
    @Override
    public List<Category> listByTypeServe(String type) {
        //调用mapper层接口
        int type_int  = Integer.parseInt(type); //转化为整型传入接口

       return  categoryMapper.listByType(type_int);
    }
>>>>>>> Stashed changes
}

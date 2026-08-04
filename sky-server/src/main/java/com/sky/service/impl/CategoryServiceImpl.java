package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    DishMapper dishMapper;
    @Autowired
    SetmealMapper setmealMapper;
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
    public long updateCategory(CategoryDTO c){
        Category category = new Category();
        BeanUtils.copyProperties(c,category);

       return categoryMapper.updateCategory(category);

    }

    /**
     * 新增分类
     * @param c
     * @return
     */
    @Override
    public long addCategory(CategoryDTO c) {
        Category category= new Category();
        BeanUtils.copyProperties(c,category);
        category.setStatus(0); //默认状态禁用：0
        return categoryMapper.addCategory(category);
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

    /**
     * 删除分类
     * @param id
     * @return
     */
    @Override
    public long delete(Integer id) {
        long count = dishMapper.countByCategoryId(id);
        if(count>0){
            // 当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        
        count = setmealMapper.countByCategoryId(id);
        if(count>0){
            // 当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        return categoryMapper.delete(id);
    }

    @Override
    /**
     * 启用或禁用
     */
    public void startOrStop(Integer status,Long id){
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();
        categoryMapper.updateCategory(category);
    }

    /**
     *
     * 根据type 查询分类列表
     * @param type
     * @return
     */
    @Override
    public List<Category> listByTypeServe(String type) {
            //调用mapper层接口
        int IType = Integer.parseInt(type);

        return  categoryMapper.listByType(IType);
    }
}

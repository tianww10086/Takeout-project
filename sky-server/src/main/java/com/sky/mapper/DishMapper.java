package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishPageVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper
{
    /**
     * 根据分类id查询菜品数量
     */
    @Select(
            """
    select count(0) from dish where category_id=#{categoryId}
    """
    )
    long countByCategoryId(long categoryId);


    /**
     * 菜品分页查询
     */
    List<DishPageVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品分页查询：总条目项，也要加条件
     * @param dishPageQueryDTO
     * @return
     */
    Long getCounts(DishPageQueryDTO dishPageQueryDTO);

    /**
     *  插入菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 查询该菜品是否存在
     * @param name
     * @return
     */
    Dish existDish(String name);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteListById(List<Integer> ids);


    /**
     * 修改菜品
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    @Select("""
    select * from dish where id =#{id}
    """)
    Dish findById(Long id);

    @Select("""
    select * from dish where category_id = #{id}
    """)
    List<Dish> selectBatchId(long id);


    List<Dish> list(Dish dish);
}

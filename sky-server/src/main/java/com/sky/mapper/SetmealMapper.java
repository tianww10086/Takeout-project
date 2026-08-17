package com.sky.mapper;


import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface SetmealMapper {
    /**
     * 查询该分类id下的套餐数量
     * @param categoryId
     * @return
     */
    @Select("""
    select count(0) from setmeal where category_id = #{categoryId}
    """)
    long countByCategoryId(Integer categoryId);

    /**
     * 插入菜品
     * @param setmeal
     */
    //在插入前AOP自动填充create,update等字段给实体对象
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 更新菜品
     * @param s
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal s);

    /**
     * 根据菜品id查询菜品
     * @param id
     * @return
     */
    @Select("""
    select * from setmeal where id =#{id}
    """)
    Setmeal select(Integer id);

    /**
     * 分页查询：setmeal：前端传入的条件
     * @param setmeal
     * @return
     */
    List<SetmealPageVO> page(SetmealPageQueryDTO setmeal);

    /**
     * 根据条件查询条目数量
     * @param setmeal
     * @return
     */
    Long count(Setmeal setmeal);

    /**
     * 传入id列表批量删除菜品
     * @param idList
     */
    void deleteBatch(@Param("idList") List<Long> idList);

    /**
     * 查询符合条件的菜品
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     *  根据id查询菜品选项--
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemBySetmealId(Long id);

    Integer countByMap(Map map);
}

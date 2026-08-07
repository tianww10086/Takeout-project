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
import java.util.Set;

@Mapper
public interface SetmealMapper {

    @Select("""
    select count(0) from setmeal where category_id = #{categoryId}
    """)
    long countByCategoryId(Integer categoryId);


    //在插入前AOP自动填充create,update等字段给实体对象
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    @AutoFill(OperationType.UPDATE)
    void update(Setmeal s);

    @Select("""
    select * from setmeal where id =#{id}
    """)
    Setmeal select(Integer id);

    List<SetmealPageVO> page(SetmealPageQueryDTO setmeal);


    Long count(Setmeal setmeal);

    void deleteBatch(@Param("idList") List<Long> idList);

    List<Setmeal> list(Setmeal setmeal);

    List<DishItemVO> getDishItemBySetmealId(Long id);
}

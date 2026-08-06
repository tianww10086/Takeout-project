package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.SetmealNoDishException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    SetmealMapper setmealMapper;

    @Autowired
    SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void saveAndSetmealDish(SetmealDTO setmealDTO) {
        if(setmealDTO == null){
            throw new SetmealNoDishException("传入对象为空");
        }

        //从DTO中拿出套餐数据
        Setmeal setmeal = new Setmeal();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        if(setmealDishes==null || setmealDishes.isEmpty()){
            throw new SetmealNoDishException("套餐必须至少要一个菜品");
        }

        BeanUtils.copyProperties(setmealDTO,setmeal);

        //插入套餐数据
        setmealMapper.insert(setmeal);
        long setmeal_id  = setmeal.getId();
        //拿出菜品id和套餐的关系



        setmealDishes.forEach((s)->{
            s.setSetmealId(setmeal_id); //设置套餐id
        });

        //批量插入套餐和菜品的关系
        setmealDishMapper.insertBatch(setmealDishes);
    }


    @Override
    @Transactional
    public void updateAndSetmealDish(SetmealDTO dto) {
        if(dto==null)
            throw new RuntimeException("传入对象为空");

        Setmeal s = new Setmeal();
        BeanUtils.copyProperties(dto,s);
        setmealMapper.update(s);
        long s_id = s.getId(); //获取套餐id
        List<SetmealDish> list = dto.getSetmealDishes();
        //先无条件删除， 后插入更新
        setmealDishMapper.deleteBatchBySetmealId(s_id); //批量删除
        if(list==null || list.isEmpty())
            throw new SetmealNoDishException("菜品不能为空");
        list.forEach(sd->{
            sd.setSetmealId(s_id); //设置套餐id
        });
        //插入列表更新
        setmealDishMapper.insertBatch(list);
    }

    /**
     * 根据id查询套餐和对应 菜品关系（setmealDishes）
     * @param id 套餐id
     * @return 视图对象集合
     */
    @Override
    public SetmealVO selectById(Integer id) {
        Setmeal setmeal =  setmealMapper.select(id);
        if(setmeal ==null )
            throw new RuntimeException("setmeal为空，考虑没有id为："+id+"的套餐");
        SetmealVO vo = new SetmealVO();
        BeanUtils.copyProperties(setmeal,vo);

        List<SetmealDish> dishes = setmealDishMapper.selectList(id);
        if(dishes==null)
            throw new SetmealNoDishException("套餐菜品关系为空");
        vo.setSetmealDishes(dishes);

        return vo;
    }

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @Override
    public PageResult<Setmeal> pageQuery(SetmealPageQueryDTO dto) {
        if(dto==null)
            throw new RuntimeException("dto为空");
        int page_index = dto.getPage();
        page_index=(page_index-1)*10;
        dto.setPage(page_index);
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(dto,setmeal);
        List<Setmeal> list =  setmealMapper.page(dto);

        long counts = setmealMapper.count(setmeal);

        PageResult<Setmeal> pages = new PageResult<>();
        pages.setTotal(counts);
        pages.setRecords(list);

        return pages;
    }

    /**
     * 根据id列表删除套餐和对应关系的套餐菜品表
     * @param ids
     */
    @Override
    @Transactional
    public void deleteList(String ids) {
        if(ids==null || ids.isEmpty()){
            throw new IllegalArgumentException("非法参数");
        }
        String []idArray = ids.split(",");
        List<Long> idList = new ArrayList<>();

        for(String s:idArray){
            long id = Long.parseLong(s);
            idList.add(id);
        }

        setmealMapper.deleteBatch(idList);
        setmealDishMapper.deleteBatchBySetmealIds(idList);
    }

    @Override
    public void onOff(Integer id, Integer status) {
        Setmeal setmeal= new Setmeal();
        setmeal.setId(Long.valueOf(id));
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
    }
}

package com.sky.service.impl;

import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import com.sky.vo.DishPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.events.DTD;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    //自动注入mapper层接口
    @Autowired
    DishMapper dishMapper;

    @Override
    public List<DishPageVO> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        int start_index = dishPageQueryDTO.getPage();
        start_index = (start_index-1)*10;
        dishPageQueryDTO.setPage(start_index);
       return dishMapper.pageQuery(dishPageQueryDTO);
    }

    @Override
    public long getCounts(DishPageQueryDTO dishPageQueryDTO) {
        return dishMapper.getCounts(dishPageQueryDTO);
    }
}

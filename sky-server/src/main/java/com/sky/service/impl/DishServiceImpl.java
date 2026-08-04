package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DishExistentException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import com.sky.vo.DishPageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    //自动注入Dish数据库接口
    @Autowired
    DishMapper dishMapper;

    //自动注入Flavor数据库接口
    @Autowired
    DishFlavorMapper flavorMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;


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


    /**
     * 新增菜品 ，保存菜品的同时要保存对应的口味
     * @param dto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dto) {
        //根据dto构造Dish实体
        Dish dish = new Dish();
        BeanUtils.copyProperties(dto,dish);
        //AOP自动插入其他四个字段的值给dish

        //插入前先检查该菜品名是否存在
        if(dishMapper.existDish(dish.getName()) !=null){
            throw new DishExistentException("菜品已存在");
        }
        dishMapper.insert(dish);

        //根据dish实体获取id
        long dish_id = dish.getId();
        // 获取flavor口味列表，一个菜品包含多个口味，我们需要把这些口味插入到数据库中
        List<DishFlavor> flavors = dto.getFlavors();

        if(flavors==null || flavors.isEmpty()){
            throw new RuntimeException("flavors为空");
        }

        //设置菜品id
        for(DishFlavor flavor :flavors){
            flavor.setDishId(dish_id);
        }

      //  传入列表全部插入
        flavorMapper.insertBatch(flavors);
        log.info("{}插入完成", dish.getName());
    }

    /**
     *  根据id 批量删除菜品和 口味
     * @param ids 1,2,3 根据,分隔
     */
    @Transactional
    @Override
    public void deleteWithFlavor(String ids) {
        if(ids ==null || ids.trim().isEmpty())
            throw new RuntimeException("不存在id,参数错误");
        String []idArray = ids.split(",");
        List<Integer> idList = new ArrayList<>();
        for(String idA :idArray){
            int id = Integer.parseInt(idA.trim());
            //将id转化为整型处理
            idList.add(id);

        }
        dishMapper.deleteListById(idList);
        //删除口味
        dishFlavorMapper.deleteListById(idList);
    }
}

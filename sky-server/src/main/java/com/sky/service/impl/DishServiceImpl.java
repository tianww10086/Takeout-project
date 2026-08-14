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
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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
        //插入菜品
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
        //根据菜品id列表删除菜品
        dishMapper.deleteListById(idList);
        //根据菜品id列表删除口味
        dishFlavorMapper.deleteListById(idList);
    }

    /**
     * 修改菜品服务：在修改菜品之后，要把口味表中对应的口味表删除，
     * 再通过前端传入的口味列表插入到口味表中
     * @param dish
     */
    @Override
    @Transactional
    @CacheEvict() //清除掉所有缓存
    public void updateWithFlavor(DishDTO dish) {
        Dish dish1 = new Dish();
        BeanUtils.copyProperties(dish,dish1); //复制属性
        long dish_id = dish1.getId(); //获取菜品id
        //先修改dish表，再修改口味表
        dishMapper.update(dish1);

        //先删除原先所有的口味
        dishFlavorMapper.deleteByDishId(dish_id);

        // 获取口味列表
        List<DishFlavor> flavors = dish.getFlavors();

        if(flavors!=null && !flavors.isEmpty()){
            for(DishFlavor flavor :flavors){
                flavor.setDishId(dish_id); //设置对应的id;
            }
            dishFlavorMapper.insertBatch(flavors);
        }



    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO findByIdWithFlavor(Long id) {
        Dish dish =  dishMapper.findById(id); //查询出菜品

        List<DishFlavor> flavors = dishFlavorMapper.findByDishId(id); //查询出口味

        if(dish==null || flavors==null)
            throw new RuntimeException("菜品查询失败");

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO); //将菜品传入VO
        dishVO.setFlavors(flavors); //将口味传入VO
        return dishVO;
    }

    /**
     * 根据菜品id修改菜品状态
     * @param status
     * @param id
     */
    @Override
    public void updateWithStatus(Integer status, Long id) {
        if(id == null || status ==null)
            throw new IllegalArgumentException("非法参数异常");
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }

    /**
     *  根据分类id查询菜品列表
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> listFindById(String categoryId) {
        long id = Long.parseLong(categoryId); //转化为long型

        return  dishMapper.selectBatchId(id);
    }

    /**
     * 条件查询菜品和口味 : 传入的条件是分类id
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {

        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.findByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}

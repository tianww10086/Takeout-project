package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishPageVO;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品接口")
@Slf4j
public class DishController {

    @Autowired
    DishService service;
    @Autowired
    private DishService dishService;

    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult<DishPageVO>> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        // 要返回的数据有，总条数，菜品列表
        // 调用pageQuery服务获取菜品列表
        List<DishPageVO> list =  service.pageQuery(dishPageQueryDTO);
        //调用getCounts获取总条数
        long counts = service.getCounts(dishPageQueryDTO);
        //返回Result<PageResult<DishPageVO>>
        PageResult<DishPageVO> pageResult = new PageResult<>();
        pageResult.setTotal(counts);
        pageResult.setRecords(list);
        return Result.success(pageResult);
    }

    /**
     * 添加菜品接口
     * @param dto
     * @return
     */
    @PostMapping
    @ApiOperation("添加菜品")
    public Result save(@RequestBody DishDTO dto){

        log.info("新增菜品：{}",dto);

        service.saveWithFlavor(dto);
        return Result.success();
    }

    /**
     * 删除菜品接口
     */
    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result delete(String ids){
        log.info("删除菜品（id：{}）",ids);
        if(ids.isEmpty())
            throw new RuntimeException("id为空");
        service.deleteWithFlavor(ids);
        return Result.success();
    }

    /**
     * 修改菜品接口
     * @param dish
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dish){
        log.info("修改菜品:{}",dish);

        service.updateWithFlavor(dish);

        return Result.success();
    }

    /**
     * 查询菜品方法 根据id
     */
    @GetMapping("/{id}")
    @ApiOperation("根据菜品id查询出菜品和对应的口味")
    public Result<DishVO> findById(@PathVariable Long id){
        DishVO dishvo =  service.findByIdWithFlavor(id);
        //再根据菜品id查询口味列表

        return Result.success(dishvo);
    }

    /**
     * 修改菜品状态
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("根据菜品id修改状态")
    public Result OnOff(@PathVariable Integer status,Long id){
        log.info("修改菜品id为{} 的状态为{}",id,status);
        service.updateWithStatus(status,id);
        return Result.success();
    }

    public Result<List<Dish>> findByCategoryId(String categoryId){
       List<Dish> dishes =   service.listFindById(categoryId);

       return Result.success(dishes);
    }
}

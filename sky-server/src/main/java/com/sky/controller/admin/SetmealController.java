package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealPageVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/admin/setmeal")
@Api(tags="套餐相关接口")
@Slf4j
public class SetmealController {

    @Autowired
    public SetmealService service;

    /**
     * 新增套餐接口
     * @param setmealDTO 前端传输的数据
      */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result saveAndSetmealDish(@RequestBody SetmealDTO setmealDTO){
        service.saveAndSetmealDish(setmealDTO);

        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO dto){
        service.updateAndSetmealDish(dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> selectList(@PathVariable Integer id){
        log.info("根据id查询套餐:{}",id);
        SetmealVO vo = service.selectById(id);
        if(vo!=null){
            return Result.success(vo);
        }
        return Result.error("查询失败");
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    @ApiOperation("分页查询功能")
    public Result<PageResult<SetmealPageVO>> pageQuery(SetmealPageQueryDTO dto){
        PageResult<SetmealPageVO> pageResult = service.pageQuery(dto);

        return Result.success(pageResult);   // ← 包上 Result
    }


    /**
     * 批量删除套餐
     */
    @DeleteMapping
    @ApiOperation("删除套餐功能")
    public Result delete(String ids){
        service.deleteList(ids);

        return Result.success();
    }

    /**
     * 套餐起售停售
     * @param id 套餐id
     * @param status 状态：1起售，0停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售停售")
    public Result onOff(Integer id,@PathVariable Integer status){
        service.onOff(id,status);


        return Result.success();
    }
}

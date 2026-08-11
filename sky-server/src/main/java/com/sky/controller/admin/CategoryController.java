package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.CategoryExistedException;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api( tags="分类管理相关接口")
public class CategoryController {
    @Autowired
    CategoryService service;

    /**
     * 分页查询功能，
     * @param dto 前端发送来的条件
     * @return 返回总条目数 和该页的PageSize个条目
     */
    @GetMapping("/page")
    @ApiOperation("分页查询功能")
    public Result< PageResult<Category>> pageQuery( CategoryPageQueryDTO dto){
        PageResult<Category> pageResult = service.pageQuery(dto);

        return Result.success(pageResult);
    }

    /**
     * 更新分类信息功能
     * @param dto
     * @return
     */
    @PutMapping
    @ApiOperation("更新功能")
    public Result updateCategory(@RequestBody CategoryDTO dto){
        //新建一个对象接受参数

        return service.updateCategory(dto)>0?Result.success():Result.error("更新失败");
    }

    @PostMapping("/status/{status}")
    @ApiOperation("状态修改功能")
    public Result StartOrStop(@PathVariable Integer status,Long id){
        log.info("启用或禁用员工账户：{},{}",status,id);
        service.startOrStop(status,id);

        return Result.success();
    }

    @PostMapping
    @ApiOperation("新增分类功能")
    public Result addCategory(@RequestBody CategoryDTO dto){
        //根据名字查询分类，如果在数据库存在，抛出已存在异常，全局异常处理器会返回错误信息
        if(service.existByName(dto.getName())){
            throw new CategoryExistedException("分类:"+dto.getName()+"已存在");
        }
        service.addCategory(dto);

        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除功能")
    public Result deleteCategory(Integer id){
        return service.delete(id)>0?Result.success():Result.error("删除失败");
    }

    /**
     *  根据type返回分类列表
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据type返回分类列表")
    public Result<List<Category>> listByType(Integer type){

        return Result.success(service.listByTypeServe(type));
    }

}

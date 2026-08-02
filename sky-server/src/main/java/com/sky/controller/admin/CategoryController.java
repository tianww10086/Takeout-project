package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
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

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api("分类管理相关接口")
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
        Category c = new Category();
        BeanUtils.copyProperties(dto,c); //将前端发送的参数给c
        c.setUpdateTime(LocalDateTime.now()); //更新时间
        c.setUpdateUser(BaseContext.getCurrentId()); //更新id
        return service.updateCategory(c)>0?Result.success():Result.error("更新失败");
    }

    @PostMapping("/status/{status}")
    @ApiOperation("状态修改功能")
    public Result StartOrStop(@PathVariable Integer status,Long id){
        log.info("启用或禁用员工账户：{},{}",status,id);
        Category c = new Category();
        c.setStatus(status);
        c.setId(id);

        return service.updateCategory(c)>0?Result.success():Result.error("状态修改失败");
    }
}

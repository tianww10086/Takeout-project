package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result runTimeExceptionHandler(RuntimeException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }


    /**
     * 捕获sql异常
     * @param e 表示 SQL 语句由于违反数据库完整性约束而失败。
     * @return 返回错误信息
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException e){
      //Duplicate entry 'employee1' for key 'employee.idx_username'
        String msg = e.getMessage();

        //当错误信息包含Duplicate entry，则说明表中有限制唯一的字段值，该值不允许重复
        if(msg.contains("Duplicate entry")){
            log.info("添加了重复的用户名，异常");
            String[] split = msg.split(" ");
            String name = split[2].replace("'",""); //获取到重复的名字 删除引号
            msg = name+ MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }else{
            return Result.error("未知错误");
        }

     }

}

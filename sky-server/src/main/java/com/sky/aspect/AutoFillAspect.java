package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //定义切入点 ：mapper下的所有的类，所有的方法 并且这个方法被AutoFill注解
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){
    }



    //定义通知 前置通知
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint jp) throws Throwable{
        //进行公共字段的赋值
        log.info("开始进行公共字段自动填充....");

        //获取当前被拦截方法上注解的值：数据库操作类型
        MethodSignature signature = (MethodSignature) jp.getSignature(); //获取签名
       AutoFill autoFill =  signature.getMethod().getAnnotation(AutoFill.class); //获取这个方法上的注解
        OperationType type = autoFill.value(); //数据库操作类型

        //获得被拦截方法的参数
        Object[] args = jp.getArgs();

        //方法上的实体类
        Object entity = args[0]; //获取实体类，约定这些操作的第一个参数是实体类

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        long currentId = BaseContext.getCurrentId();

        //通过反射调用方法
        var setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);//通过反射获取方法
        var setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
        var setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
        var setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);

        //根据不同的操作类型，为对应的属性进行赋值
        if(type==OperationType.UPDATE){
            //为两个公共字段进行赋值：updateTime，updateUser
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);
        }else if(type ==OperationType.INSERT){
            setCreateTime.invoke(entity,now);
            setCreateUser.invoke(entity,currentId);
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);
        }
    }
}

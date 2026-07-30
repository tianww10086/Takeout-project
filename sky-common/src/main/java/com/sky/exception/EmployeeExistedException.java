package com.sky.exception;

/**
 * 用户已存在异常
 */
public class EmployeeExistedException extends BaseException {

    public EmployeeExistedException(String msg){
        super(msg);
    }
}

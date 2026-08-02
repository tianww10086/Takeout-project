package com.sky.exception;

import com.sky.context.BaseContext;

public class CategoryExistedException  extends BaseException {

    public CategoryExistedException(String msg){
        super(msg);
    }
}

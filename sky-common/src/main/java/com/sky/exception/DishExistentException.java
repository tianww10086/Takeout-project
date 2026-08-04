package com.sky.exception;

import com.fasterxml.jackson.databind.ser.Serializers;

public class DishExistentException extends BaseException{
    public DishExistentException(String message) {
        super(message);
    }
}

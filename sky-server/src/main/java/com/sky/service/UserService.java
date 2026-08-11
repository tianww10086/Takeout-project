package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

import java.io.IOException;

public interface UserService {


    /**
     * 微信登录
     * @param dto
     * @return
     */
    User wxLogin(UserLoginDTO dto) throws IOException;
}

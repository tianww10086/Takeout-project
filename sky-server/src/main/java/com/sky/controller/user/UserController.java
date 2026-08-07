package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping("/user/user")
@Api("用户端操作")
@Slf4j
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private JwtProperties jwtProperties;
    /**
     * 微信小程序用户登录
     */
    @PostMapping("/login")
    public Result<UserLoginVO> userLogin(@RequestBody UserLoginDTO dto) throws IOException {
        log.info("微信用户登录 openid:{}",dto.getCode());
        User user = service.wxLogin(dto);

        //构造载荷数据
        HashMap<String,Object> claims = new HashMap<>();

        claims.put(JwtClaimsConstant.USER_ID,user.getId()); //放入用户标识
        //为微信用户生成jwt命令
        String token =  JwtUtil.createJWT(jwtProperties.getUserSecretKey()
        ,jwtProperties.getUserTtl()
        , claims);

        //构造返回数据对象
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(userLoginVO);

    }
}

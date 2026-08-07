package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    /**
     * wx登录实现 ,前端dto发送临时code，使用临时code + appId+appSecret +固定值请求微信登录接
     * 口，返回User数据
     * @param dto
     * @return
     */

    @Autowired
    private WeChatProperties properties;

    @Autowired
    private UserMapper userMapper;

    @Override
    public User wxLogin(UserLoginDTO dto) throws IOException {

        //通过辅助方法获取openid
        String openId = getOpenId(dto.getCode());


        //判断openid是否为空， 如果为空，则判断登录失败，抛出异常
        if(openId==null)
            throw new LoginFailedException("获取openid失败，登录失败");
        //如果不为空，判断是否是新用户（新用户的openid不存在于user表，如果已存在，则认为是老用户）


        User exist = userMapper.selectByOpenId(openId);
        if(exist==null){
            //如果是新用户，自动完成注册（根据openid，用户名，封装一个user对象插入到表中，保存核心标识
            //其他数据后续通过个人中心修改
            User newUser = User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build(); //根据openid创建新用户

            //插入
            userMapper.insert(newUser);
            return newUser;
        }


        //返回这个用户对象

        return exist;
    }

    //辅助方法，通过临时code 获取到openid
    private String getOpenId(String code){
        //调用微信服务接口， 获取当前微信用户的openid
        // 接口：GET https://api.weixin.qq.com/sns/jscode2session

        Map<String,String > map = new HashMap<>();
        //将请求参数写到map里
        map.put("appid",properties.getAppid());
        map.put("secret",properties.getSecret());
        map.put("js_code",code); //code由前端传输给后端
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN_URL,map); //返回一个字符串，json格式

        //根据字符串构造一个json对象
        JSONObject jsonObject = JSON.parseObject(json);

        //从json对象中解析出 openId 并返回

        return jsonObject.getString("openid");
    }
}

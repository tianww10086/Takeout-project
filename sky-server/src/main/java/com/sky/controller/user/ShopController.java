package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "用户店铺相关接口")
@Slf4j
@Qualifier("UserShopController")
public class ShopController {


    @Autowired
    RedisTemplate template;


        @GetMapping("/status")
        @ApiOperation("获取营业状态")
        public Result<Integer> getStatus(){
            log.info("获取店铺状态");
            var ValueOperation = template.opsForValue(); //获取操作字符串类型的对象
            Integer status = (Integer) ValueOperation.get("SHOP_STATUS");

            return Result.success(status);
        }

}

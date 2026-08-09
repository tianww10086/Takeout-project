package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;

@Mapper
@Validated //开启方法参数检验
public interface OrderDetailMapper
{

     void insertBatch(List<OrderDetail> orderDetails);

     @NotNull(message="条件不能为空")
     Orders selectByCondition(@NotNull Orders conditions);

     /**
      * 根据订单号查询订单，用于支付回调
      * @param orderNumber
      * @return
      */
     @NotNull(message = "订单号不能为空")
     Orders getByNumber(@NotNull String orderNumber);
}

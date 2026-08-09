package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;

@Mapper
@Validated //开启方法参数检验
public interface OrderDetailMapper
{
     /**
      * 批量插入订单详细
      * @param orderDetails
      */
     void insertBatch(List<OrderDetail> orderDetails);


     /**
      * 查询对应订单详细列表
      * @param order
      * @return
      */
     List<OrderDetail> select(Orders order);


     /**
      * 根据订单id集合查询订单明细
      * @param ids
      * @return
      */
     List<OrderDetail> selectByOrderIds(List<Long> ids);
}

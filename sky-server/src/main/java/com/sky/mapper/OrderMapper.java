package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     * @param order
     */
     void insert(Orders order);

    /**
     * 更新订单
     * @param order
     */
    void update(Orders order);

    /**
     * 分页查询历史订单（PageHelper自动拼接limit）
     * @param dto
     * @return
     */
    Page<Orders> getPage(@Param("dto") OrdersPageQueryDTO dto,@Param("userId") Long currentUserId);

    Page<Orders> getPage(@Param("dto") OrdersPageQueryDTO dto);
    @NotNull(message="条件不能为空")
    Orders selectByCondition(@NotNull Orders conditions);

    /**
     * 根据订单号查询订单，用于支付回调
     * @param orderNumber
     * @return
     */
    @NotNull(message = "订单号不能为空")
    Orders getByNumber(@NotNull String orderNumber);


    Orders selectById(Long id);

    Integer countByStatus(int i);

    @Select("""
           select * from orders
           where
               pay_status=#{unPaid}
           AND
               order_time<#{now}
           """)
    List<Orders> getByStatusAndOrderTimeLT(Integer unPaid, LocalDateTime now);

    Integer countByMap(Map map);

    Double sumByMap(Map map);
}

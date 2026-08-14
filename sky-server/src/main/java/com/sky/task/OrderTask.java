package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    OrderMapper orderMapper;

    /**
     * 处理超时订单
    */
    @Scheduled(cron = "0 * * * * *") // //每分钟检查一次订单是否已支付。
    public void checkOutTimeOrder(){

        //outTime：如果订单时间小于当前时间的再减去15分钟，则说明超时，
        LocalDateTime outTime = LocalDateTime.now().minusMinutes(15);
       List<Orders> list= orderMapper.getByStatusAndOrderTimeLT(Orders.UN_PAID,outTime);
       if(list !=null && list.size()>0){
           for(Orders order:list){
               order.setStatus(Orders.CANCELLED);
               order.setCancelReason("订单超时，自动取消");
               order.setCancelTime(LocalDateTime.now());
               orderMapper.update(order);
           }
       }
    }
    // 每天凌晨一点触发一次
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("定时处理处于派送中的订单:{}",LocalDateTime.now());

        //前一天的时间
        LocalDateTime time = LocalDateTime.now().minusMinutes(60);
        //所有小于前一天的时间都会被定时处理
        List<Orders> list= orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS,time);

        if(list !=null && list.size()>0){
            for(Orders order:list){
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}

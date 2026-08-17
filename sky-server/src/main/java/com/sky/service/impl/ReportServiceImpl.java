package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.dto.DataOverViewQueryDTO;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.StringBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    /**
     *
     * @param begin 起始时间
     * @param end  结束时间
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 1. 查询区间：左闭右开 [begin 00:00:00, end+1 00:00:00)
        LocalDateTime start = begin.atStartOfDay();
        LocalDateTime queryEnd = end.plusDays(1).atStartOfDay();

        //sumByGroup查询出List<Map<String,Object>> groupingBy进行分组为map
        Map<LocalDate, Double> turnoverByDay = reportMapper.sumByGroup(start, queryEnd).stream()
                .collect(Collectors.groupingBy(
                        r -> ((java.sql.Date) r.get("day")).toLocalDate(),
                        Collectors.summingDouble(r -> ((Number) r.get("total")).doubleValue())
                ));

        // 通过begin end构建日期列表
        List<LocalDate> dateList = LongStream.rangeClosed(begin.toEpochDay(), end.toEpochDay())
                .mapToObj(LocalDate::ofEpochDay)
                .collect(Collectors.toList());

        // 4. 两个字符串一步成型：joining 自动加逗号、无尾逗号，内部就是 StringBuilder
        String dateStr = dateList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String turnoverStr = dateList.stream()
                .map(d -> String.format(Locale.ROOT, "%.2f", turnoverByDay.getOrDefault(d, 0.0)))
                .collect(Collectors.joining(","));

        // 5. 组装并返回
        TurnoverReportVO vo = new TurnoverReportVO();
        vo.setDateList(dateStr);
        vo.setTurnoverList(turnoverStr);
        return vo;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        //构造时间区间集合
        List<LocalDate> dates = LongStream.rangeClosed(begin.toEpochDay(),end.toEpochDay())
                .mapToObj(LocalDate::ofEpochDay)
                .collect(Collectors.toList());

        List<Integer> totalUserList = new ArrayList<>(); //用户总量集合

        List<Integer> newUserList = new ArrayList<>(); //新增用户集合

        for(LocalDate date:dates){
            LocalDateTime beginTime = date.atStartOfDay(); //当天凌晨零点
            LocalDateTime endTime = date.plusDays(1).atStartOfDay(); //次日凌晨零点

            //查询当天新增用户集合,传入beginTime endTime，创建时间位于当天的是新增用户，
            HashMap<String ,Object> map= new HashMap<>();
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);
            Integer newUser= reportMapper.countUsersByMap(map); //countUserByMap通过map的条件查询当天创建的用户数有多少

            map.clear();//清空map，查询当天用户总量，不传入当天起始时间，只要创建时间小于当天结束时间即视为用户总量
            map.put("endTime",endTime);
            Integer oldUser = reportMapper.countUsersByMap(map);
            totalUserList.add(oldUser); //加入集合
            newUserList.add(newUser); //加入集合
        }

        return UserReportVO.builder()
                .totalUserList(totalUserList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .newUserList(newUserList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .dateList(dates.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .build();
    }


    /**
     * 查询统计单独数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //计算日期区间列表
        List<LocalDate> dateList = LongStream.rangeClosed(begin.toEpochDay(),end.toEpochDay())
                .mapToObj(LocalDate::ofEpochDay)
                .collect(Collectors.toList());

        List<Integer> orderCountList = new ArrayList<>(); //每日订单数
        List<Integer> validOrderCountList = new ArrayList<>(); //每日有效订单数


        Integer totalOrderCount =0; //订单总数
        Integer validOrderCount =0;//有效订单总数

        for(LocalDate date:dateList){
            //计算当日区间
            LocalDateTime beginTime = date.atStartOfDay(); //起始时间
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();//结束时间

            //获取每日订单数
            HashMap<String,Object> map =new HashMap<>();
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);
            Integer orderCount = reportMapper.countOrderByMap(map);
            //获取每日有效订单数(订单状态已完成)
            map.put("status",5);
            Integer validCount = reportMapper.countOrderByMap(map);

            orderCountList.add(orderCount);
            validOrderCountList.add(validCount);
        }

        //获取订单总数
        HashMap<String,Object> map = new HashMap<>();
        //要查询订单总数，那么只需传入一个条件，就是截止时间,sql会判定订单创建时间小于截止时间的订单
        map.put("endTime",end.plusDays(1).atStartOfDay());
        totalOrderCount = reportMapper.countOrderByMap(map);


        //获取有效订单总数
        map.put("status",5); //有效订单的状态为5
        validOrderCount = reportMapper.countOrderByMap(map);


        //计算订单完成率
        double orderCompletionRate = (double) validOrderCount / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(dateList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .orderCountList(orderCountList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .validOrderCountList(validOrderCountList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 查询销量前十的商品
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        HashMap<String,Object> map = new HashMap<>();
        map.put("beginTime",beginTime);
        map.put("endTime",endTime);
    map.put("status",5);
        map.put("N",10);
       List<Map<String,Object>> tops= reportMapper.findTopNGoods(map);

       return SalesTop10ReportVO.builder()
               .nameList(tops
                       .stream()
                       .map(m->m.get("name"))
                       .map(String::valueOf)
                       .collect(Collectors.joining(",")))
               .numberList(tops.
                       stream()
                       .map(m->m.get("number"))
                       .map(String::valueOf)
                       .collect(Collectors.joining(",")))
               .build();


    }
}

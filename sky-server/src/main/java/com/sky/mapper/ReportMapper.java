package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.constraints.Max;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {


    List<Map<String,Object>> sumByGroup(
            @Param("begin")LocalDateTime begin,
            @Param("end")LocalDateTime end);


    /**
     * map是动态条件的一个集合
     * 查询符合条件的记录数
     * @param map
     * @return
     */
    Integer countUsersByMap(HashMap<String, Object> map);


    /**
     * map是动态条件的一个集合
     * 查询符合条件的记录数
     * @param map
     * @return
     */
    Integer countOrderByMap(HashMap<String, Object> map);

    List<Map<String,Object>> findTopNGoods(HashMap<String, Object> map);
}

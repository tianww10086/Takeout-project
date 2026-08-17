package com.sky.controller.admin;

import com.sky.dto.DataOverViewQueryDTO;
import com.sky.exception.BaseException;
import com.sky.exception.ParamEmptyException;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Api("数据统计相关接口")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService service;
    /**
     * 统计时间范围内的营业额数据
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("统计营业额接口")
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                                                       @DateTimeFormat(pattern="yyyy-MM-dd")LocalDate end){
        log.info("营业额数据统计：{},{}",begin,end);

        return Result.success(service.getTurnoverStatistics(begin,end));
    }

    @GetMapping("/userStatistics")
    @ApiOperation("用户统计接口")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                                               @DateTimeFormat(pattern="yyyy-MM-dd")LocalDate end){
        log.info("用户数据统计:{},{}",begin,end);

        return Result.success(service.getUserStatistics(begin,end));

    }

    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计接口")
    public Result<OrderReportVO> orderStatistics(@DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                                                 @DateTimeFormat(pattern="yyyy-MM-dd")LocalDate end){
        log.info("订单数据统计:{},{}",begin,end);

        return Result.success(service.getOrderStatistics(begin,end));
    }

    /**
     * 统计销量排名top10
     */
    @GetMapping("/top10")
    @ApiOperation("销量数据前10统计")
    public Result<SalesTop10ReportVO> top10Sale(@DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                                                @DateTimeFormat(pattern="yyyy-MM-dd")LocalDate end){
        log.info("统计销量排名前十的商品:{},{}",begin,end);

        return  Result.success(service.getTop10(begin,end));
    }

    /**
     * 导出最近三十天的报表
     */
    @GetMapping("/export")
    @ApiOperation("导出最近三十天的数据报表")
    public void exportExcel(HttpServletResponse response)  {
        service.exportBusinessData(response);
    }
}

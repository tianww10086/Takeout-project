package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags= "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login") //  @RequestBody接收前端json数据
    @ApiOperation(value = "员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        //打印日志
        log.info("员工登录：{}", employeeLoginDTO);

        //调用员工服务，获取员工信息
        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        //放入载荷数据，EMP_ID，和该员工对应的id
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(), // 读取jwt配置类的密钥信息，并设置
                jwtProperties.getAdminTtl(), // 该JWT令牌声明周期
                claims); //载荷 员工id

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出登录")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     *
     *  根据id查询员工
     * @Param Integer 员工id
     * @return 返回结果
     */
    @GetMapping("/{id}")
    @ApiOperation("员工id查询")
    public Result<Employee> findById(@PathVariable Integer id){
        Employee e=  employeeService.findById(id);
        e.setPassword(" ");
        return Result.success(e);
    }

    /**
     *
     * @param employeeDTO 前端传入的数据
     * @return 操作的结果
     */
    @PostMapping
    @ApiOperation("新增员工")
    public Result<String> addEmployee(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工：{}",employeeDTO);
        boolean result = employeeService.addEmployee(employeeDTO);
        return result?Result.success():Result.error("新增失败");
    }

    /**
     *
     * @param epqd
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult<Employee>> pages(EmployeePageQueryDTO epqd){
        log.info("员工分页查询，参数为{}",epqd);
        PageResult<Employee> pageResult = employeeService.pageQuery(epqd);

        return Result.success(pageResult);
    }

    /**
     * 修改员工状态
     * @param status 状态值
     * @param id 员工id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用或禁用员工状态")
    public Result StartOrStop(@PathVariable Integer status,Long id){
        log.info("启用或禁用员工账户：{},{}",status,id);

        boolean tof=  employeeService.StartOrStop(status,id);


        return tof?Result.success():Result.error("员工状态修改失败");
    }

    /**
     * 更新（编辑员工信息）
     * @param dto 员工数据传输对象，用于接受前端发送的json数据
     * @return 返回成功或错误信息
     */
    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result updateEmployee(@RequestBody EmployeeDTO dto){
        log.info("修改员工：{}的信息",dto);
        boolean tof = employeeService.updateEmployee(dto);
        return tof?Result.success():Result.error("编辑员工信息失败");
    }

}

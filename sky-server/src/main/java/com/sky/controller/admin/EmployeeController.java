package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
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
                jwtProperties.getAdminTtl(), // 改JWT令牌声明周期
                claims); //载荷

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
    public Result<Employee> findById(@PathVariable Integer id){
        Employee e=  employeeService.findById(id);
        return Result.success(e);
    }

    /**
     *
     * @param employeeDTO 前端传入的数据
     * @return 操作的结果
     */
    @PostMapping
    public Result<String> addEmployee(@RequestBody EmployeeDTO employeeDTO){
        boolean result = employeeService.addEmployee(employeeDTO);
        return result?Result.success():Result.error("新增失败");
    }
}

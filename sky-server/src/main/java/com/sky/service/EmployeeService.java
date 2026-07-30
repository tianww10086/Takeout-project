package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

import java.util.List;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
    * 根据员工id查询
    * @param integer 员工id
    * @return 返回查询到的id
    * */
    Employee findById(Integer integer);

    /**
     *
     * @param employeeDTO 前端传输过来的数据模型
     * @return 操作成功与否
     */
    boolean addEmployee(EmployeeDTO employeeDTO);


    PageResult<Employee> pageQuery(EmployeePageQueryDTO epqd);
}

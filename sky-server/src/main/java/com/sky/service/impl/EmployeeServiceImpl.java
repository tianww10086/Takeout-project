package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.*;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee  login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes()); //md5加密
        System.out.println("password:"+password);
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }


    public Employee findById(Integer id){
        Employee employee = employeeMapper.getByUserId(id);
        if(employee==null){
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        return employee;
    }

    public boolean addEmployee(EmployeeDTO employeeDTO){

        //健壮性代码
        if(employeeDTO==null)
            throw new NullEmployeeException("数据为空");

        //构造Employee对象
        Employee  e = new Employee(employeeDTO);
        String password = DigestUtils.md5DigestAsHex("123456".getBytes());
        e.setPassword(password);
        e.setCreateTime(LocalDateTime.now());
        e.setUpdateTime(LocalDateTime.now());
        e.setCreateUser(BaseContext.getCurrentId());
        e.setUpdateUser(BaseContext.getCurrentId());
        return employeeMapper.addUEmployee(e)>0;
    }

    @Override
    public PageResult<Employee> pageQuery(EmployeePageQueryDTO epqd) {
        //开始分页查询
        PageHelper.startPage(epqd.getPage(),epqd.getPageSize());
        Page<Employee> page =  employeeMapper.getPage(epqd);

        // page.getTotal()是总条数，page.getResult是拿到的集合
        return new PageResult<>(page.getTotal(),page.getResult());
    }

    /**
     * 启用禁用员工账户
     * @param status
     * @param id
     */
    public boolean StartOrStop(Integer status, Long id){
        Employee e = Employee.builder()
                .id(id)
                .status(status)
                .build();
        return employeeMapper.update(e)>0;
    }

    @Override
    public boolean updateEmployee(EmployeeDTO dto) {
        Employee e = new Employee();

        // 把DTO的同名属性拷贝到Employee对象里
        BeanUtils.copyProperties(dto,e);
        e.setUpdateTime(LocalDateTime.now()); //设置修改时间
        e.setUpdateUser(BaseContext.getCurrentId()); //设置修改者id
        return employeeMapper.update(e)>0;
    }
}

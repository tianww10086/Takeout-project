package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * @param id 员工的id
     * @return 查询到的员工
     */
    @Select("select * from employee where id = #{id}")
    Employee getByUserId(Integer id);

    @Insert("""
    INSERT INTO employee(name,username,password,phone,sex,id_number,
                         create_time,update_time,create_user,update_user)
            values(#{name},#{username},#{password},#{phone},#{sex},#{idNumber}
            ,#{createTime},#{updateTime},#{createUser},#{updateUser})
    """)
    Integer addUEmployee(Employee employee);


    Page<Employee> getPage(EmployeePageQueryDTO dto);


    /**
     * 根据主键动态修改属性
     */

    Integer  update(Employee e);
}

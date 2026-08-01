package com.sky.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sky.dto.EmployeeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username; //用户名

    private String name; //员工姓名

    private String password;

    private String phone;

    private String sex;

    private String idNumber; // 身份证号

    private Integer status; // 状态，1启用，0禁用


    private LocalDateTime createTime;  //员工创建时间


    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;

    //根据前端传输过来的数据模型快捷构造
    public Employee(EmployeeDTO dto){
        this.name = dto.getName();
        this.username = dto.getUsername();
        this.sex = dto.getSex();
        this.phone = dto.getPhone();
        this.idNumber = dto.getIdNumber();
    }
}

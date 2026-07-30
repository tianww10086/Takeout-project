package com.sky.dto;

import io.swagger.annotations.Api;
import lombok.Data;

import java.io.Serializable;

@Data
@Api("新增员工时的数据模型")
public class EmployeeDTO implements Serializable {

    private Long id;

    private String username; //用户名

    private String name; //

    private String phone;

    private String sex;

    private String idNumber;

}

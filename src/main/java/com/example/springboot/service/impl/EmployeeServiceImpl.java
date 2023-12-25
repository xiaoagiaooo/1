package com.example.springboot.service.impl;

import com.example.springboot.entity.Employee;
import com.example.springboot.mapper.EmployeeMapper;
import com.example.springboot.service.IEmployeeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements IEmployeeService {

}

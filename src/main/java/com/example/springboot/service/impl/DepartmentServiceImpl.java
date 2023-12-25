package com.example.springboot.service.impl;

import com.example.springboot.entity.Department;
import com.example.springboot.mapper.DepartmentMapper;
import com.example.springboot.service.IDepartmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {

}

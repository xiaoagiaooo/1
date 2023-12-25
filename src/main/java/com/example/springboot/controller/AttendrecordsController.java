package com.example.springboot.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Attendrecords;
import com.example.springboot.entity.Employee;
import com.example.springboot.entity.User;
import com.example.springboot.service.IAttendrecordsService;
import com.example.springboot.service.IEmployeeService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;


@RestController
@RequestMapping("/attendrecords")
public class AttendrecordsController {

    @Resource
    private IAttendrecordsService attendrecordsService;

    @Resource
    private IEmployeeService employeeService;

    private final String now = DateUtil.now();

    // 新增或者更新
    @PostMapping
    public Result save(@RequestBody Attendrecords attendrecords) {

        //08:30
        String startTime = attendrecords.getStartTime();
        String endTime = attendrecords.getEndTime();
        String status = ""; // 初始化状态为空
        if (startTime.compareTo("09:00") > 0) {
            status += "迟到";
        }
        if (endTime.compareTo("18:00") < 0) {
            if (!status.isEmpty()) {
                status += "，";
            }
            status += "早退";
        }

        if (attendrecords.getId() != null) {
            if (status.isEmpty()) {
                status = "正常出勤";
            }
        }
        attendrecords.setStatus(status);
        attendrecordsService.saveOrUpdate(attendrecords);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        attendrecordsService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        attendrecordsService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    public Result findAll() {
        return Result.success(attendrecordsService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(attendrecordsService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Attendrecords> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("employee", name);
        }


        List<Employee> employeeList = employeeService.list();
        Page<Attendrecords> page = attendrecordsService.page(new Page<>(pageNum, pageSize), queryWrapper);
        for (Attendrecords record : page.getRecords()) {
           employeeList.stream()
                   .filter(employee -> employee.getName().equals(record.getEmployee()))
                   .findFirst()
                   .ifPresent(employee -> {
                       record.setEmployeeId(employee.getId());
                       attendrecordsService.updateById(record);
                   });
        }
        return Result.success(page);
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Attendrecords> list = attendrecordsService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Attendrecords信息表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();

        }

    /**
     * excel 导入
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Attendrecords> list = reader.readAll(Attendrecords.class);

        attendrecordsService.saveBatch(list);
        return Result.success();
    }

    private User getUser() {
        return TokenUtils.getCurrentUser();
    }

}


package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.DepartmentEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.service.DepartmentService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Author: XYmar
 * Date: 2019/5/17 10:18
 */
@RestController
@RequestMapping(value = "/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // 查询所有部门信息
    @GetMapping
    public ResultEntity getAll(){
        return ResultUtils.success(departmentService.getAll());
    }

    // 新增部门信息
    @PostMapping
    public ResultEntity saveDepartment(@RequestBody @Valid DepartmentEntity departmentEntity){
        return ResultUtils.success(departmentService.saveDepartment(departmentEntity));
    }

    // 修改部门信息
    @PostMapping(value = "/{departmentId}")
    public ResultEntity updateDepartmentById(@PathVariable(value = "departmentId") String departmentId, @RequestBody @Valid DepartmentEntity departmentEntity){
        return ResultUtils.success(departmentService.updateDepartmentById(departmentId, departmentEntity));
    }

    // 删除部门信息
    @DeleteMapping(value = "/{departmentId}")
    public ResultEntity deleteById(@PathVariable(value = "departmentId") String departmentId){
        return ResultUtils.success(departmentService.deleteDepartmentById(departmentId));
    }
}

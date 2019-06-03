package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.Department;
import com.rengu.cosimulation.entity.Users;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.DepartmentRepository;
import com.rengu.cosimulation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/5/17 9:19
 */
@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    // 查询所有部门
    public List<Department> getAll(){
        return departmentRepository.findAll();
    }

    // 新增部门
    public Department saveDepartment(Department department){
        return departmentRepository.save(department);
    }

    // 修改部门信息
    public Department updateDepartmentById(String id, Department departmentArgs){
        if(!hasDepartmentById(id)){
            throw new ResultException(ResultCode.DEPARTMENT_ID_NOT_FOUND_ERROR);
        }
        Department department = getDepartmentById(id);
        if(!StringUtils.isEmpty(departmentArgs.getName()) && !department.getName().equals(departmentArgs.getName())){
            if(hasDepartmentByName(departmentArgs.getName())){
                throw new ResultException(ResultCode.DEPARTMENT_NAME_EXISTED_ERROR);
            }
            department.setName(departmentArgs.getName());
        }
        if(!StringUtils.isEmpty(departmentArgs.getDescription())){
            department.setDescription(departmentArgs.getDescription());
        }
        return departmentRepository.save(department);
    }

    // 删除部门
    public Department deleteDepartmentById(String id){
        if(!hasDepartmentById(id)){
            throw new ResultException(ResultCode.DEPARTMENT_ID_NOT_FOUND_ERROR);
        }
        Department department = getDepartmentById(id);
        List<Users> usersList = userRepository.findByDepartment(department);
        userRepository.deleteInBatch(usersList);
        departmentRepository.delete(department);
        return department;
    }

    // 根据id查询部门是否存在
    public boolean hasDepartmentById(String id){
        if(StringUtils.isEmpty(id)){
            return false;
        }
        return departmentRepository.existsById(id);
    }

    // 根据id查询部门
    public Department getDepartmentById(String id){
        if(!hasDepartmentById(id)){
            throw new ResultException(ResultCode.DEPARTMENT_ID_NOT_FOUND_ERROR);
        }
        return departmentRepository.findById(id).get();
    }

    // 根据名称查询部门
    public Department getDepartmentByName(String name){
        if(!hasDepartmentByName(name)){
            throw new ResultException(ResultCode.DEPARTMENT_NAME_NOT_FOUND_ERROR);
        }
        return departmentRepository.findByName(name);
    }

    // 根据名称查询部门是否存在
    public boolean hasDepartmentByName(String name){
        if(StringUtils.isEmpty(name)){
            throw new ResultException(ResultCode.DEPARTMENT_NAME_NOT_FOUND_ERROR);
        }
        return departmentRepository.existsByName(name);
    }
}

package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.DepartmentEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.DepartmentRepository;
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

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // 查询所有部门
    public List<DepartmentEntity> getAll(){
        return departmentRepository.findAll();
    }

    // 新增部门
    public DepartmentEntity saveDepartment(DepartmentEntity departmentEntity){
        return departmentRepository.save(departmentEntity);
    }

    // 修改部门信息
    public DepartmentEntity updateDepartmentById(String id, DepartmentEntity departmentEntityArgs){
        if(!hasDepartmentById(id)){
            throw new ResultException(ResultCode.DEPARTMENT_ID_NOT_FOUND_ERROR);
        }
        DepartmentEntity departmentEntity = getDepartmentById(id);
        if(!StringUtils.isEmpty(departmentEntityArgs.getName())){
            if(hasDepartmentByName(departmentEntityArgs.getName())){
                throw new ResultException(ResultCode.DEPARTMENT_NAME_EXISTED_ERROR);
            }
            departmentEntity.setName(departmentEntityArgs.getName());
        }
        if(!StringUtils.isEmpty(departmentEntityArgs.getDescription())){
            departmentEntity.setDescription(departmentEntityArgs.getDescription());
        }
        return departmentRepository.save(departmentEntity);
    }

    // 删除部门
    public DepartmentEntity deleteDepartmentById(String id){
        if(!hasDepartmentById(id)){
            throw new ResultException(ResultCode.DEPARTMENT_ID_NOT_FOUND_ERROR);
        }
        DepartmentEntity departmentEntity = getDepartmentById(id);
        departmentRepository.delete(departmentEntity);
        return departmentEntity;
    }

    // 根据id查询部门是否存在
    public boolean hasDepartmentById(String id){
        if(StringUtils.isEmpty(id)){
            return false;
        }
        return departmentRepository.existsById(id);
    }

    // 根据id查询部门
    public DepartmentEntity getDepartmentById(String id){
        if(!hasDepartmentById(id)){
            throw new ResultException(ResultCode.DEPARTMENT_ID_NOT_FOUND_ERROR);
        }
        return departmentRepository.findById(id).get();
    }

    // 根据名称查询部门
    public DepartmentEntity getDepartmentByName(String name){
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

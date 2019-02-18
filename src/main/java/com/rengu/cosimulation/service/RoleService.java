package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.RoleEntity;
import com.rengu.cosimulation.entity.UserEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.RoleRepository;
import com.rengu.cosimulation.utils.ApplicationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/12 16:19
 */
@Service
@Slf4j
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // 保存角色
    @CacheEvict(value = "Role_Cache", allEntries = true)
    public RoleEntity saveRole(RoleEntity roleEntity){
        if(roleEntity == null){
            throw new RuntimeException(ApplicationMessage.ROLE_ARGS_NOT_FOUND);
        }
        if(StringUtils.isEmpty(roleEntity.getName())){
            throw new ResultException(ResultCode.ROLE_NAME_ARGS_NOT_FOUND_ERROR);
        }
        if(hasRoleByName(roleEntity.getName())){
            throw new RuntimeException(ApplicationMessage.ROLE_NAME_EXISTED);
        }
        if(roleEntity.getChangeable() == null){
            roleEntity.setChangeable(true);
        }
        return roleRepository.save(roleEntity);
    }

    // 根据名称查询角色是否存在
    public boolean hasRoleByName(String name) {
        if(StringUtils.isEmpty(name)){
            return false;
        }

        return roleRepository.existsByName(name);
    }

    // 查询所有角色信息
    public List<RoleEntity> getAll() {
        return roleRepository.findAll();
    }

    // 根据角色名称查询角色信息
    public RoleEntity getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    // 根据ID查询角色信息
    @Cacheable(value = "Role_Cache", key = "#roleId")
    public RoleEntity getRoleById(String roleId) {
        if(!hasRoleById(roleId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        return roleRepository.findById(roleId).get();
    }

    // 根据Id查询角色是否存在
    public boolean hasRoleById(String roleId) {
        if (StringUtils.isEmpty(roleId)) {
            return false;
        }
        return roleRepository.existsById(roleId);
    }

    // 根据ID修改角色信息
    @CachePut(value = "Role_Cache", key = "#roleId")
    public RoleEntity updateRoleByRoleId(String roleId, RoleEntity roleEntityArgs) {
        if(!hasRoleById(roleId)){
            throw new ResultException(ResultCode.ROLE_ID_NOT_FOUND_ERROR);
        }
        RoleEntity roleEntity = getRoleById(roleId);
        if(!roleEntity.getChangeable()){    // 不可修改的角色
           throw new ResultException(ResultCode.ROLE_CHANGE_NOT_SUPPORT_ERROR);
        }
        if(roleEntityArgs == null){
            throw new ResultException(ResultCode.ROLE_ARGS_NOT_FOUND_ERROR);
        }

        if(!StringUtils.isEmpty(roleEntityArgs.getName()) && !roleEntity.getName().equals(roleEntityArgs.getName())){
            roleEntity.setName(roleEntityArgs.getName());
        }
        if(!StringUtils.isEmpty(roleEntityArgs.getDescription()) && !roleEntity.getDescription().equals(roleEntityArgs.getDescription())){
            roleEntity.setDescription(roleEntityArgs.getDescription());
        }
        return roleRepository.save(roleEntity);
    }

    // 根据ID删除角色信息
    public RoleEntity deleteByRoleId(String roleId) {
        if(!hasRoleById(roleId)){
            throw new ResultException(ResultCode.ROLE_ID_NOT_FOUND_ERROR);
        }
        RoleEntity roleEntity = getRoleById(roleId);
        if(!roleEntity.getChangeable()){    // 不可修改的角色
            throw new ResultException(ResultCode.ROLE_CHANGE_NOT_SUPPORT_ERROR);
        }
        roleRepository.delete(roleEntity);
        return roleEntity;
    }
}

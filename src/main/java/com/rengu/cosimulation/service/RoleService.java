package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.RoleEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.RoleRepository;
import com.rengu.cosimulation.utils.ApplicationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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
}

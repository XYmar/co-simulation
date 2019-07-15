package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.Role;
import com.rengu.cosimulation.entity.Users;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.RoleRepository;
import com.rengu.cosimulation.repository.UserRepository;
import com.rengu.cosimulation.utils.ApplicationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: XYmar
 * Date: 2019/2/12 16:19
 */
@Service
@Slf4j
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    // 保存角色
    @CacheEvict(value = "Role_Cache", allEntries = true)
    public Role saveRole(Role role){
        if(role == null){
            throw new RuntimeException(ApplicationMessage.ROLE_ARGS_NOT_FOUND);
        }
        if(StringUtils.isEmpty(role.getName())){
            throw new ResultException(ResultCode.ROLE_NAME_ARGS_NOT_FOUND_ERROR);
        }
        if(hasRoleByName(role.getName())){
            throw new ResultException(ResultCode.ROLE_NAME_EXISTED_ERROR);
        }
        if(role.getChangeable() == null){
            role.setChangeable(true);
        }
        return roleRepository.save(role);
    }

    // 根据名称查询角色是否存在
    public boolean hasRoleByName(String name) {
        if(StringUtils.isEmpty(name)){
            return false;
        }

        return roleRepository.existsByName(name);
    }

    // 查询所有角色信息
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    // 根据角色名称查询角色信息
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    // 根据ID查询角色信息
    @Cacheable(value = "Role_Cache", key = "#roleId")
    public Role getRoleById(String roleId) {
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
    public Role updateRoleByRoleId(String roleId, Role roleArgs) {
        if(!hasRoleById(roleId)){
            throw new ResultException(ResultCode.ROLE_ID_NOT_FOUND_ERROR);
        }
        Role role = getRoleById(roleId);
        if(!role.getChangeable()){    // 不可修改的角色
           throw new ResultException(ResultCode.ROLE_CHANGE_NOT_SUPPORT_ERROR);
        }
        if(roleArgs == null){
            throw new ResultException(ResultCode.ROLE_ARGS_NOT_FOUND_ERROR);
        }

        if(!StringUtils.isEmpty(roleArgs.getName()) && !role.getName().equals(roleArgs.getName())){
            if(hasRoleByName(roleArgs.getName())){
                throw new ResultException(ResultCode.ROLE_NAME_EXISTED_ERROR);
            }
            role.setName(roleArgs.getName());
        }
        if(!StringUtils.isEmpty(roleArgs.getDescription()) && !role.getDescription().equals(roleArgs.getDescription())){
            role.setDescription(roleArgs.getDescription());
        }
        return roleRepository.save(role);
    }

    // 根据ID删除角色信息
    public Role deleteByRoleId(String roleId) {
        if(!hasRoleById(roleId)){
            throw new ResultException(ResultCode.ROLE_ID_NOT_FOUND_ERROR);
        }
        Role role = getRoleById(roleId);
        if(!role.getChangeable()){    // 不可修改的角色
            throw new ResultException(ResultCode.ROLE_CHANGE_NOT_SUPPORT_ERROR);
        }
        List<Users> usersList = userRepository.findByRoleEntitiesContaining(role);
        if(usersList.size() > 0){
            for(Users users : usersList){
                if(users.getRoleEntities().size() == 1){
                    Set<Role> roleHashSet = new HashSet<>();
                    roleHashSet.add(getRoleByName("normal_designer"));
                    users.setRoleEntities(roleHashSet);
                }else {
                    users.getRoleEntities().remove(role);
                }
            }
            userRepository.saveAll(usersList);
        }
        roleRepository.delete(role);
        return role;
    }
}

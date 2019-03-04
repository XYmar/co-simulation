package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.RoleEntity;
import com.rengu.cosimulation.entity.UserEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.UserRepository;
import com.rengu.cosimulation.utils.ApplicationConfig;
import com.rengu.cosimulation.utils.ApplicationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: XYmar
 * Date: 2019/2/12 16:15
 */
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleService roleService;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    // 保存用户 , 一个角色
    @CacheEvict(value = "User_Cache", allEntries = true)
    public UserEntity saveUser(UserEntity userEntity, String roleName) {
        if (userEntity == null) {
            throw new ResultException(ResultCode.USER_ARGS_NOT_FOUND_ERROR);
        }
        if (hasUserByUsername(userEntity.getUsername())) {
            throw new ResultException(ResultCode.USER_USERNAME_EXISTED_ERROR);
        }
        userEntity.setPassword(new BCryptPasswordEncoder().encode(userEntity.getPassword()));

        if(StringUtils.isEmpty(roleName)){
            throw new ResultException(ResultCode.USER_ROLE_NOT_FOUND_ERROR);
        }
        Set<RoleEntity> roleEntitySet = new HashSet<>();
        roleEntitySet.add(roleService.getRoleByName(roleName));
        userEntity.setRoleEntities(roleEntitySet);
        /*if(userEntity.getRoleEntities() != null){                    // 设置三元等
            userEntity.setRoleEntities(userEntity.getRoleEntities());
        }else{                                                       // 设置普通用户
            Set<RoleEntity> roleEntitySet = new HashSet<>();
            roleEntitySet.add(roleService.getRoleByName(ApplicationConfig.DEFAULT_USER_ROLE_NAME));
            userEntity.setRoleEntities(roleEntitySet);
        }*/

        return userRepository.save(userEntity);
    }

    //保存管理员用户
    /*public UserEntity saveAdminUser(UserEntity userEntity, String roleName) {
        return saveUser(userEntity);
    }*/

    // 查询所有用户
    public List<UserEntity> getAll() {
        return userRepository.findAll();
    }

    // 根据用户名查询用户是否存在
    public boolean hasUserByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return false;
        }
        return userRepository.existsByUsername(username);
    }

    // 根据用户名查询用户
    @Cacheable(value = "User_Cache", key = "#username")
    public UserEntity getUserByUsername(String username) {
        if (!hasUserByUsername(username)) {
            throw new ResultException(ResultCode.USER_USERNAME_NOT_FOUND_ERROR);
        }
        return userRepository.findByUsername(username).get();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserByUsername(username);
    }

    // 根据Id查询用户是否存在
    public boolean hasUserById(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        return userRepository.existsById(userId);
    }

    // 根据id查询用户
    @Cacheable(value = "User_Cache", key = "#userId")
    public UserEntity getUserById(String userId) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        return userRepository.findById(userId).get();
    }

    // 根据id修改用户
    @CachePut(value = "User_Cache", key = "#userId")
    public UserEntity updateUserByUserId(String userId, UserEntity userEntityArgs, String[] ids) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = getUserById(userId);
        if(userEntityArgs == null){
            throw new ResultException(ResultCode.USER_ARGS_NOT_FOUND_ERROR);
        }

        if(!StringUtils.isEmpty(userEntityArgs.getUsername()) && !userEntity.getUsername().equals(userEntityArgs.getUsername())){
            if (hasUserByUsername(userEntityArgs.getUsername())) {
                throw new ResultException(ResultCode.USER_USERNAME_EXISTED_ERROR);
            }
            userEntity.setUsername(userEntityArgs.getUsername());
        }

        if(!StringUtils.isEmpty(String.valueOf(userEntityArgs.getSecretClass()))){
            userEntity.setSecretClass(userEntityArgs.getSecretClass());
        }

        List<RoleEntity> roleEntityList = new ArrayList<>();
        if(ids.length == 0){
            throw new ResultException(ResultCode.USER_ROLE_NOT_FOUND_ERROR);
        }
        for (String id : ids) {
            roleEntityList.add(roleService.getRoleById(id));
        }

        HashSet<RoleEntity> roleEntityHashSet = new HashSet<>(roleEntityList);

        userEntity.setRoleEntities(roleEntityHashSet);

        return userRepository.save(userEntity);
    }

    // 根据id删除用户
    @CacheEvict(value = "User_Cache", key = "#userId")
    public UserEntity deleteByUserId(String userId) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = getUserById(userId);
        userRepository.delete(userEntity);
        return userEntity;
    }

    // 根据id修改用户密码
    @CacheEvict(value = "User_Cache", key = "#userId")
    public UserEntity updatePasswordById(String userId, String password) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(password)){
            throw new ResultException(ResultCode.USER_PASSWORD_ARGS_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = getUserById(userId);
        userEntity.setPassword(new BCryptPasswordEncoder().encode(password));
        return userRepository.save(userEntity);
    }

    // 根据id分配用户权限,  一次只能分配一个角色, 再次传入角色的时候累加
    /*@CacheEvict(value = "User_Cache", key = "#userId")
    public UserEntity distributeUserById(String userId, String roleId) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if(!roleService.hasRoleById(roleId)){
            throw new ResultException(ResultCode.ROLE_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = getUserById(userId);
        Set<RoleEntity> roleEntitySet = new HashSet<>();
        roleEntitySet.add(roleService.getRoleById(roleId));
        userEntity.setRoleEntities(roleEntitySet);
        return userRepository.save(userEntity);
    }*/

    // 根据id禁用或解除禁用
    @CacheEvict(value = "User_Cache", key = "#userId")
    public UserEntity assignUserById(String userId, Boolean enabled) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(enabled)){
            throw new ResultException(ResultCode.USER_ENABLED_NOT_SUPPORT_ERROR);
        }
        UserEntity userEntity = getUserById(userId);
        userEntity.setEnabled(enabled);
        return userRepository.save(userEntity);
    }
}

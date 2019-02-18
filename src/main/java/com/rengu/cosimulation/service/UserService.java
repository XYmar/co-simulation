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

import java.util.List;

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

    // 保存用户
    @CacheEvict(value = "User_Cache", allEntries = true)
    public UserEntity saveUser(UserEntity userEntity) {
        if (userEntity == null) {
            throw new ResultException(ResultCode.USER_ARGS_NOT_FOUND_ERROR);
        }
        /*if (StringUtils.isEmpty(userEntity.getUsername())) {
            throw new ResultException(ResultCode.USER_USERNAME_ARGS_NOT_FOUND_ERROR);
        }
        if (StringUtils.isEmpty(userEntity.getPassword())) {
            throw new ResultException(ResultCode.USER_PASSWORD_ARGS_NOT_FOUND_ERROR);
        }*/
        if (hasUserByUsername(userEntity.getUsername())) {
            throw new ResultException(ResultCode.USER_USERNAME_EXISTED_ERROR);
        }
        if(userEntity.getRoleEntity() != null){
            userEntity.setRoleEntity(userEntity.getRoleEntity());
        }else{
            userEntity.setRoleEntity(roleService.getRoleByName(ApplicationConfig.DEFAULT_USER_ROLE_NAME));
        }
        userEntity.setPassword(new BCryptPasswordEncoder().encode(userEntity.getPassword()));
        return userRepository.save(userEntity);
    }

    //保存管理员用户
    public UserEntity saveAdminUser(UserEntity userEntity) {
        userEntity.setRoleEntity(roleService.getRoleByName(ApplicationConfig.DEFAULT_ADMIN_ROLE_NAME));
        return saveUser(userEntity);
    }

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
    private boolean hasUserById(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        return userRepository.existsById(userId);
    }

    @Cacheable(value = "User_Cache", key = "#userId")
    public UserEntity getUserById(String userId) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        return userRepository.findById(userId).get();
    }

    @CachePut(value = "User_Cache", key = "#userId")
    public UserEntity updateUserByUserId(String userId, UserEntity userEntityArgs) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = getUserById(userId);
        if(userEntityArgs == null){
            throw new ResultException(ResultCode.USER_ARGS_NOT_FOUND_ERROR);
        }

        if(!StringUtils.isEmpty(userEntityArgs.getUsername()) && !userEntity.getUsername().equals(userEntityArgs.getUsername())){
            if (hasUserByUsername(userEntity.getUsername())) {
                throw new ResultException(ResultCode.USER_USERNAME_EXISTED_ERROR);
            }
            userEntity.setUsername(userEntityArgs.getUsername());
        }
        return userRepository.save(userEntity);
    }

    @CacheEvict(value = "User_Cache", key = "#userId")
    public UserEntity deleteByUserId(String userId) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = getUserById(userId);
        userRepository.delete(userEntity);
        return userEntity;
    }

    @CacheEvict(value = "User_Cache", key = "#userId")
    public UserEntity distributeUserById(String userId, String roleId) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if(!roleService.hasRoleById(roleId)){
            throw new ResultException(ResultCode.ROLE_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = getUserById(userId);
        userEntity.setRoleEntity(roleService.getRoleById(roleId));
        return userRepository.save(userEntity);
    }

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

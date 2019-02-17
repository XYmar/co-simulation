package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.RoleEntity;
import com.rengu.cosimulation.entity.UserEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.UserRepository;
import com.rengu.cosimulation.utils.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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
        if(userEntity.getRoleEntity() != null){
            userEntity.setRoleEntity(userEntity.getRoleEntity());
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
}

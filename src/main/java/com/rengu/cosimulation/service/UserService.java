package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.Department;
import com.rengu.cosimulation.entity.Role;
import com.rengu.cosimulation.entity.Users;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.RoleRepository;
import com.rengu.cosimulation.repository.UserRepository;
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

/**
 * Author: XYmar
 * Date: 2019/2/12 16:15
 */
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final DepartmentService departmentService;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService, DepartmentService departmentService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.departmentService = departmentService;
    }

    // 保存用户 , 一个角色
    @CacheEvict(value = "User_Cache", allEntries = true)
    public Users saveUser(String departmentName, Users users, String roleName) {
        if (users == null) {
            throw new ResultException(ResultCode.USER_ARGS_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(users.getUsername())){
            throw new ResultException(ResultCode.USER_PASSWORD_ARGS_NOT_FOUND_ERROR);
        }
        if (hasUserByUsername(users.getUsername())) {
            throw new ResultException(ResultCode.USER_USERNAME_EXISTED_ERROR);
        }
        /*if(StringUtils.isEmpty(users.getPassword())){
            throw new ResultException(ResultCode.USER_PASSWORD_ARGS_NOT_FOUND_ERROR);
        }*/
        if(StringUtils.isEmpty(String.valueOf(users.getSecretClass()))){
            throw new ResultException(ResultCode.USER_SECRETCLASS_NOT_FOUND_ERROR);
        }
        users.setPassword(new BCryptPasswordEncoder().encode("123456"));

        if(StringUtils.isEmpty(roleName)){
            throw new ResultException(ResultCode.USER_ROLE_NOT_FOUND_ERROR);
        }
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(roleService.getRoleByName(roleName));
        users.setRoleEntities(roleSet);
        if(StringUtils.isEmpty(String.valueOf(users.getSecretClass()))){
            users.setSecretClass(0);
        }

        if(!departmentService.hasDepartmentByName(departmentName)){
            throw new ResultException(ResultCode.DEPARTMENT_NAME_NOT_FOUND_ERROR);
        }
        Department department = departmentService.getDepartmentByName(departmentName);
        users.setDepartment(department);
        return userRepository.save(users);
    }

    //保存管理员用户
    /*public Users saveAdminUser(Users users, String roleName) {
        return saveUser(users);
    }*/

    // 查询所有用户
    public List<Users> getAll() {
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
    public Users getUserByUsername(String username) {
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
    public Users getUserById(String userId) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        return userRepository.findById(userId).get();
    }

    // 根据id修改用户
    @CachePut(value = "User_Cache", key = "#userId")
    public Users updateUserByUserId(String userId, Users usersArgs) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        if(usersArgs == null){
            throw new ResultException(ResultCode.USER_ARGS_NOT_FOUND_ERROR);
        }

        if(!StringUtils.isEmpty(usersArgs.getUsername()) && !users.getUsername().equals(usersArgs.getUsername())){
            if (hasUserByUsername(usersArgs.getUsername())) {
                throw new ResultException(ResultCode.USER_USERNAME_EXISTED_ERROR);
            }
            users.setUsername(usersArgs.getUsername());
        }

        return userRepository.save(users);
    }

    // 根据id删除用户
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users deleteByUserId(String userId) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        users.setRoleEntities(new HashSet<>());
        userRepository.save(users);
        userRepository.delete(users);
        return users;
    }

    // 根据id修改用户密码
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users updatePasswordById(String userId, String password) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(password)){
            throw new ResultException(ResultCode.USER_PASSWORD_ARGS_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        users.setPassword(new BCryptPasswordEncoder().encode(password));
        return userRepository.save(users);
    }

    // 根据id修改用户所属部门
    public Users updateDepartmentById(String userId, String departmentId){
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        if(!departmentService.hasDepartmentById(departmentId)){
            throw new ResultException(ResultCode.DEPARTMENT_ID_NOT_FOUND_ERROR);
        }
        Department department = departmentService.getDepartmentById(departmentId);
        users.setDepartment(department);
        return userRepository.save(users);
    }

    // 安全保密员修改用户密级
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users updateSecretClassById(String userId, int secretClass) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(secretClass)){
            throw new ResultException(ResultCode.USER_SECRETCLASS_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        users.setSecretClass(secretClass);
        return userRepository.save(users);
    }

    // 根据id分配用户权限,  一次只能分配一个角色, 再次传入角色的时候累加
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users distributeUserById(String userId, String[] ids) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);

        List<Role> roleList = new ArrayList<>();
        if(ids.length == 0){
            throw new ResultException(ResultCode.USER_ROLE_NOT_FOUND_ERROR);
        }
        for (String id : ids) {
            roleList.add(roleService.getRoleById(id));
        }

        HashSet<Role> roleHashSet = new HashSet<>(roleList);

        users.setRoleEntities(roleHashSet);
        return userRepository.save(users);
    }

    // 根据id禁用或解除禁用
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users assignUserById(String userId, Boolean enabled) {
        if(!hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(enabled)){
            throw new ResultException(ResultCode.USER_ENABLED_NOT_SUPPORT_ERROR);
        }
        Users users = getUserById(userId);
        users.setEnabled(enabled);
        return userRepository.save(users);
    }
}

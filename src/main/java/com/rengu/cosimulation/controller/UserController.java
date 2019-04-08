package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.entity.RoleEntity;
import com.rengu.cosimulation.entity.UserEntity;
import com.rengu.cosimulation.service.RoleService;
import com.rengu.cosimulation.service.UserService;
import com.rengu.cosimulation.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Author: XYmar
 * Date: 2019/2/14 13:55
 */
@Slf4j
@RestController
@RequestMapping(value = "/users")
public class UserController {
    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    // 新增用户（只有系统管理员可执行此操作）
    @PostMapping
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResultEntity saveUser(UserEntity userEntity, @RequestHeader(name = "roleName") String roleName){
        return ResultUtils.success(userService.saveUser(userEntity, roleName));
    }

    // 查询所有用户
    @GetMapping
    public ResultEntity getUsers(){
        return ResultUtils.success(userService.getAll());
    }

    // 根据ID查询用户
    @GetMapping(value = "/{userId}")
    public ResultEntity getUserById(@PathVariable(value = "userId") String userId){
        return ResultUtils.success(userService.getUserById(userId));
    }

    // 根据ID修改用户信息
    @PatchMapping(value = "/{userId}")
    public ResultEntity updateUserById(@PathVariable(value = "userId") String userId, UserEntity userEntity){
        return ResultUtils.success(userService.updateUserByUserId(userId, userEntity));
    }

    // 根据ID删除用户
    @DeleteMapping(value = "/{userId}")
    public ResultEntity deleteUserById(@PathVariable(value = "userId") String userId){
        return ResultUtils.success(userService.deleteByUserId(userId));
    }

    // 根据ID修改用户密码
    @PatchMapping(value = "/{userId}/password")
    public ResultEntity updatePasswordById(@PathVariable(value = "userId") String userId, String password){
        return ResultUtils.success(userService.updatePasswordById(userId, password));
    }

    // 安全保密员根据用户id修改用户密级
    @PatchMapping(value = "/{userId}/secretClass")
    @PreAuthorize(value = "hasRole('SECURITY_GUARD')")
    public ResultEntity updateSecretClassById(@PathVariable(value = "userId") String userId, int secretClass){
        return ResultUtils.success(userService.updateSecretClassById(userId, secretClass));
    }

    // 根据ID为用户分配角色
    @PatchMapping(value = "/{userId}/distribute")
    public ResultEntity distributeUserById(@PathVariable(value = "userId") String userId,  @RequestParam(value = "ids") String[] ids){
        return ResultUtils.success(userService.distributeUserById(userId, ids));
    }

    // 根据ID禁用或解除
    @PatchMapping(value = "/{userId}/authority")
    public ResultEntity assignUserById(@PathVariable(value = "userId") String userId, Boolean enabled){
        return ResultUtils.success(userService.assignUserById(userId, enabled));
    }
}

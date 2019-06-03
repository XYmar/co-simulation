package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.Result;
import com.rengu.cosimulation.entity.Role;
import com.rengu.cosimulation.service.RoleService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Author: XYmar
 * Date: 2019/2/13 15:25
 */
@RestController
@RequestMapping(value = "/role")
public class RoleController {
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // 添加角色信息
    @PostMapping
    public Result saveRole(@RequestBody @Valid Role role) {
        return ResultUtils.success(roleService.saveRole(role));
    }

    // 查询所有角色
    @GetMapping
    public Result getRoles() {
        return ResultUtils.success(roleService.getAll());
    }

    // 根据ID查询角色
    @GetMapping(value = "/{roleId}")
    public Result getRoleById(@PathVariable(value = "roleId") String roleId){
        return ResultUtils.success(roleService.getRoleById(roleId));
    }

    // 根据ID修改角色信息
    @PatchMapping(value = "/{roleId}")
    public Result updateRoleById(@PathVariable(value = "roleId") String roleId, Role role){
        return ResultUtils.success(roleService.updateRoleByRoleId(roleId, role));
    }

    // 根据ID删除角色
    @DeleteMapping(value = "/{roleId}")
    public Result deleteRoleById(@PathVariable(value = "roleId") String roleId){
        return ResultUtils.success(roleService.deleteByRoleId(roleId));
    }
}

package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.entity.RoleEntity;
import com.rengu.cosimulation.service.RoleService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
    public RoleEntity saveRole(@RequestBody @Valid RoleEntity roleEntity) {
        return roleService.saveRole(roleEntity);
    }

    // 查询所有角色
    @GetMapping
    public List<RoleEntity> getRoles() {
        return roleService.getAll();
    }

    // 根据ID查询角色
    @GetMapping(value = "/{roleId}")
    public ResultEntity getRoleById(@PathVariable(value = "roleId") String roleId){
        return ResultUtils.success(roleService.getRoleById(roleId));
    }

    // 根据ID修改角色信息
    @PatchMapping(value = "/{roleId}")
    public ResultEntity updateRoleById(@PathVariable(value = "roleId") String roleId, RoleEntity roleEntity){
        return ResultUtils.success(roleService.updateRoleByRoleId(roleId, roleEntity));
    }

    // 根据ID删除角色
    @DeleteMapping(value = "/{roleId}")
    public ResultEntity deleteRoleById(@PathVariable(value = "roleId") String roleId){
        return ResultUtils.success(roleService.deleteByRoleId(roleId));
    }
}

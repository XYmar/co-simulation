package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.RoleEntity;
import com.rengu.cosimulation.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public RoleEntity saveRole(@RequestBody RoleEntity roleEntity) {
        return roleService.saveRole(roleEntity);
    }

    // 查询所有角色
    @GetMapping
    public List<RoleEntity> getRoles() {
        return roleService.getAll();
    }
}

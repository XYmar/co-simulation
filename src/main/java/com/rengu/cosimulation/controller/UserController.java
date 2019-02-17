package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.entity.UserEntity;
import com.rengu.cosimulation.service.UserService;
import com.rengu.cosimulation.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/14 13:55
 */
@Slf4j
@RestController
@RequestMapping(value = "/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 新增用户（只有系统管理员可执行此操作）
    @PostMapping
    public ResultEntity saveUser(@RequestBody @Valid UserEntity userEntity){
        return ResultUtils.success(userService.saveUser(userEntity));
    }

    // 查询所有用户
    @GetMapping
    public ResultEntity getUsers(){
        return ResultUtils.success(userService.getAll());
    }
}

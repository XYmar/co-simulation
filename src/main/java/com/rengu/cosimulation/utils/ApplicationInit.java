package com.rengu.cosimulation.utils;

import com.rengu.cosimulation.entity.DepartmentEntity;
import com.rengu.cosimulation.entity.DesignLinkEntity;
import com.rengu.cosimulation.entity.RoleEntity;
import com.rengu.cosimulation.entity.UserEntity;
import com.rengu.cosimulation.service.DepartmentService;
import com.rengu.cosimulation.service.DesignLinkService;
import com.rengu.cosimulation.service.RoleService;
import com.rengu.cosimulation.service.UserService;
import com.rengu.cosimulation.thread.UDPReceiveThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/13 9:12
 */
@Slf4j
@Order(value = -1)
@Component
public class ApplicationInit  implements ApplicationRunner {

    private final UserService userService;
    private final DesignLinkService designLinkService;
    private final UDPReceiveThread udpReceiveThread;
    private final DepartmentService departmentService;

    @Autowired
    public ApplicationInit(UserService userService, DesignLinkService designLinkService, UDPReceiveThread udpReceiveThread, DepartmentService departmentService) {
        this.userService = userService;
        this.designLinkService = designLinkService;
        this.udpReceiveThread = udpReceiveThread;
        this.departmentService = departmentService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动UDP消息接受线程
        // udpReceiveThread.UDPMessageReceiver();

        // 初始化库（4个）

        // 初始化子库

        /**
         * 初始化部门 信息化部
         * */
        if(!departmentService.hasDepartmentByName(ApplicationConfig.INFORMATION_MINISTRY)){
            DepartmentEntity departmentEntity = new DepartmentEntity();
            departmentEntity.setName(ApplicationConfig.INFORMATION_MINISTRY);
            departmentEntity.setDescription(ApplicationConfig.INFORMATION_MINISTRY_DESCRIPTION);
            departmentService.saveDepartment(departmentEntity);
        }

        /**
         * 初始化三元  信息化部门
         * */
        // 初始化管理员用户
        if (!userService.hasUserByUsername(ApplicationConfig.DEFAULT_ADMIN_USERNAME)) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(ApplicationConfig.DEFAULT_ADMIN_USERNAME);
            userEntity.setPassword(ApplicationConfig.DEFAULT_ADMIN_PASSWORD);
            userEntity.setSecretClass(3);
            userService.saveUser(ApplicationConfig.INFORMATION_MINISTRY, userEntity, ApplicationConfig.DEFAULT_ADMIN_ROLE_NAME);
        }

        // 初始化安全保密员用户
        if (!userService.hasUserByUsername(ApplicationConfig.DEFAULT_SECURITY_GUARD_USERNAME)) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(ApplicationConfig.DEFAULT_SECURITY_GUARD_USERNAME);
            userEntity.setPassword(ApplicationConfig.DEFAULT_SECURITY_GUARD_PASSWORD);
            userEntity.setSecretClass(3);
            userService.saveUser(ApplicationConfig.INFORMATION_MINISTRY, userEntity, ApplicationConfig.DEFAULT_SECURITY_GUARD_ROLE_NAME);
        }

        // 初始化安全审计员用户
        if (!userService.hasUserByUsername(ApplicationConfig.DEFAULT_SECURITY_AUDITOR_USERNAME)) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(ApplicationConfig.DEFAULT_SECURITY_AUDITOR_USERNAME);
            userEntity.setPassword(ApplicationConfig.DEFAULT_SECURITY_AUDITOR_PASSWORD);
            userEntity.setSecretClass(3);
            userService.saveUser(ApplicationConfig.INFORMATION_MINISTRY, userEntity, ApplicationConfig.DEFAULT_SECURITY_AUDITOR_ROLE_NAME);
        }
    }

}

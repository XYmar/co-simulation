package com.rengu.cosimulation.utils;

import com.rengu.cosimulation.entity.Department;
import com.rengu.cosimulation.entity.Users;
import com.rengu.cosimulation.service.DepartmentService;
import com.rengu.cosimulation.service.DesignLinkService;
import com.rengu.cosimulation.service.UserService;
import com.rengu.cosimulation.thread.TCPReceiveThread;
import com.rengu.cosimulation.thread.UDPReceiveThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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
    private final TCPReceiveThread tcpReceiveThread;

    @Autowired
    public ApplicationInit(UserService userService, DesignLinkService designLinkService, UDPReceiveThread udpReceiveThread, DepartmentService departmentService, TCPReceiveThread tcpReceiveThread) {
        this.userService = userService;
        this.designLinkService = designLinkService;
        this.udpReceiveThread = udpReceiveThread;
        this.departmentService = departmentService;
        this.tcpReceiveThread = tcpReceiveThread;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动UDP消息接受线程
         udpReceiveThread.UDPMessageReceiver();
        tcpReceiveThread.TCPMessageReceiver();

        // 初始化库（4个）

        // 初始化子库

        /**
         * 初始化部门 信息化部
         * */
        if(!departmentService.hasDepartmentByName(ApplicationConfig.INFORMATION_MINISTRY)){
            Department department = new Department();
            department.setName(ApplicationConfig.INFORMATION_MINISTRY);
            department.setDescription(ApplicationConfig.INFORMATION_MINISTRY_DESCRIPTION);
            departmentService.saveDepartment(department);
        }

        /**
         * 初始化三元  信息化部门
         * */
        // 初始化管理员用户
        if (!userService.hasUserByUsername(ApplicationConfig.DEFAULT_ADMIN_USERNAME)) {
            Users users = new Users();
            users.setUsername(ApplicationConfig.DEFAULT_ADMIN_USERNAME);
            users.setRealName(ApplicationConfig.DEFAULT_ADMIN_REALNAME);
            users.setPassword(ApplicationConfig.DEFAULT_ADMIN_PASSWORD);
            users.setSecretClass(3);
            userService.saveUser(ApplicationConfig.INFORMATION_MINISTRY, users, ApplicationConfig.DEFAULT_ADMIN_ROLE_NAME);
        }

        // 初始化安全保密员用户
        if (!userService.hasUserByUsername(ApplicationConfig.DEFAULT_SECURITY_GUARD_USERNAME)) {
            Users users = new Users();
            users.setUsername(ApplicationConfig.DEFAULT_SECURITY_GUARD_USERNAME);
            users.setRealName(ApplicationConfig.DEFAULT_SECURITY_GUARD_REALNAME);
            users.setPassword(ApplicationConfig.DEFAULT_SECURITY_GUARD_PASSWORD);
            users.setSecretClass(3);
            userService.saveUser(ApplicationConfig.INFORMATION_MINISTRY, users, ApplicationConfig.DEFAULT_SECURITY_GUARD_ROLE_NAME);
        }

        // 初始化安全审计员用户
        if (!userService.hasUserByUsername(ApplicationConfig.DEFAULT_SECURITY_AUDITOR_USERNAME)) {
            Users users = new Users();
            users.setUsername(ApplicationConfig.DEFAULT_SECURITY_AUDITOR_USERNAME);
            users.setRealName(ApplicationConfig.DEFAULT_SECURITY_AUDITOR_REALNAME);
            users.setPassword(ApplicationConfig.DEFAULT_SECURITY_AUDITOR_PASSWORD);
            users.setSecretClass(3);
            userService.saveUser(ApplicationConfig.INFORMATION_MINISTRY, users, ApplicationConfig.DEFAULT_SECURITY_AUDITOR_ROLE_NAME);
        }
    }

}

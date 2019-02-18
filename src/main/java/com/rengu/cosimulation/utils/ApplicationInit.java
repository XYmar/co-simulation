package com.rengu.cosimulation.utils;

import com.rengu.cosimulation.entity.RoleEntity;
import com.rengu.cosimulation.entity.UserEntity;
import com.rengu.cosimulation.service.RoleService;
import com.rengu.cosimulation.service.UserService;
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

    private final RoleService roleService;
    private final UserService userService;

    @Autowired
    public ApplicationInit(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 创建角色数组
        RoleEntity[] roles = new RoleEntity[7];

        // 实例化每一个角色
        for(int i=0;i<roles.length;i++){
            roles[i] = new RoleEntity();
        }

        // 创建6个默认角色，并赋值
        roles[0].setName(ApplicationConfig.DEFAULT_ADMIN_ROLE_NAME);
        roles[0].setDescription("系统管理员");
        roles[0].setChangeable(false);
        roles[1].setName(ApplicationConfig.DEFAULT_SECURITY_GUARD_ROLE_NAME);
        roles[1].setDescription("安全保密员");
        roles[1].setChangeable(false);
        roles[2].setName(ApplicationConfig.DEFAULT_SECURITY_AUDITOR_ROLE_NAME);
        roles[2].setDescription("安全审计员");
        roles[2].setChangeable(false);
        roles[3].setName(ApplicationConfig.DEFAULT_PROJECT_MANAGER_ROLE_NAME);
        roles[3].setDescription("项目管理员");
        roles[4].setName(ApplicationConfig.DEFAULT_FILE_AUDITOR__ROLE_NAME);
        roles[4].setDescription("仿真技术文件审核员");
        roles[5].setName(ApplicationConfig.DEFAULT_NORMAL_DESIGNER_ROLE_NAME);
        roles[5].setDescription("一般仿真设计员");
        roles[6].setName(ApplicationConfig.DEFAULT_USER_ROLE_NAME);
        roles[6].setDescription("普通用户");

        // 初始化6个角色
        /*if (!roleService.hasRoleByName(ApplicationConfig.DEFAULT_ADMIN_ROLE_NAME)) {
            RoleEntity roleEntity = new RoleEntity();
            roleEntity.setName(ApplicationConfig.DEFAULT_ADMIN_ROLE_NAME);
            roleEntity.setDescription("系统管理员");
            roleService.saveRole(roleEntity);*/

            for (RoleEntity role : roles) {
                if (!roleService.hasRoleByName(role.getName())) {
                    roleService.saveRole(role);
                }
            }
        /*}*/

        // 初始化管理员用户
        if (!userService.hasUserByUsername(ApplicationConfig.DEFAULT_ADMIN_USERNAME)) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(ApplicationConfig.DEFAULT_ADMIN_USERNAME);
            userEntity.setPassword(ApplicationConfig.DEFAULT_ADMIN_PASSWORD);
            userService.saveAdminUser(userEntity);
        }

    }
}

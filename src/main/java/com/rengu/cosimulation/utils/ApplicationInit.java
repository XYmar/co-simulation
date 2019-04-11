package com.rengu.cosimulation.utils;

import com.rengu.cosimulation.entity.DesignLinkEntity;
import com.rengu.cosimulation.entity.RoleEntity;
import com.rengu.cosimulation.entity.UserEntity;
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

    private final RoleService roleService;
    private final UserService userService;
    private final DesignLinkService designLinkService;
    private final UDPReceiveThread udpReceiveThread;

    @Autowired
    public ApplicationInit(RoleService roleService, UserService userService, DesignLinkService designLinkService, UDPReceiveThread udpReceiveThread) {
        this.roleService = roleService;
        this.userService = userService;
        this.designLinkService = designLinkService;
        this.udpReceiveThread = udpReceiveThread;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动UDP消息接受线程
        // udpReceiveThread.UDPMessageReceiver();

        // 初始化库（4个）

        // 初始化子库

        // 创建角色数组
        /*RoleEntity[] roles = new RoleEntity[7];

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
        for (RoleEntity role : roles) {
            if (!roleService.hasRoleByName(role.getName())) {
                roleService.saveRole(role);
            }
        }
*/
        /**
         * 初始化三元
         * */
        // 初始化管理员用户
        if (!userService.hasUserByUsername(ApplicationConfig.DEFAULT_ADMIN_USERNAME)) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(ApplicationConfig.DEFAULT_ADMIN_USERNAME);
            userEntity.setPassword(ApplicationConfig.DEFAULT_ADMIN_PASSWORD);
            userEntity.setSecretClass(3);
            userService.saveUser(userEntity, ApplicationConfig.DEFAULT_ADMIN_ROLE_NAME);
        }

        // 初始化安全保密员用户
        if (!userService.hasUserByUsername(ApplicationConfig.DEFAULT_SECURITY_GUARD_USERNAME)) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(ApplicationConfig.DEFAULT_SECURITY_GUARD_USERNAME);
            userEntity.setPassword(ApplicationConfig.DEFAULT_SECURITY_GUARD_PASSWORD);
            userEntity.setSecretClass(3);
            userService.saveUser(userEntity, ApplicationConfig.DEFAULT_SECURITY_GUARD_ROLE_NAME);
        }

        // 初始化安全审计员用户
        if (!userService.hasUserByUsername(ApplicationConfig.DEFAULT_SECURITY_AUDITOR_USERNAME)) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(ApplicationConfig.DEFAULT_SECURITY_AUDITOR_USERNAME);
            userEntity.setPassword(ApplicationConfig.DEFAULT_SECURITY_GUARD_PASSWORD);
            userEntity.setSecretClass(3);
            userService.saveUser(userEntity, ApplicationConfig.DEFAULT_SECURITY_AUDITOR_ROLE_NAME);
        }

        // 初始化设计环节
        // 创建设计环节数组
        DesignLinkEntity[] designLinkEntities = new DesignLinkEntity[7];

        // 实例化每一个设计环节
        for(int i=0;i<designLinkEntities.length;i++){
            designLinkEntities[i] = new DesignLinkEntity();
        }

        // 创建7个默认设计环节，并赋值
        designLinkEntities[0].setName(ApplicationConfig.DEFAULT_STRUCTURAL_MODELING_NAME);
        designLinkEntities[0].setDescription("结构建模");
        designLinkEntities[0].setType(0);
        designLinkEntities[1].setName(ApplicationConfig.DEFAULT_ELECTRICAL_MODELING_NAME);
        designLinkEntities[1].setDescription("电气建模");
        designLinkEntities[1].setType(0);
        designLinkEntities[2].setName(ApplicationConfig.DEFAULT_STRUCTURAL_SIMULATION_NAME);
        designLinkEntities[2].setDescription("结构仿真");
        designLinkEntities[2].setType(1);
        designLinkEntities[3].setName(ApplicationConfig.DEFAULT_ELECTRICAL_SIMULATION_NAME);
        designLinkEntities[3].setDescription("电气仿真");
        designLinkEntities[3].setType(1);
        designLinkEntities[4].setName(ApplicationConfig.DEFAULT_THERMAL_SIMULATION_NAME);
        designLinkEntities[4].setDescription("热学仿真");
        designLinkEntities[4].setType(1);
        designLinkEntities[5].setName(ApplicationConfig.DEFAULT_MECHANICAL_SIMULATION_NAME);
        designLinkEntities[5].setDescription("力学仿真");
        designLinkEntities[5].setType(1);
        designLinkEntities[6].setName(ApplicationConfig.DEFAULT_ASSEMBLY_SIMULATION_NAME);
        designLinkEntities[6].setDescription("装配仿真");
        designLinkEntities[6].setType(1);

        // 初始化7个设计环节
        for (DesignLinkEntity designLinkEntitie : designLinkEntities) {
            if (!designLinkService.hasDesignLinkByName(designLinkEntitie.getName())) {
                designLinkService.saveDesignLink(designLinkEntitie);
            }
        }

    }

}

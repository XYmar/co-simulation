package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.DesignLinkEntity;
import com.rengu.cosimulation.entity.ProDesignLinkEntity;
import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.UserEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.ProDesignLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: XYmar
 * Date: 2019/2/26 10:04
 */
@Service
public class ProDesignLinkService {
    private final ProDesignLinkRepository proDesignLinkRepository;
    private final ProjectService projectService;
    private final UserService userService;
    @Autowired
    private DesignLinkService designLinkService;

    @Autowired
    public ProDesignLinkService(ProDesignLinkRepository proDesignLinkRepository, ProjectService projectService, UserService userService) {
        this.proDesignLinkRepository = proDesignLinkRepository;
        this.projectService = projectService;
        this.userService = userService;
    }

    // 根据项目id查询所有子任务
    public List<ProDesignLinkEntity> findByProjectId(String projectId) {
        return proDesignLinkRepository.findByProjectEntity(projectService.getProjectById(projectId));
    }

    // 保存项目子任务
    // 项目设置子任务(执行者，子任务，节点)
    public ProDesignLinkEntity setProDesignLink(String projectId, String designLinkEntityId, String userId, String finishTime){
        ProDesignLinkEntity proDesignLinkEntity = new ProDesignLinkEntity();

        // 选择设计环节
        if(!designLinkService.hasDesignLinkById(designLinkEntityId)){
            throw new ResultException(ResultCode.DESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        DesignLinkEntity designLinkEntity = designLinkService.getDesignLinkById(designLinkEntityId);

        // 设置子任务相关内容
        proDesignLinkEntity.setName(designLinkEntity.getName());                   // 名称
        proDesignLinkEntity.setDescription(designLinkEntity.getDescription());     // 描述
        if(!userService.hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = userService.getUserById(userId);
        proDesignLinkEntity.setUserEntity(userEntity);                            // 负责人

        if(!projectService.hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = projectService.getProjectById(projectId);

        proDesignLinkEntity.setProjectEntity(projectEntity);                      // 所属项目

        return proDesignLinkRepository.save(proDesignLinkEntity);
    }

}

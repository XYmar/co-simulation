package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SubtaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/26 10:04
 */
@Service
public class SubtaskService {
    private final SubtaskRepository subtaskRepository;
    private final ProjectService projectService;
    private final UserService userService;
    @Autowired
    private DesignLinkService designLinkService;

    @Autowired
    public SubtaskService(SubtaskRepository subtaskRepository, ProjectService projectService, UserService userService) {
        this.subtaskRepository = subtaskRepository;
        this.projectService = projectService;
        this.userService = userService;
    }

    // 根据项目id查询所有子任务
    public List<SubtaskEntity> findByProjectId(String projectId) {
        return subtaskRepository.findByProjectEntity(projectService.getProjectById(projectId));
    }

    // 保存项目子任务
    // 项目设置子任务(执行者，子任务，节点)
    public SubtaskEntity setProDesignLink(String projectId, String designLinkEntityId, String userId, String finishTime){
        SubtaskEntity subtaskEntity = new SubtaskEntity();

        // 选择设计环节
        if(!designLinkService.hasDesignLinkById(designLinkEntityId)){
            throw new ResultException(ResultCode.DESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        DesignLinkEntity designLinkEntity = designLinkService.getDesignLinkById(designLinkEntityId);

        // 设置子任务相关内容
        subtaskEntity.setName(designLinkEntity.getName());                   // 名称
        subtaskEntity.setDescription(designLinkEntity.getDescription());     // 描述
        subtaskEntity.setFinishTime(finishTime);
        if(!userService.hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = userService.getUserById(userId);
        subtaskEntity.setUserEntity(userEntity);                            // 负责人

        if(!projectService.hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = projectService.getProjectById(projectId);

        subtaskEntity.setProjectEntity(projectEntity);                      // 所属项目

        return subtaskRepository.save(subtaskEntity);
    }

    // 根据id查询子任务是否存在
    public boolean hasProDesignLinkById(String proDesignLinkById) {
        if (StringUtils.isEmpty(proDesignLinkById)) {
            return false;
        }
        return subtaskRepository.existsById(proDesignLinkById);
    }

    // 根据子任务名称查询子任务是否存在
    public boolean hasProDesignLinkByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return subtaskRepository.existsByName(name);
    }

    // 根据id查询子任务
    public SubtaskEntity getProDesignLinkById(String proDesignLinkById) {
        if(!hasProDesignLinkById(proDesignLinkById)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        return subtaskRepository.findById(proDesignLinkById).get();
    }

    // 根据id修改子任务
    public SubtaskEntity updateProDesignLinkById(String proDesignLinkById, String designLinkEntityId, String userId, String finishTime){
        if(!hasProDesignLinkById(proDesignLinkById)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getProDesignLinkById(proDesignLinkById);
        if(designLinkService.hasDesignLinkById(designLinkEntityId)){
            DesignLinkEntity designLinkEntity = designLinkService.getDesignLinkById(designLinkEntityId);
            if(hasProDesignLinkByName(designLinkEntity.getName())){
                throw new ResultException(ResultCode.PRODESIGN_LINK_NAME_EXISTED_ERROR);
            }
            subtaskEntity.setDesignLinkEntity(designLinkEntity);
        }
        if(userService.hasUserById(userId)){
            UserEntity userEntity = userService.getUserById(userId);
            subtaskEntity.setUserEntity(userEntity);
        }
        if(!StringUtils.isEmpty(finishTime)){
            subtaskEntity.setFinishTime(finishTime);
        }

        return subtaskRepository.save(subtaskEntity);
    }

    // 删除子任务
    public SubtaskEntity deleteProDesignLinkById(String proDesignLinkId){
        if(!hasProDesignLinkById(proDesignLinkId)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getProDesignLinkById(proDesignLinkId);
        subtaskRepository.delete(subtaskEntity);
        return subtaskEntity;
    }

    // 根据子任务id为子任务添加审核员
    public SubtaskEntity arrangeAssessorsById(String proDesignLinkId, String userId, String[] userIds) {
        if(!userService.hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = userService.getUserById(userId);
        if(!hasProDesignLinkById(proDesignLinkId)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getProDesignLinkById(proDesignLinkId);
        if(!userEntity.getId().equals(subtaskEntity.getUserEntity().getId())){
            throw new ResultException(ResultCode.PRODESIGN_LINK_USER_ARRANGE_AUTHORITY_DENIED_ERROR);
        }
        if(userIds.length == 0){
            throw new ResultException(ResultCode.PRODESIGN_LINK_ASSESSORS_NOT_FOUND_ERROR);
        }

        List<UserEntity> userEntityList = new ArrayList<>();
        for (String id : userIds) {
            userEntityList.add(userService.getUserById(id));
        }
        HashSet<UserEntity> userEntityHashSet = new HashSet<>(userEntityList);

        subtaskEntity.setAssessorSet(userEntityHashSet);
        return subtaskRepository.save(subtaskEntity);
    }
}

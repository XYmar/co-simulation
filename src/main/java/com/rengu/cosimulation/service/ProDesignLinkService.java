package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.ProDesignLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
        proDesignLinkEntity.setFinishTime(finishTime);
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

    // 根据id查询子任务是否存在
    public boolean hasProDesignLinkById(String proDesignLinkById) {
        if (StringUtils.isEmpty(proDesignLinkById)) {
            return false;
        }
        return proDesignLinkRepository.existsById(proDesignLinkById);
    }

    // 根据子任务名称查询子任务是否存在
    public boolean hasProDesignLinkByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return proDesignLinkRepository.existsByName(name);
    }

    // 根据id查询子任务
    public ProDesignLinkEntity getProDesignLinkById(String proDesignLinkById) {
        if(!hasProDesignLinkById(proDesignLinkById)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        return proDesignLinkRepository.findById(proDesignLinkById).get();
    }

    // 根据id修改子任务
    public ProDesignLinkEntity updateProDesignLinkById(String proDesignLinkById, String designLinkEntityId, String userId, String finishTime){
        if(!hasProDesignLinkById(proDesignLinkById)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        ProDesignLinkEntity proDesignLinkEntity = getProDesignLinkById(proDesignLinkById);
        if(designLinkService.hasDesignLinkById(designLinkEntityId)){
            DesignLinkEntity designLinkEntity = designLinkService.getDesignLinkById(designLinkEntityId);
            if(hasProDesignLinkByName(designLinkEntity.getName())){
                throw new ResultException(ResultCode.PRODESIGN_LINK_NAME_EXISTED_ERROR);
            }
            proDesignLinkEntity.setDesignLinkEntity(designLinkEntity);
        }
        if(userService.hasUserById(userId)){
            UserEntity userEntity = userService.getUserById(userId);
            proDesignLinkEntity.setUserEntity(userEntity);
        }
        if(!StringUtils.isEmpty(finishTime)){
            proDesignLinkEntity.setFinishTime(finishTime);
        }

        return proDesignLinkRepository.save(proDesignLinkEntity);
    }

    // 删除子任务
    public ProDesignLinkEntity deleteProDesignLinkById(String proDesignLinkId){
        if(!hasProDesignLinkById(proDesignLinkId)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        ProDesignLinkEntity proDesignLinkEntity = getProDesignLinkById(proDesignLinkId);
        proDesignLinkRepository.delete(proDesignLinkEntity);
        return proDesignLinkEntity;
    }

    // 根据子任务id为子任务添加审核员
    public ProDesignLinkEntity arrangeAssessorsById(String proDesignLinkId, String userId, String[] userIds) {
        if(!userService.hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = userService.getUserById(userId);
        if(!hasProDesignLinkById(proDesignLinkId)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        ProDesignLinkEntity proDesignLinkEntity = getProDesignLinkById(proDesignLinkId);
        if(!userEntity.getId().equals(proDesignLinkEntity.getUserEntity().getId())){
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

        proDesignLinkEntity.setAssessorSet(userEntityHashSet);
        return proDesignLinkRepository.save(proDesignLinkEntity);
    }
}

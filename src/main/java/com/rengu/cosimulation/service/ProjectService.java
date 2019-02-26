package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.DesignLinkEntity;
import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.UserEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.DesignLinkRepository;
import com.rengu.cosimulation.repository.ProjectRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.xml.transform.Result;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: XYmar
 * Date: 2019/2/20 9:35
 */
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final DesignLinkService designLinkService;
    private final DesignLinkRepository designLinkRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, UserService userService, DesignLinkService designLinkService, DesignLinkRepository designLinkRepository) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.designLinkService = designLinkService;
        this.designLinkRepository = designLinkRepository;
    }

    // 新建项目(名称、负责人)
    @CacheEvict(value = "Project_Cache", allEntries = true)
    public ProjectEntity saveProject(ProjectEntity projectEntity, String picId){
        if(projectEntity == null){
            throw new ResultException(ResultCode.PROJECT_ARGS_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(projectEntity.getName())){
            throw new ResultException(ResultCode.PROJECT_NAME_ARGS_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(picId)){
            throw new ResultException(ResultCode.PROJECT_PIC_ARGS_NOT_FOUND_ERROR);
        }
        projectEntity.setPic(userService.getUserById(picId));
        projectEntity.setState(0);
        return projectRepository.save(projectEntity);
    }

    public List<ProjectEntity> getAll() {
        return projectRepository.findAll();
    }

    // 查询所有项目,根据用户Id(负责人)
    public List<ProjectEntity> getProjectsByUser(UserEntity userEntity, boolean deleted) {
        return projectRepository.findByPicAndDeleted(userEntity, deleted);
    }

    // 根据项目id查询项目
    @Cacheable(value = "Project_Cache", key = "#projectId")
    public ProjectEntity getProjectById(String projectId) {
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        return projectRepository.findById(projectId).get();
    }

    // 根据id查询项目是否存在
    public boolean hasProjectById(String projectId) {
        if (StringUtils.isEmpty(projectId)) {
            return false;
        }
        return projectRepository.existsById(projectId);
    }

    // 根据id删除项目
    @CacheEvict(value = " Project_Cache", allEntries = true)
    public ProjectEntity deleteProjectById(String projectId) {
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = getProjectById(projectId);
        projectEntity.setDeleted(true);
        return projectRepository.save(projectEntity);
    }

    // 根据id撤销删除
    @CacheEvict(value = " Project_Cache", allEntries = true)
    public ProjectEntity restoreProjectById(String projectId) {
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = getProjectById(projectId);
        projectEntity.setDeleted(false);
        return projectRepository.save(projectEntity);
    }

    // 负责人指定项目令号、设节点
    public ProjectEntity arrangeProject(String projectId, ProjectEntity projectEntityArgs) {
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = getProjectById(projectId);
        if(projectEntityArgs == null){
            throw new ResultException(ResultCode.PROJECT_ARGS_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(projectEntityArgs.getOrderNum())){
            throw new ResultException(ResultCode.PROJECT_ORDER_NUM_NOT_FOUND_ERROR);
        }
        if(projectEntityArgs.getFinishTime() == null){
            throw new ResultException(ResultCode.PROJECT_FINISH_TIME_NOT_FOUND_ERROR);
        }
        projectEntity.setOrderNum(projectEntityArgs.getOrderNum());
        projectEntity.setFinishTime(projectEntityArgs.getFinishTime());
        return projectRepository.save(projectEntity);
    }

    // 项目管理员修改项目负责人
    public ProjectEntity updateProjectPic(String projectId, String picId){
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = getProjectById(projectId);
        if(StringUtils.isEmpty(picId)){
            throw new ResultException(ResultCode.PROJECT_PIC_ARGS_NOT_FOUND_ERROR);
        }
        projectEntity.setPic(userService.getUserById(picId));
        return projectRepository.save(projectEntity);
    }

    // 修改项目相关信息
    public ProjectEntity updateProjectById(String projectId, ProjectEntity projectEntityArgs) {
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        if(projectEntityArgs == null){
            throw new ResultException(ResultCode.PROJECT_ARGS_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(projectEntityArgs.getName())){
            throw new ResultException(ResultCode.PROJECT_NAME_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(projectEntityArgs.getOrderNum())){
            throw new ResultException(ResultCode.PROJECT_ORDER_NUM_NOT_FOUND_ERROR);
        }
        if(projectEntityArgs.getFinishTime() == null){
            throw new ResultException(ResultCode.PROJECT_FINISH_TIME_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = getProjectById(projectId);
        projectEntity.setName(projectEntityArgs.getName());
        projectEntity.setOrderNum(projectEntityArgs.getOrderNum());
        projectEntity.setFinishTime(projectEntityArgs.getFinishTime());
        projectEntity.setState(projectEntityArgs.getState());
        return projectRepository.save(projectEntity);
    }

    /*// 项目设置子任务(执行者，子任务，节点)
    public ProjectEntity setDesignLink(String projectId, String designLinkEntityId, String userId, String finishTime){
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = getProjectById(projectId);
        if(!userService.hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = userService.getUserById(userId);
        if(!designLinkService.hasDesignLinkById(designLinkEntityId)){
            throw new ResultException(ResultCode.DESIGN_LINK_ID_NOT_FOUND_ERROR);
        }

        // 子任务设置负责人
        DesignLinkEntity designLinkEntity = designLinkService.getDesignLinkById(designLinkEntityId);
        Set<UserEntity> userEntitySet = new HashSet<>();
        userEntitySet.add(userEntity);
        designLinkEntity.setUserEntities(userEntitySet);

        //项目添加子任务
        Set<DesignLinkEntity> designLinkEntitySet = projectEntity.getDesignLinkEntities() == null ? new HashSet<>() : projectEntity.getDesignLinkEntities();
        if(StringUtils.isEmpty(finishTime)){
            throw new ResultException(ResultCode.DESIGN_LINK_FINISH_TIME_NOT_FOUND_ERROR);
        }
        designLinkEntity.setFinishTime(finishTime);
        designLinkEntitySet.add(designLinkRepository.save(designLinkEntity));


        projectEntity.setDesignLinkEntities(designLinkEntitySet);
        return projectRepository.save(projectEntity);

        // TODO 把子任务保存好保存到项目中
    }*/

}

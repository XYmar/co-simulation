package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.*;
import com.rengu.cosimulation.utils.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Author: XYmar
 * Date: 2019/2/20 9:35
 */
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final ProcessNodeRepository1 processNodeRepository1;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, UserService userService, ProcessNodeRepository1 processNodeRepository1) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.processNodeRepository1 = processNodeRepository1;
    }

    // 新建项目(创建者、名称、负责人)  项目负责人密级限制
    @CacheEvict(value = "Project_Cache", allEntries = true)
    public Project saveProject(Project project, String creatorId, String picId){
        if(project == null){
            throw new ResultException(ResultCode.PROJECT_ARGS_NOT_FOUND_ERROR);
        }
        if(!userService.hasUserById(creatorId)){
            throw new ResultException(ResultCode.PROJECT_CREATOR_ARGS_NOT_FOUND_ERROR);
        }
        project.setCreator(userService.getUserById(creatorId));
        Users users = userService.getUserById(picId);
        if(users.getSecretClass() < project.getSecretClass()){
            throw new ResultException(ResultCode.USER_SECRETCLASS_NOT_SUPPORT_ERROR);
        }
        if(StringUtils.isEmpty(project.getName())){
            throw new ResultException(ResultCode.PROJECT_NAME_ARGS_NOT_FOUND_ERROR);
        }
        if(hasProjectByNameAndDeleted(project.getName(), false)){
            throw new ResultException(ResultCode.PROJECT_NAME_EXISTED_ERROR);
        }
        if(!userService.hasUserById(picId)){
            throw new ResultException(ResultCode.PROJECT_PIC_ARGS_NOT_FOUND_ERROR);
        }
        project.setPic(userService.getUserById(picId));
        project.setState(0);
        return projectRepository.save(project);
    }

    // 安全保密员修改项目密级
    @CacheEvict(value = "Project_Cache", key = "#projectId")
    public Project updateSecretClassById(String projectId, int secretClass) {
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(secretClass)){
            throw new ResultException(ResultCode.PROJECT_SECRETCLASS_NOT_FOUND_ERROR);
        }
        Project project = getProjectById(projectId);
        project.setSecretClass(secretClass);
        return projectRepository.save(project);
    }

    // 查询所有项目(所有人都可以查询所有项目)
    public List<Project> getAllByDeleted(boolean deleted) {
        return projectRepository.findByDeleted(deleted);
    }

    // 根据密级控制查看项目详情  同时判断项目是否超时
    public boolean getProjectDetails(String projectId, String userId){
        Project project = getProjectById(projectId);
        Users users = userService.getUserById(userId);
        if(ifOverTime(project.getFinishTime())){
            project.setState(ApplicationConfig.PROJECT_OVER_TIME);
        }
        projectRepository.save(project);

        return users.getSecretClass() >= project.getSecretClass();
    }

    public boolean ifOverTime(String projectTimestamp) {
        long timeStamp = System.currentTimeMillis();
        long projectStamp = Long.parseLong(projectTimestamp);

        boolean ifOver = false;
        if(projectStamp < timeStamp){
            ifOver = true;
        }
        return ifOver;
    }

    // 根据用户查询其所有未删除的项目： 项目管理员、项目负责人、子任务负责人
    public List<Project> getProjectsByUser(Users users) {
        return projectRepository.findByPicOrCreatorAndDeleted(users, users, false);
    }

    // 根据用户密级查询项目（返回小于等于用户密级的项目）
    public List<Project> getProjectsBySecretClass(int secretClass, boolean deleted) {
        List<Project> projectList = projectRepository.findByDeleted(deleted);
        List<Project> projectEntities = new ArrayList<>();
        for(Project project : projectList){
            if(project.getSecretClass() <= secretClass){
                projectEntities.add(project);
            }
        }
        return projectEntities;
    }

    // 根据名称查询等项目是否存在
    public boolean hasProjectByNameAndDeleted(String name, boolean deleted) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return projectRepository.existsByNameAndDeleted(name, deleted);
    }

    // 根据项目id查询项目
    public Project getProjectById(String projectId) {
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

    // 清空项目
    @CacheEvict(value = " Project_Cache", allEntries = true)
    public void deleteAllProject() {
        List<Project> projectEntities = getAllByDeleted(true);
        if(projectEntities.size() > 0){
            projectRepository.deleteAll(projectEntities);
        }
    }

    // 根据id删除项目（回收站）
    @CacheEvict(value = " Project_Cache", allEntries = true)
    public Project deleteProjectById(String projectId, String userId) {
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        Project project = getProjectById(projectId);
        // 非项目管理员，非负责人
        if(!project.getCreator().getId().equals(userId) && !project.getPic().getId().equals(userId)){
            throw new ResultException(ResultCode.AUTHORITY_DENIED_ERROR);
        }
        project.setDeleted(true);
        return projectRepository.save(project);
    }

    // 根据id撤销删除
    @CacheEvict(value = " Project_Cache", allEntries = true)
    public Project restoreProjectById(String projectId, String userId) {
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        Project project = getProjectById(projectId);
        // 非项目管理员，非负责人
        if(!project.getCreator().getId().equals(userId) && !project.getPic().getId().equals(userId)){
            throw new ResultException(ResultCode.AUTHORITY_DENIED_ERROR);
        }
        project.setDeleted(false);
        return projectRepository.save(project);
    }

    // 负责人指定项目令号、设节点
    public Project arrangeProject(String projectId, String userId, Project projectArgs) {
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        Project project = getProjectById(projectId);

        // 非项目管理员，非负责人
        if(!(project.getCreator().getId().equals(userId)) && !(project.getPic().getId().equals(userId))){
            throw new ResultException(ResultCode.AUTHORITY_DENIED_ERROR);
        }

        if(projectArgs == null){
            throw new ResultException(ResultCode.PROJECT_ARGS_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(projectArgs.getOrderNum())){
            throw new ResultException(ResultCode.PROJECT_ORDER_NUM_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(projectArgs.getFinishTime())){
            throw new ResultException(ResultCode.PROJECT_FINISH_TIME_NOT_FOUND_ERROR);
        }
        project.setOrderNum(projectArgs.getOrderNum());
        project.setFinishTime(projectArgs.getFinishTime());
        if(!ifOverTime(projectArgs.getFinishTime())){
            project.setState(ApplicationConfig.PROJECT_NOT_START);
        }
        return projectRepository.save(project);
    }

    // 项目管理员指定项目负责人： 项目负责人密级高于或等于项目密级
    public Project updateProjectPic(String projectId, String creatorId, String picId){
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        Project project = getProjectById(projectId);
        if(!project.getCreator().getId().equals(creatorId)){
            throw new ResultException(ResultCode.AUTHORITY_DENIED_ERROR);
        }
        if(StringUtils.isEmpty(picId)){
            throw new ResultException(ResultCode.PROJECT_PIC_ARGS_NOT_FOUND_ERROR);
        }
        Users users = userService.getUserById(picId);

        if(users.getSecretClass() < project.getSecretClass()){
            throw new ResultException(ResultCode.USER_SECRETCLASS_NOT_SUPPORT_ERROR);
        }
        project.setPic(users);
        return projectRepository.save(project);
    }

    // 修改项目相关信息
    public Project updateProjectById(String projectId, String userId, Project projectArgs) {
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        if(projectArgs == null){
            throw new ResultException(ResultCode.PROJECT_ARGS_NOT_FOUND_ERROR);
        }

        Project project = getProjectById(projectId);
        // 非项目管理员，非负责人
        if(!(project.getCreator().getId().equals(userId)) && !(project.getPic().getId().equals(userId))){
            throw new ResultException(ResultCode.AUTHORITY_DENIED_ERROR);
        }

        if(!StringUtils.isEmpty(projectArgs.getName()) && !project.getName().equals(projectArgs.getName())){
            if(hasProjectByNameAndDeleted(projectArgs.getName(), false)){
                throw new ResultException(ResultCode.PROJECT_NAME_EXISTED_ERROR);
            }
            project.setName(projectArgs.getName());
        }
        if(!StringUtils.isEmpty(projectArgs.getOrderNum()) && !project.getOrderNum().equals(projectArgs.getOrderNum())){
            project.setOrderNum(projectArgs.getOrderNum());
        }
        if(!StringUtils.isEmpty(projectArgs.getFinishTime())){
            project.setFinishTime(projectArgs.getFinishTime());
        }

        if(!StringUtils.isEmpty(String.valueOf(projectArgs.getState()))){
            project.setState(projectArgs.getState());
        }

        if(!StringUtils.isEmpty(projectArgs.getDescription())){
            project.setDescription(projectArgs.getDescription());
        }

        if(!StringUtils.isEmpty(String.valueOf(projectArgs.getSecretClass()))){
            project.setSecretClass(projectArgs.getSecretClass());
        }
        return projectRepository.save(project);
    }

    // 启动项目： 1.项目状态改为进行中   2：项目的第一个子任务状态改为进行中
    public Project startProject(String projectId){
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        Project project = getProjectById(projectId);
        if(project.getState() != ApplicationConfig.PROJECT_NOT_START){
           throw new ResultException(ResultCode.PROJECT_ALREADY_START_ERROR);
        }
        if(StringUtils.isEmpty(project.getOrderNum())){
            throw new ResultException(ResultCode.PROJECT_ORDER_NUM_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(project.getFinishTime())){
            throw new ResultException(ResultCode.PROJECT_FINISH_TIME_NOT_FOUND_ERROR);
        }
        project.setState(ApplicationConfig.PROJECT_START);

        // 第一个开始的一系列子任务的状态改为进行中
        // 查看流程图上无父节点的节点
       // List<ProcessNode> processNodeEntityList = processNodeRepository1.findByProjectAndLinkList(project, null);
        List<ProcessNode> processNodeEntityList = processNodeRepository1.findByProject(project);
        if(processNodeEntityList.size() == 0){
            throw new ResultException(ResultCode.PROCESS_NODE_NOT_FOUND_ERROR);
        }
        // 开启子任务
        for(ProcessNode processNodeEntity : processNodeEntityList){
            if(processNodeEntity.getLinkList().size() == 0){
                processNodeEntity.getSubtask().setState(ApplicationConfig.SUBTASK_START);
            }
        }
        processNodeRepository1.saveAll(processNodeEntityList);
        return projectRepository.save(project);
    }

    // 根据项目id查询高于等于该项目密级的用户
    public List<Users> getUsersByProjectId(String projectId){
        if(!hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        int secretClass = getProjectById(projectId).getSecretClass();
        List<Users> usersList = new ArrayList<>();
        for(Users users : userService.getAll()){
            if(users.getSecretClass() >= secretClass){
                usersList.add(users);
            }
        }
        return usersList;
    }
}

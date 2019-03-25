package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.DesignLinkEntity;
import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.service.ProjectService;
import com.rengu.cosimulation.service.UserService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Author: XYmar
 * Date: 2019/2/20 13:25
 */
@RestController
@RequestMapping(value = "/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final UserService userService;

    @Autowired
    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    // 新增项目（名称，负责人）
    @PostMapping
    @PreAuthorize(value = "hasRole('PROJECT_MANAGER')")
    public ResultEntity saveProject(ProjectEntity projectEntity, String creatorId, String picId){
        return ResultUtils.success(projectService.saveProject(projectEntity, creatorId, picId));
    }

    // 项目管理员查询所有项目
    @GetMapping
    @PreAuthorize(value = "hasRole('PROJECT_MANAGER')")
    public ResultEntity getProjects(boolean deleted){
        return ResultUtils.success(projectService.getAllByDeleted(deleted));
    }

    // 根据用户id查询所有项目(负责人)
    @GetMapping(value = "/byUserId/{userId}")
    public ResultEntity getProjectsByUserId(@PathVariable(value = "userId") String userId, boolean deleted){
        return ResultUtils.success(projectService.getProjectsByUser(userService.getUserById(userId), deleted));
    }

    // 根据ID查询项目
    @GetMapping(value = "/{projectId}")
    public ResultEntity getProjectById(@PathVariable(value = "projectId") String projectId){
        return ResultUtils.success(projectService.getProjectById(projectId));
    }

    // 负责人指定项目令号、设节点
    @PatchMapping(value = "/{projectId}/arrange")
    public ResultEntity arrangeProject(@PathVariable(value = "projectId") String projectId, String userId, ProjectEntity projectEntityArgs){
        return ResultUtils.success(projectService.arrangeProject(projectId, userId, projectEntityArgs));
    }

    // 根据ID删除项目
    @PatchMapping(value = "/{projectId}/delete")
    public ResultEntity deleteProjectById(@PathVariable(value = "projectId") String projectId, String userId){
        return ResultUtils.success(projectService.deleteProjectById(projectId, userId));
    }

    // 根据ID撤销删除项目
    @PatchMapping(value = "/{projectId}/restore")
    public ResultEntity restoreProjectById(@PathVariable(value = "projectId") String projectId, String userId){
        return ResultUtils.success(projectService.restoreProjectById(projectId, userId));
    }

    // 根据id修改项目
    @PatchMapping(value = "/{projectId}")
    public ResultEntity updateProjectById(@PathVariable(value = "projectId") String projectId, String userId, ProjectEntity projectEntityArgs){
        return ResultUtils.success(projectService.updateProjectById(projectId, userId, projectEntityArgs));
    }

    // 管理员修改项目负责人
    @PatchMapping(value = "/{projectId}/updatePic")
    public ResultEntity updateProjectPic(@PathVariable(value = "projectId") String projectId, String creatorId, String picId){
        return ResultUtils.success(projectService.updateProjectPic(projectId, creatorId, picId));
    }

    // 启动项目
    /*@PatchMapping(value = "/{projectId}/startProject")
    public ResultEntity startProject(@PathVariable(value = "projectId") String projectId){
        return ResultUtils.success(projectService.startProject(projectId));
    }*/

}

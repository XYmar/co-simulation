package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.FileMetaEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.service.SubtaskFilesService;
import com.rengu.cosimulation.service.SubtaskService;
import com.rengu.cosimulation.service.UserService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/26 10:26
 */
@RestController
@RequestMapping(value = "/subtasks")
public class SubtaskController {
    private final SubtaskService subtaskService;
    private final SubtaskFilesService subtaskFilesService;
    private final UserService userService;

    @Autowired
    public SubtaskController(SubtaskService subtaskService, SubtaskFilesService subtaskFilesService, UserService userService) {
        this.subtaskService = subtaskService;
        this.subtaskFilesService = subtaskFilesService;
        this.userService = userService;
    }

    // 根据项目id查询子任务
    @GetMapping(value = "/byProject/{projectId}")
    public ResultEntity findByProjectId(@PathVariable(value = "projectId") String projectId){
        return ResultUtils.success(subtaskService.findByProjectId(projectId));
    }

    // 保存子任务， 项目设置子任务(执行者，子任务，节点)
    @PatchMapping(value = "/byProject/{projectId}/setDesignLink")
    public ResultEntity setDesignLink(@PathVariable(value = "projectId") String projectId, String designLinkEntityId, String userId, String finishTime){
        return ResultUtils.success(subtaskService.setSubtask(projectId, designLinkEntityId, userId, finishTime));
    }

    // 根据id查询子任务
    @GetMapping(value = "/{subtaskId}")
    public ResultEntity getSubtaskById(String subtaskId){
        return ResultUtils.success(subtaskService.getSubtaskById(subtaskId));
    }

    // 修改子任务(执行者，子任务，节点)
    @PatchMapping(value = "/{subtaskId}/updateDesignLink")
    public ResultEntity updateSubtaskById(@PathVariable(value = "subtaskId") String subtaskId, String designLinkEntityId, String userId, String finishTime){
        return ResultUtils.success(subtaskService.updateSubtaskById(subtaskId, designLinkEntityId, userId, finishTime));
    }

    // 删除子任务
    @DeleteMapping(value = "/{subtaskId}")
    public ResultEntity deleteSubtaskById(@PathVariable(value = "subtaskId") String subtaskId){
        return ResultUtils.success(subtaskService.deleteSubtaskById(subtaskId));
    }

    // 根据子任务id创建文件
    @PostMapping(value = "/{subtaskId}/uploadfiles")
    public ResultEntity saveSubtaskFilesByProDesignId(@PathVariable(value = "subtaskId") String subtaskId, @RequestBody List<FileMetaEntity> fileMetaEntityList){
        return ResultUtils.success(subtaskFilesService.saveSubtaskFilesByProDesignId(subtaskId, fileMetaEntityList));
    }

    // 根据子任务id查询子任务下的文件
    @GetMapping(value = "/{subtaskId}/files")
    public ResultEntity getSubtaskFilesByProDesignId(@PathVariable(value = "subtaskId") String subtaskId){
        return ResultUtils.success(subtaskFilesService.getSubtaskFilesByProDesignId(subtaskId));
    }

    // 根据子任务id为子任务添加审核员
    @PatchMapping(value = "/{subtaskId}/arrangeAssessors")
    public ResultEntity arrangeAssessorsById(@PathVariable(value = "subtaskId") String subtaskId, String userId, @RequestParam(value = "ids") String[] userIds){
        return ResultUtils.success(subtaskService.arrangeAssessorsById(subtaskId, userId, userIds));
    }

    // 根据审核人id查询待其审核的子任务的相关信息
    @GetMapping(value = "/byAssessorId/{assessorId}")
    public ResultEntity findSubtasksByAssessor(@PathVariable(value = "assessorId") String assessorId){
        return ResultUtils.success(subtaskService.findSubtasksByAssessor(userService.getUserById(assessorId)));
    }

}

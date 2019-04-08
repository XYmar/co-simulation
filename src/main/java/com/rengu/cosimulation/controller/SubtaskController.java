package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.FileMetaEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
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
        List<SubtaskEntity> subtaskEntityList = subtaskService.findByProjectId(projectId);
        return ResultUtils.success(subtaskEntityList);
    }

    // 根据id查询子任务
    @GetMapping(value = "/{subtaskId}")
    public ResultEntity getSubtaskById(String subtaskId){
        return ResultUtils.success(subtaskService.getSubtaskById(subtaskId));
    }

    // 修改子任务(执行者，子任务，节点)
    @PatchMapping(value = "/{subtaskId}/byProject/{projectId}")
    public ResultEntity updateSubtaskById(@PathVariable(value = "projectId") String projectId, @PathVariable(value = "subtaskId") String subtaskId, String userId, String finishTime){
        return ResultUtils.success(subtaskService.updateSubtaskById(projectId, subtaskId, userId, finishTime));
    }
    // 根据审核人id查询待其审核的子任务的相关信息
    @GetMapping(value = "/byAssessorId/{assessorId}")
    public ResultEntity findSubtasksByAssessor(@PathVariable(value = "assessorId") String assessorId){
        return ResultUtils.success(subtaskService.findSubtasksByAllAssessor(userService.getUserById(assessorId)));
    }
    // 删除子任务
    @DeleteMapping(value = "/{subtaskId}")
    public ResultEntity deleteSubtaskById(@PathVariable(value = "subtaskId") String subtaskId){
        return ResultUtils.success(subtaskService.deleteSubtaskById(subtaskId));
    }

    // 根据子任务id创建文件
    @PostMapping(value = "/{subtaskId}/uploadfiles")
    public ResultEntity saveSubtaskFilesByProDesignId(@PathVariable(value = "subtaskId") String subtaskId, @RequestHeader(value = "projectId") String projectId, @RequestBody List<FileMetaEntity> fileMetaEntityList){
        return ResultUtils.success(subtaskFilesService.saveSubtaskFilesByProDesignId(subtaskId, projectId, fileMetaEntityList));
    }

    // 根据子任务id查询子任务下的文件
    @GetMapping(value = "/{subtaskId}/files")
    public ResultEntity getSubtaskFilesByProDesignId(@PathVariable(value = "subtaskId") String subtaskId){
        return ResultUtils.success(subtaskFilesService.getSubtaskFilesByProDesignId(subtaskId));
    }

    // 根据子任务id审核子任务
    /*@PatchMapping(value = "/{subtaskId}/assessSubtask")
    public ResultEntity assessSubtaskById(@PathVariable(value = "subtaskId") String subtaskId, SubtaskEntity subtaskEntityArgs){
        return ResultUtils.success(subtaskService.assessSubtaskById(subtaskId, subtaskEntityArgs));
    }*/

    // 根据子任务id为子任务添加审核员以及会签状态
    @PatchMapping(value = "/{subtaskId}/arrangeAssessors")
    public ResultEntity arrangeAssessorsByIds(@PathVariable(value = "subtaskId") String subtaskId, String userId, int countersignState, String[] collatorIds, String[] auditIds, String[] countersignIds, String[] approverIds) {
        return ResultUtils.success(subtaskService.arrangeAssessorsByIds(subtaskId, userId, countersignState, collatorIds, auditIds, countersignIds, approverIds));
    }
    // 根据子任务id审核子任务
    @PatchMapping(value = "/{subtaskId}/assessSubtask")
    public ResultEntity assessSubtaskById(@PathVariable(value = "subtaskId") String subtaskId, SubtaskEntity subtaskEntityArgs, String userId) {
        return ResultUtils.success(subtaskService.assessSubtaskByIds(subtaskId, subtaskEntityArgs, userService.getUserById(userId)));
    }
    //  根据当前审核状态的ID查询审核意见
    @GetMapping(value = "/{assessStateId}/illustration")
    public ResultEntity illustrationByAssessStateIds(@PathVariable(value = "assessStateId") String assessStateId) {
        return ResultUtils.success(subtaskService.illustrationByAssessStateIds(assessStateId));
    }
    //  根据子任务Id查询审核所有的流程
    @GetMapping(value = "/{subtaskId}/Allillustration")
    public ResultEntity AllillustrationBySubtaskId(@PathVariable(value = "subtaskId") String subtaskId) {
        return ResultUtils.success(subtaskService.allIllustrationBysubtaskId(subtaskService.getSubtaskById(subtaskId)));
    }
}

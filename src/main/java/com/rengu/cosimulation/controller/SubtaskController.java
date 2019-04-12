package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.FileMetaEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.entity.SubtaskAuditEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
import com.rengu.cosimulation.service.SubtaskFilesService;
import com.rengu.cosimulation.service.SubtaskService;
import com.rengu.cosimulation.service.UserService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // 根据用户id查询待校对、待审核、待会签、待批准
    @GetMapping(value = "/byAssessorId/{assessorId}")
    public ResultEntity findSubtasksByAssessor(@PathVariable(value = "assessorId") String userId) {
        return ResultUtils.success(subtaskService.findToBeAuditedsSubtasksByUserId(userId));
    }


    // 根据子任务id为子任务添加审核员以及会签状态
    @PatchMapping(value = "/{subtaskId}/arrangeAssessors")
    public ResultEntity arrangeAssessorsByIds(@PathVariable(value = "subtaskId") String subtaskId, String userId, int auditMode, String[] proofreadUserIds, String[] auditUserIds, String[] countersignUserIds, String[] approveUserIds) {
        return ResultUtils.success(subtaskService.arrangeAssessorsByIds(subtaskId, userId, auditMode, proofreadUserIds, auditUserIds, countersignUserIds, approveUserIds));
    }

    // 根据子任务id查询子任务下的文件
    @GetMapping(value = "/{subtaskId}/files")
    public ResultEntity getSubtaskFilesByProDesignId(@PathVariable(value = "subtaskId") String subtaskId) {
        return ResultUtils.success(subtaskFilesService.getSubtaskFilesByProDesignId(subtaskId));
    }

    // 根据子任务id审核子任务
    @PatchMapping(value = "/{subtaskId}/subtaskAudit")
    public ResultEntity subtaskAudit(@PathVariable(value = "subtaskId") String subtaskId, String userId, SubtaskEntity subtaskEntityArgs, SubtaskAuditEntity subtaskAuditEntityArgs) {
        return ResultUtils.success(subtaskService.subtaskAudit(subtaskId, userId, subtaskEntityArgs, subtaskAuditEntityArgs));
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

    // 申请二次修改
    @PostMapping(value = "/{subtaskId}/applyForModify")
    public ResultEntity applyForModify(@PathVariable(value = "subtaskId") String subtaskId){
        return ResultUtils.success(subtaskService.applyForModify(subtaskId));
    }

    // 项目负责人查询所有待审核的二次修改申请
    @GetMapping(value = "/findModifyToBeAudit")
    public ResultEntity findModifyToBeAudit(String userId) {
        return ResultUtils.success(subtaskService.findByState(userId));
    }

    // 项目负责人处理二次修改申请
    @PostMapping(value = "/{subtaskId}/handleModifyApply")
    public ResultEntity handleModifyApply(@PathVariable(value = "subtaskId") String subtaskId, boolean ifModifyApprove){
        return ResultUtils.success(subtaskService.handleModifyApply(subtaskId, ifModifyApprove));
    }
}

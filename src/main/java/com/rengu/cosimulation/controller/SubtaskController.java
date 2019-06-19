package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.FileMeta;
import com.rengu.cosimulation.entity.Result;
import com.rengu.cosimulation.entity.Subtask;
import com.rengu.cosimulation.entity.SubtaskAudit;
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
    public Result findByProjectId(@PathVariable(value = "projectId") String projectId){
        List<Subtask> subtaskList = subtaskService.findByProjectId(projectId);
        return ResultUtils.success(subtaskList);
    }

    // 根据id查询子任务
    @GetMapping(value = "/{subtaskId}")
    public Result getSubtaskById(@PathVariable(value = "subtaskId") String subtaskId){
        return ResultUtils.success(subtaskService.getSubtaskById(subtaskId));
    }

    // 修改子任务(执行者，子任务，节点)
    @PatchMapping(value = "/{subtaskId}/byProject/{projectId}")
    public Result updateSubtaskById(@PathVariable(value = "projectId") String projectId, @PathVariable(value = "subtaskId") String subtaskId, String loginUserId, String userId, String finishTime){
        return ResultUtils.success(subtaskService.updateSubtaskById(projectId, subtaskId, loginUserId, userId, finishTime));
    }

    // 删除子任务
    @DeleteMapping(value = "/{subtaskId}")
    public Result deleteSubtaskById(@PathVariable(value = "subtaskId") String subtaskId){
        return ResultUtils.success(subtaskService.deleteSubtaskById(subtaskId));
    }

    // 根据子任务id创建文件
    @PostMapping(value = "/{subtaskId}/uploadfiles")
    public Result saveSubtaskFilesByProDesignId(@PathVariable(value = "subtaskId") String subtaskId, @RequestHeader(value = "projectId") String projectId, @RequestBody List<FileMeta> fileMetaList){
        return ResultUtils.success(subtaskFilesService.saveSubtaskFilesByProDesignId(subtaskId, projectId, fileMetaList));
    }

    // 根据文件属性判断重复文件
    @PostMapping(value = "/{subtaskId}/findExistSubtaskFiles")
    public Result findExistSubtaskFiles(@PathVariable(value = "subtaskId") String subtaskId, @RequestBody List<FileMeta> fileMetaList){
        return ResultUtils.success(subtaskFilesService.findExistSubtaskFiles(subtaskId, fileMetaList));
    }

    // 根据用户id查询待校对、待审核、待会签、待批准
    @GetMapping(value = "/byAssessorId/{assessorId}")
    public Result findSubtasksByAssessor(@PathVariable(value = "assessorId") String userId) {
        return ResultUtils.success(subtaskService.findToBeAuditedsSubtasksByUserId(userId));
    }

    // 根据子任务id为子任务添加审核员以及会签状态
    @PatchMapping(value = "/{subtaskId}/arrangeAssessors")
    public Result arrangeAssessorsByIds(@PathVariable(value = "subtaskId") String subtaskId, String userId, int commitMode, boolean ifBackToStart, int auditMode, String[] proofreadUserIds, String[] auditUserIds, String[] countersignUserIds, String[] approveUserIds) {
        return ResultUtils.success(subtaskService.arrangeAssessorsByIds(subtaskId, userId, commitMode, ifBackToStart, auditMode, proofreadUserIds, auditUserIds, countersignUserIds, approveUserIds));
    }

    // 根据子任务id查询子任务下的文件
    @GetMapping(value = "/{subtaskId}/files")
    public Result getSubtaskFilesBySubtaskId(@PathVariable(value = "subtaskId") String subtaskId) {
        return ResultUtils.success(subtaskFilesService.getSubtaskFilesBySubtaskId(subtaskId));
    }

    // 根据子任务id审核子任务
    @PatchMapping(value = "/{subtaskId}/subtaskAudit")
    public Result subtaskAudit(@PathVariable(value = "subtaskId") String subtaskId, String userId, Subtask subtaskArgs, SubtaskAudit subtaskAuditArgs) {
        return ResultUtils.success(subtaskService.subtaskAudit(subtaskId, userId, subtaskArgs, subtaskAuditArgs));
    }

    //  根据子任务Id查询审核所有的流程
    @GetMapping(value = "/{subtaskId}/Allillustration")
    public Result AllillustrationBySubtaskId(@PathVariable(value = "subtaskId") String subtaskId) {
        return ResultUtils.success(subtaskService.allIllustrationBysubtaskId(subtaskService.getSubtaskById(subtaskId)));
    }

    // 申请二次修改
    @PostMapping(value = "/{subtaskId}/applyForModify")
    public Result applyForModify(@PathVariable(value = "subtaskId") String subtaskId, String version){
        return ResultUtils.success(subtaskService.applyForModify(subtaskId, version));
    }

    // 项目负责人查询所有待审核的二次修改申请
    @GetMapping(value = "/findModifyToBeAudit")
    public Result findModifyToBeAudit(String userId) {
        return ResultUtils.success(subtaskService.findByState(userId));
    }

    // 项目负责人处理二次修改申请
    @PostMapping(value = "/{subtaskId}/handleModifyApply")
    public Result handleModifyApply(@PathVariable(value = "subtaskId") String subtaskId, boolean ifModifyApprove){
        return ResultUtils.success(subtaskService.handleModifyApply(subtaskId, ifModifyApprove));
    }

    // 根据用户查询所有项目
    @GetMapping(value = "/findProjectsByUserId/{userId}")
    public Result findProjectsByUserId(@PathVariable(value = "userId") String userId){
        return ResultUtils.success(subtaskService.findProjectsByUserId(userService.getUserById(userId)));
    }

    // 返回整个系统的所有项目及以下子任务的树结构
    @GetMapping(value = "/getProjectTrees")
    public Result getProjectTrees(@RequestHeader(value = "userSecretClass") int userSecretClass){
        return ResultUtils.success(subtaskService.getProjectTrees(userSecretClass));
    }

    // 查询用户是否是项目或子任务负责人
    @PostMapping("/ifIncharge")
    public Result ifIncharge(String userId){
        return ResultUtils.success(subtaskService.ifIncharge(userId));
    }
}

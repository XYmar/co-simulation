package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.*;
import com.rengu.cosimulation.utils.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Author: XYmar
 * Date: 2019/2/26 10:04
 */

@Slf4j
@Service
public class SubtaskService {
    private final SubtaskRepository subtaskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final ProcessNodeRepository1 processNodeRepository1;
    private final SubtaskAuditRepository subtaskAuditRepository;
    private final SublibraryFilesService sublibraryFilesService;
    private final LinkRepository linkRepository;
    private final SublibraryFilesRepository sublibraryFilesRepository;
    private final SubtaskFilesRepository subtaskFilesRepository;


    @Autowired
    public SubtaskService(SubtaskRepository subtaskRepository, ProjectService projectService, UserService userService, ProcessNodeRepository1 processNodeRepository1, SubtaskAuditRepository subtaskAuditRepository, SublibraryFilesService sublibraryFilesService, LinkRepository linkRepository, ProjectRepository projectRepository, SublibraryFilesRepository sublibraryFilesRepository, SubtaskFilesRepository subtaskFilesRepository) {
        this.subtaskRepository = subtaskRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.processNodeRepository1 = processNodeRepository1;
        this.subtaskAuditRepository = subtaskAuditRepository;
        this.sublibraryFilesService = sublibraryFilesService;
        this.linkRepository = linkRepository;
        this.projectRepository = projectRepository;
        this.sublibraryFilesRepository = sublibraryFilesRepository;
        this.subtaskFilesRepository = subtaskFilesRepository;
    }

    // 根据项目id查询所有子任务
    public List<Subtask> findByProjectId(String projectId) {
        return subtaskRepository.findByProject(projectService.getProjectById(projectId));
    }

    // 根据id查询子任务是否存在
    public boolean hasSubtaskById(String subtaskById) {
        if (StringUtils.isEmpty(subtaskById)) {
            return false;
        }
        return subtaskRepository.existsById(subtaskById);
    }

    // 根据子任务名称查询子任务是否存在
    public boolean hasSubtaskByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return subtaskRepository.existsByName(name);
    }

    // 根据id查询子任务
    public Subtask getSubtaskById(String subtaskById) {
        if (!hasSubtaskById(subtaskById)) {
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        return subtaskRepository.findById(subtaskById).get();
    }

    // 根据id修改子任务-->子任务负责人密级大于等于项目密级
    public Subtask updateSubtaskById(String projectId, String subtaskById, String loginUserId, String userId, String finishTime) {
        Project project = projectService.getProjectById(projectId);
        Users users = userService.getUserById(userId);
        Subtask subtask = getSubtaskById(subtaskById);
        if (subtask.getState() >= ApplicationConfig.SUBTASK_TO_BE_AUDIT && subtask.getState() <= ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY) {              // 子任务审核中及审核后无权进行修改
            throw new ResultException(ResultCode.MODIFY_DENIED_ERROR);
        }
        if (!project.getPic().getId().equals(loginUserId)) {
            throw new ResultException(ResultCode.SUBTASK_USER_HAVE_NO_AUTHORITY_TO_ARRANGE);
        }
        if (users.getSecretClass() < project.getSecretClass()) {
            throw new ResultException(ResultCode.USER_SECRETCLASS_NOT_SUPPORT_ERROR);
        }
        subtask.setUsers(users);
        if (!StringUtils.isEmpty(finishTime)) {              // 子任务节点不能晚于项目节点
            if(Long.parseLong(finishTime) > Long.parseLong(project.getFinishTime())){
                throw new ResultException(ResultCode.SUBTASK_FINISH_TIME_NOT_ALLOWED);
            }
            subtask.setFinishTime(finishTime);
        }

        return subtaskRepository.save(subtask);
    }

    // 删除子任务
    public Subtask deleteSubtaskById(String subtaskId) {
        if (!hasSubtaskById(subtaskId)) {
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        Subtask subtask = getSubtaskById(subtaskId);
        if (subtask.getState() >= ApplicationConfig.SUBTASK_TO_BE_AUDIT && subtask.getState() <= ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY) {                     // 子任务审核中及审核后无权进行删除
            throw new ResultException(ResultCode.DELETE_DENIED_ERROR);
        }
        subtaskRepository.delete(subtask);
        return subtask;
    }

    // 判断项目的所有子任务是否已全部完成，完成后将项目状态设置为完成
    public void setProjectComplete(Project project) {
        List<ProcessNode> processNodeList = processNodeRepository1.findByProject(project);

        // 查找最后的节点  即查找无已此节点为父节点的link，然后找到对应的节点,查看他们的状态
        boolean ifAllComplete = true;
        for (ProcessNode processNode : processNodeList) {
            if (!linkRepository.existsByProjectAndParentId(project, processNode.getId())) {
                if (!(processNode.getSubtask().getState() == ApplicationConfig.SUBTASK_AUDIT_OVER)) {
                    ifAllComplete = false;
                }
            }
        }
        if (ifAllComplete) {
            project.setState(ApplicationConfig.PROJECT_OVER);
        }
        projectRepository.save(project);
    }

    // 根据子任务开启其后续任务
    public void startNextSubtasksById(Subtask subtask) {
        // 根据子任务查找对应的节点
        ProcessNode processNode = processNodeRepository1.findBySubtask(subtask);
        // 根据连接关系找到对应的子节点连接
        List<Link> childLinkList = linkRepository.findByParentId(processNode.getId());

        List<ProcessNode> processNodeList = new ArrayList<>();
        // 根据连接查找子节点
        for (Link link : childLinkList) {
            processNodeList.add(processNodeRepository1.findById(link.getSelfId()).get());
        }

        // 获取后续子任务   如果没有后续子任务了则
        List<Subtask> subtaskList = new ArrayList<>();
        if (processNodeList.size() > 0) {
            for (ProcessNode processNodeEntity : processNodeList) {
                subtaskList.add(processNodeEntity.getSubtask());
            }
        }

        if (subtaskList.size() > 0) {                     // 如果后续有子任务的话则开启子任务
            for (Subtask subtask1 : subtaskList) {
                subtask1.setState(ApplicationConfig.SUBTASK_START);
            }
            subtaskRepository.saveAll(subtaskList);
        }
    }

    // 根据子任务查询其父节点任务是否全部完成
    public boolean ifAllParentSubtasksOver(Subtask subtask) {
        boolean ifOver = false;
        // 根据子任务查找对应的节点
        ProcessNode processNode = processNodeRepository1.findBySubtask(subtask);
        // 查找该块对应的各个父节点连接
        List<Link> parentLinkList = linkRepository.findBySelfId(processNode.getId());
        // 根据连接查找父节点
        List<ProcessNode> parentProcessNodeEntityList = new ArrayList<>();
        for (Link link : parentLinkList) {
            parentProcessNodeEntityList.add(processNodeRepository1.findById(link.getParentId()).get());
        }
        if (parentProcessNodeEntityList.size() == 0) {             // 无父节点时
            ifOver = true;
        } else {
            for (ProcessNode processNodeEntity11 : parentProcessNodeEntityList) {
                ifOver = processNodeEntity11.getSubtask().getState() == ApplicationConfig.SUBTASK_AUDIT_OVER;
            }
        }

        return ifOver;
    }

    /**
     * 第一次提交： 判断父节点是否全部完成;
     * 直接修改：   指定驳回流程（是否回到初始流程） --> 修改子任务状态到指定的流程
     * 二次修改：   重新提交，重新指定 --> 不用判断父节点是否全部完成
     * 重置各种状态
     */
    // 提交审核  根据子任务id为子任务选择审核模式及四类审核人  提交：第一次提交，直接修改，二次修改
    public Subtask arrangeAssessorsByIds(String subtaskId, String userId, int commitMode, boolean ifBackToStart, int auditMode, String[] proofreadUserIds, String[] auditUserIds, String[] countersignUserIds, String[] approveUserIds) {
        Subtask subtask = getSubtaskById(subtaskId);
        if (subtask.getState() >= ApplicationConfig.SUBTASK_TO_BE_AUDIT && subtask.getState() <= ApplicationConfig.SUBTASK_APPROVE) {                     // 子任务审核中及审核后无权进行删除
            throw new ResultException(ResultCode.ARRANGE_DENIED_ERROR);
        }
        if (commitMode == ApplicationConfig.SUBTASK_DIRECT_MODIFY) {    // 直接修改提交审核
            if (ifBackToStart) {                // 驳回后的修改提交到第一个流程
                subtask.setState(ApplicationConfig.SUBTASK_TO_BE_AUDIT);
            } else {
                subtask.setState(subtask.getRejectState());
            }
        } else {              // 第一次提交、二次修改
            if (commitMode == ApplicationConfig.SUBTASK_SECOND_MODIFY) {           // 二次修改  重置二次修改申请状态状态
                subtask.setIfModifyApprove(false);
                subtask.setState(ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY_APPROVE_AND_COMMITED);
            }else {
                subtask.setState(ApplicationConfig.SUBTASK_TO_BE_AUDIT);
            }
            Users users = userService.getUserById(userId);
            if (!ifAllParentSubtasksOver(subtask)) {                            // 父节点未全部完成
                throw new ResultException(ResultCode.SUBTASK_PARENT_NOT_ALL_OVER);
            }
            if (!users.getId().equals(subtask.getUsers().getId())) {           // 只有子任务负责人才可选择审核人
                throw new ResultException(ResultCode.SUBTASK_USER_ARRANGE_AUTHORITY_DENIED_ERROR);
            }
            if (StringUtils.isEmpty(String.valueOf(auditMode))) {
                throw new ResultException(ResultCode.AUDITMODE_NOT_FOUND_ERROR);
            }
            if (ArrayUtils.isEmpty(proofreadUserIds)) {
                throw new ResultException(ResultCode.PROOFREADUSERS_NOT_FOUND_ERROR);
            }
            if (ArrayUtils.isEmpty(auditUserIds)) {
                throw new ResultException(ResultCode.AUDITUSERS_NOT_FOUND_ERROR);
            }
            if (auditMode != ApplicationConfig.AUDIT_NO_COUNTERSIGN) {
                if (ArrayUtils.isEmpty(countersignUserIds)) {
                    throw new ResultException(ResultCode.COUNTERSIGNUSERS_NOT_FOUND_ERROR);
                }
            }
            if (ArrayUtils.isEmpty(approveUserIds)) {
                throw new ResultException(ResultCode.APPROVEUSERS_NOT_FOUND_ERROR);
            }
            subtask.setProofSet(idsToSet(proofreadUserIds));
            subtask.setAuditSet(idsToSet(auditUserIds));
            if (auditMode != ApplicationConfig.AUDIT_NO_COUNTERSIGN) {
                subtask.setCountSet(idsToSet(countersignUserIds));
            }
            subtask.setApproveSet(idsToSet(approveUserIds));
            subtask.setAuditMode(auditMode);
            // subtask.setState(ApplicationConfig.SUBTASK_TO_BE_AUDIT);
        }
        subtask.setIfReject(false);
        subtask.setIfApprove(false);
        return subtaskRepository.save(subtask);
    }

    // 根据用户id数组，将用户数组转为set集合
    private Set<Users> idsToSet(String[] ids) {
        Set<Users> usersEntities = new HashSet<>();
        if (!ArrayUtils.isEmpty(ids)) {
            for (String id : ids) {
                usersEntities.add(userService.getUserById(id));
            }
        }
        return usersEntities;
    }

    // 根据用户id查询待校对、待审核、待会签、待批准
    public Map<String, List> findToBeAuditedsSubtasksByUserId(String userId) {
        Users users = userService.getUserById(userId);
        Map<String, List> subtaskToBeAudited = new HashMap<>();
        subtaskToBeAudited.put("proofreadSubtask", subtaskRepository.findByProofSetContaining(users));
        subtaskToBeAudited.put("auditSubtask", subtaskRepository.findByAuditSetContaining(users));
        subtaskToBeAudited.put("countersignSubtask", subtaskRepository.findByCountSetContaining(users));
        subtaskToBeAudited.put("approveSubtask", subtaskRepository.findByApproveSet(users));
        subtaskToBeAudited.put("alreadyAudit", subtaskAuditRepository.findByUsersAndStateAndIfOver(users, ApplicationConfig.SUBTASK_COUNTERSIGN, false));
        return subtaskToBeAudited;
    }

    // 审核操作
    public Subtask subtaskAudit(String subtaskId, String userId, Subtask subtaskArgs, SubtaskAudit subtaskAuditArgs) {
        Subtask subtask = getSubtaskById(subtaskId);
        Users users = userService.getUserById(userId);             // 登录的用户
        int state = subtask.getState();

        if (StringUtils.isEmpty(String.valueOf(subtaskArgs.getState()))) {
            throw new ResultException(ResultCode.STATE_NOT_FOUND_ERROR);
        }
        if (subtaskArgs.getState() == 3) {        // 校对中设置状态为校对
            subtask.setState(ApplicationConfig.SUBTASK_PROOFREAD);
        }
        if (subtaskArgs.getState() != subtask.getState()) {
            throw new ResultException(ResultCode.CURRENT_PROGRESS_NOT_ARRIVE_ERROR);
        }

        SubtaskAudit subtaskAudit = new SubtaskAudit();      // 审核详情
        /**
         *  子任务流程控制：
         *  当前审核结果： pass --> 当前审核人：自己--> 报错，无通过权限
         *                                      其他人--> 设置子任务进入下一模式：
         *                                                当前阶段为审核时-->  1)无会签：审核-->批准
         *                                                                     2)一人/多人会签：审核-->会签
         *                                                当前阶段为会签时-->  1)一人会签：会签-->批准（同上）
         *          *                                                          2)多人会签：多人审核通过审核-->批准
         *                                                                                 若当前用户已会签过则报错，您已会签过
         *                                                当前阶段为批准时-->  (1)修改子任务的通过状态为true； 设置任务状态为审批结束
         *                                                                     (2)将此子任务的后续任务状态改为进行中，即开启后续子任务
         *                                                                     (3)将此子任务的所有文件分别入库
         *                                                                     (4)若所有子任务都已完成，则将项目状态设为完成
         *                 no   --> 停留当前模式   --> 设置子任务状态为审批结束
         *                                             记录当前驳回的阶段
         * */
        // 若当前用户已审批过过则报错，您已执行过审批操作
        if (subtaskAuditRepository.existsBySubtaskAndUsersAndStateAndIfOver(subtask, users, subtaskArgs.getState(), false)) {
            throw new ResultException(ResultCode.USER_ALREADY_COUNTERSIGN_ERROR);
        }

        if (subtaskAuditArgs.isIfPass()) {
            if (subtask.getUsers().getId().equals(userId)) {    // 自己无权通过
                throw new ResultException(ResultCode.USER_PASS_DENIED_ERROR);
            }
            if (subtaskArgs.getState() == ApplicationConfig.SUBTASK_AUDIT) {               // 当前为审核
                if (subtaskArgs.getAuditMode() == ApplicationConfig.AUDIT_NO_COUNTERSIGN) {  // 无会签
                    subtask.setState(ApplicationConfig.SUBTASK_APPROVE);
                } else {
                    subtask.setState(ApplicationConfig.SUBTASK_COUNTERSIGN);
                }
                subtaskAudit.setState(subtaskArgs.getState());
            } else if (subtaskArgs.getState() == ApplicationConfig.SUBTASK_COUNTERSIGN && subtaskArgs.getAuditMode() == ApplicationConfig.AUDIT_MANY_COUNTERSIGN) {
                // 当前为会签 且模式为多人会签
                if ((subtask.getManyCounterSignState() + 1) != subtask.getCountSet().size()) {
                    subtask.setManyCounterSignState(subtask.getManyCounterSignState() + 1);
                } else {                          // 所有人都已会签过
                    subtask.setState(subtaskArgs.getState() + 1);
                }
                subtaskAudit.setState(subtaskArgs.getState());
            } else if (subtaskArgs.getState() == ApplicationConfig.SUBTASK_APPROVE) {                   // 当前为批准，即批准通过后
                subtask.setIfApprove(true);                                                          // 通过审核
                subtask.setState(ApplicationConfig.SUBTASK_AUDIT_OVER);                              // 审批结束
                subtaskAudit.setState(subtaskArgs.getState());
                subtask.setIfModifyApprove(false);

                // 子任务文件入库
                sublibraryFilesService.stockIn(subtask);

                // 开启后续子任务
                startNextSubtasksById(subtask);

                // 子任务全部完成后项目完成
                setProjectComplete(subtask.getProject());
            } else {
                subtask.setState(subtaskArgs.getState() + 1);
                subtaskAudit.setState(subtaskArgs.getState());                                 // 在哪步驳回
            }
        } else {                // 驳回
            if(state == ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY_APPROVE_AND_COMMITED){                   // 二次修改被驳回后仍可继续二次修改
                subtask.setState(ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY_APPROVE);
                subtask.setIfModifyApprove(true);
            }else {
                subtask.setState(ApplicationConfig.SUBTASK_AUDIT_OVER);                                  // 审批结束
            }
            subtaskAudit.setState(subtaskArgs.getState());
            subtask.setRejectState(subtaskArgs.getState());

            subtask.setIfReject(true);           // 设置驳回状态为true
        }
        /**
         * 审核模式、审核阶段、审核结果、审核人、审核意见、当前步骤结束
         * */
        subtaskAudit.setIfPass(subtaskAuditArgs.isIfPass());               // 审核结果
        subtaskAudit.setUsers(users);                                          // 审核人
        subtaskAudit.setSubtask(subtask);                    // 审核详情所属子库文件
        subtaskAudit.setAuditDescription(subtaskAuditArgs.getAuditDescription());   // 审核意见
        subtaskAuditRepository.save(subtaskAudit);

        // 批准通过后 或 驳回后, 将详情改为已结束
        if (!(subtaskAuditArgs.isIfPass()) || subtaskArgs.getState() == ApplicationConfig.SUBTASK_APPROVE) {
            List<SubtaskAudit> subtaskAuditList = subtaskAuditRepository.findBySubtask(subtask);
            for (SubtaskAudit subtaskAudit1 : subtaskAuditList) {
                subtaskAudit1.setIfOver(true);
            }
            subtaskAuditRepository.saveAll(subtaskAuditList);
        }
        return subtask;
    }

    //  根据子任务Id查询所有审核信息
    public List<SubtaskAudit> allIllustrationBysubtaskId(Subtask subtask) {
        return subtaskAuditRepository.findBySubtask(subtask);
    }

    //  根据子任务ID查询单个审核信息
    public Subtask illustrationByAssessStateIds(String assessStateId) {
        Optional<Subtask> subtaskOptional = subtaskRepository.findById(assessStateId);
        if (!subtaskOptional.isPresent()) {
            throw new ResultException(ResultCode.SUBTASK_ARGS_NOT_FOUND_ERROR);
        }
        return subtaskOptional.get();
    }

    // 申请二次修改
    public Subtask applyForModify(String subtaskId, String version) {
        Subtask subtask = getSubtaskById(subtaskId);
        if (subtask.getState() != ApplicationConfig.SUBTASK_AUDIT_OVER) {
            throw new ResultException(ResultCode.SECOND_MODIFY_DENIED_ERROR);
        }
        subtask.setState(ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY);
        subtask.setVersion(version);
        return subtaskRepository.save(subtask);
    }

    // 项目负责人查询所有待审核的二次修改申请
    public List<Subtask> findByState(String userId) {
        List<Project> projectList = projectRepository.findByPic(userService.getUserById(userId));
        List<Subtask> allSubtaskList = new ArrayList<>();
        for (Project project : projectList) {
            List<Subtask> subtaskList = subtaskRepository.findByProjectAndState(project, ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY);
            allSubtaskList.addAll(subtaskList);
        }
        // Users users = userService.getUserById(userId);
        return allSubtaskList;
    }

    // 项目负责人处理二次修改申请
    public Subtask handleModifyApply(String subtaskId, boolean ifModifyApprove) {
        Subtask subtask = getSubtaskById(subtaskId);
        subtask.setIfModifyApprove(ifModifyApprove);
        if (ifModifyApprove) {
            subtask.setState(ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY_APPROVE);
            subtask.setIfReject(false);

            // 二次修改申请统一后将该子任务文件的版本统一修改
            List<SubtaskFile> subtaskFileList = subtaskFilesRepository.findBySubtask(subtask);
            for(SubtaskFile subtaskFile : subtaskFileList){
                subtaskFile.setVersion(subtask.getVersion());
            }
            subtaskFilesRepository.saveAll(subtaskFileList);
        } else {
            subtask.setState(ApplicationConfig.SUBTASK_AUDIT_OVER);
        }
        return subtaskRepository.save(subtask);
    }

    // 根据子任务负责人查询其所有项目(去重后)
    public List<Project> findProjectsByUserId(Users users) {
        // 根据子任务负责人查询其所有项目(去重后)
        List<Subtask> subtaskEntities = subtaskRepository.findByUsers(users);
        Map<String, Project> projectMap = new HashMap<>();
        for (Subtask subtask : subtaskEntities) {
            if (!projectMap.containsKey(subtask.getProject().getId())) {
                projectMap.put(subtask.getProject().getId(), subtask.getProject());
            }
        }
        List<Project> projectEntities1 = new ArrayList<>(projectMap.values());
        // 根据项目管理员/项目负责人查询所有未删除的项目
        List<Project> projectEntities2 = projectService.getProjectsByUser(users);
        projectEntities1.removeAll(projectEntities2);
        projectEntities1.addAll(projectEntities2);
        return projectEntities1;
    }

    // 返回整个系统的所有项目及以下子任务的树结构
    public List<Map<String, Object>> getProjectTrees(int secretClass) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Project> projectList = projectService.getProjectsBySecretClass(secretClass, false);
        for (Project project : projectList) {
            Map<String, Object> map = new HashMap<>();
            map.put("project", project);
            map.put("subtask", findByProjectId(project.getId()));
            list.add(map);
        }
        return list;
    }

    // 用户是否是项目负责人
    public boolean ifIncharge(String userId) {
        Users users = userService.getUserById(userId);
        List<Subtask> subtaskList = subtaskRepository.findByUsersOrProofSetContainingOrAuditSetContainingOrCountSetContainingOrApproveSetContaining(users, users, users, users, users);
        List<Project> projectList = projectRepository.findByPicOrCreator(users, users);
        List<SubDepotFile> subDepotFileList = sublibraryFilesRepository.findByProofSetContainingOrAuditSetContainingOrCountSetContainingOrApproveSetContaining(users, users, users, users);

        return (subtaskList.size() > 0 || projectList.size() > 0 || subDepotFileList.size() > 0);
    }

}

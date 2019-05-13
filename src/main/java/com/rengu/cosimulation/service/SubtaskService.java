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


    @Autowired
    public SubtaskService(SubtaskRepository subtaskRepository, ProjectService projectService, UserService userService, ProcessNodeRepository1 processNodeRepository1, SubtaskAuditRepository subtaskAuditRepository, SublibraryFilesService sublibraryFilesService, LinkRepository linkRepository, ProjectRepository projectRepository) {
        this.subtaskRepository = subtaskRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.processNodeRepository1 = processNodeRepository1;
        this.subtaskAuditRepository = subtaskAuditRepository;
        this.sublibraryFilesService = sublibraryFilesService;
        this.linkRepository = linkRepository;
        this.projectRepository = projectRepository;
    }

    // 根据项目id查询所有子任务
    public List<SubtaskEntity> findByProjectId(String projectId) {
        return subtaskRepository.findByProjectEntity(projectService.getProjectById(projectId));
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
    public SubtaskEntity getSubtaskById(String subtaskById) {
        if (!hasSubtaskById(subtaskById)) {
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        return subtaskRepository.findById(subtaskById).get();
    }

    // 根据id修改子任务-->子任务负责人密级大于等于项目密级
    public SubtaskEntity updateSubtaskById(String projectId, String subtaskById, String loginUserId, String userId, String finishTime) {
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        UserEntity userEntity = userService.getUserById(userId);
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskById);
        if(subtaskEntity.getState() >= ApplicationConfig.SUBTASK_TO_BE_AUDIT && subtaskEntity.getState() <= ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY){              // 子任务审核中及审核后无权进行修改
            throw new ResultException(ResultCode.MODIFY_DENIED_ERROR);
        }
        if(!projectEntity.getPic().getId().equals(loginUserId)){
            throw new ResultException(ResultCode.SUBTASK_USER_HAVE_NO_AUTHORITY_TO_ARRANGE);
        }
        if (userEntity.getSecretClass() < projectEntity.getSecretClass()) {
            throw new ResultException(ResultCode.USER_SECRETCLASS_NOT_SUPPORT_ERROR);
        }
        subtaskEntity.setUserEntity(userEntity);
        if (!StringUtils.isEmpty(finishTime)) {
            subtaskEntity.setFinishTime(finishTime);
        }

        return subtaskRepository.save(subtaskEntity);
    }

    // 删除子任务
    public SubtaskEntity deleteSubtaskById(String subtaskId) {
        if (!hasSubtaskById(subtaskId)) {
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskId);
        if(subtaskEntity.getState() >= ApplicationConfig.SUBTASK_TO_BE_AUDIT && subtaskEntity.getState() <= ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY){                     // 子任务审核中及审核后无权进行删除
            throw new ResultException(ResultCode.DELETE_DENIED_ERROR);
        }
        subtaskRepository.delete(subtaskEntity);
        return subtaskEntity;
    }

    // 判断项目的所有子任务是否已全部完成，完成后将项目状态设置为完成
    public void setProjectComplete(ProjectEntity projectEntity){
        List<ProcessNodeEntity1> processNodeEntity1List = processNodeRepository1.findByProjectEntity(projectEntity);

        // 查找最后的节点  即查找无已此节点为父节点的link，然后找到对应的节点,查看他们的状态
        boolean ifAllComplete = true;
        for(ProcessNodeEntity1 processNodeEntity1 : processNodeEntity1List){
            if(!linkRepository.existsByProjectEntityAndParentId(projectEntity, processNodeEntity1.getId())){
                if(!(processNodeEntity1.getSubtaskEntity().getState() == ApplicationConfig.SUBTASK_AUDIT_OVER)){
                    ifAllComplete = false;
                }
            }
        }
        if(ifAllComplete){
            projectEntity.setState(ApplicationConfig.PROJECT_OVER);
        }
        projectRepository.save(projectEntity);
    }

    // 根据子任务开启其后续任务
    public void startNextSubtasksById(SubtaskEntity subtaskEntity){
        // 根据子任务查找对应的节点
        ProcessNodeEntity1 processNodeEntity1 = processNodeRepository1.findBySubtaskEntity(subtaskEntity);
        // 根据连接关系找到对应的子节点连接
        List<LinkEntity> childLinkEntityList = linkRepository.findByParentId(processNodeEntity1.getId());

        List<ProcessNodeEntity1> processNodeEntity1List = new ArrayList<>();
        // 根据连接查找子节点
        for(LinkEntity linkEntity : childLinkEntityList){
            processNodeEntity1List.add(processNodeRepository1.findById(linkEntity.getSelfId()).get());
        }

        // 获取后续子任务   如果没有后续子任务了则
        List<SubtaskEntity> subtaskEntityList = new ArrayList<>();
        if(processNodeEntity1List.size() > 0){
            for(ProcessNodeEntity1 processNodeEntity : processNodeEntity1List){
                subtaskEntityList.add(processNodeEntity.getSubtaskEntity());
            }
        }

        if(subtaskEntityList.size() > 0){                     // 如果后续有子任务的话则开启子任务
            for(SubtaskEntity subtaskEntity1 : subtaskEntityList){
                subtaskEntity1.setState(ApplicationConfig.SUBTASK_START);
            }
            subtaskRepository.saveAll(subtaskEntityList);
        }
    }

    // 根据子任务查询其父节点任务是否全部完成
    public boolean ifAllParentSubtasksOver(SubtaskEntity subtaskEntity){
        boolean ifOver = false;
        // 根据子任务查找对应的节点
        ProcessNodeEntity1 processNodeEntity1 = processNodeRepository1.findBySubtaskEntity(subtaskEntity);
        // 查找该块对应的各个父节点连接
        List<LinkEntity> parentLinkEntityList = linkRepository.findBySelfId(processNodeEntity1.getId());
        // 根据连接查找父节点
        List<ProcessNodeEntity1> parentProcessNodeEntityList = new ArrayList<>();
        for(LinkEntity linkEntity : parentLinkEntityList){
            parentProcessNodeEntityList.add(processNodeRepository1.findById(linkEntity.getParentId()).get());
        }
        /*for(ProcessNodeEntity1 processNodeEntity : parentProcessNodeEntityList){
            ProcessNodeEntity parentProcessNodeEntity = null;
            if(processNodeRepository1.existsByProjectEntityAndSelfSign(subtaskEntity.getProjectEntity(), processNodeEntity1.getParentSign())){
                parentProcessNodeEntity = processNodeRepository1.findByProjectEntityAndSelfSign(subtaskEntity.getProjectEntity(), processNodeEntity1.getParentSign()).get(0);
            }

            if(parentProcessNodeEntity != null){
                parentProcessNodeEntityList.add(parentProcessNodeEntity);
            }
        }*/
        if(parentProcessNodeEntityList.size() == 0){             // 无父节点时
            ifOver = true;
        }else{
            for(ProcessNodeEntity1 processNodeEntity11 : parentProcessNodeEntityList){
                ifOver = false;
                if(processNodeEntity11.getSubtaskEntity().getState() == ApplicationConfig.SUBTASK_AUDIT_OVER){
                    ifOver = true;
                }
            }
        }

        return ifOver;
    }

    /**
     *    第一次提交： 判断父节点是否全部完成;
     *    直接修改：   指定驳回流程（是否回到初始流程） --> 修改子任务状态到指定的流程
     *    二次修改：   重新提交，重新指定 --> 不用判断父节点是否全部完成
     *                 重置各种状态
     * */
    // 提交审核  根据子任务id为子任务选择审核模式及四类审核人  提交：第一次提交，直接修改，二次修改
    public SubtaskEntity arrangeAssessorsByIds(String subtaskId, String userId, int commitMode, boolean ifBackToStart, int auditMode, String[] proofreadUserIds, String[] auditUserIds, String[] countersignUserIds, String[] approveUserIds) {
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskId);
        if(subtaskEntity.getState() >= ApplicationConfig.SUBTASK_TO_BE_AUDIT && subtaskEntity.getState() <= ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY){                     // 子任务审核中及审核后无权进行删除
            throw new ResultException(ResultCode.ARRANGE_DENIED_ERROR);
        }
        if(commitMode == ApplicationConfig.SUBTASK_DIRECT_MODIFY){    // 直接修改提交审核
            if(ifBackToStart){                // 驳回后的修改提交到第一个流程
                subtaskEntity.setState(ApplicationConfig.SUBTASK_TO_BE_AUDIT);
            }else{
                subtaskEntity.setState(subtaskEntity.getRejectState());
            }
        }else{              // 第一次提交、二次修改
            if(commitMode == ApplicationConfig.SUBTASK_SECOND_MODIFY){           // 二次修改  重置二次修改申请状态状态
                subtaskEntity.setIfModifyApprove(false);
            }
            UserEntity userEntity = userService.getUserById(userId);
            if(!ifAllParentSubtasksOver(subtaskEntity)){                            // 父节点未全部完成
                throw new ResultException(ResultCode.SUBTASK_PARENT_NOT_ALL_OVER);
            }
            if (!userEntity.getId().equals(subtaskEntity.getUserEntity().getId())) {           // 只有子任务负责人才可选择审核人
                throw new ResultException(ResultCode.SUBTASK_USER_ARRANGE_AUTHORITY_DENIED_ERROR);
            }
            if(StringUtils.isEmpty(String.valueOf(auditMode))){
                throw new ResultException(ResultCode.AUDITMODE_NOT_FOUND_ERROR);
            }
            if (ArrayUtils.isEmpty(proofreadUserIds)) {
                throw new ResultException(ResultCode.PROOFREADUSERS_NOT_FOUND_ERROR);
            }
            if (ArrayUtils.isEmpty(auditUserIds)) {
                throw new ResultException(ResultCode.AUDITUSERS_NOT_FOUND_ERROR);
            }
            if(auditMode != ApplicationConfig.AUDIT_NO_COUNTERSIGN){
                if(ArrayUtils.isEmpty(countersignUserIds)){
                    throw new ResultException(ResultCode.COUNTERSIGNUSERS_NOT_FOUND_ERROR);
                }
            }
            if (ArrayUtils.isEmpty(approveUserIds)) {
                throw new ResultException(ResultCode.APPROVEUSERS_NOT_FOUND_ERROR);
            }
            subtaskEntity.setProofreadUserSet(idsToSet(proofreadUserIds));
            subtaskEntity.setAuditUserSet(idsToSet(auditUserIds));
            if(auditMode != ApplicationConfig.AUDIT_NO_COUNTERSIGN){
                subtaskEntity.setCountersignUserSet(idsToSet(countersignUserIds));
            }
            subtaskEntity.setApproveUserSet(idsToSet(approveUserIds));
            subtaskEntity.setAuditMode(auditMode);
            subtaskEntity.setState(ApplicationConfig.SUBTASK_TO_BE_AUDIT);
            subtaskEntity.setIfReject(false);
        }

        return subtaskRepository.save(subtaskEntity);
    }

    // 根据用户id数组，将用户数组转为set集合
    private Set<UserEntity> idsToSet(String[] ids){
        Set<UserEntity> userEntities = new HashSet<>();
        if(!ArrayUtils.isEmpty(ids)){
            for(String id : ids){
                userEntities.add(userService.getUserById(id));
            }
        }
        return userEntities;
    }

    // 根据用户id查询待校对、待审核、待会签、待批准
    public Map<String, List> findToBeAuditedsSubtasksByUserId(String userId) {
        UserEntity userEntity = userService.getUserById(userId);
        Map<String, List> subtaskToBeAudited = new HashMap<>();
        subtaskToBeAudited.put("proofreadSubtask", subtaskRepository.findByProofreadUserSetContaining(userEntity));
        subtaskToBeAudited.put("auditSubtask", subtaskRepository.findByAuditUserSetContaining(userEntity));
        subtaskToBeAudited.put("countersignSubtask", subtaskRepository.findByCountersignUserSetContaining(userEntity));
        subtaskToBeAudited.put("approveSubtask", subtaskRepository.findByApproveUserSet(userEntity));
        subtaskToBeAudited.put("alreadyAudit", subtaskAuditRepository.findByUserEntityAndState(userEntity, ApplicationConfig.SUBTASK_COUNTERSIGN));
        return subtaskToBeAudited;
    }

    // 审核操作
    public SubtaskEntity subtaskAudit(String subtaskId, String userId, SubtaskEntity subtaskEntityArgs, SubtaskAuditEntity subtaskAuditEntityArgs){
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskId);
        UserEntity userEntity = userService.getUserById(userId);             // 登录的用户

        if(StringUtils.isEmpty(String.valueOf(subtaskEntityArgs.getState()))){
            throw new ResultException(ResultCode.STATE_NOT_FOUND_ERROR);
        }
        if(subtaskEntityArgs.getState() == 3){        // 校对中设置状态为校对
            subtaskEntity.setState(ApplicationConfig.SUBTASK_PROOFREAD);
        }
        if(subtaskEntityArgs.getState() != subtaskEntity.getState()){
            throw new ResultException(ResultCode.CURRENT_PROGRESS_NOT_ARRIVE_ERROR);
        }

        SubtaskAuditEntity subtaskAuditEntity = new SubtaskAuditEntity();      // 审核详情
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
        if(subtaskAuditRepository.existsBySubtaskEntityAndUserEntityAndStateAndIfOver(subtaskEntity,userEntity,subtaskEntityArgs.getState(), false)){
            throw new ResultException(ResultCode.USER_ALREADY_COUNTERSIGN_ERROR);
        }

        if(subtaskAuditEntityArgs.isIfPass()){
            if(subtaskEntity.getUserEntity().getId().equals(userId)){    // 自己无权通过
                throw new ResultException(ResultCode.USER_PASS_DENIED_ERROR);
            }
            if(subtaskEntityArgs.getState() == ApplicationConfig.SUBTASK_AUDIT){               // 当前为审核
                if(subtaskEntityArgs.getAuditMode() == ApplicationConfig.AUDIT_NO_COUNTERSIGN){  // 无会签
                    subtaskEntity.setState(ApplicationConfig.SUBTASK_APPROVE);
                }else{
                    subtaskEntity.setState(ApplicationConfig.SUBTASK_COUNTERSIGN);
                }
                subtaskAuditEntity.setState(subtaskEntityArgs.getState());
            }else if(subtaskEntityArgs.getState() == ApplicationConfig.SUBTASK_COUNTERSIGN && subtaskEntityArgs.getAuditMode() == ApplicationConfig.AUDIT_MANY_COUNTERSIGN){
                // 当前为会签 且模式为多人会签
                if((subtaskEntity.getManyCounterSignState() + 1) != subtaskEntity.getCountersignUserSet().size()){
                    subtaskEntity.setManyCounterSignState(subtaskEntity.getManyCounterSignState() + 1);
                }else{                          // 所有人都已会签过
                    subtaskEntity.setState(subtaskEntityArgs.getState() + 1);
                }
                subtaskAuditEntity.setState(subtaskEntityArgs.getState());
            }else if(subtaskEntityArgs.getState() == ApplicationConfig.SUBTASK_APPROVE){                   // 当前为批准，即批准通过后
                subtaskEntity.setIfApprove(true);                                                          // 通过审核
                subtaskEntity.setState(ApplicationConfig.SUBTASK_AUDIT_OVER);                              // 审批结束
                subtaskAuditEntity.setState(subtaskEntityArgs.getState());

                // 子任务文件入库
                sublibraryFilesService.stockIn(subtaskEntity);

                // 开启后续子任务
                startNextSubtasksById(subtaskEntity);

                // 子任务全部完成后项目完成
                setProjectComplete(subtaskEntity.getProjectEntity());
            }else{
                subtaskEntity.setState(subtaskEntityArgs.getState() + 1);
                subtaskAuditEntity.setState(subtaskEntityArgs.getState());                                 // 在哪步驳回
            }
        }else{                // 驳回
            subtaskEntity.setState(ApplicationConfig.SUBTASK_AUDIT_OVER);                                  // 审批结束
            subtaskAuditEntity.setState(subtaskEntityArgs.getState());
            subtaskEntity.setRejectState(subtaskEntityArgs.getState());

            subtaskEntity.setIfReject(true);           // 设置驳回状态为true
        }
        /**
         * 审核模式、审核阶段、审核结果、审核人、审核意见、当前步骤结束
         * */
        subtaskAuditEntity.setIfPass(subtaskAuditEntityArgs.isIfPass());               // 审核结果
        subtaskAuditEntity.setUserEntity(userEntity);                                          // 审核人
        subtaskAuditEntity.setSubtaskEntity(subtaskEntity);                    // 审核详情所属子库文件
        subtaskAuditEntity.setAuditDescription(subtaskAuditEntityArgs.getAuditDescription());   // 审核意见
        subtaskAuditRepository.save(subtaskAuditEntity);

        // 批准通过后 或 驳回后, 将详情改为已结束
        if(!(subtaskAuditEntityArgs.isIfPass()) || subtaskEntityArgs.getState() == ApplicationConfig.SUBTASK_APPROVE){
            List<SubtaskAuditEntity> subtaskAuditEntityList = subtaskAuditRepository.findBySubtaskEntity(subtaskEntity);
            for(SubtaskAuditEntity subtaskAuditEntity1 : subtaskAuditEntityList){
                subtaskAuditEntity1.setIfOver(true);
            }
            subtaskAuditRepository.saveAll(subtaskAuditEntityList);
        }
        return subtaskEntity;
    }

    //  根据子任务Id查询所有审核信息
    public List<SubtaskAuditEntity> allIllustrationBysubtaskId(SubtaskEntity subtaskEntity) {
        return subtaskAuditRepository.findBySubtaskEntity(subtaskEntity);
    }

    //  根据子任务ID查询单个审核信息
    public SubtaskEntity illustrationByAssessStateIds(String assessStateId) {
        Optional<SubtaskEntity> subtaskEntityOptional = subtaskRepository.findById(assessStateId);
        if (!subtaskEntityOptional.isPresent()) {
            throw new ResultException(ResultCode.SUBTASK_ARGS_NOT_FOUND_ERROR);
        }
        return subtaskEntityOptional.get();
    }

    // 申请二次修改
    public SubtaskEntity applyForModify(String subtaskId){
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskId);
        if(subtaskEntity.getState() != ApplicationConfig.SUBTASK_AUDIT_OVER){
            throw new ResultException(ResultCode.SECOND_MODIFY_DENIED_ERROR);
        }
        subtaskEntity.setState(ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY);
        return subtaskRepository.save(subtaskEntity);
    }

    // 项目负责人查询所有待审核的二次修改申请
    public List<SubtaskEntity> findByState(String userId){
        List<ProjectEntity> projectEntityList = projectRepository.findByPic(userService.getUserById(userId));
        List<SubtaskEntity> allSubtaskEntityList = new ArrayList<>();
        for(ProjectEntity projectEntity : projectEntityList){
            List<SubtaskEntity> subtaskEntityList = subtaskRepository.findByProjectEntityAndState(projectEntity, ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY);
            allSubtaskEntityList.addAll(subtaskEntityList);
        }
        // UserEntity userEntity = userService.getUserById(userId);
        return allSubtaskEntityList;
    }

    // 项目负责人处理二次修改申请
    public SubtaskEntity handleModifyApply(String subtaskId, boolean ifModifyApprove){
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskId);
        subtaskEntity.setIfModifyApprove(ifModifyApprove);
        if(ifModifyApprove){
            subtaskEntity.setState(ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY_APPROVE);
        }else{
            subtaskEntity.setState(ApplicationConfig.SUBTASK_AUDIT_OVER);
        }
        return subtaskRepository.save(subtaskEntity);
    }

    // 根据子任务负责人查询其所有项目(去重后)
    public List<ProjectEntity> findProjectsByUserId(UserEntity userEntity){
        // 根据子任务负责人查询其所有项目(去重后)
        List<SubtaskEntity> subtaskEntities = subtaskRepository.findByUserEntity(userEntity);
        Map<String, ProjectEntity> projectEntityMap = new HashMap<>();
        for(SubtaskEntity subtaskEntity : subtaskEntities){
            if(!projectEntityMap.containsKey(subtaskEntity.getProjectEntity().getId())){
                projectEntityMap.put(subtaskEntity.getProjectEntity().getId(), subtaskEntity.getProjectEntity());
            }
        }
        List<ProjectEntity> projectEntities1 = new ArrayList<>(projectEntityMap.values());
        // 根据项目管理员/项目负责人查询所有未删除的项目
        List<ProjectEntity> projectEntities2 = projectService.getProjectsByUser(userEntity);
        projectEntities1.removeAll(projectEntities2);
        projectEntities1.addAll(projectEntities2);
        return projectEntities1;
    }

    // 返回整个系统的所有项目及以下子任务的树结构
    public List<Map<String, Object>> getProjectTrees(int secretClass){
        List<Map<String, Object>> list = new ArrayList<>();
        List<ProjectEntity> projectEntityList = projectService.getProjectsBySecretClass(secretClass, false);
        for(ProjectEntity projectEntity : projectEntityList){
            Map<String, Object> map = new HashMap<>();
            map.put("project", projectEntity);
            map.put("subtask", findByProjectId(projectEntity.getId()));
            list.add(map);
        }
        return list;
    }

}

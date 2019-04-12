package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SubtaskAuditRepository;
import com.rengu.cosimulation.repository.ProcessNodeRepository;
import com.rengu.cosimulation.repository.SubtaskRepository;
import com.rengu.cosimulation.utils.ApplicationConfig;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: XYmar
 * Date: 2019/2/26 10:04
 */
@Service
public class SubtaskService {
    private final SubtaskRepository subtaskRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final ProcessNodeRepository processNodeRepository;
    private final SubtaskAuditRepository subtaskAuditRepository;
    private final SublibraryFilesService sublibraryFilesService;


    @Autowired
    public SubtaskService(SubtaskRepository subtaskRepository, ProjectService projectService, UserService userService, ProcessNodeRepository processNodeRepository, SubtaskAuditRepository subtaskAuditRepository, SublibraryFilesService sublibraryFilesService) {
        this.subtaskRepository = subtaskRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.processNodeRepository = processNodeRepository;
        this.subtaskAuditRepository = subtaskAuditRepository;
        this.sublibraryFilesService = sublibraryFilesService;
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
        subtaskRepository.delete(subtaskEntity);
        return subtaskEntity;
    }

    // 根据子任务开启其后续任务
    public void startNextSubtasksById(SubtaskEntity subtaskEntity){
        // 根据子任务查找对应的节点(多个)  其实只有一个块
        ProcessNodeEntity processNodeEntity = processNodeRepository.findBySubtaskEntity(subtaskEntity).get(0);
        // 查找子节点
        List<ProcessNodeEntity> childProcessNodeEntityList = processNodeRepository.findByProjectEntityAndParentSign(subtaskEntity.getProjectEntity(), processNodeEntity.getSelfSign());

        // 获取后续子任务
        List<SubtaskEntity> subtaskEntityList = new ArrayList<>();
        if(childProcessNodeEntityList.size() > 0){
            for(ProcessNodeEntity processNodeEntity1 : childProcessNodeEntityList){
                subtaskEntityList.add(processNodeEntity1.getSubtaskEntity());
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
        // 根据子任务查找对应的节点(多个)  其实只有一个块
        ProcessNodeEntity processNodeEntity = processNodeRepository.findBySubtaskEntity(subtaskEntity).get(0);
        // 查找该块对应的父节点
        List<ProcessNodeEntity> parentProcessNodeEntityList = processNodeRepository.findByProjectEntityAndSelfSign(subtaskEntity.getProjectEntity(), processNodeEntity.getParentSign());
        if(parentProcessNodeEntityList.size() == 0){             // 无父节点时
            ifOver = true;
        }else{
            for(ProcessNodeEntity processNodeEntity1 : parentProcessNodeEntityList){
                ifOver = false;
                if(processNodeEntity1.getSubtaskEntity().getState() == ApplicationConfig.SUBTASK_AUDIT_OVER){
                    ifOver = true;
                }
            }
        }

        return ifOver;
    }

    // 根据子任务id审核子任务
    /*public SubtaskEntity assessSubtaskById(String subtaskById, SubtaskEntity subtaskEntityArgs){
        if(!hasSubtaskById(subtaskById)){
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskById);
        if(StringUtils.isEmpty(String.valueOf(subtaskEntityArgs.getState()))){
            throw new ResultException(ResultCode.SUBTASK_STATE_NOT_FOUND_ERROR);
        }
        subtaskEntity.setState(subtaskEntityArgs.getState());
        // 若通过则设置其后续的子任务状态为进行中
        if(subtaskEntityArgs.getState() == 1){
            List<SubtaskEntity> subtaskEntityList = findNextSubtasksById(subtaskById);
            for(SubtaskEntity subtaskEntity1 : subtaskEntityList){
                subtaskEntity1.setState(1);
            }
            subtaskRepository.saveAll(subtaskEntityList);
        }
        if(!StringUtils.isEmpty(subtaskEntityArgs.getIllustration())){
            subtaskEntity.setIllustration(subtaskEntityArgs.getIllustration());
        }
        return subtaskRepository.save(subtaskEntity);
    }*/

    // 根据子任务id为子任务选择审核模式及四类审核人
    public SubtaskEntity arrangeAssessorsByIds(String subtaskId, String userId, int auditMode, String[] proofreadUserIds, String[] auditUserIds, String[] countersignUserIds, String[] approveUserIds) {
        UserEntity userEntity = userService.getUserById(userId);
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskId);
        if(!ifAllParentSubtasksOver(subtaskEntity)){
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
         *                                                                     (2)将此子任务的后续任务状态改为进行中
         *                                                    @TODO                 (3)将此子任务的所有文件分别入库
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

    /*public SubtaskEntity assessSubtaskByIds(String subtaskById, SubtaskEntity subtaskEntityArgs, UserEntity userEntity) {
        *//**
         *  当前审核结果： pass --> 当前审核人：自己--> 报错，无通过权限
         *                                      其他人--> 设置子任务进入下一模式：
         *                                                当前阶段为审核时-->  1)无会签：审核-->批准
         *                                                                     2)一人/多人会签：审核-->会签
         *                                                当前阶段为会签时-->  1)一人会签：会签-->批准（同上）
         *          *                                                          2)多人会签：多人审核通过审核-->批准
         *                                                                                 若当前用户已会签过则报错，您已会签过
         *                                                当前阶段为批准时-->  修改子任务的通过状态为true； 设置子任务状态为审批结束
         *                 no   --> 停留当前模式   --> 设置子任务状态为审批结束
         *                                             记录当前驳回的阶段
         * *//*
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!hasSubtaskById(subtaskById)) {
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskById);
        List<SubtaskAuditEntity> allIllustrationList = subtaskAuditRepository.findBySubtaskEntity(getSubtaskById(subtaskById));
        int maxState = 0;
        int a = 0;
        for (SubtaskAuditEntity subtaskAuditEntitys : allIllustrationList) {
            if (subtaskById.equals(subtaskAuditEntitys.getSubtaskEntity().getId())) {
                if (maxState < subtaskAuditEntitys.getAssessState()) {
                    maxState = subtaskAuditEntitys.getAssessState();
                }
                if (subtaskAuditEntitys.getAssessState() == 2 && subtaskAuditEntitys.isPass()) {
                    a++;
                }
            }

        }
        Optional<SubtaskEntity> subtaskEntityOptional = subtaskRepository.findById(subtaskById);
        Set<UserEntity> set = subtaskEntityOptional.get().getCountersignSet();
        System.out.println(set.size());
        if (!CollectionUtils.isEmpty(allIllustrationList)) {
            if (subtaskEntityArgs.getState() - maxState > 3) {
                throw new ResultException(ResultCode.THE_PREVIOUS_LAYER_HAS_NOT_BEEN_REVIEWED_OR_REJECTED);
            } else if (subtaskEntityArgs.getState() - maxState > 3 && set.size() == 0) {
                throw new ResultException(ResultCode.THE_PROCESS_SUBTASK_HAS_BEEN_REVIEWED);
            }
        }
        for (SubtaskAuditEntity subtaskAuditEntity : allIllustrationList) {
            if (!CollectionUtils.isEmpty(allIllustrationList)) {
                //  判断是否重复评估
                if (subtaskById.equals(subtaskAuditEntity.getSubtaskEntity().getId())) {
                    if (subtaskEntityArgs.getState() - subtaskAuditEntity.getAssessState() == 2 && subtaskEntityArgs.getState() != 4) {
                        throw new ResultException(ResultCode.THE_PROCESS_SUBTASK_HAS_BEEN_REVIEWED);
                    }
                }
            }
        }
        if (StringUtils.isEmpty(String.valueOf(subtaskEntityArgs.getState()))) {
            throw new ResultException(ResultCode.SUBTASK_STATE_NOT_FOUND_ERROR);
        }
        //  判断会签是否多人同意

        if (subtaskEntity.getCountersignState() == ApplicationConfig.MANY_PEOPLE_COUNTERSIGNSTATE && subtaskEntityArgs.getState() == ApplicationConfig.COUNTERSIGN_ING) {
            if (!subtaskEntityOptional.isPresent()) {
                throw new ResultException(ResultCode.SUBTASK_ARGS_NOT_FOUND_ERROR);
            }
            List<SubtaskAuditEntity> allIllustrationLists = subtaskAuditRepository.findByUserEntity(userEntity);
            if (!CollectionUtils.isEmpty(allIllustrationLists)) {
                for (SubtaskAuditEntity subtaskAuditEntityS : allIllustrationLists) {
                    if (subtaskAuditEntityS.getAssessState() == 2 && subtaskEntityArgs.getState() == ApplicationConfig.COUNTERSIGN_ING && subtaskAuditEntityS.getSubtaskEntity().getId() == subtaskById) {
                        throw new ResultException(ResultCode.THE_CURRENT_COUNTERSIGNATURE_HAS_BEEN_COUNTERSIGNED);
                    }
                }
            }
            if (subtaskEntityArgs.getPassState() == ApplicationConfig.ASSESSOR_NOT_PASS) {
                // 多人会签一人驳回即驳回
                subtaskEntity.setIllustration(subtaskEntityArgs.getIllustration());
                subtaskEntity.setPass(ApplicationConfig.ASSESS_STATE_NOT_PASS);
                subtaskEntity.setState(1);
                subtaskEntity.setPassState(ApplicationConfig.ASSESSOR_NOT_PASS);
                return getSubtaskEntity(subtaskById, subtaskEntityArgs, userEntity, df, subtaskEntity);
            }
            //  如果当前为pass则通过，如果小于set.size()，则+1，当加到等于时，则通过
            if (subtaskEntityArgs.getPassState() == ApplicationConfig.ASSESSOR_PASS && subtaskEntity.getManyCountersignState() < set.size()) {
                subtaskEntity.setManyCountersignState(subtaskEntity.getManyCountersignState() + 1);
                subtaskEntity.setState(ApplicationConfig.COUNTERSIGN_ING);
                return getSubtaskEntity(subtaskById, subtaskEntityArgs, userEntity, df, subtaskEntity);
            }
        }
        SubtaskAuditEntity subtaskAuditEntity = new SubtaskAuditEntity();
        //  如果当前状态是批准状态
        if ((subtaskEntityArgs.getState() == ApplicationConfig.APPROVER_ING && set.size() != a && subtaskEntity.getCountersignState() == ApplicationConfig.MANY_PEOPLE_COUNTERSIGNSTATE)) {
            throw new ResultException(ResultCode.THE_PREVIOUS_LAYER_HAS_NOT_BEEN_REVIEWED_OR_REJECTED);
        }
        if (subtaskEntityArgs.getPassState() == ApplicationConfig.ASSESSOR_PASS) {
            //  自己审核不算通过，但驳回可以
            String subtaskUserID = getSubtaskById(subtaskById).getUserEntity().getId();
            String assessId = userEntity.getId();
            subtaskEntity.setState(subtaskEntityArgs.getState() + 1);
            if (subtaskUserID.equals(assessId)) {
                subtaskEntity.setState(subtaskEntityArgs.getState());
            } else {
                //  如果是子任务无会签并且在审核中
                if (subtaskEntity.getCountersignState() == ApplicationConfig.NOT_PEOPLE_COUNTERSIGNSTATE && subtaskEntityArgs.getState() == ApplicationConfig.AUDITOR_ING) {
                    subtaskEntity.setState(ApplicationConfig.APPROVER_ING);
                } else {
                    subtaskEntity.setState(subtaskEntityArgs.getState() + 1);
                }
                subtaskEntity.setPassState(ApplicationConfig.TO_AUDIT);
                //  当前审核通过_
                subtaskEntity.setPass(ApplicationConfig.ASSESS_STATE_PASS);
            }
            //  判断是否批准完成
            if (subtaskEntityArgs.getState() == ApplicationConfig.APPROVER_ING) {
                subtaskEntity.setPassState(ApplicationConfig.ASSESSOR_PASS);
            }
        }
        if (subtaskEntityArgs.getPassState() == ApplicationConfig.ASSESSOR_NOT_PASS) {
            // 驳回以后同时要返回给前端是谁驳回的并且拿到当前驳回人的驳回信息
            // 将驳回状态改成true即可，
            subtaskEntity.setPass(ApplicationConfig.ASSESS_STATE_NOT_PASS);
            subtaskEntity.setPassState(ApplicationConfig.ASSESSOR_NOT_PASS);
            subtaskEntity.setState(1);
        }
        if (subtaskEntityArgs.getState() == ApplicationConfig.PROOFREAD_ING) {
            subtaskAuditEntity.setAssessState(0);
        } else if (subtaskEntityArgs.getState() == ApplicationConfig.AUDITOR_ING) {
            subtaskAuditEntity.setAssessState(1);
        } else if (subtaskEntityArgs.getState() == ApplicationConfig.COUNTERSIGN_ING) {
            subtaskAuditEntity.setAssessState(2);
        } else if (subtaskEntityArgs.getState() == ApplicationConfig.APPROVER_ING) {
            subtaskAuditEntity.setAssessState(3);
        }
        saveIllustration(subtaskById, subtaskEntityArgs, userEntity, df, subtaskEntity, subtaskAuditEntity);
        subtaskEntity.setIllustration(subtaskEntityArgs.getIllustration());
        subtaskAuditRepository.save(subtaskAuditEntity);
        return subtaskRepository.save(subtaskEntity);
    }

    //  多人会签中保存当前会签信息
    private SubtaskEntity getSubtaskEntity(String subtaskById, SubtaskEntity subtaskEntityArgs, UserEntity userEntity, SimpleDateFormat df, SubtaskEntity subtaskEntity) {
        SubtaskAuditEntity subtaskAuditEntity = new SubtaskAuditEntity();
        saveIllustration(subtaskById, subtaskEntityArgs, userEntity, df, subtaskEntity, subtaskAuditEntity);
        subtaskAuditEntity.setAssessState(2);
        subtaskAuditRepository.save(subtaskAuditEntity);
        return subtaskRepository.save(subtaskEntity);
    }

    private void saveIllustration(String subtaskById, SubtaskEntity subtaskEntityArgs, UserEntity userEntity, SimpleDateFormat df, SubtaskEntity subtaskEntity, SubtaskAuditEntity subtaskAuditEntity) {
        subtaskAuditEntity.setUserEntity(userEntity);
        subtaskAuditEntity.setIllustration(subtaskEntityArgs.getIllustration());
        subtaskAuditEntity.setAuditTime(df.format(new Date()));
        subtaskAuditEntity.setPass(subtaskEntity.isPass());
        subtaskAuditEntity.setSubtaskEntity(getSubtaskById(subtaskById));
    }*/

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
        subtaskEntity.setState(ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY);
        return subtaskRepository.save(subtaskEntity);
    }

    // 项目负责人查询所有待审核的二次修改申请
    public List<SubtaskEntity> findByState(String userId){
        UserEntity userEntity = userService.getUserById(userId);
        return subtaskRepository.findByUserEntityAndState(userEntity, ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY);
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
}

package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.AllIllustrationEntity;
import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
import com.rengu.cosimulation.entity.UserEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.AllIllustrationRepository;
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
    private final AllIllustrationRepository allIllustrationRepository;


    @Autowired
    public SubtaskService(SubtaskRepository subtaskRepository, ProjectService projectService, UserService userService, ProcessNodeRepository processNodeRepository, AllIllustrationRepository allIllustrationRepository) {
        this.subtaskRepository = subtaskRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.processNodeRepository = processNodeRepository;
        this.allIllustrationRepository = allIllustrationRepository;
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
    public SubtaskEntity updateSubtaskById(String projectId, String subtaskById, String userId, String finishTime) {
        if (!projectService.hasProjectById(projectId)) {
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        if (!userService.hasUserById(userId)) {
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = userService.getUserById(userId);
        if (userEntity.getSecretClass() < projectEntity.getSecretClass()) {
            throw new ResultException(ResultCode.USER_SECRETCLASS_NOT_SUPPORT_ERROR);
        }
        if (!hasSubtaskById(subtaskById)) {
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskById);
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

    // 根据子任务id为子任务添加审核员
//    public SubtaskEntity arrangeAssessorsById(String subtaskId, String userId, String[] userIds) {
//        if(!userService.hasUserById(userId)){
//            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
//        }
//        UserEntity userEntity = userService.getUserById(userId);
//        if(!hasSubtaskById(subtaskId)){
//            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
//        }
//        SubtaskEntity subtaskEntity = getSubtaskById(subtaskId);
//        if(!userEntity.getId().equals(subtaskEntity.getUserEntity().getId())){
//            throw new ResultException(ResultCode.SUBTASK_USER_ARRANGE_AUTHORITY_DENIED_ERROR);
//        }
//        if(userIds.length == 0){
//            throw new ResultException(ResultCode.SUBTASK_ASSESSORS_NOT_FOUND_ERROR);
//        }
//
//        List<UserEntity> userEntityList = new ArrayList<>();
//        for (String id : userIds) {
//            userEntityList.add(userService.getUserById(id));
//        }
//        HashSet<UserEntity> userEntityHashSet = new HashSet<>(userEntityList);
//
//        subtaskEntity.setAssessorSet(userEntityHashSet);
//        return subtaskRepository.save(subtaskEntity);
//    }

    // 根据审核人id查询待其审核的子任务
//    public List<SubtaskEntity> findSubtasksByAssessor(UserEntity userEntity){
//        return subtaskRepository.findByAssessorSetContaining(userEntity);
//    }

    // 根据子任务id查询其后续任务
    /*public List<SubtaskEntity> findNextSubtasksById(String subtaskId){
        if(!hasSubtaskById(subtaskId)){
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskId);
        // 查询以此节点为父节点的节点
        String sign = subtaskEntity.getProcessNodeEntity().getSelfSign();
        List<ProcessNodeEntity> processNodeEntityList = processNodeRepository.findByParentSign(sign);

        List<SubtaskEntity> subtaskEntityList = new ArrayList<>();
        for(ProcessNodeEntity processNodeEntity : processNodeEntityList){
            subtaskEntityList.add(subtaskRepository.findByProcessNodeEntity(processNodeEntity));
        }

        return subtaskEntityList;
    }*/

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
    // 根据子任务id为子任务添加审核员
    public SubtaskEntity arrangeAssessorsByIds(String subtaskId, String userId, int countersignState, String[] collatorIds, String[] auditIds, String[] countersignIds, String[] approverIds) {
        if (!userService.hasUserById(userId)) {
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = userService.getUserById(userId);
        if (!hasSubtaskById(subtaskId)) {
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskId);
        if (!userEntity.getId().equals(subtaskEntity.getUserEntity().getId())) {
            throw new ResultException(ResultCode.SUBTASK_USER_ARRANGE_AUTHORITY_DENIED_ERROR);
        }
        if (ArrayUtils.isEmpty(collatorIds)) {
            throw new ResultException(ResultCode.PRODESIGN_LINK_CHECK_NOT_FOUND_ERROR);
        }
        if (ArrayUtils.isEmpty(auditIds)) {
            throw new ResultException(ResultCode.SUBTASK_USER_ARRANGE_AUTHORITY_DENIED_ERROR);
        }
        if (ArrayUtils.isEmpty(approverIds)) {
            throw new ResultException(ResultCode.PRODESIGN_LINK_APPROVER_NOT_FOUND_ERROR);
        }
        //  储存核对人信息
        subtaskEntity.setCollatorSet(saveAssessInfo(collatorIds));
        subtaskRepository.save(subtaskEntity);
        //  储存审核人信息
        subtaskEntity.setAuditorSet(saveAssessInfo(auditIds));
        subtaskRepository.save(subtaskEntity);
        //  储存会签人信息
        subtaskEntity.setCountersignState(countersignState);
        if (subtaskEntity.getCountersignState() == 3 || subtaskEntity.getCountersignState() == 2) {
            if (subtaskEntity.getCountersignState() == 3) {
                subtaskEntity.setManyCountersignState(1);
            }
            subtaskEntity.setCountersignSet(saveAssessInfo(countersignIds));
            subtaskRepository.save(subtaskEntity);
        }

        //  储存批准人信息

        subtaskEntity.setApporverSet(saveAssessInfo(approverIds));
        return subtaskRepository.save(subtaskEntity);
    }

    private Set<UserEntity> saveAssessInfo(String[] assessIds) {
        List<UserEntity> assessIdList = new ArrayList<>();
        for (String countersignId : assessIds) {
            assessIdList.add(userService.getUserById(countersignId));
        }
        HashSet<UserEntity> assessIdSet = new HashSet<>(assessIdList);
        return assessIdSet;
    }

    // 根据审核人id查询待其核对的子任务
    public Map<String, Object> findSubtasksByAllAssessor(UserEntity userEntity) {
        List<SubtaskEntity> collatorSubtaskEntityList = subtaskRepository.findByCollatorSetContaining(userEntity);
        List<SubtaskEntity> auditorSubtaskEntityList = subtaskRepository.findByAuditorSetContaining(userEntity);
        List<SubtaskEntity> countersignSubtaskEntityList = subtaskRepository.findByCountersignSetContaining(userEntity);
        List<SubtaskEntity> apporverSubtaskEntityList = subtaskRepository.findByApporverSetContaining(userEntity);
        List<AllIllustrationEntity> allIllustrationEntityList = allIllustrationRepository.findByUserEntity(userEntity);
        Map<String, Object> map = new HashMap<>();
        map.put("collatorList", collatorSubtaskEntityList);
        map.put("auditorList", auditorSubtaskEntityList);
        map.put("countersignList", countersignSubtaskEntityList);
        map.put("apporverList", apporverSubtaskEntityList);
        map.put("userEntityList", allIllustrationEntityList);
        return map;
    }

    //  根据子任务审核id审核任务

    /**
     * 子任务文件流程控制：
     * 当前审核结果： pass --> 当前审核人：自己--> 报错，无通过权限
     * 其他人--> 设置子任务文件进入下一模式：
     * 当前阶段为审核时-->  1)无会签：审核-->批准
     * 2)一人/多人会签：审核-->会签
     * 当前阶段为会签时-->  1)一人会签：会签-->批准（同上）
     * *                                                           2)多人会签：多人审核通过审核-->批准
     * 若当前用户已会签过则报错，您已会签过
     * 当前阶段为批准时-->  修改子文件的通过状态为true
     * no   --> 停留当前模式   （--> 子库文件状态改为进行中）
     */
    public SubtaskEntity assessSubtaskByIds(String subtaskById, SubtaskEntity subtaskEntityArgs, UserEntity userEntity) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!hasSubtaskById(subtaskById)) {
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskById);
        List<AllIllustrationEntity> allIllustrationList = allIllustrationRepository.findBySubtaskEntity(getSubtaskById(subtaskById));
        int maxState = 0;
        int a = 0;
        for (AllIllustrationEntity allIllustrationEntitys : allIllustrationList) {
            if (subtaskById.equals(allIllustrationEntitys.getSubtaskEntity().getId())) {
                if (maxState < allIllustrationEntitys.getAssessState()) {
                    maxState = allIllustrationEntitys.getAssessState();
                }
                if (allIllustrationEntitys.getAssessState() == 2 && allIllustrationEntitys.isPass()) {
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
        for (AllIllustrationEntity allIllustrationEntity : allIllustrationList) {
            if (!CollectionUtils.isEmpty(allIllustrationList)) {
                //  判断是否重复评估
                if (subtaskById.equals(allIllustrationEntity.getSubtaskEntity().getId())) {
                    if (subtaskEntityArgs.getState() - allIllustrationEntity.getAssessState() == 2 && subtaskEntityArgs.getState() != 4) {
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
            List<AllIllustrationEntity> allIllustrationLists = allIllustrationRepository.findByUserEntity(userEntity);
            if (!CollectionUtils.isEmpty(allIllustrationLists)) {
                for (AllIllustrationEntity allIllustrationEntityS : allIllustrationLists) {
                    if (allIllustrationEntityS.getAssessState() == 2 && subtaskEntityArgs.getState() == ApplicationConfig.COUNTERSIGN_ING && allIllustrationEntityS.getSubtaskEntity().getId() == subtaskById) {
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
        AllIllustrationEntity allIllustrationEntity = new AllIllustrationEntity();
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
            allIllustrationEntity.setAssessState(0);
        } else if (subtaskEntityArgs.getState() == ApplicationConfig.AUDITOR_ING) {
            allIllustrationEntity.setAssessState(1);
        } else if (subtaskEntityArgs.getState() == ApplicationConfig.COUNTERSIGN_ING) {
            allIllustrationEntity.setAssessState(2);
        } else if (subtaskEntityArgs.getState() == ApplicationConfig.APPROVER_ING) {
            allIllustrationEntity.setAssessState(3);
        }
        saveIllustration(subtaskById, subtaskEntityArgs, userEntity, df, subtaskEntity, allIllustrationEntity);
        subtaskEntity.setIllustration(subtaskEntityArgs.getIllustration());
        allIllustrationRepository.save(allIllustrationEntity);
        return subtaskRepository.save(subtaskEntity);
    }

    //  多人会签中保存当前会签信息
    private SubtaskEntity getSubtaskEntity(String subtaskById, SubtaskEntity subtaskEntityArgs, UserEntity userEntity, SimpleDateFormat df, SubtaskEntity subtaskEntity) {
        AllIllustrationEntity allIllustrationEntity = new AllIllustrationEntity();
        saveIllustration(subtaskById, subtaskEntityArgs, userEntity, df, subtaskEntity, allIllustrationEntity);
        allIllustrationEntity.setAssessState(2);
        allIllustrationRepository.save(allIllustrationEntity);
        return subtaskRepository.save(subtaskEntity);
    }

    private void saveIllustration(String subtaskById, SubtaskEntity subtaskEntityArgs, UserEntity userEntity, SimpleDateFormat df, SubtaskEntity subtaskEntity, AllIllustrationEntity allIllustrationEntity) {
        allIllustrationEntity.setUserEntity(userEntity);
        allIllustrationEntity.setIllustration(subtaskEntityArgs.getIllustration());
        allIllustrationEntity.setAuditTime(df.format(new Date()));
        allIllustrationEntity.setPass(subtaskEntity.isPass());
        allIllustrationEntity.setSubtaskEntity(getSubtaskById(subtaskById));
    }

    //  根据子任务Id查询所有审核信息
    public List<AllIllustrationEntity> allIllustrationBysubtaskId(SubtaskEntity subtaskEntity) {
        return allIllustrationRepository.findBySubtaskEntity(subtaskEntity);
    }

    //  根据子任务ID查询单个审核信息
    public SubtaskEntity illustrationByAssessStateIds(String assessStateId) {
        Optional<SubtaskEntity> subtaskEntityOptional = subtaskRepository.findById(assessStateId);
        if (!subtaskEntityOptional.isPresent()) {
            throw new ResultException(ResultCode.SUBTASK_ARGS_NOT_FOUND_ERROR);
        }
        return subtaskEntityOptional.get();
    }
}

package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.ProcessNodeRepository;
import com.rengu.cosimulation.repository.SubtaskRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.xml.transform.Result;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

    @Autowired
    public SubtaskService(SubtaskRepository subtaskRepository, ProjectService projectService, UserService userService, ProcessNodeRepository processNodeRepository) {
        this.subtaskRepository = subtaskRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.processNodeRepository = processNodeRepository;
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
        if(!hasSubtaskById(subtaskById)){
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        return subtaskRepository.findById(subtaskById).get();
    }

    // 根据id修改子任务-->子任务负责人密级大于等于项目密级
    public SubtaskEntity updateSubtaskById(String projectId, String subtaskById, String userId, String finishTime){
        if(!projectService.hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        if(!userService.hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = userService.getUserById(userId);
        if(userEntity.getSecretClass() < projectEntity.getSecretClass()){
            throw new ResultException(ResultCode.USER_SECRETCLASS_NOT_SUPPORT_ERROR);
        }
        if(!hasSubtaskById(subtaskById)){
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskById);
        subtaskEntity.setUserEntity(userEntity);
        if(!StringUtils.isEmpty(finishTime)){
            subtaskEntity.setFinishTime(finishTime);
        }

        return subtaskRepository.save(subtaskEntity);
    }

    // 删除子任务
    public SubtaskEntity deleteSubtaskById(String subtaskId){
        if(!hasSubtaskById(subtaskId)){
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskId);
        subtaskRepository.delete(subtaskEntity);
        return subtaskEntity;
    }

    // 根据子任务id为子任务添加审核员
    public SubtaskEntity arrangeAssessorsById(String subtaskId, String userId, String[] userIds) {
        if(!userService.hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = userService.getUserById(userId);
        if(!hasSubtaskById(subtaskId)){
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subtaskEntity = getSubtaskById(subtaskId);
        if(!userEntity.getId().equals(subtaskEntity.getUserEntity().getId())){
            throw new ResultException(ResultCode.SUBTASK_USER_ARRANGE_AUTHORITY_DENIED_ERROR);
        }
        if(userIds.length == 0){
            throw new ResultException(ResultCode.SUBTASK_ASSESSORS_NOT_FOUND_ERROR);
        }

        List<UserEntity> userEntityList = new ArrayList<>();
        for (String id : userIds) {
            userEntityList.add(userService.getUserById(id));
        }
        HashSet<UserEntity> userEntityHashSet = new HashSet<>(userEntityList);

        subtaskEntity.setAssessorSet(userEntityHashSet);
        return subtaskRepository.save(subtaskEntity);
    }

    // 根据审核人id查询待其审核的子任务
    public List<SubtaskEntity> findSubtasksByAssessor(UserEntity userEntity){
        return subtaskRepository.findByAssessorSetContaining(userEntity);
    }

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
}

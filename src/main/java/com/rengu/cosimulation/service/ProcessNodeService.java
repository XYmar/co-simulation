package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.ProcessNodeEntity;
import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.ProcessNodeRepository;
import com.rengu.cosimulation.repository.ProjectRepository;
import com.rengu.cosimulation.repository.SubtaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: XYmar
 * Date: 2019/3/7 14:52
 */

@Slf4j
@Service
@Transactional
public class ProcessNodeService {
    private final ProcessNodeRepository processNodeRepository;
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final SubtaskRepository subtaskRepository;

    @Autowired
    public ProcessNodeService(ProcessNodeRepository processNodeRepository, ProjectService projectService, ProjectRepository projectRepository, SubtaskRepository subtaskRepository) {
        this.processNodeRepository = processNodeRepository;
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.subtaskRepository = subtaskRepository;
    }

    // 根据项目id保存项目流程节点信息,并设置对应的子任务
    /**
     * 第一次保存： 直接保存
     * 有子任务id的节点: 判断节点内容是否有变化，在节点信息中保留，其他节点删除；
     *                    添加无子任务id的节点。
     * */
    public List<ProcessNodeEntity> saveProcessNodes(String projectId, ProcessNodeEntity[] processNodeEntities) {
        if (!projectService.hasProjectById(projectId)) {
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        /*for(){

        }*/
        // 根据项目删除子任务和流程节点信息
        if(processNodeRepository.findByProjectEntity(projectEntity).size() > 0){
            processNodeRepository.deleteAllByProjectEntity(projectEntity);
            //subtaskRepository.deleteAllByProjectEntity(projectEntity);
        }

        Map<String,ProcessNodeEntity> processNodeEntityMap = new HashMap<>();
        Map<String, SubtaskEntity> subtaskEntityMap = new HashMap<>();
        for (ProcessNodeEntity processNodeEntity:processNodeEntities){
            processNodeEntity.setProjectEntity(projectEntity);
            if (!processNodeEntityMap.containsKey(processNodeEntity.getSelfSign())){
                SubtaskEntity subtaskEntity = new SubtaskEntity();
                subtaskEntity.setName(processNodeEntity.getNodeName());
                subtaskEntity.setProjectEntity(projectEntity);
                subtaskEntity.setManyCounterSignState(0);                            // 多人会签模式，此时无人开始会签
                subtaskEntityMap.put(processNodeEntity.getSelfSign(),subtaskEntity);
                processNodeEntity.setSubtaskEntity(subtaskEntity);
                processNodeEntityMap.put(processNodeEntity.getSelfSign(),processNodeEntity);
            }else {
                processNodeEntity.setSubtaskEntity(subtaskEntityMap.get(processNodeEntity.getSelfSign()));
            }
        }

        return processNodeRepository.saveAll(Arrays.asList(processNodeEntities));
/*        if (!projectService.hasProjectById(projectId)) {
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        // 根据项目的清空所有流程节点包括子任务
        subtaskRepository.deleteAllByProjectEntity(projectEntity);
        processNodeRepository.deleteAllByProjectEntity(projectEntity);
        if (processNodeEntities.length <= 0) {
            throw new ResultException(ResultCode.PROCESS_ARGS_NOT_FOUND_ERROR);
        }
        List<ProcessNodeEntity> processNodeEntityList = new ArrayList<>();

        for (ProcessNodeEntity processNodeEntity : processNodeEntities) {         // 保存流程节点
            processNodeEntity.setProjectEntity(projectEntity);
            processNodeEntityList.add(processNodeEntity);
        }
        processNodeRepository.saveAll(processNodeEntityList);

        List<ProcessNodeEntity> processSubtaskList = new ArrayList<>();         // 存放流程节点，sign重复的只保存一个
        List<String> signList = new ArrayList<>();                              // 标识节点
        List<SubtaskEntity> subtaskEntityList = new ArrayList<>();
        for (ProcessNodeEntity processNodeEntity : processNodeEntities) {
            // 标识去重并存储
            if (!signList.contains(processNodeEntity.getSelfSign())) {
                signList.add(processNodeEntity.getSelfSign());
            }
        }
        for (String selfSign : signList) {
            List<ProcessNodeEntity> pro2 = processNodeRepository.findBySelfSignAndProjectEntity(selfSign, projectEntity);
            processSubtaskList.add(pro2.get(0));       // 取第一个
        }

        for(ProcessNodeEntity processNodeEntity : processSubtaskList){
            // 根据项目流程设置默认子任务（名称、项目）
            SubtaskEntity subtaskEntity = new SubtaskEntity();
            subtaskEntity.setName(processNodeEntity.getNodeName());
            subtaskEntity.setProjectEntity(processNodeEntity.getProjectEntity());
            subtaskEntity.setProcessNodeEntity(processNodeEntity);               // 设置子任务对应节点
            subtaskEntityList.add(subtaskEntity);
        }
        subtaskRepository.saveAll(subtaskEntityList);

        return processNodeRepository.findAll();*/
    }

    // 根据项目查询流程是否存在
    public boolean hasProcessNodeByProject(String projectId){
       if(StringUtils.isEmpty(projectId)){
           throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
       }
       return processNodeRepository.existsByProjectEntity(projectService.getProjectById(projectId));
    }

    // 根据项目返回流程节点信息
    public List<ProcessNodeEntity> getProcessNodesByProjectId(String projectId) {
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        return processNodeRepository.findByProjectEntity(projectEntity);
    }
}

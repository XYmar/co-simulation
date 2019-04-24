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

import java.util.*;

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
     * 项目流程是否存在：
     *     不存在 -->  第一次保存： 直接保存
     *     存在   -->  获取项目流程节点
     *                 有子任务id的节点: 节点基本内容修改： 名称、大小、location、fromPort、toPort
     *                                   删除项目中本来剩下的节点
     *                 无子任务id的节点: 直接保存。
     * */
    public List<ProcessNodeEntity> saveProcessNodes(String projectId, ProcessNodeEntity[] processNodeEntities) {
        if (!projectService.hasProjectById(projectId)) {
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        List<ProcessNodeEntity> processNodeEntityList = new ArrayList<>();           // 待保存的所有节点
        if(hasProcessNodeByProject(projectId)){              // 非第一次保存
            List<ProcessNodeEntity> oldProcessNodeEntityList = getProcessNodesByProjectId(projectId);    // 原项目流程节点

            List<ProcessNodeEntity> toBeChangedNode = new ArrayList<>();            // 待修改节点的信息
            List<ProcessNodeEntity> toBeChangedProcessNode = new ArrayList<>();            // 待修改的原节点
            List<ProcessNodeEntity> toBeAddedNode = new ArrayList<>();              // 待新增节点
            List<ProcessNodeEntity> toBeDeletedNode = new ArrayList<>();              // 待删除原节点
            for(ProcessNodeEntity processNodeEntity : processNodeEntities){
                if(StringUtils.isEmpty(processNodeEntity.getSubtaskId())){
                    toBeAddedNode.add(processNodeEntity);
                }else{
                    toBeChangedNode.add(processNodeEntity);
                }
            }
            List<ProcessNodeEntity> list = new ArrayList<>();

            if(toBeChangedNode.size() != 0){
                for(ProcessNodeEntity processNode : toBeChangedNode){
                    ProcessNodeEntity processNodeEntity = getByProjectEntityAndSelfSignAndParentSign(projectEntity, processNode.getSelfSign(), processNode.getParentSign());
                    toBeChangedProcessNode.add(processNodeEntity);
                    processNodeEntity.setNodeName(processNode.getNodeName());
                    processNodeEntity.setNodeSize(processNode.getNodeSize());
                    processNodeEntity.setLocation(processNode.getLocation());
                    processNodeEntity.setFromPort(processNode.getFromPort());
                    processNodeEntity.setToPort(processNode.getToPort());
                    processNodeEntity.setSubtaskEntity(processNodeEntity.getSubtaskEntity());
                    list.add(processNodeEntity);
                }
            }
            oldProcessNodeEntityList.removeAll(toBeChangedProcessNode);
            toBeDeletedNode = oldProcessNodeEntityList;

            processNodeRepository.deleteAll(toBeDeletedNode);
            processNodeRepository.saveAll(list);
            ProcessNodeEntity[] toBeAddedNodeArr = toBeAddedNode.toArray(new ProcessNodeEntity[0]);
            processNodeEntityList.addAll(processNodeEntitysToList(projectId, toBeAddedNodeArr));
        }else{             // 第一次提交
            processNodeEntityList = processNodeEntitysToList(projectId, processNodeEntities);
        }

        processNodeRepository.saveAll(processNodeEntityList);

        return getProcessNodesByProjectId(projectId);
    }

    // 返回待保存的节点
    public List<ProcessNodeEntity> processNodeEntitysToList(String projectId, ProcessNodeEntity[] processNodeEntities) {
        if (!projectService.hasProjectById(projectId)) {
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        // 根据项目删除子任务和流程节点信息
        /*if(processNodeRepository.findByProjectEntity(projectEntity).size() > 0){
            processNodeRepository.deleteAllByProjectEntity(projectEntity);
        }*/
        Map<String, ProcessNodeEntity> processNodeEntityMap = new HashMap<>();
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

        return Arrays.asList(processNodeEntities);
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

    // 根据项目和selfSign以及parentSign查询流程节点
    public ProcessNodeEntity getByProjectEntityAndSelfSignAndParentSign(ProjectEntity projectEntity, String selfSign, String parentSign){
        return processNodeRepository.findByProjectEntityAndSelfSignAndParentSign(projectEntity, selfSign, parentSign);
    }
}

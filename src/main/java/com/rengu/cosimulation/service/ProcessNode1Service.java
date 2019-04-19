package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.ProcessNodeEntity1;
import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.ProcessNodeRepository1;
import com.rengu.cosimulation.repository.SubtaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/4/17 19:00
 */
@Service
public class ProcessNode1Service {
    private final ProcessNodeRepository1 processNodeRepository1;
    private final ProjectService projectService;
    private final SubtaskService subtaskService;
    private final SubtaskRepository subtaskRepository;

    @Autowired
    public ProcessNode1Service(ProcessNodeRepository1 processNodeRepository1, ProjectService projectService, SubtaskService subtaskService, SubtaskRepository subtaskRepository) {
        this.processNodeRepository1 = processNodeRepository1;
        this.projectService = projectService;
        this.subtaskService = subtaskService;
        this.subtaskRepository = subtaskRepository;
    }

    // 保存流程节点
    // 根据项目id保存项目流程节点信息,并设置对应的子任务
    /**
     * 节点id是否存在：
     *     不存在 -->  add
     *     存在   -->  修改  名称、大小、location、subtask
     *                 删除  删除项目中本来剩下的节点
     * */
    public List<ProcessNodeEntity1> saveProcessNodes(String projectId, ProcessNodeEntity1[] processNodeEntity1s) {
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        List<ProcessNodeEntity1> processNodeEntity1List = new ArrayList<>();
        List<ProcessNodeEntity1> oldProcessNodeEntity1List = processNodeRepository1.findByProjectEntity(projectEntity);        // 原流程节点
        List<ProcessNodeEntity1> stayProcessNodeEntity1List = new ArrayList<>();                  // 待保留流程节点

        for(ProcessNodeEntity1 processNodeEntity1 : processNodeEntity1s){
            ProcessNodeEntity1 processNodeEntity;
            SubtaskEntity subtaskEntity;
            if(!StringUtils.isEmpty(processNodeEntity1.getId())){                // 节点id存在
                processNodeEntity = getProcessNodeById(processNodeEntity1.getId());
                subtaskEntity = processNodeEntity.getSubtaskEntity();
                subtaskEntity.setName(processNodeEntity1.getNodeName());
                stayProcessNodeEntity1List.add(processNodeEntity);
            }else {            // 第一次保存
                processNodeEntity = new ProcessNodeEntity1();

                processNodeEntity.setSelfSign(processNodeEntity1.getSelfSign());
                processNodeEntity.setFigure(processNodeEntity1.getFigure());
                processNodeEntity.setProjectEntity(projectEntity);

                subtaskEntity = new SubtaskEntity();
                subtaskEntity.setName(processNodeEntity1.getNodeName());
                subtaskEntity.setProjectEntity(projectEntity);
                subtaskEntity.setManyCounterSignState(0);                            // 多人会签模式，此时无人开始会签
            }
            processNodeEntity.setNodeName(processNodeEntity1.getNodeName());
            processNodeEntity.setNodeSize(processNodeEntity1.getNodeSize());
            processNodeEntity.setLocation(processNodeEntity1.getLocation());

            processNodeEntity.setSubtaskEntity(subtaskEntity);
            processNodeEntity1List.add(processNodeEntity);
        }
        oldProcessNodeEntity1List.removeAll(stayProcessNodeEntity1List);
        List<SubtaskEntity> subtaskEntityList = new ArrayList<>();
        for (ProcessNodeEntity1 processNodeEntity1 : oldProcessNodeEntity1List) {
            subtaskEntityList.add(processNodeEntity1.getSubtaskEntity());
        }
        processNodeRepository1.deleteInBatch(oldProcessNodeEntity1List);
        subtaskRepository.deleteInBatch(subtaskEntityList);
        return processNodeRepository1.saveAll(processNodeEntity1List);
    }

    // 根据项目返回流程节点信息
    public List<ProcessNodeEntity1> getProcessNodesByProjectId(String projectId) {
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        return processNodeRepository1.findByProjectEntity(projectEntity);
    }

    // 根据Id查询节点是否存在
    public boolean hasUserById(String processNodeId) {
        if (StringUtils.isEmpty(processNodeId)) {
            return false;
        }
        return processNodeRepository1.existsById(processNodeId);
    }

    // 根据id查询节点
    @Cacheable(value = "ProcessNode1_Cache", key = "#processNodeId")
    public ProcessNodeEntity1 getProcessNodeById(String processNodeId) {
        if(!hasUserById(processNodeId)){
            throw new ResultException(ResultCode.PROCESS_NODE_ID_NOT_FOUND_ERROR);
        }
        return processNodeRepository1.findById(processNodeId).get();
    }

}

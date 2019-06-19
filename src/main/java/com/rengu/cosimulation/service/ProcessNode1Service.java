package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.ProcessNode;
import com.rengu.cosimulation.entity.Project;
import com.rengu.cosimulation.entity.Subtask;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.ProcessNodeRepository1;
import com.rengu.cosimulation.repository.SubtaskRepository;
import com.rengu.cosimulation.utils.ResultUtils;
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
    public List<ProcessNode> saveProcessNodes(String projectId, ProcessNode[] processNodes) {
        Project project = projectService.getProjectById(projectId);
        List<ProcessNode> processNodeList = new ArrayList<>();
        List<ProcessNode> oldProcessNodeList = processNodeRepository1.findByProject(project);        // 原流程节点
        List<ProcessNode> stayProcessNodeList = new ArrayList<>();                  // 待保留流程节点

        for(ProcessNode processNode : processNodes){
            ProcessNode processNodeEntity;
            Subtask subtask;
            if(!StringUtils.isEmpty(processNode.getId())){                // 节点id存在
                processNodeEntity = getProcessNodeById(processNode.getId());
                subtask = processNodeEntity.getSubtask();
                subtask.setName(processNode.getNodeName());
                stayProcessNodeList.add(processNodeEntity);
            }else {            // 第一次保存
                processNodeEntity = new ProcessNode();

                processNodeEntity.setSelfSign(processNode.getSelfSign());
                processNodeEntity.setFigure(processNode.getFigure());
                processNodeEntity.setProject(project);

                subtask = new Subtask();
                subtask.setName(processNode.getNodeName());
                subtask.setProject(project);
                subtask.setManyCounterSignState(0);                            // 多人会签模式，此时无人开始会签

                // 若此节点的父节点已经完成，则设置此子任务为正在进行中
                /*if(){

                }*/
            }
            processNodeEntity.setNodeName(processNode.getNodeName());
            processNodeEntity.setNodeSize(processNode.getNodeSize());
            processNodeEntity.setLocation(processNode.getLocation());

            processNodeEntity.setSubtask(subtask);
            processNodeList.add(processNodeEntity);
        }
        oldProcessNodeList.removeAll(stayProcessNodeList);
        List<Subtask> subtaskList = new ArrayList<>();
        for (ProcessNode processNode : oldProcessNodeList) {
            subtaskList.add(processNode.getSubtask());
        }
        processNodeRepository1.deleteInBatch(oldProcessNodeList);
        subtaskRepository.deleteInBatch(subtaskList);
        return processNodeRepository1.saveAll(processNodeList);
    }

    // 根据项目返回流程节点信息
    public List<ProcessNode> getProcessNodesByProjectId(String projectId) {
        Project project = projectService.getProjectById(projectId);
        return processNodeRepository1.findByProject(project);
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
    public ProcessNode getProcessNodeById(String processNodeId) {
        if(!hasUserById(processNodeId)){
            throw new ResultException(ResultCode.PROCESS_NODE_ID_NOT_FOUND_ERROR);
        }
        return processNodeRepository1.findById(processNodeId).get();
    }

    // 项目是否已经包含项目流程
    public boolean ifHasProcessNode(String projectId){
        Project project = projectService.getProjectById(projectId);
        List<ProcessNode> processNodeList = processNodeRepository1.findByProject(project);

        return (processNodeList.size() > 0);
    }

}

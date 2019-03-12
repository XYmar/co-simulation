package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.ProcessNodeEntity;
import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.ProcessNodeRepository;
import com.rengu.cosimulation.repository.ProjectRepository;
import com.rengu.cosimulation.repository.SubtaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/3/7 14:52
 */
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
    public List<ProcessNodeEntity> saveProcessNodes(String projectId, ProcessNodeEntity[] processNodeEntities) {
        if(!projectService.hasProjectById(projectId)){
            throw new ResultException(ResultCode.PROJECT_ID_NOT_FOUND_ERROR);
        }
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        if(processNodeEntities.length <= 0){
            throw new ResultException(ResultCode.PROCESS_ARGS_NOT_FOUND_ERROR);
        }
        List<ProcessNodeEntity> processNodeEntityList = new ArrayList<>();
        List<SubtaskEntity> subtaskEntityList = new ArrayList<>();
        for(ProcessNodeEntity processNodeEntity : processNodeEntities){
            processNodeEntity.setProjectEntity(projectEntity);
            processNodeEntityList.add(processNodeEntity);

            // 根据项目流程设置默认子任务（名称、项目）
            SubtaskEntity subtaskEntity = new SubtaskEntity();
            subtaskEntity.setName(processNodeEntity.getNodeName());
            subtaskEntity.setProjectEntity(processNodeEntity.getProjectEntity());
            subtaskEntity.setProcessNodeEntity(processNodeEntity);               // 设置子任务对应节点
            subtaskEntityList.add(subtaskEntity);
        }
        subtaskRepository.saveAll(subtaskEntityList);

        // return processNodeRepository.saveAll(Arrays.asList(processNodeEntities));
        return processNodeRepository.saveAll(processNodeEntityList);
    }

    // 根据项目id以及父节点查询项目的第一个子任务(第一个可能多个)
    public List<SubtaskEntity> findFirstSubtasks(ProjectEntity projectEntity){
        // 查看流程图上无父节点的节点
        List<ProcessNodeEntity> processNodeEntityList = processNodeRepository.findByProjectEntityAndParentSign(projectEntity, "NULL");
        // 根据节点查询子任务信息
        List<SubtaskEntity> subtaskEntityList = new ArrayList<>();
        for(ProcessNodeEntity processNodeEntity : processNodeEntityList){
            subtaskEntityList.add(subtaskRepository.findByProcessNodeEntity(processNodeEntity));
        }
        return subtaskEntityList;
    }
}

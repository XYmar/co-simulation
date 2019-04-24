package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.LinkEntity;
import com.rengu.cosimulation.entity.ProcessNodeEntity1;
import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.repository.LinkRepository;
import com.rengu.cosimulation.repository.ProcessNodeRepository1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/4/17 19:47
 */
@Service
@Transactional
public class LinkService {
    private final LinkRepository linkRepository;
    private final ProcessNode1Service processNode1Service;
    private final ProcessNodeRepository1 processNodeRepository1;
    private final ProjectService projectService;

    @Autowired
    public LinkService(LinkRepository linkRepository, ProcessNode1Service processNode1Service, ProcessNodeRepository1 processNodeRepository1, ProjectService projectService) {
        this.linkRepository = linkRepository;
        this.processNode1Service = processNode1Service;
        this.processNodeRepository1 = processNodeRepository1;
        this.projectService = projectService;
    }

    // 保存流程节点连接
    public List<ProcessNodeEntity1> saveLinks(String projectId, LinkEntity[] linkEntities){
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        List<ProcessNodeEntity1> processNodeEntity1List = processNode1Service.getProcessNodesByProjectId(projectId);      // 所有节点
        if(processNodeEntity1List.size() > 0){                // 流程节点存在时，清空节点的连接
            for(ProcessNodeEntity1 processNodeEntity1 : processNodeEntity1List){
                processNodeEntity1.setLinkEntityList(null);
            }
            processNodeRepository1.saveAll(processNodeEntity1List);
        }

        if(linkRepository.existsByProjectEntity(projectEntity)){
            linkRepository.deleteAllByProjectEntity(projectEntity);
        }
        List<LinkEntity> linkEntityList = new ArrayList<>();
        for(LinkEntity linkEntity : linkEntities){
            linkEntity.setProjectEntity(projectEntity);
            linkEntityList.add(linkEntity);
        }
        linkRepository.saveAll(linkEntityList);
        return saveRelations(projectId);
    }

    // 将连接保存到对应的节点中
    public List<ProcessNodeEntity1> saveRelations(String projectId){
        List<ProcessNodeEntity1> processNodeEntity1List = processNode1Service.getProcessNodesByProjectId(projectId);      // 所有节点
        for(ProcessNodeEntity1 processNodeEntity1 : processNodeEntity1List){
            List<LinkEntity> selfLinks = linkRepository.findBySelfId(processNodeEntity1.getId());
           // List<LinkEntity> selfLinks = linkRepository.findBySelfId("0037aeae-b50a-47f3-b917-f7f7ab81bc9b");
            if(selfLinks.size() > 0){
                processNodeEntity1.setLinkEntityList(selfLinks);
            }
        }
        return processNodeRepository1.saveAll(processNodeEntity1List);
    }
}

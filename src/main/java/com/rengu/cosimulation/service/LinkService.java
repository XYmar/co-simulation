package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.Link;
import com.rengu.cosimulation.entity.ProcessNode;
import com.rengu.cosimulation.entity.Project;
import com.rengu.cosimulation.repository.LinkRepository;
import com.rengu.cosimulation.repository.ProcessNodeRepository1;
import com.rengu.cosimulation.utils.ApplicationConfig;
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
    public List<ProcessNode> saveLinks(String projectId, Link[] linkEntities){
        Project project = projectService.getProjectById(projectId);
        List<ProcessNode> processNodeList = processNode1Service.getProcessNodesByProjectId(projectId);      // 所有节点
        if(processNodeList.size() > 0){                // 流程节点存在时，清空节点的连接
            for(ProcessNode processNode : processNodeList){
                processNode.setLinkList(null);
            }
            processNodeRepository1.saveAll(processNodeList);
        }

        if(linkRepository.existsByProject(project)){
            linkRepository.deleteAllByProject(project);
        }
        List<Link> linkList = new ArrayList<>();
        for(Link link : linkEntities){
            link.setProject(project);
            linkList.add(link);
        }
        linkRepository.saveAll(linkList);
        return saveRelations(projectId);
    }

    // 将连接保存到对应的节点中
    public List<ProcessNode> saveRelations(String projectId){
        List<ProcessNode> processNodeList = processNode1Service.getProcessNodesByProjectId(projectId);      // 所有节点
        for(ProcessNode processNode : processNodeList){
            List<Link> selfLinks = linkRepository.findBySelfId(processNode.getId());

            if(selfLinks.size() > 0){
                processNode.setLinkList(selfLinks);

                for(Link link : selfLinks){
                    if(processNodeRepository1.findById(link.getParentId()).get().getSubtask().getState() == ApplicationConfig.SUBTASK_AUDIT_OVER){
                        processNode.getSubtask().setState(ApplicationConfig.SUBTASK_START);
                        break;
                    }
                }
            }
        }
        return processNodeRepository1.saveAll(processNodeList);
    }
}

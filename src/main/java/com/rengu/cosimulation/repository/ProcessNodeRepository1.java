package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.ProcessNode;
import com.rengu.cosimulation.entity.Project;
import com.rengu.cosimulation.entity.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/4/17 18:58
 */
@Repository
public interface ProcessNodeRepository1 extends JpaRepository<ProcessNode, String> {
    boolean existsByProject(Project project);
    List<ProcessNode> findByProject(Project project);
    ProcessNode findBySubtask(Subtask subtask);
    List<ProcessNode> findByProjectAndLinkList(Project project, List link);

}

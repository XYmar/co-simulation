package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.ProcessNodeEntity1;
import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/4/17 18:58
 */
@Repository
public interface ProcessNodeRepository1 extends JpaRepository<ProcessNodeEntity1, String> {
    boolean existsByProjectEntity(ProjectEntity projectEntity);
    List<ProcessNodeEntity1> findByProjectEntity(ProjectEntity projectEntity);
    ProcessNodeEntity1 findBySubtaskEntity(SubtaskEntity subtaskEntity);
    List<ProcessNodeEntity1> findByProjectEntityAndLinkEntityList(ProjectEntity projectEntity, List linkEntity);

}

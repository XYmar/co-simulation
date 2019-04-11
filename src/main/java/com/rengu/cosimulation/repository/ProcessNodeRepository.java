package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.ProcessNodeEntity;
import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.ProjectedPayload;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessNodeRepository extends JpaRepository<ProcessNodeEntity, String> {

    List<ProcessNodeEntity> findByProjectEntityAndParentSign(ProjectEntity projectEntity, String parentSign);

    List<ProcessNodeEntity> findByProjectEntityAndSelfSign(ProjectEntity projectEntity, String selfSign);

    void deleteAllByProjectEntity(ProjectEntity projectEntity);

    List<ProcessNodeEntity> findByProjectEntity(ProjectEntity projectEntity);
    ProcessNodeEntity findBySubtaskEntity(SubtaskEntity subtaskEntity);
}

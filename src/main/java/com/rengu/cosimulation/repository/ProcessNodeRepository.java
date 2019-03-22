package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.ProcessNodeEntity;
import com.rengu.cosimulation.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.web.ProjectedPayload;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessNodeRepository extends JpaRepository<ProcessNodeEntity, String> {
    List<ProcessNodeEntity> findByProjectEntityAndParentSign(ProjectEntity projectEntity, String parentSign);
    List<ProcessNodeEntity> findByParentSign(String parentSign);
}

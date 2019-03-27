package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.ProcessNodeEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/26 10:06
 */
@Repository
public interface SubtaskRepository extends JpaRepository<SubtaskEntity, String> {
    List<SubtaskEntity> findByProjectEntity(ProjectEntity projectEntity);

    boolean existsByName(String name);
    List<SubtaskEntity> findByAssessorSetContaining(UserEntity assessor);
//    SubtaskEntity findByProcessNodeEntity(ProcessNodeEntity processNodeEntity);
    void deleteAllByProjectEntity(ProjectEntity projectEntity);

}

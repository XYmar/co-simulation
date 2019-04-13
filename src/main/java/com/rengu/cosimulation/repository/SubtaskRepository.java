package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
import com.rengu.cosimulation.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Author: XYmar
 * Date: 2019/2/26 10:06
 */
@Repository
public interface SubtaskRepository extends JpaRepository<SubtaskEntity, String> {
    List<SubtaskEntity> findByProjectEntity(ProjectEntity projectEntity);

    boolean existsByName(String name);

    List<SubtaskEntity> findByProofreadUserSetContaining(UserEntity userEntity);
    List<SubtaskEntity> findByAuditUserSetContaining(UserEntity userEntity);
    List<SubtaskEntity> findByCountersignUserSetContaining(UserEntity userEntity);
    List<SubtaskEntity> findByApproveUserSet(UserEntity userEntity);
    List<SubtaskEntity> findByUserEntityAndState(UserEntity userEntity, int state);

    List<SubtaskEntity> findById(UserEntity userEntity);

    Optional<SubtaskEntity> findById(String assessStateId);

    void deleteAllByProjectEntity(ProjectEntity projectEntity);


}

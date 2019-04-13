package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SublibraryFilesAuditEntity;
import com.rengu.cosimulation.entity.SubtaskAuditEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
import com.rengu.cosimulation.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Author: XY
 * Date: 2019/4/11 16:21
 */
@Repository
public interface SubtaskAuditRepository extends JpaRepository<SubtaskAuditEntity, String> {
    boolean existsBySubtaskEntityAndUserEntityAndStateAndIfOver(SubtaskEntity subtaskEntity, UserEntity userEntity, int state, boolean ifOver);
    List<SubtaskAuditEntity> findBySubtaskEntityAndCreateTimeAfter(SubtaskEntity subtaskEntity, Date sublibraryDate);
    List<SubtaskAuditEntity> findBySubtaskEntity(SubtaskEntity subtaskEntity);
    List<SubtaskAuditEntity> findByUserEntityAndState(UserEntity userEntity, int state);
    List<SubtaskAuditEntity> findByUserEntity(UserEntity userEntity);
}

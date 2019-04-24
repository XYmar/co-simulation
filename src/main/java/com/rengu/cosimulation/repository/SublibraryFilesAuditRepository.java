package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SublibraryFilesAuditEntity;
import com.rengu.cosimulation.entity.SublibraryFilesEntity;
import com.rengu.cosimulation.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SublibraryFilesAuditRepository extends JpaRepository<SublibraryFilesAuditEntity, String> {
    boolean existsBySublibraryFilesEntityAndUserEntityAndStateAndIfOver(SublibraryFilesEntity sublibraryFilesEntity, UserEntity userEntity, int state, boolean ifOver);
    List<SublibraryFilesAuditEntity> findBySublibraryFilesEntityAndCreateTimeAfter(SublibraryFilesEntity sublibraryFilesEntity, Date sublibraryDate);
    List<SublibraryFilesAuditEntity> findByUserEntityAndState(UserEntity userEntity, int state);
    List<SublibraryFilesAuditEntity> findBySublibraryFilesEntity(SublibraryFilesEntity sublibraryFilesEntity);
    List<SublibraryFilesAuditEntity> findBySublibraryFilesEntityAndIfOver(SublibraryFilesEntity sublibraryFilesEntity, boolean ifOver);
}

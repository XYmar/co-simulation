package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SublibraryFilesAuditEntity;
import com.rengu.cosimulation.entity.SublibraryFilesEntity;
import com.rengu.cosimulation.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SublibraryFilesAuditRepository extends JpaRepository<SublibraryFilesAuditEntity, String> {
    boolean existsBySublibraryFilesEntityAndUserEntityContainingAndState(SublibraryFilesEntity sublibraryFilesEntity, UserEntity userEntity, int state);
}
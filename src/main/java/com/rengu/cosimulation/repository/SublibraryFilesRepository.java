package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.SublibraryEntity;
import com.rengu.cosimulation.entity.SublibraryFilesEntity;
import com.rengu.cosimulation.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SublibraryFilesRepository extends JpaRepository<SublibraryFilesEntity, String>, JpaSpecificationExecutor<ProjectEntity> {
    boolean existsByNameAndPostfixAndSublibraryEntity(String name, String extension, SublibraryEntity sublibraryEntity);

    Optional<SublibraryFilesEntity> findByNameAndPostfixAndSublibraryEntity(String name, String postfix, SublibraryEntity sublibraryEntity);

    List<SublibraryFilesEntity> findBySublibraryEntityAndIfApprove(SublibraryEntity sublibraryEntity, boolean ifApprove);

    List<SublibraryFilesEntity> findByProofreadUserSetContaining(UserEntity userEntity);
    List<SublibraryFilesEntity> findByAuditUserSetContaining(UserEntity userEntity);
    List<SublibraryFilesEntity> findByCountersignUserSetContaining(UserEntity userEntity);
    List<SublibraryFilesEntity> findByApproveUserSet(UserEntity userEntity);
    List<SublibraryFilesEntity> findByState(int state);
}

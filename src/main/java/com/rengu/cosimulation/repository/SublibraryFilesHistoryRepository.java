package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SublibraryFilesEntity;
import com.rengu.cosimulation.entity.SublibraryFilesHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SublibraryFilesHistoryRepository extends JpaRepository<SublibraryFilesHistoryEntity, String> {
    boolean existsBySublibraryEntityAndIfDirectModify(SublibraryFilesEntity sublibraryFilesEntity, boolean ifDirectModify);
    boolean existsBySublibraryEntity(SublibraryFilesEntity sublibraryFilesEntity);
    List<SublibraryFilesHistoryEntity> findBySublibraryEntityAndIfDirectModify(SublibraryFilesEntity sublibraryFilesEntity, boolean ifDirectModify);
    SublibraryFilesHistoryEntity findBySublibraryEntityAndIfDirectModifyAndVersion(SublibraryFilesEntity sublibraryFilesEntity, boolean ifDirectModify, String version);

}

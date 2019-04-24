package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SublibraryFilesEntity;
import com.rengu.cosimulation.entity.SublibraryFilesHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SublibraryFilesHistoryRepository extends JpaRepository<SublibraryFilesHistoryEntity, String> {
    boolean existsByLeastSublibraryFilesEntityAndIfDirectModify(SublibraryFilesEntity sublibraryFilesEntity, boolean ifDirectModify);
    boolean existsByLeastSublibraryFilesEntityAndIfTemp(SublibraryFilesEntity sublibraryFilesEntity, boolean ifTemp);
    boolean existsByLeastSublibraryFilesEntity(SublibraryFilesEntity sublibraryFilesEntity);
    List<SublibraryFilesHistoryEntity> findByLeastSublibraryFilesEntityAndIfDirectModify(SublibraryFilesEntity sublibraryFilesEntity, boolean ifDirectModify);
    List<SublibraryFilesHistoryEntity> findByLeastSublibraryFilesEntityAndIfTemp(SublibraryFilesEntity sublibraryFilesEntity, boolean ifTemp);
    SublibraryFilesHistoryEntity findByLeastSublibraryFilesEntityAndIfDirectModifyAndVersion(SublibraryFilesEntity sublibraryFilesEntity, boolean ifDirectModify, String version);
    SublibraryFilesHistoryEntity findByLeastSublibraryFilesEntityAndIfTempAndVersion(SublibraryFilesEntity sublibraryFilesEntity, boolean ifTemp, String version);


}

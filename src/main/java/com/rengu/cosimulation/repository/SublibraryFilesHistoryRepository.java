package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SublibraryFilesEntity;
import com.rengu.cosimulation.entity.SublibraryFilesHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SublibraryFilesHistoryRepository extends JpaRepository<SublibraryFilesHistoryEntity, String> {
    boolean existsBySublibraryEntityAndIfDirectModify(SublibraryFilesEntity sublibraryFilesEntity, boolean ifDirectModify);
    SublibraryFilesHistoryEntity findBySublibraryEntityAndIfDirectModify(SublibraryFilesEntity sublibraryFilesEntity, boolean ifDirectModify);
    SublibraryFilesHistoryEntity findBySublibraryEntityAndIfDirectModifyAndVersion(SublibraryFilesEntity sublibraryFilesEntity, boolean ifDirectModify, String version);

}

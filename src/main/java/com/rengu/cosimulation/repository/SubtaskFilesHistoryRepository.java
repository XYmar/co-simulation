package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SubtaskFilesEntity;
import com.rengu.cosimulation.entity.SubtaskFilesHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubtaskFilesHistoryRepository extends JpaRepository<SubtaskFilesHistoryEntity, String> {
    boolean existsByLeastSubtaskFilesEntityAndIfDirectModify(SubtaskFilesEntity subtaskFilesEntity, boolean ifDirectModify);
    boolean existsByLeastSubtaskFilesEntity(SubtaskFilesEntity subtaskFilesEntity);
    List<SubtaskFilesHistoryEntity> findByLeastSubtaskFilesEntityAndIfDirectModify(SubtaskFilesEntity subtaskFilesEntity, boolean ifDirectModify);
    SubtaskFilesHistoryEntity findByLeastSubtaskFilesEntityAndIfDirectModifyAndVersion(SubtaskFilesEntity subtaskFilesEntity, boolean ifDirectModify, String version);
    boolean existsByLeastSubtaskFilesEntityAndIfTemp(SubtaskFilesEntity subtaskFilesEntity, boolean ifTemp);
    List<SubtaskFilesHistoryEntity> findByLeastSubtaskFilesEntityAndIfTemp(SubtaskFilesEntity subtaskFilesEntity, boolean ifTemp);
    SubtaskFilesHistoryEntity findByLeastSubtaskFilesEntityAndIfTempAndVersion(SubtaskFilesEntity subtaskFilesEntity, boolean ifTemp, String version);


}

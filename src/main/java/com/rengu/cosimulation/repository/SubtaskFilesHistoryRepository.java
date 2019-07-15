package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SubtaskFile;
import com.rengu.cosimulation.entity.SubtaskFileHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubtaskFilesHistoryRepository extends JpaRepository<SubtaskFileHis, String> {
    boolean existsByLeastSubtaskFileAndIfDirectModify(SubtaskFile subtaskFile, boolean ifDirectModify);
    boolean existsByLeastSubtaskFile(SubtaskFile subtaskFile);
    List<SubtaskFileHis> findByLeastSubtaskFile(SubtaskFile subtaskFile);
    SubtaskFileHis findByLeastSubtaskFileAndIfDirectModifyAndVersion(SubtaskFile subtaskFile, boolean ifDirectModify, String version);
    boolean existsByLeastSubtaskFileAndIfTemp(SubtaskFile subtaskFile, boolean ifTemp);
    List<SubtaskFileHis> findByLeastSubtaskFileAndIfTemp(SubtaskFile subtaskFile, boolean ifTemp);
    SubtaskFileHis findByLeastSubtaskFileAndIfTempAndVersion(SubtaskFile subtaskFile, boolean ifTemp, String version);


}

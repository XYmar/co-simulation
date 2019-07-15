package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SubDepotFile;
import com.rengu.cosimulation.entity.SubdepotFileHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SublibraryFilesHistoryRepository extends JpaRepository<SubdepotFileHis, String> {
    boolean existsByLeastSubDepotFileAndIfDirectModify(SubDepotFile subDepotFile, boolean ifDirectModify);
    boolean existsByLeastSubDepotFileAndIfTemp(SubDepotFile subDepotFile, boolean ifTemp);
    boolean existsByLeastSubDepotFile(SubDepotFile subDepotFile);
    List<SubdepotFileHis> findByLeastSubDepotFile(SubDepotFile subDepotFile);
    List<SubdepotFileHis> findByLeastSubDepotFileAndIfTemp(SubDepotFile subDepotFile, boolean ifTemp);
    SubdepotFileHis findByLeastSubDepotFileAndIfDirectModifyAndVersion(SubDepotFile subDepotFile, boolean ifDirectModify, String version);
    SubdepotFileHis findByLeastSubDepotFileAndIfTempAndVersion(SubDepotFile subDepotFile, boolean ifTemp, String version);


}

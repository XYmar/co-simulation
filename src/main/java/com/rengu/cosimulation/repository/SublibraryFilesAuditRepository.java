package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SubDepotFile;
import com.rengu.cosimulation.entity.SubDepotFileAudit;
import com.rengu.cosimulation.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SublibraryFilesAuditRepository extends JpaRepository<SubDepotFileAudit, String> {
    boolean existsBySubDepotFileAndUsersAndStateAndIfOver(SubDepotFile subDepotFile, Users users, int state, boolean ifOver);
    List<SubDepotFileAudit> findBySubDepotFileAndCreateTimeAfter(SubDepotFile subDepotFile, Date sublibraryDate);
    List<SubDepotFileAudit> findByUsersAndStateAndIfOver(Users users, int state, boolean ifOver);
    List<SubDepotFileAudit> findBySubDepotFile(SubDepotFile subDepotFile);
    List<SubDepotFileAudit> findBySubDepotFileAndIfOver(SubDepotFile subDepotFile, boolean ifOver);
}

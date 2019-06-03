package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Project;
import com.rengu.cosimulation.entity.SubDepot;
import com.rengu.cosimulation.entity.SubDepotFile;
import com.rengu.cosimulation.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SublibraryFilesRepository extends JpaRepository<SubDepotFile, String>, JpaSpecificationExecutor<Project> {
    boolean existsByNameAndPostfixAndSubDepot(String name, String extension, SubDepot subDepot);

    Optional<SubDepotFile> findByNameAndPostfixAndSubDepot(String name, String postfix, SubDepot subDepot);

    List<SubDepotFile> findBySubDepotAndIfApprove(SubDepot subDepot, boolean ifApprove);

    List<SubDepotFile> findByProofSetContaining(Users users);
    List<SubDepotFile> findByAuditSetContaining(Users users);
    List<SubDepotFile> findByCountSetContaining(Users users);
    List<SubDepotFile> findByApproveSet(Users users);
    List<SubDepotFile> findByState(int state);
    List<SubDepotFile> findByUsersAndIfApprove(Users users, boolean ifApprove);
}

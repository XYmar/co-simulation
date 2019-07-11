package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Project;
import com.rengu.cosimulation.entity.SubDepot;
import com.rengu.cosimulation.entity.SubDepotFile;
import com.rengu.cosimulation.entity.Users;
import com.rengu.cosimulation.utils.ApplicationConfig;
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
    List<SubDepotFile> findBySubDepotAndUsersAndIfApprove(SubDepot subDepot, Users users, boolean ifApprove);
    List<SubDepotFile> findByProofSetContainingOrAuditSetContainingOrCountSetContainingOrApproveSetContaining(Users proof, Users audit, Users count, Users approve);
    List<SubDepotFile> findByUsers(Users users);
    SubDepotFile findByNameAndFileNoAndProductNoAndPostfixAndSecretClassAndSubDepotAndTypeAndUsersAndVersion(String name, String fileNo, String productNo, String postfix, int secretClass, SubDepot subDepot, String type, Users user, String version);
    // 用于上传或入库前判断该文件是否已存在
    boolean existsBySubtaskFileId(String subtaskFileId);
    SubDepotFile findBySubtaskFileId(String subtaskFileId);

    // 根据二次修改申请同意状态及申请人返回子库文件
    List<SubDepotFile> findByIfModifyApproveAndStateAndApplicantAndSubDepot(boolean ifModifyApprove, int state, Users applicant, SubDepot subDepot);
}

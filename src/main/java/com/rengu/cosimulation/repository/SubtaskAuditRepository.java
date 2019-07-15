package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Subtask;
import com.rengu.cosimulation.entity.SubtaskAudit;
import com.rengu.cosimulation.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Author: XY
 * Date: 2019/4/11 16:21
 */
@Repository
public interface SubtaskAuditRepository extends JpaRepository<SubtaskAudit, String> {
    boolean existsBySubtaskAndUsersAndStateAndIfOver(Subtask subtask, Users users, int state, boolean ifOver);
    List<SubtaskAudit> findBySubtaskAndCreateTimeAfter(Subtask subtask, Date sublibraryDate);
    List<SubtaskAudit> findBySubtask(Subtask subtask);
    List<SubtaskAudit> findByUsersAndStateAndIfOver(Users users, int state, boolean ifOver);
    List<SubtaskAudit> findByUsers(Users users);
}

package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Project;
import com.rengu.cosimulation.entity.Subtask;
import com.rengu.cosimulation.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/26 10:06
 */
@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, String> {
    List<Subtask> findByProject(Project project);

    boolean existsByName(String name);

    List<Subtask> findByProofSetContaining(Users users);
    List<Subtask> findByAuditSetContaining(Users users);
    List<Subtask> findByCountSetContaining(Users users);
    List<Subtask> findByApproveSet(Users users);
    List<Subtask> findByUsersAndState(Users users, int state);
    List<Subtask> findByProjectAndState(Project project, int state);
    void deleteAllByProject(Project project);

    List<Subtask>  findByUsers(Users users);
    List<Subtask>  findByUsersOrProofSetContainingOrAuditSetContainingOrCountSetContainingOrApproveSetContaining(Users users, Users proof, Users audit, Users count, Users approve);

}

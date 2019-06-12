package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Department;
import com.rengu.cosimulation.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Author: XYmar
 * Date: 2019/2/12 16:09
 */
@Repository
public interface UserRepository extends JpaRepository<Users, String> {
    boolean existsByUsernameAndDeleted(String username, Boolean deleted);

    Optional<Users> findByUsernameAndDeleted(String username, Boolean deleted);

    List<Users> findByDepartment(Department department);

    List<Users> findByDeleted(Boolean deleted);
}

package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/12 16:07
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByName(String name);

    boolean existsByName(String name);
}

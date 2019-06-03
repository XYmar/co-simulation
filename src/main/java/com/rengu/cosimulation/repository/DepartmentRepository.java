package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {
    boolean existsByName(String name);

    Department findByName(String name);
}

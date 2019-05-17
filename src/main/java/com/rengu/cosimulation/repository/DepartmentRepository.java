package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, String> {
    boolean existsByName(String name);

    DepartmentEntity findByName(String name);
}

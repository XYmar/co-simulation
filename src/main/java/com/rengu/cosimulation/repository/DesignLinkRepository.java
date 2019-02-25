package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.DesignLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DesignLinkRepository extends JpaRepository<DesignLinkEntity, String> {
    boolean existsByName(String name);
}

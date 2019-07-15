package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.DesignLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DesignLinkRepository extends JpaRepository<DesignLink, String> {
    boolean existsByName(String name);
}

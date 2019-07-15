package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Depot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryRepository extends JpaRepository<Depot, String> {
}

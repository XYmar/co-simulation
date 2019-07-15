package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Depot;
import com.rengu.cosimulation.entity.SubDepot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/3/28 14:25
 */
@Repository
public interface SubLibraryRepository extends JpaRepository<SubDepot, String> {
    List<SubDepot> findByDepot(Depot depot);
    boolean existsByType(String type);
}

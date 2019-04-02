package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.LibraryEntity;
import com.rengu.cosimulation.entity.SublibraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/3/28 14:25
 */
@Repository
public interface SubLibraryRepository extends JpaRepository<SublibraryEntity, String> {
    List<SublibraryEntity> findByLibraryEntity(LibraryEntity libraryEntity);
    boolean existsByType(String type);
}

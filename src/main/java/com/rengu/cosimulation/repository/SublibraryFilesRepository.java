package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SublibraryEntity;
import com.rengu.cosimulation.entity.SublibraryFilesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SublibraryFilesRepository extends JpaRepository<SublibraryFilesEntity, String> {
    boolean existsByNameAndPostfixAndSublibraryEntity(String name, String extension, SublibraryEntity sublibraryEntity);

    Optional<SublibraryFilesEntity> findByNameAndPostfixAndSublibraryEntity(String name, String postfix, SublibraryEntity sublibraryEntity);

    List<SublibraryFilesEntity> findBySublibraryEntityAndIfApprove(SublibraryEntity sublibraryEntity, boolean ifApprove);

}

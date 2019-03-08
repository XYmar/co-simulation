package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SubtaskEntity;
import com.rengu.cosimulation.entity.ProDesignLinkFilesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProDesignLinkFilesRepository extends JpaRepository<ProDesignLinkFilesEntity, String> {
    boolean existsByNameAndPostfixAndSubTaskEntity(String name, String extension, SubtaskEntity subTaskEntity);

    Optional<ProDesignLinkFilesEntity> findByNameAndPostfixAndSubTaskEntity(String name, String postfix, SubtaskEntity subTaskEntity);

    List<ProDesignLinkFilesEntity> findBySubTaskEntity(SubtaskEntity subTaskEntity);
}

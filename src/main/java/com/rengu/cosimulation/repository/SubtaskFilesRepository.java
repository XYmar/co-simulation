package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.SubtaskEntity;
import com.rengu.cosimulation.entity.SubtaskFilesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubtaskFilesRepository extends JpaRepository<SubtaskFilesEntity, String> {
    boolean existsByNameAndPostfixAndSubTaskEntity(String name, String extension, SubtaskEntity subTaskEntity);

    Optional<SubtaskFilesEntity> findByNameAndPostfixAndSubTaskEntity(String name, String postfix, SubtaskEntity subTaskEntity);

    List<SubtaskFilesEntity> findBySubTaskEntity(SubtaskEntity subTaskEntity);
}

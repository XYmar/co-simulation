package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {
    boolean existsByMD5(String md5);

    Optional<FileEntity> findByMD5(String md5);
}

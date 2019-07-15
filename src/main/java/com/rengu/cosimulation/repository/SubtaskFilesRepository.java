package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Subtask;
import com.rengu.cosimulation.entity.SubtaskFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubtaskFilesRepository extends JpaRepository<SubtaskFile, String> {
    boolean existsByNameAndPostfixAndSubtask(String name, String extension, Subtask subTask);

    Optional<SubtaskFile> findByNameAndPostfixAndSubtask(String name, String postfix, Subtask subTask);

    List<SubtaskFile> findBySubtask(Subtask subTask);

    // 查看重复文件，只可能存在一个
    SubtaskFile findByNameAndFileNoAndProductNoAndPostfixAndSecretClassAndSubtaskAndVersionAndType(String name, String fileNo, String productNo, String postfix, int secretClass, Subtask subtask, String version, String type);
}

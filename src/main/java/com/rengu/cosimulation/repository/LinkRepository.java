package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.LinkEntity;
import com.rengu.cosimulation.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinkRepository extends JpaRepository<LinkEntity, String> {
    List<LinkEntity> findBySelfId(String selfId);
    List<LinkEntity> deleteAllByProjectEntity(ProjectEntity projectEntity);
    boolean existsByProjectEntity(ProjectEntity projectEntity);
    List<LinkEntity> findByParentId(String parentId);         // 查找以此节点为父节点的所有子节点
}

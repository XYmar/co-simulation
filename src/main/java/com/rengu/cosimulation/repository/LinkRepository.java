package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Link;
import com.rengu.cosimulation.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinkRepository extends JpaRepository<Link, String> {
    List<Link> findBySelfId(String selfId);
    List<Link> deleteAllByProject(Project project);
    boolean existsByProject(Project project);
    List<Link> findByParentId(String parentId);         // 查找以此节点为父节点的所有子节点
    boolean existsByProjectAndParentId(Project project, String parentId);
}

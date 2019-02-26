package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.ProDesignLinkEntity;
import com.rengu.cosimulation.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/26 10:06
 */
@Repository
public interface ProDesignLinkRepository extends JpaRepository<ProDesignLinkEntity, String> {
    List<ProDesignLinkEntity> findByProjectEntity(ProjectEntity projectEntity);
}

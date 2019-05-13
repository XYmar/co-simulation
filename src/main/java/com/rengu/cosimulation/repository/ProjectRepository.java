package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.ProjectEntity;
import com.rengu.cosimulation.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.web.ProjectedPayload;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/20 09:33
 */
@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, String>, JpaSpecificationExecutor<ProjectEntity> {

    List<ProjectEntity> findByPicOrCreatorAndDeleted(UserEntity pic, UserEntity creator, boolean deleted);

    boolean existsByNameAndDeleted(String name, boolean deleted);

    List<ProjectEntity> findByDeleted(boolean deleted);

    List<ProjectEntity> findByPic(UserEntity pic);
}

package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Project;
import com.rengu.cosimulation.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/20 09:33
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, String>, JpaSpecificationExecutor<Project> {

    List<Project> findByPicOrCreatorAndDeleted(Users pic, Users creator, boolean deleted);

    List<Project> findByPicOrCreator(Users pic, Users creator);

    boolean existsByNameAndDeleted(String name, boolean deleted);

    List<Project> findByDeleted(boolean deleted);

    List<Project> findByPic(Users pic);
}

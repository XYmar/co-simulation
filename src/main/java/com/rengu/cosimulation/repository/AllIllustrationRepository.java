package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.AllIllustrationEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
import com.rengu.cosimulation.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: YJH
 * Date: 2019/3/29 10:24
 */
@Repository
public interface AllIllustrationRepository extends JpaRepository<AllIllustrationEntity, String> {
    List<AllIllustrationEntity> findBySubtaskEntity(SubtaskEntity subtaskEntity);

    List<AllIllustrationEntity> findByUserEntity(UserEntity userEntity);
}

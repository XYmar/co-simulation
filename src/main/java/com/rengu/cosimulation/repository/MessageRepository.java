package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.MessageEntity;
import com.rengu.cosimulation.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/4/22 16:01
 */
@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, String> {
    List<MessageEntity> findByArrangedPerson(UserEntity arrangedPerson);
    List<MessageEntity> findByArrangedPersonAndIfRead(UserEntity userEntity, boolean ifRead);
}

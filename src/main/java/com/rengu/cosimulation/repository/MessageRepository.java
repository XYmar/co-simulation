package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.Message;
import com.rengu.cosimulation.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/4/22 16:01
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByArrangedPersonName(String arrangedPersonName);
    List<Message> findByArrangedPersonNameAndIfRead(String arrangedPersonName, boolean ifRead);
    Long countByArrangedPersonNameAndIfRead(String arrangedPersonName, boolean ifRead);
}

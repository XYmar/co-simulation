package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.Users;
import junit.framework.Assert;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    public void getUserById() throws Exception {
        Users user = userService.getUserById("cb078327-476d-41a3-be3d-a040efca5e02");
        // Assert.assertEquals("admin", user.getUsername());
        log.info("test---------------");
        TestCase.assertEquals("admin0", user.getUsername());
    }

}
package com.rengu.cosimulation.config;

/**
 * Author: XYmar
 * Date: 2019/7/18 11:02
 */
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
@Configuration
@EnableWebMvc
@Import({ WebSecurityConfig.class })
public class AppConfig {
}
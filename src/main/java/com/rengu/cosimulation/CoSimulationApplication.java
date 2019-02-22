package com.rengu.cosimulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CoSimulationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoSimulationApplication.class, args);
    }

}


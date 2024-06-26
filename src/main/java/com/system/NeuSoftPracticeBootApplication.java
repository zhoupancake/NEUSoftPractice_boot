package com.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDynamicDataSource
public class NeuSoftPracticeBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeuSoftPracticeBootApplication.class, args);
    }

}

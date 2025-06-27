package com.bubo.videoharvester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableJpaRepositories(basePackages = "com.bubo.videoharvester.repository")
@EnableTransactionManagement
public class VideoHarvesterApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(VideoHarvesterApplication.class);

    public static void main(String[] args) {

        SpringApplication.run(VideoHarvesterApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // Initial run or any startup tasks
    }
}


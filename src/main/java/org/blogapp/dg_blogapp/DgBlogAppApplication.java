package org.blogapp.dg_blogapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableJpaAuditing
@SpringBootApplication
public class DgBlogAppApplication {
    private static final Logger logger = LoggerFactory.getLogger(DgBlogAppApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(DgBlogAppApplication.class, args);
        logger.info("Application started successfully");
    }
}

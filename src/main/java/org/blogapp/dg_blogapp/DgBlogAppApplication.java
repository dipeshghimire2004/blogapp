package org.blogapp.dg_blogapp;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableJpaAuditing
@SpringBootApplication
@Slf4j
public class DgBlogAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(DgBlogAppApplication.class, args);
        log.info("Application started successfully");
    }
}

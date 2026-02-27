package org.blogapp.dg_blogapp.payment.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "khalti")
public class KhaltiConfig {

    private String secretKey;
    private String publicKey;
    private String baseUrl;
    private String initiatePath;
    private String verifyPath;
    private String returnUrl;
    private String websiteUrl;

    @Bean
    public WebClient khaltiWebClient() {
        log.info("Creating Khalti WebClient with baseUrl: {}", baseUrl);
        log.info("Secret key present: {}", secretKey != null && !secretKey.isEmpty());
        
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Key " + secretKey)
                .build();
    }
}

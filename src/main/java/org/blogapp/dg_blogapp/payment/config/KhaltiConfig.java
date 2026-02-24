//package org.blogapp.dg_blogapp.payment.config;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@Getter
//@Setter
//@Configuration
//@ConfigurationProperties(prefix = "khalti")
//public class KhaltiConfig {
//
//    private String baseUrl;
//
//    private String publicKey;
//
//    private String secretKey;
//
//    private String returnUrl;
//
//    private String initiatePath;
//
//    private String verifyPath;
//
//    private String websiteUrl;
//
//    @Bean
//    public WebClient webClient() {
//        return WebClient.builder().baseUrl(baseUrl).build();
//    }
//
//}

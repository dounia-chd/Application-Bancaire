package com.ebanca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.ebanca.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@ConfigurationPropertiesScan
public class EBancaApplication {
    public static void main(String[] args) {
        SpringApplication.run(EBancaApplication.class, args);
    }
} 
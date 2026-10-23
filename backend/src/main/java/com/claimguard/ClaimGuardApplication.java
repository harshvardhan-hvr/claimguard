package com.claimguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class ClaimGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaimGuardApplication.class, args);
    }
}

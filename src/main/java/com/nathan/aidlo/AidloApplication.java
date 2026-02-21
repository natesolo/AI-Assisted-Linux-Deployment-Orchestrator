package com.nathan.aidlo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AidloApplication {
    public static void main(String[] args) {
        SpringApplication.run(AidloApplication.class, args);
    }
}

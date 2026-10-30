package ru.warmhouse.heating;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class HeatingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeatingApplication.class, args);
    }
}

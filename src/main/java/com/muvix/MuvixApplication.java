package com.muvix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MuvixApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuvixApplication.class, args);
    }

}

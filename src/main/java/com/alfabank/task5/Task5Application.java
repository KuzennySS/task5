package com.alfabank.task5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = {"models"})
@ComponentScan(basePackages = {"service","controller"})
public class Task5Application {

    public static void main(String[] args) {
        SpringApplication.run(Task5Application.class, args);
    }

}

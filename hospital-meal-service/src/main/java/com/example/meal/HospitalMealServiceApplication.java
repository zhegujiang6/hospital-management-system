package com.example.meal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HospitalMealServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalMealServiceApplication.class, args);
    }

}

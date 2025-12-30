package com.university.dorm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.university.dorm.module.*.mapper")
public class DormSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(DormSystemApplication.class, args);
    }

}

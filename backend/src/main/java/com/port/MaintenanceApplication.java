package com.port;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.port.mapper")
@EnableScheduling
public class MaintenanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaintenanceApplication.class, args);
    }
}

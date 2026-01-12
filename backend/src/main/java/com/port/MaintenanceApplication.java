package com.port;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.port.mapper")
public class MaintenanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaintenanceApplication.class, args);
    }
}

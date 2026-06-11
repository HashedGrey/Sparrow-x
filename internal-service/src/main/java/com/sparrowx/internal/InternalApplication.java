package com.sparrowx.internal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.sparrowx.internal",
        "buildingblocks"
//                .core",
//        "buildingblocks.infrastructure.observability",
//        "buildingblocks.infrastructure.grpc"
})
public class InternalApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternalApplication.class, args);
    }
}
package com.sparrowx.tweet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.sparrowx",
                "buildingblocks"
        }
)
public class TweetApplication {

    public static void main(String[] args) {
        SpringApplication.run(TweetApplication.class, args);
    }
}

package com.balmaya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BalmayaApplication {
  public static void main(String[] args) {
    SpringApplication.run(BalmayaApplication.class, args);
  }
}


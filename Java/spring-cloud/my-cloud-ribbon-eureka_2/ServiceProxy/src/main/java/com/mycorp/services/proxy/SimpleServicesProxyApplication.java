package com.mycorp.services.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class SimpleServicesProxyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SimpleServicesProxyApplication.class, args);
    }
}

package com.mycorp.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simple-services")
public class ServiceControler {

    @Value("${server.port}")
    private String port;

    @GetMapping("/hello")
    public String helloService() {

        System.out.println("helloService get invoked from port:"+port);

        return "Hello from port : " + port;
    }

    @GetMapping("/")
    public String refresh() {
        return "";
    }

}

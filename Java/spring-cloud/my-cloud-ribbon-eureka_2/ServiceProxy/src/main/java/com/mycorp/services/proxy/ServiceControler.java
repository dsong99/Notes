package com.mycorp.services.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/simple-services")
@EnableFeignClients
public class ServiceControler {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FeignClientProxy feignClientProxy;

    @Value("${server.port}")
    private String port;

    @GetMapping("/hello")
    public String helloService() {

        System.out.println("proxy helloService get invoked from port:"+port);
        //
        // URL http://services matches to @RibbonClient(name = "services")
        // and matches to <name>services</name> in service project pom file.
        //
        return restTemplate.getForObject("http://services/simple-services/hello" , String.class);
    }

    @GetMapping("/hello-feign")
    public String helloServiceFeign() {

        System.out.println("proxy helloService feign get invoked from port:"+port);
        return feignClientProxy.hello();
    }

    @GetMapping("/")
    public String refresh() {
        return "";
    }

    //
    // must use @LoadBalanced annotation to enable Ribbon load balancer
    //
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

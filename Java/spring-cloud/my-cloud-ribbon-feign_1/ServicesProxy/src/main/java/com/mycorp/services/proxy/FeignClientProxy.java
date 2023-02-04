package com.mycorp.services.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "services")
public interface FeignClientProxy {

    @RequestMapping("simple-services/hello")
    public String hello();
}

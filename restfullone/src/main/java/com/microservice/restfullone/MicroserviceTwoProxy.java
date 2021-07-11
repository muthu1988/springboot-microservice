package com.microservice.restfullone;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "microservice-two")
public interface MicroserviceTwoProxy {

    @GetMapping("/servicetwo/greet")
    public Map<String, String> greet();

}

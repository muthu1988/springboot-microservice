package com.microservice.restfullone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@EnableFeignClients
public class RestfullController {

    @Autowired
    Configurations configuration;

    @Autowired
    MicroserviceTwoProxy microserviceTwoProxy;

    @GetMapping("/serviceone/sayhello/{name}")
    public String sayHello(@PathVariable String name) {
        Map<String, String> microserviceTwoResp = microserviceTwoProxy.greet();
        return String.format("Hello %s %s & %s from %s", name, configuration.getEnv(), microserviceTwoResp.get("greetings"), microserviceTwoResp.get("env"));
    }

}

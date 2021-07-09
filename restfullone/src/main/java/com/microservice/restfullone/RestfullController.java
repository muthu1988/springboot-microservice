package com.microservice.restfullone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestfullController {

    @Autowired
    Configurations configuration;

    @GetMapping("serviceone/sayhello/{name}")
    public String sayHello(@PathVariable String name) {
        return String.format("Hello %s %s", name, configuration.getEnv());
    }

}

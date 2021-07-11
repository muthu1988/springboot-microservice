package com.microservice.restfulltwo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RestfullController {

    @Autowired
    Environment environment;

    @GetMapping("/servicetwo/greet")
    public Map<String, String> greetings() {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("greetings", "Hi");
        returnMap.put("env", environment.getProperty("local.server.port"));
        return returnMap;
    }

}

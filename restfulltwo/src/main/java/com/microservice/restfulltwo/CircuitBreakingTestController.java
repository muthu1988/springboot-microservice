package com.microservice.restfulltwo;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CircuitBreakingTestController {

    @GetMapping("/test")
    @Retry(name = "test-api", fallbackMethod = "fallBackMethod")
//    @CircuitBreaker(name = "test-api", fallbackMethod = "fallBackMethod")
    public String actualMethod() {
        int i = 1/0;
        return "circuit breaker method";
    }

    public String fallBackMethod(Exception exception) {
        return "fallback method";
    }

}

# springboot-microservice
Microservice basic architecture implementation using spring boot

  1. Naming service
  2. Simple restfull microservices
  3. Api gateway
  4. Config server
  5. Client side Load balancer (openfeign)
  6. Circuit breaker (resilience4j)
  7. Distributed tracing (sleuth, zipkin)

## 1. Naming Server:

  Create a naming server (discovery service):
    
   ### create spring boot service with spring initializer as shown below:
   
   ![eureka-init](https://user-images.githubusercontent.com/17717124/125009888-0db16800-e083-11eb-8bfb-4c750e8d8971.png)

   ### Add below properties to application.properties:
    
        spring.application.name = discovery-service
        server.port = 8761
        eureka.instance.hostname = localhost
        eureka.client.registerWithEureka = false
        eureka.client.fetchRegistry = false
        eureka.client.serviceUrl.defaultZone = http://${eureka.instance.hostname}:${server.port}/eureka/

   ### Enable discovery service by adding below annotaion in main class:
        @EnableEurekaServer

   ### Start Application and hit below endpoint, It should display a dashboard
        http://localhost:8761/ 


## 2. Microservice One:

   Create a simple restful service
   
   ### create spring boot service with spring initializer as shown below:

   ![restfullone-init](https://user-images.githubusercontent.com/17717124/125011207-9c26e900-e085-11eb-93b0-65aaf4284ada.png)

   ### Add below properties to application.properties:
        spring.application.name=microservice-one
        server.port=8100
        eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka

   ### Lets create some end points:
        @RestController
        public class RestfullController {

            @GetMapping("serviceone/sayhello/{name}")
            public String sayHello(@PathVariable String name) {
                return String.format("Hello %s", name);
            }

        }

   ### Start Application:
        Hit: http://localhost:8100/serviceone/sayhello/muthu (should return "Hello muthu")
        http://localhost:8761/ should display a dashboard with microservice-one registered


## 3. API Gateway:

  Create a API Gateway service
    
   ### create spring boot service with spring initializer as shown below:
   
   ![gateway-init](https://user-images.githubusercontent.com/17717124/125011459-24a58980-e086-11eb-94eb-de712e7f79b0.png)

   ### Add below properties to application.properties
        spring.application.name=api-gateway
        server.port=8765
        eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka

        spring.cloud.gateway.routes[0].id=microservice-one
        spring.cloud.gateway.routes[0].uri=http://localhost:8100/
        spring.cloud.gateway.routes[0].predicates[0]=Path=/microservice-one/**

   ### Start Application:
        Hit: http://localhost:8765/microservice-one/serviceone/sayhello/muthu (should return "Hello muthu")
        http://localhost:8761/ should display a dashboard with api-gateway registered


## 4. Config Repo:
    
   ### Create a git repo with property files as in below repo:
    https://github.com/muthu1988/springboot-microservice/tree/main/config

   ### Each property file can have a property like:
        microservice-one.env = {{some_env_string}}

   ### Git repo and the subdirectory name will be used in config server for fetching properties

## 5. Config Server:

  Create a config server, that gets configurations from a git hub repo
    
   ### create spring boot service with spring initializer as shown below:
   
   ![config-init](https://user-images.githubusercontent.com/17717124/125011896-ea88b780-e086-11eb-915a-d7f550694958.png)
   
   ### Add below properties to application.properties
        spring.application.name = config-server
        server.port = 8888
        spring.cloud.config.server.git.uri = https://github.com/muthu1988/springboot-microservice
        spring.cloud.config.server.git.default-label = main
        spring.cloud.config.server.git.search-paths = config

   ### Enable config server by adding below annotaion in main class:
        @EnableConfigServer

   ### Start Application:
        Hit: http://localhost:8888/microservice-one/dev (should get a json with properties from microservice-one-dev.properties files)

## 6. Microservice one - To use properties from config server:

  Update the restfull service that was created as in below steps

   ### Add spring-config depedency to your pom.xml
        <dependency>
          <groupId>org.springframework.cloud</groupId>
          <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>

   ### Add below properties to application.properties
        spring.config.import=optional:configserver:http://localhost:8888
        spring.profiles.active=stage

   ### Create Configration class as shown below:
        @Component
        @ConfigurationProperties("microservice-one")
        public class Configurations {

            private String env;

            public String getEnv() {
                return env;
            }

            public void setEnv(String env) {
                this.env = env;
            }
        }

   ### In RestfullController, inject Configration bean and get 'env' property from config server:
        @RestController
        public class RestfullController {

            @Autowired
            Configurations configuration;

            @GetMapping("serviceone/sayhello/{name}")
            public String sayHello(@PathVariable String name) {
                return String.format("Hello %s %s", name, configuration.getEnv());
            }

        }

   ### Start Application:
        Hit: http://localhost:8100/serviceone/sayhello/muthu (should return "Hello muthu stage")
    
## 7. Microservice Two (2 instances):

     Create restful service, same as Microservice one.

   ### Add below properties to application.properties:
        spring.application.name=microservice-two
        server.port=8200
        eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka

   ### Lets create some end points:
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
         
   ### Start Application:
        Hit: http://localhost:8200/servicetwo/greet (should get a json {"greetings": "Hi",  "env": "8200"} - returning the port of the service)
        http://localhost:8761/ should display a dashboard with microservice-two registered

  ### Create one more instance of microservice-two
        In local machines to do so, have to run them in different ports. I use intellij by following steps:
        1. Run -> Edit Configuration, then click on "RestfulltwoApplication"
        2. Modify Options -> Enable "Add multiple instances" & "Add VM Options"
        3. In VM Options, add "-Dserver.port=8210"
        
   ### Start Application:
        Hit: http://localhost:8210/servicetwo/greet (should get a json {"greetings": "Hi",  "env": "8210"} - returning the port 8210)
        http://localhost:8761/ should display a dashboard with 2 instances of microservice-two registered
    
## 8. Microservices one - Client side load balancing (openfeign)

  Enable microservice-one to call miroservice-two by looking up eureka along with client side load balancing
    
   ### Add openfeign depedency to your pom.xml
        <dependency>
          <groupId>org.springframework.cloud</groupId>
          <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        
   ### Create a proxy interface as shown below :
        @FeignClient(name = "microservice-two")
        public interface MicroserviceTwoProxy {
            @GetMapping("/servicetwo/greet")
            public Map<String, String> greet();
        }
        
  ### Modify our RestfullController to enable openfeign and call microservice-two via the proxy inteface
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
        
   ### Start Application:
       Hit: http://localhost:8100/serviceone/sayhello/muthu (should return "Hello muthu stage & Hi from 8200")
       On the next hit, we will get "Hello muthu stage & Hi from 8210" (Thus client side load balacing is enabled)
       
## 9. API Gateway - Update it to use eureka service registry

   ### Update application.properties, by replacing the hardcoded service endpoints

   Periously we had spring.cloud.gateway.routes[0].uri=http://localhost:8100/
      
        spring.cloud.gateway.routes[0].id=microservice-one
        spring.cloud.gateway.routes[0].uri=lb://microservice-one
        spring.cloud.gateway.routes[0].predicates[0]=Path=/microservice-one/**

        spring.cloud.gateway.routes[1].id=microservice-two
        spring.cloud.gateway.routes[1].uri=lb://microservice-two
        spring.cloud.gateway.routes[1].predicates[0]=Path=/microservice-two/**
        
   ### Start Application:
        Hit: http://localhost:8765/microservice-one/serviceone/sayhello/muthu (should return "Hello muthu stage & Hi from 8200")
        Hit: http://localhost:8765/microservice-two/servicetwo/greet (should return "{"greetings":"Hi","env":"8200"}")

## 10. Circuit Breaker - microservice-two

  ### Add resilienace4j dependecy to pom.xml (microservice-two)
        <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
          <groupId>io.github.resilience4j</groupId>
          <artifactId>resilience4j-spring-boot2</artifactId>
        </dependency>
    
  ### Lets add a new controller to test circuit breaker
          @RestController
          public class CircuitBreakingTestController {

              @GetMapping("/test")
              @CircuitBreaker(name = "test-api", fallbackMethod = "fallBackMethod")
              public String actualMethod() {
                  int i = 1/0;
                  return "circuit breaker method";
              }

              public String fallBackMethod() {
                  return "fallback method";
              }

          }
    
   Hit: http://localhost:8200/test (should return "circuit breaker method")
   Lets introduce an runtime exception in test route by adding "int i = 1/0;"
   Hit: http://localhost:8200/test (should return "fallback method")
   
## 11. Zipkin - for distributed tracing

   ### Download zipkin jar and run the same from [zipkin website](https://zipkin.io/pages/quickstart.html). (can use docker on clound)

   Hit: http://localhost:9411/ (should see zipkin ui)

   ### In api-gateway, microservice-one and microservice-two add below dependecies (sleuth will add unique id to a transaction & send data to zipkin)

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-sleuth-zipkin</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-sleuth</artifactId>
        </dependency>

   ### Also in application.properties add below property:

        spring.sleuth.sampler.probability=1

   ### Restart all 3 service:

   Hit: http://localhost:8765/microservice-one/serviceone/sayhello/muthu (gateway endpoint)

   Check http://localhost:9411/ (should display a trace of the request as shown below)
   
   ![zipkin-1](https://user-images.githubusercontent.com/17717124/125571580-338072d3-cc8b-4d27-bdda-1bb4faa08c2d.png)
   ![zipkin-2](https://user-images.githubusercontent.com/17717124/125571589-d5ebee9d-1efc-4ddd-930b-e320942ef25c.png)


        


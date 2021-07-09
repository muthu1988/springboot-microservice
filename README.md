# springboot-microservice
Microservice basic architecture implementation using spring boot

1. Naming service
2. Simple restfull microservice
3. Api gateway
4. Config server

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
    

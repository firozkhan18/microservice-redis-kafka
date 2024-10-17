
# Event-Driven Microservices Architecture

## Overview

This document outlines a comprehensive microservices architecture that includes:

- **Microservices**:
  - User Service
  - Order Service
  - Notification Service
- **Kafka**: Message broker for asynchronous communication.
- **Keycloak**: Authentication and authorization management.
- **Load Balancer**: Nginx or Traefik for request distribution.
- **OpenAPI**: API documentation.
- **Resilience4j**: Circuit breakers and retry mechanisms.

## Architecture Diagram

```
+-------------------+       +------------------+       +-------------------+
|    Load Balancer  | ----> |    Kafka Broker   | ----> |  Order Service     |
| (Nginx/Traefik)   |       |                  |       | (Protected API)    |
+-------------------+       +------------------+       +-------------------+
                                    |
                                    |
                         +---------------------+
                         |  User Service       |
                         | (Protected API)     |
                         +---------------------+
                                    |
                                    |
                         +---------------------+
                         | Notification Service |
                         | (Protected API)     |
                         +---------------------+
                                    |
                                    |
                            +-----------------+
                            |   Keycloak      |
                            +-----------------+
```

```mermaid

graph TD
    A[Load Balancer<br>(Nginx/Traefik)] --> B[Kafka Broker]
    B --> C[Order Service<br>(Protected API)]
    B --> D[User Service<br>(Protected API)]
    B --> E[Notification Service<br>(Protected API)]
    D --> F[Keycloak]
    E --> F
    C --> F
```

## 1. Setting Up Keycloak

### Install Keycloak

Run Keycloak using Docker:

```bash
docker run -d -p 8080:8080 \
  -e KEYCLOAK_USER=admin \
  -e KEYCLOAK_PASSWORD=admin \
  --name keycloak \
  jboss/keycloak
```

### Configure Realm and Clients

1. **Access Keycloak Admin Console**: Navigate to `http://localhost:8080/auth/admin` and log in.
2. **Create a Realm**: E.g., `myrealm`.
3. **Create Clients** for each microservice (User Service, Order Service, Notification Service) with appropriate settings.
4. **Create Roles** and **Users** as needed.

## 2. Microservices Setup

### User Service

#### Maven Dependencies

Add dependencies to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-spring-boot-starter</artifactId>
    <version>15.0.2</version>
</dependency>
```

#### User Service Application

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

#### User Controller

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @GetMapping("/api/users")
    public String getUsers() {
        return "List of users";
    }
}
```

### Order Service

Follow similar steps as the User Service, ensuring the appropriate endpoints are defined.

### Notification Service

Repeat the same setup with relevant API endpoints.

### Security Configuration

Create a security configuration class in each microservice:

```java
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.AuthenticationEntryPoint;

@EnableWebSecurity
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeRequests()
            .antMatchers("/api/users").hasRole("USER")
            .antMatchers("/api/orders").hasRole("USER")
            .anyRequest().permitAll();
    }

    @Bean
    @Override
    protected AuthenticationEntryPoint authenticationEntryPoint() {
        return new KeycloakAuthenticationEntryPoint(adapter());
    }
}
```

### 3. Kafka Setup

Add Kafka producer and consumer logic in each service as needed.

### 4. Adding Load Balancer

#### Using Nginx

Create an Nginx configuration file `nginx.conf`:

```nginx
http {
    upstream user_service {
        server user-service:8080;
        server user-service-2:8080;
    }

    upstream order_service {
        server order-service:8080;
        server order-service-2:8080;
    }

    upstream notification_service {
        server notification-service:8080;
        server notification-service-2:8080;
    }

    server {
        listen 80;

        location /api/users {
            proxy_pass http://user_service;
        }

        location /api/orders {
            proxy_pass http://order_service;
        }

        location /api/notifications {
            proxy_pass http://notification_service;
        }
    }
}
```

#### Docker Configuration for Nginx

Create a Dockerfile for Nginx:

```dockerfile
FROM nginx:alpine
COPY nginx.conf /etc/nginx/nginx.conf
```

### Docker Compose Configuration

Update your `docker-compose.yml` to include the load balancer and multiple service instances:

```yaml
version: '3.8'

services:
  zookeeper:
    image: wurstmeister/zookeeper:3.4.6
    ports:
      - "2181:2181"

  kafka:
    image: wurstmeister/kafka:latest
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: INSIDE://kafka:9093,OUTSIDE://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT
      KAFKA_LISTENERS: INSIDE://0.0.0.0:9093,OUTSIDE://0.0.0.0:9092
    depends_on:
      - zookeeper

  user-service:
    build: ./user-service
    ports:
      - "8081:8080"

  user-service-2:
    build: ./user-service
    ports:
      - "8084:8080"

  order-service:
    build: ./order-service
    ports:
      - "8082:8080"

  order-service-2:
    build: ./order-service
    ports:
      - "8085:8080"

  notification-service:
    build: ./notification-service
    ports:
      - "8083:8080"

  notification-service-2:
    build: ./notification-service
    ports:
      - "8086:8080"

  nginx:
    build: ./nginx
    ports:
      - "80:80"
    depends_on:
      - user-service
      - order-service
      - notification-service
```

### 5. Testing the Setup

1. **Start Services**:
   Run Docker Compose:

   ```bash
   docker-compose up
   ```

2. **Access the Load Balancer**:
   Use the following URLs:
   - User Service: `http://localhost/api/users`
   - Order Service: `http://localhost/api/orders`
   - Notification Service: `http://localhost/api/notifications`

### 6. Accessing Traefik (Optional)

If using Traefik, add to your `docker-compose.yml` as follows:

```yaml
services:
  traefik:
    image: traefik:v2.4
    command:
      - "--api.insecure=true"
      - "--providers.docker=true"
      - "--entrypoints.web.address=:80"
    ports:
      - "80:80"
      - "8080:8080"
    volumes:
      - "/var/run/docker.sock:/var/run/docker.sock"
```

### Conclusion

This document provides a detailed guide to creating an event-driven microservices architecture using Kafka, Keycloak, and a load balancer. This setup ensures scalability, robustness, and secure access to your services. Customize the configurations according to your specific needs, and enjoy building resilient microservices!
```

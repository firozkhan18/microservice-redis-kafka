# Securing REST API using Keycloak and Spring OAuth2

Keycloak is an open-source Identity and Access Management Server that supports OAuth2 and OpenID Connect (OIDC) protocols. This article explains how to secure Spring Boot REST APIs with Keycloak using the Spring OAuth2 library.

## Overview
Keycloak documentation suggests three ways to secure Spring-based REST APIs:
1. Using Keycloak Spring Boot Adapter
2. Using Keycloak Spring Security Adapter
3. Using OpenID Connect (OIDC) + OAuth2

We will focus on using Keycloak's OIDC support and the Spring OAuth2 library. The benefits of using Spring OAuth2 over Keycloak adapters are discussed at the end of this article.

## Steps to Set Up Keycloak and Spring OAuth2

This is a comprehensive guide with step-by-step instructions, screenshots, and code snippets. The complete code is available on [GitHub](https://github.com/bcarun/spring-oauth2-keycloak-connector). I recommend reading this article before looking into the code.

### Step 1: Getting Started With Keycloak
Refer to the [Keycloak getting started documentation](https://www.keycloak.org/docs/latest/getting-started/) to run and set up the Keycloak admin user.

1. After running Keycloak, access the admin console at `http://localhost:8080/auth`.
2. Set up Keycloak username: `admin`, password: `admin`.

> **Note**: Standalone Keycloak runs on the Wildfly server. We need a Keycloak admin user to create realms, clients, users, roles, etc.

### Step 2: Create a Dev Realm
- Name: `dev`

![Add Dev Realm](path/to/image)

### Step 3: Create a Client (Micro-Service)
- **Client ID**: `employee-service`
- **Client Protocol**: `openid-connect`

![Add Client](path/to/image)

### Step 4: Configure Client
Ensure your microservice runs on a different port (e.g., 8085).

- **Access Type**: `confidential`
- **Valid Redirect URIs**: `http://localhost:8085`
- **Service Accounts Enabled**: On
- **Authorization Enabled**: On

> **Note**: The `confidential` access type supports getting access tokens using both the client credentials grant and the authorization code grant.

![Configure Client](path/to/image)

### Step 5: Create Client Role
Create a role under the client. For example, create a role named `USER` under `employee-service`.

![Create Role](path/to/image)

### Step 6: Create a Mapper
To get the `user_name` in the access token, create a mapper. By default, the logged-in username is returned in a claim named `preferred_username`. Create a mapper to map this to `user_name`.

![Create Mapper](path/to/image)

### Step 7: Create User
![Create User](path/to/image)

### Step 8: Map Client Role To User
Assign the respective role to the user to provide access to the client (micro-service).

![Assign Role to User](path/to/image)

### Step 9: Get Configuration From OpenID Configuration Endpoint
Retrieve security endpoint details using:

```
GET http://localhost:8080/auth/realms/dev/.well-known/openid-configuration
```

Important URLs to note from the response:
- `issuer`: `http://localhost:8080/auth/realms/dev`
- `authorization_endpoint`: `${issuer}/protocol/openid-connect/auth`
- `token_endpoint`: `${issuer}/protocol/openid-connect/token`
- `userinfo_endpoint`: `${issuer}/protocol/openid-connect/userinfo`

### Step 10: Get Access Token Using Postman
Select `Authorization Type` as `OAuth 2.0`, click on ‘Get New Access Token’, and enter the following details.

![Postman Screenshot](path/to/image)

> **Note**: Ensure you select client authentication as “Send client credentials in body” while requesting the token.

### Step 11: Create a Spring Boot Application
#### Parent Configuration
```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>2.0.1.RELEASE</version>
</parent>
```

#### Dependencies
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.security.oauth.boot</groupId>
  <artifactId>spring-security-oauth2-autoconfigure</artifactId>
  <version>2.0.1.RELEASE</version>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### Step 12: Configure `application.properties`
General Security Properties:
```properties
rest.security.enabled=true
rest.security.api-matcher=/api/**
rest.security.cors.allowed-origins=*
rest.security.cors.allowed-headers=*
rest.security.cors.allowed-methods=GET,POST,PUT,PATCH,DELETE,OPTIONS
rest.security.cors.max-age=3600
```

Properties to secure REST Endpoints using OAuth2 Resource Server:
```properties
rest.security.issuer-uri=http://localhost:8080/auth/realms/dev
security.oauth2.resource.id=employee-service
security.oauth2.resource.token-info-uri=${rest.security.issuer-uri}/protocol/openid-connect/token/introspect
security.oauth2.resource.user-info-uri=${rest.security.issuer-uri}/protocol/openid-connect/userinfo
security.oauth2.resource.jwt.key-value=-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhWOcKAVAwt+5FF/eE2hLaMVD5zQBBr+RLdc7HFUrlvU9Pm548rnD+zRTfOhnl5b6qMjtpLTRe3fG+8chjPwQriRyFKCzg7eYNxuR/2sK4okJbfQSZFs16TFhXtoQW5tWnzK6PqcB2Bpmy3x7QN78Hi04CjNrPz2BX8U+5BYMavYJANpp4XzPE8fZxlROmSSyNeyJdW30rJ/hsWZJ5nnxSZ685eT4IIUHM4g+sQQTZxnCUnazNXng5B5yZz/sh+9GOXDGT286fWdGbhGKU8oujjSJLOHYewFZX5Jw8aMrKKspL/6glRLSiV8FlEHbeRWxFffjZs/D+e9A56XuRJSQ9QIDAQAB\n-----END PUBLIC KEY-----
```

### Step 13: JWT Access Token Customizer
To parse and set the `SecurityContextHolder`, Spring OAuth2 needs the roles or authorities from the token. This setup requires special handling.

### Step 14: OAuth2 Resource Server Configurer
If your micro-service needs to call another micro-service, `OAuth2RestTemplate` is required.

### Step 15: Secure REST Endpoints
Use the `@PreAuthorize` annotation to secure REST endpoints with appropriate roles.

### Step 16: Disable Basic Auth If Not Required
To disable default security, exclude `SecurityAutoConfiguration` and `UserDetailsServiceAutoConfiguration`.

## Benefits Of Using Spring OAuth2 Over Keycloak Adapters
1. **Ease of Upgrades**: Upgrading Keycloak Server doesn't require upgrading the adapters in all micro-services.
2. **Simplified Migration**: Migrating from Keycloak to another OAuth2 provider is easier with Spring OAuth2.
3. **Token Management**: `OAuth2RestTemplate` manages refreshing and caching access tokens seamlessly.

To understand how to access a secured micro-service from another micro-service using `OAuth2RestTemplate`, refer to my other articles.

---

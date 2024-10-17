# Spring Security OAuth2 Keycloak Demo

This repository contains source code to demonstrate OAuth2 features using Spring Security and KeyCloak Authorization Server.

After you checked out the project, run the following command:

```
mvn clean verify
```

This project contains examples for 3 OAuth2 Grant Types:

- Authorization Code Flow (oauth2-authorization-code-demo)
- PKCE Authorization Code Flow (oauth2-pkce-demo)
- Client Credentials Flow (oauth2-client-credentials-demo)

## Run Keycloak

Download Keycloak from [https://www.keycloak.org/downloads](https://www.keycloak.org/downloads). If you are using standalone version, run the following command, by making sure you are inside the bin directory:

```
standalone.bat -Djboss.http.port=<port>
```

You can provide whatever port number you like.

### Importing Realm

Instead of creating the realm, you can make use of the `realm-export.json` file to import all the realm details into your Keycloak instance.

## About

This repository contains source code to demonstrate OAuth2 features using Spring Security and KeyCloak Authorization Server.

---

### Create and configure Keycloak OAuth 2.0 authorization server

If you want to know how to set up and configure Keycloak as an authorization server (for OAuth 2.0 framework) with Docker this article is for you.

A target project will consist of three pieces:

- **Authorization server (Keycloak)** — an application which is used to issue access and identity tokens.
- **Protected resource (Java, Spring Boot, backend application)** — an application that serves information using REST API, but requires a valid access token for security reasons.
- **Client (Angular, frontend application)** — a website, which requires user to be logged in order to be able to get access token and query the backend app.

In this article I’ll focus on the authorization server, how to create a basic `docker-compose.yaml` file and then configure a Keycloak instance.

### Running Keycloak instance

Before adding any project-specific configuration I’ve configured my OS’s hosts file, so that I’ll have a convenient URL address for testing. The location of this file depends on the OS:

- Linux (Ubuntu)
- Windows 10
- Mac

Once you found it add the following line:

```
127.0.0.1	keycloak
```

After that instead of `http://localhost` we will be able to use the `http://keycloak` URL. Be mindful, that it's not necessary step, you can skip it and use the localhost instead of keycloak as I will through this blog post.

Having it we can move on to setting up Keycloak. First thing to do is to create a Docker Compose file where we will include the entire Keycloak configuration. Looking into the documentation on Docker Hub we can find out that `jboss/keycloak` Docker image supports multiple databases, like H2, MySQL, PostgreSQL, MariaDB, Oracle or Microsoft SQL Server.

I’ve decided to go with PostgreSQL, therefore here is a simple definition of the postgres component in `docker-compose.yaml`.

```yaml
version: "3.8"
services:
  postgres:
    image: postgres:13.0-alpine
    container_name: postgres
    ports:
      - 5432:5432
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    volumes:
      - postgres:/var/lib/postgresql/data
      - ./infra/postgres:/docker-entrypoint-initdb.d
volumes:
  postgres:
```

Apart from a standard definition, like an image, container_name or ports mapping I've decided to set up a user and password to the database as postgres using the environment variable, which is documented on Docker Hub for the postgres image.

Next thing worth mentioning here is that I’ve added a volume; the first one is just to persist container data on my local machine, and the latter is for adding an initial SQL script, so it will be executed during the first start up and it'll create a database and a user which will be used by Keycloak.

```sql
CREATE USER keycloak WITH ENCRYPTED PASSWORD 'keycloak';
CREATE DATABASE keycloak;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
```

This script is located in the `./infra/postgres` folder on the host machine and it's mapped to a directory inside the postgres Docker container `/docker-entrypoint-initdb.d`.

After that, we can move on to defining the Keycloak component.

```yaml
keycloak:
    image: jboss/keycloak:11.0.2
    container_name: keycloak
    ports:
      - 8080:8080
    environment:
      - KEYCLOAK_USER=admin
      - KEYCLOAK_PASSWORD=admin
      - DB_VENDOR=postgres
      - DB_ADDR=postgres
      - DB_DATABASE=keycloak
      - DB_USER=keycloak
      - DB_PASSWORD=keycloak
    depends_on:
      - postgres
```

Again, there is no rocket science here. Apart from the standard image definition, container_name and ports mapping we can see only environment variables that are used to tune Keycloak to be connected to the postgres database and to set a default username and password for an admin user.

The resulting `docker-compose.yaml` file looks as follows.

```yaml
version: "3.8"
services:
  
  keycloak:
    image: jboss/keycloak:11.0.2
    container_name: keycloak
    ports:
      - 8080:8080
    environment:
      - KEYCLOAK_USER=admin
      - KEYCLOAK_PASSWORD=admin
      - DB_VENDOR=postgres
      - DB_ADDR=postgres
      - DB_DATABASE=keycloak
      - DB_USER=keycloak
      - DB_PASSWORD=keycloak
    depends_on:
      - postgres
  postgres:
    image: postgres:13.0-alpine
    container_name: postgres
    ports:
      - 5432:5432
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    volumes:
      - postgres:/var/lib/postgresql/data
      - ./infra/postgres:/docker-entrypoint-initdb.d
volumes:
  postgres:
```

Now go to terminal, navigate to the folder where your `docker-compose.yaml` file is located and run the following command:

```
> docker-compose up -d keycloak
```

You should see output similar to this:

```
Creating volume "keycloak-security-example_postgres" with default driver
Creating volume "keycloak-security-example_prometheus" with default driver
Pulling postgres (postgres:13.0-alpine)...
13.0-alpine: Pulling from library/postgres
188c0c94c7c5: Pull complete
56f1d1b70e7f: Pull complete
9b4f01476d2b: Pull complete
16419214bc02: Pull complete
4886fc567835: Pull complete
9026d4fbeafa: Pull complete
001c336294eb: Pull complete
8abc6d154e9f: Pull complete
Digest: sha256:d26ddee3648a324a9747b3257236322141920d5f9a82ca703def6bff1cca7067
Status: Downloaded newer image for postgres:13.0-alpine
Pulling keycloak (jboss/keycloak:11.0.2)...
11.0.2: Pulling from jboss/keycloak
0fd3b5213a9b: Pull complete
aebb8c556853: Pull complete
ed3ae09abceb: Pull complete
85e3448ea914: Pull complete
9f0e9c75b3b9: Pull complete
Digest: sha256:8cdd41cb4a0b210ed3f07df5d18306762e1755bb8d6c1ffc5e083c080528783d
Status: Downloaded newer image for jboss/keycloak:11.0.2
Creating postgres ... done
Creating keycloak ... done
```

With the above command, we have pulled (downloaded) both Docker images and started new containers.

To check if everything is all right run another Docker command to see running containers:

```
> docker ps
CONTAINER ID  IMAGE                 STATUS            NAMES
f9a67e3b9756  jboss/keycloak:11.0.2  Up 33 seconds    keycloak
5d781b2b8d6f  postgres:13.0-alpine   Up 34 seconds    postgres
```

The status is Up so everything looks fine :). If for some reason it's not the case for you, check if you do not have any kind of a typo.

### Adding realm, client, roles, and users

As we have a confirmation that Keycloak is running, let’s login to it. Therefore, in your browser go to `http://keycloak:8080`, it will lead you to a home page.

![Desktop Screenshot](images/keyclock-1.png)

Select Administration Console, it will redirect you to the login page, where you need to provide admin credentials which are (same as they were defined in `docker-compose.yaml` file):

```
username: admin
password: admin
```

After login you should now be inside Keycloak’s Master realm.

![Desktop Screenshot](images/keyclock-2.png)

But what is a realm? It’s purely a Keycloak thing and it’s used to group resources like users, clients, etc. Realms are isolated from each other so users

 in one realm can’t access another realm.

Let’s create our own realm by clicking on the dropdown and selecting the `Add realm`.

![Desktop Screenshot](images/keyclock-3.png)

Fill in the required information and click `Create`. After that you should see your realm listed in the dropdown. Select your newly created realm.

![Desktop Screenshot](images/keyclock-4.png)

### Creating a client

Next thing we need to do is to create a client, which in our case will be our application which will access Keycloak in order to authenticate users.

In order to create a client click on the `Clients` link on the left menu. It should take you to the clients’ overview screen.

![Desktop Screenshot](images/keyclock-5.png)

Next click on `Create` button.

![Desktop Screenshot](images/keyclock-6.png)

In the next screen you will need to fill in:

- **Client ID:** a unique name of your application (for example: `spring-boot-client`)
- **Client Protocol:** openid-connect
- **Root URL:** `http://localhost:8080/` (or whatever your app URL is).

Once filled in click on `Save`.

![Desktop Screenshot](images/keyclock-7.png)

You should see the client configuration screen now. Set the following parameters:

- **Access Type:** confidential
- **Valid Redirect URIs:** `http://localhost:8080/*`
- **Web Origins:** `http://localhost:8080`
- **Standard Flow Enabled:** ON
- **Direct Access Grants Enabled:** OFF
- **Service Accounts Enabled:** OFF

After that, click `Save` again.

Once saved, click on `Credentials` tab to see the `secret` Keycloak generated for you. You will need it later when configuring the application.

![Desktop Screenshot](images/keyclock-8.png)

### Creating roles

Let’s create a role which will be assigned to users later. To do so, click on `Roles` on the left menu.

![Desktop Screenshot](images/keyclock-9.png)

Click `Add Role` button and create a role (for example: `user`).

![Desktop Screenshot](images/keyclock-10.png)

### Creating users

After that we can create a user. To do that click on `Users` link on the left menu.

![Desktop Screenshot](images/keyclock-11.png)

Click on `Add User` button.

![Desktop Screenshot](images/keyclock-12.png)

Fill in the details and click on `Save`. 

![Desktop Screenshot](images/keyclock-13.png)

After that, you need to enable the user. To do that, select the user from the list and click on `Enable` button.

Now we need to set the password for the user. Select the `Credentials` tab and set the `Password`.

![Desktop Screenshot](images/keyclock-14.png)

### Assigning roles to users

Finally, let’s assign the role to the user we just created. In the user settings screen select `Role Mappings` tab.

In the `Available Roles` section, find the role we just created (`user`) and click `Add selected`.

### Conclusion

Congratulations! You now have a fully working Keycloak setup for OAuth2 authorization in your applications. You can follow through the rest of the projects in this repository to see how the integration works with Spring Boot and Angular applications.
```

This README file provides a comprehensive guide on setting up a Spring Security OAuth2 project with Keycloak. If you have any specific aspects you'd like to adjust or any additional sections to include, just let me know!




Your Docker command for running a Keycloak instance looks good! Here’s a breakdown of the command:

### Command Breakdown

```bash
docker run --name keycloak -d -p 8080:8080 \
-e KEYCLOAK_ADMIN=admin \
-e KEYCLOAK_ADMIN_PASSWORD=admin \
quay.io/keycloak/keycloak:latest start-dev
```

1. **`docker run`:** This command is used to create and start a new container.
2. **`--name keycloak`:** Names the container "keycloak" for easier reference.
3. **`-d`:** Runs the container in detached mode (in the background).
4. **`-p 8080:8080`:** Maps port 8080 of the container to port 8080 on the host, making Keycloak accessible via `http://localhost:8080`.
5. **`-e KEYCLOAK_ADMIN=admin`:** Sets the environment variable for the admin username.
6. **`-e KEYCLOAK_ADMIN_PASSWORD=admin`:** Sets the environment variable for the admin password.
7. **`quay.io/keycloak/keycloak:latest`:** Specifies the image to use, pulling the latest version from the Quay.io repository.
8. **`start-dev`:** Starts Keycloak in development mode, which is suitable for testing and development.

### Considerations

- **Password Security:** Using "admin" for both username and password is fine for development, but ensure you change it in production environments.
- **Persistent Data:** If you want to persist your Keycloak data (like realms and users), consider using a volume to store data:
  ```bash
  -v keycloak_data:/opt/keycloak/data
  ```
  You can create a volume by running:
  ```bash
  docker volume create keycloak_data
  ```

### Complete Command with Volume

Here’s how your command would look with a persistent volume:

```bash
docker run --name keycloak -d -p 8080:8080 \
-e KEYCLOAK_ADMIN=admin \
-e KEYCLOAK_ADMIN_PASSWORD=admin \
-v keycloak_data:/opt/keycloak/data \
quay.io/keycloak/keycloak:latest start-dev
```

This setup will ensure that your Keycloak data persists even if the container is stopped or removed. Let me know if you need any more help!

Your setup for connecting Keycloak to a MySQL database looks solid! Here’s a streamlined version of your steps with some additional notes:

### Step 1: Create a Docker Network

This step allows Keycloak and MySQL to communicate with each other.

```bash
docker network create keycloak-network
```

### Step 2: Start MySQL

Run the MySQL container with the necessary environment variables.

```bash
docker run -p 3306:3306 --name mysql -d --net keycloak-network \
-e MYSQL_DATABASE=keycloak \
-e MYSQL_USER=keycloak \
-e MYSQL_PASSWORD=password \
-e MYSQL_ROOT_PASSWORD=root_password \
mysql
```

### Step 3: Run Keycloak

Run the Keycloak container on the same network and specify the connection parameters for MySQL.

```bash
docker run -p 8080:8080 --name keycloak --net keycloak-network \
-e KEYCLOAK_ADMIN=admin \
-e KEYCLOAK_ADMIN_PASSWORD=admin \
-e KC_DB=mysql \
-e KC_DB_URL=jdbc:mysql://mysql:3306/keycloak \
-e KC_DB_USERNAME=keycloak \
-e KC_DB_PASSWORD=password \
quay.io/keycloak/keycloak:latest start-dev
```

### Additional Notes

1. **Environment Variables:**
   - `MYSQL_ROOT_PASSWORD` is set for the root user but is not needed unless you intend to use the root account.
   - Make sure to change passwords and user credentials for production environments to enhance security.

2. **Data Persistence:**
   - To persist MySQL data, consider adding a volume to the MySQL container:
     ```bash
     -v mysql_data:/var/lib/mysql
     ```
     Create a volume:
     ```bash
     docker volume create mysql_data
     ```

3. **Complete MySQL Command with Volume:**
   ```bash
   docker run -p 3306:3306 --name mysql -d --net keycloak-network \
   -e MYSQL_DATABASE=keycloak \
   -e MYSQL_USER=keycloak \
   -e MYSQL_PASSWORD=password \
   -e MYSQL_ROOT_PASSWORD=root_password \
   -v mysql_data:/var/lib/mysql \
   mysql
   ```

### Complete Setup Example

Here’s how the complete setup commands would look:

```bash
# Step 1: Create Network
docker network create keycloak-network

# Step 2: Start MySQL
docker run -p 3306:3306 --name mysql -d --net keycloak-network \
-e MYSQL_DATABASE=keycloak \
-e MYSQL_USER=keycloak \
-e MYSQL_PASSWORD=password \
-e MYSQL_ROOT_PASSWORD=root_password \
-v mysql_data:/var/lib/mysql \
mysql

# Step 3: Run Keycloak
docker run -p 8080:8080 --name keycloak --net keycloak-network \
-e KEYCLOAK_ADMIN=admin \
-e KEYCLOAK_ADMIN_PASSWORD=admin \
-e KC_DB=mysql \
-e KC_DB_URL=jdbc:mysql://mysql:3306/keycloak \
-e KC_DB_USERNAME=keycloak \
-e KC_DB_PASSWORD=password \
quay.io/keycloak/keycloak:latest start-dev
```

This setup should give you a fully functional Keycloak instance connected to a MySQL database. Let me know if you need any further assistance!

Your commands for running MySQL and Keycloak with persistent volumes and in production mode look solid! Here’s a breakdown of each command and some additional best practices for production deployment.

### Running MySQL with Persistent Volume

This command creates a MySQL container with a persistent volume to store database data:

```bash
docker run -v G:/docker/volume:/var/lib/mysql -p 3306:3306 --name mysql -d --net keycloak-network \
-e MYSQL_DATABASE=keycloak -e MYSQL_USER=keycloak -e MYSQL_PASSWORD=password -e MYSQL_ROOT_PASSWORD=root_password mysql
```

**Key Points:**
- **Persistent Volume:** The `-v G:/docker/volume:/var/lib/mysql` option ensures that MySQL data is saved outside the container, allowing data to persist even if the container is removed.
- **Environment Variables:** You're correctly setting up the database and user credentials.

### Running Keycloak in Production Mode

This command starts the Keycloak container configured for production:

```bash
docker run -v G:/keycloak:/opt/keycloak/conf -p 8443:8443 --net keycloak-network \
-e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -e KC_DB=mysql \
-e KC_DB_URL=jdbc:mysql://mysql:3306/keycloak?useSSL=false \
-e KC_DB_USERNAME=keycloak -e KC_DB_PASSWORD=password \
-e KC_HOSTNAME=localhost quay.io/keycloak/keycloak:latest start --db=mysql
```

**Key Points:**
- **Persistent Configuration:** The `-v G:/keycloak:/opt/keycloak/conf` mounts a directory for Keycloak configurations. This is crucial for persisting configurations across container restarts.
- **Production Settings:** Using `start --db=mysql` specifies that Keycloak should use MySQL. Ensure your production setup has proper SSL configurations and access controls.

### Additional Best Practices for Production

1. **Use Environment Variables for Sensitive Data:**
   - Instead of hardcoding passwords in the command, consider using Docker secrets or environment variable files to manage sensitive data securely.

2. **Networking:**
   - Ensure your network configuration is secure, especially if deploying in a cloud environment. Consider using Docker's built-in network features to isolate your containers.

3. **Backups:**
   - Implement a backup strategy for both your MySQL data and Keycloak configurations to prevent data loss.

4. **Monitoring and Logging:**
   - Set up monitoring and logging for your containers. Use tools like Prometheus, Grafana, or ELK Stack to monitor performance and log data.

5. **Resource Limits:**
   - Consider adding resource limits to your containers to prevent one container from consuming too many resources, which could affect other services.

6. **Update and Maintenance:**
   - Regularly update your Docker images and Keycloak to benefit from security patches and new features.

### Conclusion

Your setup is a great starting point for deploying Keycloak and MySQL with Docker. By following these additional practices, you can enhance the security and reliability of your deployment. If you have any further questions or need assistance with specific configurations, feel free to ask!

Your Dockerfile for optimizing the Keycloak image looks well-structured! Here are a few suggestions to enhance it, particularly regarding security and best practices:

### Optimized Keycloak Dockerfile

```dockerfile
# Stage 1: Builder
FROM quay.io/keycloak/keycloak:latest as builder

# Enable health checks and metrics
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_FEATURES=token-exchange
ENV KC_DB=mysql

# Install custom providers
RUN curl -sL https://github.com/aerogear/keycloak-metrics-spi/releases/download/2.5.3/keycloak-metrics-spi-2.5.3.jar \
    -o /opt/keycloak/providers/keycloak-metrics-spi-2.5.3.jar

# Build Keycloak
RUN /opt/keycloak/bin/kc.sh build

# Stage 2: Final image
FROM quay.io/keycloak/keycloak:latest

# Copy built files from the builder stage
COPY --from=builder /opt/keycloak/ /opt/keycloak/

# Set working directory
WORKDIR /opt/keycloak

# Generate self-signed certificates (for demonstration only; use proper certs in production)
RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 \
    -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" \
    -keystore conf/server.keystore

# Environment variables for database connection
ENV KC_DB_URL=jdbc:mysql://mysql:3306/keycloak
ENV KC_DB_USERNAME=keycloak
ENV KC_DB_PASSWORD=password
ENV KC_HOSTNAME=localhost

# Start Keycloak
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start"]
```

### Key Improvements

1. **Multi-Stage Build:**
   - You are correctly using a multi-stage build to reduce the final image size.

2. **Security Practices:**
   - Use environment variables for sensitive data (like passwords) more securely in production. Consider using Docker secrets or another secret management tool.

3. **Self-Signed Certificates:**
   - The self-signed certificate generation is fine for development, but ensure to replace this with trusted certificates in production.

4. **Run as Non-Root User:**
   - It's a good practice to run your application as a non-root user. You can create a user in the Dockerfile and switch to that user.

5. **Version Locking:**
   - Consider specifying the version of Keycloak instead of using `latest`, to avoid unexpected issues with future updates.

### Example of Adding a Non-Root User

To implement a non-root user, you might add the following lines before the `ENTRYPOINT`:

```dockerfile
# Create a non-root user
RUN useradd -r -u 1000 keycloakuser

# Change ownership of the Keycloak files
RUN chown -R keycloakuser:keycloakuser /opt/keycloak/

# Switch to the non-root user
USER keycloakuser
```

### Final Note

With these suggestions, your Dockerfile will be more secure and maintainable. Feel free to modify it according to your specific requirements! Let me know if you have any questions or need further assistance.

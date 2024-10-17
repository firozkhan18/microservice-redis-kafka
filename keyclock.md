```markdown
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

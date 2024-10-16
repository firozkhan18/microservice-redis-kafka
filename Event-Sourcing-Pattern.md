# Event Sourcing

Event sourcing is a software architectural pattern that stores the state of an application as a sequence of events rather than as a single current state. Each event represents a state change that has occurred in the system. This approach provides a more detailed history of how an application reached its current state and allows for greater flexibility in managing state changes.

### Event Sourcing Design Pattern. 

This diagram illustrates how events are stored and how they drive the state of the application.

```mermaid
graph TD
    A[User Action] -->|Generate Event| B[Event Store]
    B -->|Persist Event| C[Event Log]
    C -->|Trigger Projection| D[Read Model]
    D -->|Fetch Current State| E[Application State]

    subgraph "Microservices"
        F[Service A]
        G[Service B]
    end

    B -->|Publish Event| F
    B -->|Publish Event| G
    F -->|Update State| E
    G -->|Update State| E
```

### Explanation of the Diagram

- **User Action**: The process starts with a user action that generates an event.
- **Event Store**: This component is responsible for persisting events generated by user actions.
- **Event Log**: A sequence of stored events that reflect all changes made to the application state over time.
- **Trigger Projection**: The event store triggers projections to create read models.
- **Read Model**: This represents the current state of the application derived from events. It is optimized for queries.
- **Application State**: The current state of the application derived from the read model.
- **Microservices**: Services (Service A and Service B) that subscribe to events from the event store and update their own states based on those events.

---

### Key Concepts of Event Sourcing

1. **Events**: Each state change is captured as an event. Events are immutable and represent facts about the past.
2. **Event Store**: Events are stored in an event store, which is a specialized database designed to handle event data.
3. **Rehydration**: The current state of an entity can be rebuilt by replaying the events in the order they occurred.
4. **CQRS**: Event sourcing is often used in conjunction with Command Query Responsibility Segregation (CQRS), which separates read and write operations to optimize performance and scalability.

### Advantages of Event Sourcing

- **Historical Record**: Provides a complete audit trail of all changes.
- **Rebuild State**: Allows you to reconstruct past states of the application.
- **Flexibility**: Enables new features and changes to be implemented without altering existing data structures.

### Example of Event Sourcing

Let’s illustrate event sourcing with a simple banking application where we manage accounts and transactions.

#### Domain Events

1. **AccountCreated**: An event indicating that a new account has been created.
2. **MoneyDeposited**: An event indicating that money has been deposited into the account.
3. **MoneyWithdrawn**: An event indicating that money has been withdrawn from the account.

#### Event Store

Each of these events would be stored in an event store:

```plaintext
1. AccountCreated(accountId: "123", owner: "Alice", initialBalance: 0)
2. MoneyDeposited(accountId: "123", amount: 100)
3. MoneyWithdrawn(accountId: "123", amount: 30)
```

#### Rehydrating State

To get the current state of the account with ID "123", you would replay the events in the order they occurred:

1. Start with an initial balance of 0 (from `AccountCreated`).
2. Add 100 (from `MoneyDeposited`).
3. Subtract 30 (from `MoneyWithdrawn`).

Current balance would be calculated as:

```plaintext
Initial Balance: 0
+ 100 (Deposit)
- 30 (Withdrawal)
= Current Balance: 70
```

#### Implementation Example in Pseudocode

Here’s how you might implement a basic event sourcing mechanism:

```java
class Account {
    private String id;
    private String owner;
    private List<Event> changes = new ArrayList<>();
    
    public Account(String id, String owner) {
        apply(new AccountCreated(id, owner, 0));
    }

    public void deposit(double amount) {
        apply(new MoneyDeposited(id, amount));
    }

    public void withdraw(double amount) {
        apply(new MoneyWithdrawn(id, amount));
    }

    private void apply(Event event) {
        changes.add(event);
        if (event instanceof AccountCreated) {
            this.id = event.getId();
            this.owner = event.getOwner();
            this.balance = event.getInitialBalance();
        } else if (event instanceof MoneyDeposited) {
            this.balance += event.getAmount();
        } else if (event instanceof MoneyWithdrawn) {
            this.balance -= event.getAmount();
        }
    }

    public List<Event> getChanges() {
        return changes;
    }
}
```

#### Storing and Retrieving Events

When a change occurs (like a deposit or withdrawal), you would store the event in the event store. To retrieve the current state, you would load the events for that account from the store and replay them.

### Considerations

- **Complexity**: Event sourcing can introduce complexity, particularly in managing event schemas and versioning.
- **Eventual Consistency**: Asynchronous processing can lead to eventual consistency challenges, where reads may not reflect the most recent changes immediately.
- **Storage**: Event stores can grow large over time, requiring strategies for archiving or compacting events.

### Conclusion

Event sourcing is a powerful pattern that provides a clear history of changes, making it easier to understand and manage application state. By storing state changes as events, you gain flexibility and traceability, but it also requires careful design to handle complexity and data growth.

---

Here’s a simple example of an event sourcing design pattern using Java and Spring Boot. This example simulates a basic account management system where account transactions (deposits and withdrawals) are stored as events.

### Project Structure

```
event-sourcing-example/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           ├── Application.java
│   │   │           ├── domain/
│   │   │           │   ├── Account.java
│   │   │           │   ├── AccountEvent.java
│   │   │           │   └── EventStore.java
│   │   │           ├── service/
│   │   │           │   └── AccountService.java
│   │   │           └── controller/
│   │   │               └── AccountController.java
│   │   └── resources/
│   │       └── application.properties
└── pom.xml
```

### 1. `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>event-sourcing-example</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <java.version>17</java.version>
        <spring.boot.version>3.3.4</spring.boot.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### 2. `Application.java`

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3. Domain Classes

#### `Account.java`

```java
package com.example.domain;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private String id;
    private String owner;
    private double balance;
    private List<AccountEvent> events = new ArrayList<>();

    public Account(String id, String owner) {
        this.id = id;
        this.owner = owner;
        this.balance = 0;
    }

    public void applyEvent(AccountEvent event) {
        events.add(event);
        if (event instanceof DepositEvent) {
            this.balance += ((DepositEvent) event).getAmount();
        } else if (event instanceof WithdrawalEvent) {
            this.balance -= ((WithdrawalEvent) event).getAmount();
        }
    }

    public double getBalance() {
        return balance;
    }

    public List<AccountEvent> getEvents() {
        return events;
    }
}
```

#### `AccountEvent.java`

```java
package com.example.domain;

public abstract class AccountEvent {
    private final String accountId;

    public AccountEvent(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}

class DepositEvent extends AccountEvent {
    private final double amount;

    public DepositEvent(String accountId, double amount) {
        super(accountId);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}

class WithdrawalEvent extends AccountEvent {
    private final double amount;

    public WithdrawalEvent(String accountId, double amount) {
        super(accountId);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
```

#### `EventStore.java`

```java
package com.example.domain;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EventStore {
    private final Map<String, Account> accounts = new HashMap<>();

    public void save(String accountId, AccountEvent event) {
        Account account = accounts.getOrDefault(accountId, new Account(accountId, "Owner"));
        account.applyEvent(event);
        accounts.put(accountId, account);
    }

    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }
}
```

### 4. Service Layer

#### `AccountService.java`

```java
package com.example.service;

import com.example.domain.AccountEvent;
import com.example.domain.EventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    @Autowired
    private EventStore eventStore;

    public void deposit(String accountId, double amount) {
        AccountEvent event = new DepositEvent(accountId, amount);
        eventStore.save(accountId, event);
    }

    public void withdraw(String accountId, double amount) {
        AccountEvent event = new WithdrawalEvent(accountId, amount);
        eventStore.save(accountId, event);
    }

    public double getBalance(String accountId) {
        return eventStore.getAccount(accountId).getBalance();
    }
}
```

### 5. Controller Layer

#### `AccountController.java`

```java
package com.example.controller;

import com.example.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<String> deposit(@PathVariable String accountId, @RequestParam double amount) {
        accountService.deposit(accountId, amount);
        return ResponseEntity.ok("Deposit successful");
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<String> withdraw(@PathVariable String accountId, @RequestParam double amount) {
        accountService.withdraw(accountId, amount);
        return ResponseEntity.ok("Withdrawal successful");
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<Double> getBalance(@PathVariable String accountId) {
        double balance = accountService.getBalance(accountId);
        return ResponseEntity.ok(balance);
    }
}
```

### 6. `application.properties`

```properties
# H2 Database Configuration
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
```

### How to Run

1. **Set Up the Project**: Create a Maven project with the provided structure and files.
2. **Build and Run**: Use your IDE or command line to run the `Application` class.
3. **Test the API**: Use a tool like Postman or curl to interact with the endpoints:

   - **Deposit**: `POST /accounts/{accountId}/deposit?amount=100`
   - **Withdraw**: `POST /accounts/{accountId}/withdraw?amount=50`
   - **Get Balance**: `GET /accounts/{accountId}/balance`

### Explanation

- **Event Store**: Stores account events (deposits and withdrawals) and maintains the current state of accounts based on these events.
- **Account**: Represents the account entity and handles applying events to change its state.
- **Events**: `DepositEvent` and `WithdrawalEvent` represent specific changes to the account.
- **Service Layer**: Contains business logic for handling deposits and withdrawals.
- **Controller Layer**: Exposes REST endpoints for client interactions.

This example illustrates the event sourcing pattern by storing events to reconstruct the state of an account, allowing for historical tracking and auditing of transactions.

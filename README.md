# Delivery System — Course Project

Modular monolith built with **Java 25**, **Spring Boot 4.1**, **JavaFX 25**, **Maven** and **MySQL**.

This repository is the starting point for the *Desarrollo de Software 2* course project. It contains:

- `core-module` — shared kernel (domain building blocks, persistence base, JavaFX shell)
- `user-module` — reference implementation of a complete CRUD module
- `catalog-module`, `order-module`, `delivery-module`, `payment-module` — modules developed by four student teams
- `delivery-system` — desktop application that assembles all modules

## Quick start

```bash
# 1. Install the shared kernel and reference module
cd core-module && ./mvnw install -DskipTests
cd ../user-module && ./mvnw install -DskipTests

# 2. Install the team modules (placeholders for now)
cd ../catalog-module && ./mvnw install -DskipTests
cd ../order-module && ./mvnw install -DskipTests
cd ../delivery-module && ./mvnw install -DskipTests
cd ../payment-module && ./mvnw install -DskipTests

# 3. Run the desktop application in memory (no database needed)
cd ../delivery-system
./mvnw javafx:run -Pmemory
```

> On Windows use `mvnw.cmd` if Git Bash is not available.

## Full documentation

See [`delivery-system/README.md`](delivery-system/README.md) for:

- Team assignment and module scope
- Prerequisites and installation instructions
- How to run with MySQL
- Architecture and conventions every team must follow
- GitLab CI configuration

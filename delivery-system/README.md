# Delivery System

Modular monolith built with Java 25, Spring Boot 4.1, JavaFX 25, Maven and MySQL.

This repository contains the **starting point** for the course project. The `core-module` and `user-module`
are fully implemented and serve as the reference architecture. The remaining modules are developed by four
student teams during the course.

## Team assignment

| Team | Module | Base package | Order | Scope | Suggested size |
|------|--------|--------------|-------|-------|----------------|
| **Team A** | `delivery-module` | `org.jala.edu.delivery` | 400 | Couriers and deliveries | 4 members |
| **Team B** | `order-module` | `org.jala.edu.order` | 300 | Cart and orders | 4 members |
| **Team C** | `catalog-module` | `org.jala.edu.catalog` | 200 | Commerce and catalog | 3 members |
| **Team D** | `payment-module` | `org.jala.edu.payment` | 500 | Payments and promotions | 4 members |

> **Note for the professor:** `catalog-module` is the fastest module to implement because it is a straightforward
> CRUD similar to `user-module` and has no dependencies on other business modules. That is why it is assigned to
> the 3-member team.

## Prerequisites

- **JDK 25** (the project uses Java 25 features and Spring Boot 4.1 requires it)
- **Git**
- **Docker Desktop** or **Docker Engine** (only if you want to run with MySQL)
- A terminal with PowerShell, Bash or Git Bash

Verify your Java version:

```bash
java -version
```

Expected output: `java version "25"` or higher.

### Linux (Ubuntu) setup

The project requires **JDK 25** (both `java` and `javac`). Installing only the JRE is not enough, because Maven needs `javac` to compile.

#### 1. Install OpenJDK 25

```bash
sudo apt update
sudo apt install openjdk-25-jdk
```

#### 2. Make Java 25 the default version

If you have other JDKs installed (e.g. 21 or 27), select the `java-25-openjdk-amd64` entry for both tools:

```bash
sudo update-alternatives --config java
sudo update-alternatives --config javac
```

Or set them directly without the interactive menu:

```bash
sudo update-alternatives --set java  /usr/lib/jvm/java-25-openjdk-amd64/bin/java
sudo update-alternatives --set javac /usr/lib/jvm/java-25-openjdk-amd64/bin/javac
```

#### 3. Configure `JAVA_HOME`

The Maven wrapper uses `JAVA_HOME`. Add it to `~/.bashrc` so it is loaded in every terminal (run this only once to avoid duplicate lines):

```bash
echo 'export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64' >> ~/.bashrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

#### 4. Verify the installation

```bash
java --version
javac --version
echo $JAVA_HOME
```

`java` and `javac` must both report version `25.x`, and `JAVA_HOME` must print `/usr/lib/jvm/java-25-openjdk-amd64`.

#### 5. Give execute permission to the Maven wrapper

After cloning, `mvnw` may not be executable (`Permission denied`). From the repository root, run:

```bash
chmod +x core-module/mvnw user-module/mvnw catalog-module/mvnw order-module/mvnw \
         delivery-module/mvnw payment-module/mvnw delivery-system/mvnw
```

Or, from inside each module folder:

```bash
chmod +x mvnw
```

## Quick start (no database needed)

The application can run entirely in memory. This is the fastest way to verify the installation and start
development.

### 1. Clone the repository

```bash
git clone <your-gitlab-repository-url>
cd delivery
```

### 2. Install the modules in dependency order

Each module is an independent Maven project. They must be installed into your local Maven repository (`~/.m2`)
before `delivery-system` can use them.

```bash
cd core-module && ./mvnw install -DskipTests
cd ../user-module && ./mvnw install -DskipTests
cd ../catalog-module && ./mvnw install -DskipTests
cd ../order-module && ./mvnw install -DskipTests
cd ../delivery-module && ./mvnw install -DskipTests
cd ../payment-module && ./mvnw install -DskipTests
```

> On Windows use `mvnw.cmd` instead of `./mvnw` if Git Bash is not available.

#### Windows command prompt

If you are using `cmd.exe` or PowerShell and `mvnw.cmd` fails with:

```
'powershell' is not recognized as an internal or external command
Cannot start maven from wrapper
```

Add PowerShell to the current terminal session first:

```cmd
set PATH=C:\Windows\System32\WindowsPowerShell\v1.0;%PATH%
```

Or, in PowerShell:

```powershell
$env:PATH = "C:\Windows\System32\WindowsPowerShell\v1.0;" + $env:PATH
```

Then run the Windows version of the commands:

```cmd
cd core-module && mvnw.cmd install -DskipTests
cd ..\user-module && mvnw.cmd install -DskipTests
cd ..\catalog-module && mvnw.cmd install -DskipTests
cd ..\order-module && mvnw.cmd install -DskipTests
cd ..\delivery-module && mvnw.cmd install -DskipTests
cd ..\payment-module && mvnw.cmd install -DskipTests
```

### 3. Run the desktop application

```bash
cd ../delivery-system
./mvnw javafx:run -Pmemory
```

On Windows (cmd/PowerShell):

```cmd
cd ..\delivery-system
mvnw.cmd javafx:run -Pmemory
```

The main window opens with a navigation bar. The **Users** module is already functional; the other modules show
placeholder views until each team implements them.

## Running with MySQL

If you want to test with a real database:

```bash
cd delivery-system
docker compose up -d
./mvnw javafx:run
```

The default database settings are in [`application.yml`](src/main/resources/application.yml). You can override
them with environment variables:

```bash
$env:DB_URL="jdbc:mysql://localhost:3306/delivery_system"
$env:DB_USERNAME="delivery"
$env:DB_PASSWORD="delivery"
./mvnw javafx:run
```

## Repositories

The system is split into one folder per module. This repository (`delivery-system`) is the desktop
application: it depends on the other modules, assembles them and starts the JavaFX UI.

| Module folder      | Base package          | Purpose                                                                  | Status |
|--------------------|-----------------------|--------------------------------------------------------------------------|--------|
| `core-module`      | `org.jala.edu.core`   | Shared kernel: base model, generic persistence, exceptions, JavaFX shell | Done |
| `user-module`      | `org.jala.edu.user`   | Users (CRUD)                                                             | Reference implementation |
| `catalog-module`   | `org.jala.edu.catalog`| Catalog of products/services                                             | Team A |
| `order-module`     | `org.jala.edu.order`  | Cart and delivery orders                                                 | Team B |
| `delivery-module`  | `org.jala.edu.delivery`| Couriers and deliveries                                                 | Team C |
| `payment-module`   | `org.jala.edu.payment`| Payments and promotions                                                  | Team D |
| `delivery-system`  | `org.jala.edu`        | Desktop application: assembles every module and launches the JavaFX UI   | Done |

Business modules depend only on `core-module`, never on each other. Only `delivery-system` depends on all of them.
Modules share their jars through the local Maven repository (`~/.m2`), as `org.jala.edu:<module>:0.0.1-SNAPSHOT`;
the versions used are properties in this `pom.xml` (`core-module.version`, `user-module.version`,
`catalog-module.version`, `order-module.version`, `delivery-module.version`, `payment-module.version`).

## Port/Adapter layout (per module)

```
org.jala.edu.<module>
├── domain                    # Pure Java: model, value objects, domain exceptions
├── application
│   ├── port/in               # Commands and queries: the input data of the use cases
│   ├── port/out              # What the module needs from the outside (driven ports)
│   └── usecase               # One class per use case with an execute(...) method (no Spring annotations)
└── infrastructure
    ├── adapter/in/ui                   # JavaFX controllers (+ .fxml views under the same path in resources)
    ├── adapter/out/persistence/jpa     # JPA entities, Spring Data repositories, mappers
    ├── adapter/out/persistence/memory  # In-memory repositories (delivery.persistence.type=memory)
    └── config                          # Spring wiring of use cases and UI navigation
```

Dependencies point inward: `infrastructure → application → domain`. The JavaFX controllers only call use cases,
so business rules live in one place and new delivery channels can be added as extra inbound adapters.

## Team conventions

Every team must follow these rules so the modules integrate cleanly at the end of the course.

### Module boundaries

- Business modules depend **only** on `core-module`. They never depend on each other.
- Only `delivery-system` depends on all business modules.
- Reference other modules only by `UUID`. For example, an order stores `catalogItemId` and `customerId`, not a
  `CatalogItem` or `User` object.

### Domain-driven files

- Every aggregate root extends `BaseModel<ID>`.
- Every identifier is a value object or `UUID` wrapped in a record (see `UserId`).
- Every business rule violation throws a subclass of `DomainException` (`ConflictException` or `NotFoundException`).
- Models are framework-free: no Spring, no JavaFX, no JPA annotations in `domain`.

### Use cases

- One class per use case in `application/usecase`.
- Implement `UseCase<Input, Output>`.
- Receive the outbound port through the constructor.
- No Spring annotations inside use cases.

### Persistence

- Define a module-specific port that extends `CrudRepositoryPort<T, ID>`.
- Provide two adapters:
  - JPA adapter under `adapter/out/persistence/jpa` annotated with `@ConditionalOnJpaPersistence`
  - In-memory adapter under `adapter/out/persistence/memory` annotated with `@ConditionalOnInMemoryPersistence`
- Add Flyway migrations under `src/main/resources/db/migration/<module>/` using timestamp versions
  (`V<yyyyMMddHHmm>__description.sql`) so versions never collide across teams.

### UI

- Add one `NavigationItem` bean with `@Order` in the team's range:
  - Catalog: 200–299
  - Orders: 300–399
  - Deliveries: 400–499
  - Payments: 500–599
- Keep all FXML paths in a single `*Views` class.
- Controllers are `@Component @Scope("prototype")` and receive use cases through their constructor.
- Run blocking work through `BackgroundTasks`.

### Testing

- Unit-test use cases against the in-memory adapter.
- Integration-test the JPA adapter with H2 in MySQL mode and the real Flyway migration
  (see `UserPersistenceAdapterTest`).
- Test that every FXML file exists on the classpath (see `UserUiConfigurationTest`).

## Development workflow

Each team works in its own module folder. The recommended weekly flow is:

1. **Design the domain** (models, value objects, exceptions).
2. **Define ports** (commands/queries in `port/in`, repository in `port/out`).
3. **Implement use cases** and unit-test them.
4. **Implement persistence** (JPA + Flyway, then memory).
5. **Implement UI** (FXML + controller).
6. **Install the module** with `./mvnw install` and run `delivery-system` to verify integration.

Before pushing changes, make sure your module still installs successfully:

```bash
cd <your-module>
./mvnw clean install
```

## Use cases

Each use case is a plain class in `application/usecase` that implements `UseCase<Input, Output>` (core-module,
`org.jala.edu.core.application.usecase`), whose single method `Output execute(Input input)` fixes the use case's
parameter and return types. It receives the repository port through its constructor, is registered as a `@Bean` in
the module's configuration class (e.g. `UserModuleConfiguration`), and is injected directly into the JavaFX
controllers. Ids cross the use case boundary as `UUID`; use cases without a result declare `Void` as output.

| Use case | Implements | Rules |
|---|---|---|
| `CreateUserUseCase` | `UseCase<CreateUserCommand, UUID>` | email must not be registered yet |
| `ListUsersUseCase` | `UseCase<ListUsersQuery, List<User>>` | oldest first |
| `UpdateUserUseCase` | `UseCase<UpdateUserCommand, Void>` | email cannot change; `UserNotFoundException` if missing |
| `DeleteUserUseCase` | `UseCase<UUID, Void>` | `UserNotFoundException` if missing |

## Shared persistence (core-module)

Storage is generic so each model only writes what is specific to it:

| Layer | Core base | What the module writes (user example) |
|---|---|---|
| Domain | `BaseModel<ID>` — id, `createdAt`, `updatedAt`, `touch()`, equality by id | `User extends BaseModel<UserId>` |
| Outbound port | `CrudRepositoryPort<T, ID>` | `UserRepositoryPort` + `existsByEmail` |
| Memory adapter | `InMemoryCrudRepository` | `InMemoryUserRepositoryAdapter` + `existsByEmail` |
| JPA adapter | `JpaCrudRepositoryAdapter`, `BaseJpaEntity` (timestamps), `BaseEntityRepository`, `PersistenceMapper` | `UserPersistenceAdapter`, `UserJpaEntity`, `UserJpaRepository`, `UserPersistenceMapper` |

`findAll` always returns models oldest first. UI helpers for screens live in `core.infrastructure.ui`:
`BackgroundTasks`, `Dialogs` (modal / confirm), `ErrorMessages`, `FormBindings`, `EnumLabelConverter`, `DisplayFormat`.

## Desktop UI (JavaFX)

The app starts the Spring context and then opens the main window. The window shell lives in `core-module`
(`org.jala.edu.core.infrastructure.ui`): a navigation bar with **one button per module**. A button opens the module's
**main view**, which works as the module's menu and leads to its screens. Each module adds its button by declaring
a `NavigationItem` bean that points to its main view:

```java
@Bean
@Order(100)
public NavigationItem userNavigationItem() {
    return new NavigationItem("Users", UserViews.MAIN);
}
```

Buttons appear in `@Order` order; each module uses its own range (Users 100–199, Catalog 200–299, Orders 300–399, Deliveries 400–499, Payments 500–599).
The button of the open module stays highlighted.

| Button | Main view | Screens reachable from it |
|---|---|---|
| Users (`user-module`) | `user-main.fxml` | Create user, List users (Edit opens a dialog — also on double-click — and Delete asks for confirmation) |
| Catalog (`catalog-module`) | `catalog-main.fxml` | none yet |
| Orders (`order-module`) | `order-main.fxml` | none yet |
| Deliveries (`delivery-module`) | `delivery-main.fxml` | none yet |
| Payments (`payment-module`) | `payment-main.fxml` | none yet |

Views inside a module switch screens with the `Navigator` bean (`navigator.show(UserViews.CREATE)`), which replaces
the content area of the main window. Keep a module's view paths in one class (e.g. `UserViews`).

FXML controllers are Spring beans (`@Component @Scope("prototype")`), so they get use cases injected through their
constructor. Views without logic (like the catalog main view) need no controller. Long-running work (use case calls)
runs off the UI thread through `BackgroundTasks`.

## Database migrations

Flyway runs migrations from `classpath:db/migration`, scanned recursively. Each module keeps its own scripts under
`src/main/resources/db/migration/<module>/`, named with a timestamp version (`V202609250001__create_users_table.sql`)
so versions never collide across modules.

## Building

Install the modules into `~/.m2` in dependency order. Each module has its own Maven wrapper.

```bash
cd core-module && ./mvnw install
cd ../user-module && ./mvnw install
cd ../catalog-module && ./mvnw install
cd ../order-module && ./mvnw install
cd ../delivery-module && ./mvnw install
cd ../payment-module && ./mvnw install
```

> **Windows users:** if Git Bash is not available, use `mvnw.cmd` instead of `./mvnw`.

After installing all modules, build and run the desktop application:

```bash
cd ../delivery-system
./mvnw javafx:run -Pmemory
```

Re-install a module every time you change it, before running `delivery-system` again:

```bash
cd catalog-module && ./mvnw install
./mvnw javafx:run -Pmemory
```

## Running

From this repository, with Maven:

```bash
# In-memory persistence (-Pmemory sets -Ddelivery.persistence.type=memory): no database needed
./mvnw javafx:run -Pmemory

# MySQL persistence (compose.yaml is in this repository)
docker compose up -d
./mvnw javafx:run
```

Database settings can be overridden with `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`.
Extra Spring arguments can be passed with `-Djavafx.args="..."`.

As a packaged jar:

```bash
./mvnw clean package
java --enable-native-access=ALL-UNNAMED -jar target/delivery-system.jar
java --enable-native-access=ALL-UNNAMED -Ddelivery.persistence.type=memory -jar target/delivery-system.jar
```

### Persistence adapters

Repository ports (e.g. `UserRepositoryPort`) have two adapters. The property `delivery.persistence.type` selects
which one is used; it can be set as a system property, an environment variable or in `application.yml`:

| `delivery.persistence.type` | Adapter                                           | Storage   |
|-----------------------------|---------------------------------------------------|-----------|
| `jpa` _(default)_           | `adapter/out/persistence/jpa` (JPA + Flyway)      | MySQL     |
| `memory`                    | `adapter/out/persistence/memory` (in-memory map)  | In memory |

```bash
java -Ddelivery.persistence.type=memory -jar target/delivery-system.jar   # system property
DELIVERY_PERSISTENCE_TYPE=memory java -jar target/delivery-system.jar     # environment variable
```

Values are case-insensitive; an unknown value stops the application at startup with the list of accepted values.

How it works (all in `core-module`, `org.jala.edu.core.infrastructure.config`):

- `PersistenceType`: the property name and its values.
- `@ConditionalOnJpaPersistence` / `@ConditionalOnInMemoryPersistence`: put one on each adapter so only the
  selected one becomes a bean.
- `PersistenceTypeEnvironmentPostProcessor`: validates the value at startup and, for `memory`, excludes the DataSource,
  JPA and Flyway auto-configuration so the application starts without a database.

The JPA adapters are tested against H2 in MySQL mode with the real Flyway migrations
(`UserPersistenceAdapterTest`), so no MySQL server is needed to run `./mvnw verify`.

## GitLab CI

A [`.gitlab-ci.yml`](../.gitlab-ci.yml) is included at the repository root. It builds `core-module` first,
then `user-module`, then each team module in parallel, and finally assembles `delivery-system`. The pipeline
uses the Maven wrapper and caches the local repository, so no Maven installation is required on runners.

Pipelines validate that every module compiles and that the full desktop application can be packaged. Each team
should make sure their module passes the `build-*` job for their module before merging changes.

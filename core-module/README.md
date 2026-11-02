# core-module

Shared kernel of the Delivery System (`org.jala.edu.core`). Every other repository depends on it.

- **Domain:** `BaseModel<ID>` (id, `createdAt`, `updatedAt`, equality by id) and base exceptions
  (`DomainException`, `NotFoundException`, `ConflictException`).
- **Application:** `UseCase<Input, Output>` and the generic outbound port `CrudRepositoryPort<T, ID>`.
- **Integration contracts:** `UserService`, `CurrentUserService`, `CatalogService`, `PaymentService`, `OrderService`,
  `UserRole` and `SampleUsers` (`org.jala.edu.core.contract`), plus a fallback for each one until its module
  implements it (`ContractFallbackAutoConfiguration`).
- **Persistence:** in-memory and JPA base adapters, plus the `delivery.persistence.type` switch
  (`PersistenceType`, `@ConditionalOnJpaPersistence`, `@ConditionalOnInMemoryPersistence`).
- **JavaFX shell:** optional login screen before the main window (`LoginScreen`, `AppShell`, `SignOutEvent`), main
  window with one navigation button per module filtered by role (`NavigationItem`, `NavigationFilter`, `Navigator`)
  and UI helpers
  (`BackgroundTasks`, `Dialogs`, `ErrorMessages`, `FormBindings`, `EnumLabelConverter`, `DisplayFormat`).

## Current user

Inject `CurrentUserService` in a use case to know who performs an operation, instead of a fixed `SampleUsers` id:

```java
CurrentUser user = currentUserService.currentUser(); // id, role and displayName; throws NotAuthenticatedException
```

Keep validating permissions with `UserService.isActiveWithRole`. Until `user-module` provides the login, the fallback
answers with the sample user of the role in `delivery.sample-user` (`ADMIN`, `MERCHANT`, `CUSTOMER` or `COURIER`;
`CUSTOMER` by default). With `javafx:run`, set it in `application.yml` or as the environment variable
`DELIVERY_SAMPLE_USER=COURIER`, because the plugin does not forward `-D` properties to the application.

## Build

```bash
./mvnw install
```

Installs `org.jala.edu:core-module:0.0.1-SNAPSHOT` into the local Maven repository (`~/.m2`), where `user-module`,
`catalog-module` and `delivery-system` pick it up. Requires JDK 25.

The architecture of the whole system is described in the `delivery-system` repository README.

# user-module

User management for the Delivery System (`org.jala.edu.user`), built with the port/adapter layout.

- **Use cases:** `CreateUserUseCase`, `ListUsersUseCase`, `UpdateUserUseCase`, `ChangeUserStatusUseCase`,
  `CheckUserAccessUseCase`. Users are deactivated, never deleted, and the last active ADMIN cannot be deactivated or
  demoted (FUN-003).
- **Status history (#82, CU-USR-07):** every activation or deactivation, from the list or the edit form, records
  the previous and new status, who made it (`CurrentUserService`) and when (`UserStatusChange`,
  `UserStatusHistoryPort`, table `user_status_changes`). Only an ADMIN can see it (`ListUserStatusHistoryUseCase`).
- **Contract:** serves core's `UserService` to the other modules (`UserServiceAdapter`): the access check, and since
  #85 `findUser` and `findActiveByRole` (`FindUserUseCase`, `ListActiveUsersByRoleUseCase`), which return users only
  as core's `UserSummary`. It also stores the core
  `SampleUsers` at startup (`SampleUsersLoader`): one active user per role plus an inactive customer and an
  inactive courier (`inactive.courier@sample.delivery`), so other modules can test the rejection of inactive users.
- **Persistence:** JPA adapter (MySQL, Flyway migration in `db/migration/user`) and in-memory adapter,
  selected with `delivery.persistence.type`.
- **Validation:** valid email (`Email`) and optional phone in E.164 format, `^\+[1-9]\d{1,14}$`
  (`PhoneNumber`, e.g. `+59170000000`).
- **Password (D-10):** required when registering, at least 8 characters, stored only as a BCrypt hash
  (`PasswordHasher`, `BCryptPasswordHasher`). Sample users share the test password `Sample123`
  (`SampleUsersLoader.SAMPLE_PASSWORD`); never use it for a real account.
- **One transaction (RNF-1.05):** changing a status (and editing a user whose status changes) stores the user and
  the history row through core's `TransactionRunner`, so with MySQL both are saved or neither is.
- **Administrator only (RF-1.14):** creating, editing, listing and activating or deactivating users check inside the
  use case that an ADMIN is signed in (`AdminOnly`), not only the menu; any other role gets "Only an administrator
  can do this". The customer sign-up reuses the creation rules without that check.
- **Login (CU-USR-06):** `LoginUseCase` checks email, password and status; `SessionCurrentUserService` serves core's
  `CurrentUserService` to the other modules. The login screen opens before the main window. Each lookup reloads the
  signed-in user (#88): if the administrator deactivates them, the session ends at once (`currentUser()` throws
  `NotAuthenticatedException`), and a new role or name applies to the next operation. The session also ends after
  `delivery.session.idle-timeout` (30 minutes by default) without activity (RF-1.15).
- **Sign-up (CU-USR-08, #89):** "New customer? Create an account" on the login opens `sign-up.fxml`; `SignUpUseCase`
  creates an active **CUSTOMER** (other roles are still created by an administrator) and signs them in.
- **My account (RF-1.07, RF-1.08, CU-USR-09):** the "My account" button, which every role sees, shows the signed-in
  user's email, role and status, lets them edit their own name and phone (`UpdateMyProfileUseCase`; email and role
  stay with the administrator) and change their password after typing the current one (`ChangePasswordUseCase`).
- **UI:** "Users" navigation button → Users main view → Create user, List users (search by email, filter by role
  and status, edit dialog, Activate/Deactivate, status history).

## Build

Depends on `core-module`, so install that first (`./mvnw install` in its repository). Then:

```bash
./mvnw install
```

Installs `org.jala.edu:user-module:1.0.0` into `~/.m2` for `delivery-system`. Tests need no database:
the JPA adapter runs against H2 in MySQL mode with the real Flyway migration. Requires JDK 25.

The architecture of the whole system is described in the `delivery-system` repository README.

# delivery-module

Deliveries of the Delivery System (`org.jala.edu.delivery`). It contributes the "Deliveries" navigation button, the module's
main view and the **Delivery history** screen.

## Delivery history

Lets a courier review every delivery they have finished, with its order, locality, delivery date and amount, plus the total
amount of all of them. It is opened from the "Delivery history" button of the Deliveries main view.

- The "Sort by" list chooses the order: most recent (the default), oldest first or highest amount. Each order is a value of
  `DeliverySortOrder`, which carries its own comparator (Strategy pattern); a new order only needs a new value there.

- Only an active user with the `COURIER` role can see a history (checked through `UserService` from `core-module`); anyone
  else gets "User not authorized for this operation" and nothing is shown.
- A courier without finished deliveries sees "Your completed deliveries will appear in this panel."
- There is no login yet, so the screen asks for the courier id; it starts with the sample courier
  (`00000000-0000-0000-0000-000000000004`).
- The deliveries are kept in memory and the application starts with four sample deliveries of the sample courier. Finished
  deliveries will be added to the same store when deliveries can be completed from the application.

Use case: `GetDeliveryHistoryUseCase` (`CU-04`).

## Build

Depends on `core-module`, so install that first (`./mvnw install` in its repository). Then:

```bash
./mvnw install
```

Installs `org.jala.edu:delivery-module:0.0.1-SNAPSHOT` into `~/.m2` for `delivery-system`. Requires JDK 25.

The architecture of the whole system is described in the `delivery-system` repository README.

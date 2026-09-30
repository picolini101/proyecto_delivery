# order-module

Delivery orders of the Delivery System (`org.jala.edu.order`). For now it only contributes the "Orders" navigation
button and the module's main view; order screens will be added to that view.

## Build

Depends on `core-module`, so install that first (`./mvnw install` in its repository). Then:

```bash
./mvnw install
```

Installs `org.jala.edu:order-module:0.0.1-SNAPSHOT` into `~/.m2` for `delivery-system`. Requires JDK 25.

The architecture of the whole system is described in the `delivery-system` repository README.

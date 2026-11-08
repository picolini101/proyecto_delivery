package org.jala.edu.order.application.port.out;

import java.util.UUID;

public interface UserGateway {
    boolean isAuthorizedCustomer(UUID customerId);
}

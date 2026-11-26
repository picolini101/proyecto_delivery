package org.jala.edu.order.infrastructure.adapter.in.ui;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Mantiene en memoria el contexto del último pedido confirmado para posteriormente usarlo en el siguinete panel.
 */
@Component
public class OrderConfirmationContext {

    private UUID lastConfirmedOrderId;

    public UUID getLastConfirmedOrderId() {
        return lastConfirmedOrderId;
    }

    public void setLastConfirmedOrderId(UUID lastConfirmedOrderId) {
        this.lastConfirmedOrderId = lastConfirmedOrderId;
    }
}

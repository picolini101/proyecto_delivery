package org.jala.edu.order.domain.model;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    READY_FOR_DELIVERY,
    ASSIGNED,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus nextStatus) {
        if (nextStatus == null) {
            return false;
        }
        switch (this) {
            case CREATED:
                return nextStatus == CONFIRMED || nextStatus == CANCELLED;
            case CONFIRMED:
                return nextStatus == READY_FOR_DELIVERY || nextStatus == CANCELLED;
            case READY_FOR_DELIVERY:
                return nextStatus == ASSIGNED;
            case ASSIGNED:
                return nextStatus == ON_THE_WAY;
            case ON_THE_WAY:
                return nextStatus == DELIVERED;
            case DELIVERED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }

    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED;
    }

}

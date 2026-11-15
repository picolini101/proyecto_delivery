package org.jala.edu.order.infrastructure.adapter.in.ui;

/** Los textos de una carta de pedido cancelado, ya listos para mostrar. */
public record FailedOrderCard(String title, String status, String reason, String detail, String date, String hint) {
}

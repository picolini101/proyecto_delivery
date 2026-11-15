package org.jala.edu.order.infrastructure.adapter.in.ui;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.jala.edu.order.application.dto.FailedPaymentItem;

/** Decide QUE texto ve el cliente en la carta de un pedido cancelado. No usa JavaFX. */
public class FailedOrderCardPresenter {

    private final DateTimeFormatter dateFormat;

    /** @param zone zona horaria con la que se muestra la fecha y la hora */
    public FailedOrderCardPresenter(ZoneId zone) {
        this.dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(zone);
    }

    /** Usa la zona horaria del equipo del cliente. */
    public FailedOrderCardPresenter() {
        this(ZoneId.systemDefault());
    }

    public FailedOrderCard present(FailedPaymentItem item) {
        return new FailedOrderCard(
                "Pedido #" + item.orderId().toString().substring(0, 8).toUpperCase(),
                "Cancelado",
                "Motivo: " + (isBlank(item.cancellationReason()) ? "sin motivo registrado" : item.cancellationReason().strip()),
                isBlank(item.paymentDetail()) ? "" : "Detalle: " + item.paymentDetail().strip(),
                dateFormat.format(item.attemptedAt()),
                "Puedes iniciar una nueva compra");
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}

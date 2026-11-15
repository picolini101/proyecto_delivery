package org.jala.edu.order.infrastructure.adapter.in.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.jala.edu.order.application.dto.FailedPaymentItem;
import org.junit.jupiter.api.Test;

class FailedOrderCardPresenterTest {

    private static final UUID ORDER_ID = UUID.fromString("3f2a91bc-0000-0000-0000-000000000001");
    private static final Instant WHEN = Instant.parse("2026-11-15T20:30:00Z");

    /** Zona fija para que el test no dependa de la zona horaria de quien lo corre. */
    private final FailedOrderCardPresenter presenter = new FailedOrderCardPresenter(ZoneId.of("UTC"));

    @Test
    void cardShowsTheCancelledOrderData() {
        FailedOrderCard card = presenter.present(
                new FailedPaymentItem(ORDER_ID, "Pago rechazado", "Fondos insuficientes", WHEN));

        assertEquals("Pedido #3F2A91BC", card.title());
        assertEquals("Cancelado", card.status());
        assertEquals("Motivo: Pago rechazado", card.reason());
        assertEquals("Detalle: Fondos insuficientes", card.detail());
        assertEquals("15/11/2026 20:30", card.date());
        assertEquals("Puedes iniciar una nueva compra", card.hint());
    }

    @Test
    void cardWithoutPaymentDetailHasNoDetailText() {
        FailedOrderCard card = presenter.present(new FailedPaymentItem(ORDER_ID, "Pago rechazado", "  ", WHEN));

        assertEquals("", card.detail());
    }

    @Test
    void cardWithoutReasonSaysSoInsteadOfShowingNull() {
        FailedOrderCard card = presenter.present(new FailedPaymentItem(ORDER_ID, null, null, WHEN));

        assertEquals("Motivo: sin motivo registrado", card.reason());
    }
}

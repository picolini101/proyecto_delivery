package org.jala.edu.core.infrastructure.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.jala.edu.core.contract.PaymentResult;
import org.jala.edu.core.contract.SamplePaymentMethods;
import org.jala.edu.core.contract.SampleUsers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FallbackPaymentServiceTest {

    private final FallbackPaymentService paymentService = new FallbackPaymentService();

    @ParameterizedTest
    @CsvSource({"0.01, true", "1000.00, true", "0, false", "-5, false", "1000.01, false"})
    void approvesOnlyAmountsAboveZeroUpToTheMaximum(String amount, boolean expectedApproval) {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        PaymentResult result = paymentService.pay(orderId, SampleUsers.CUSTOMER, SamplePaymentMethods.CUSTOMER_CARD,
                new BigDecimal(amount));

        // Assert
        assertThat(result.approved()).isEqualTo(expectedApproval);
        assertThat(result.paymentReference()).contains(orderId.toString());
    }

    @Test
    void rejectsAPaymentWithoutMethod() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        PaymentResult result = paymentService.pay(orderId, SampleUsers.CUSTOMER, null, new BigDecimal("10.00"));

        // Assert
        assertThat(result.approved()).isFalse();
        assertThat(result.reason()).isEqualTo("Payment method is required");
    }
}

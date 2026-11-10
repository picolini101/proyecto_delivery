package org.jala.edu.payment.infrastructure.persistence.repository;

import org.jala.edu.payment.infrastructure.persistence.entity.PaymentMethodEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SpringDataPaymentMethodRepositoryTest {

    @Autowired
    private SpringDataPaymentMethodRepository repository;

    @Test
    @DisplayName("Debe guardar y consultar un método de pago por userId usando JPA")
    void shouldSaveAndFindByUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        PaymentMethodEntity entity = new PaymentMethodEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setAlias("Mi Tarjeta JPA");
        entity.setType("TARJETA");
        entity.setMaskedNumber("**** **** **** 1234");
        entity.setLastRegistered(true);
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());

        // Act
        repository.save(entity);

        // Assert
        List<PaymentMethodEntity> result = repository.findByUserId(userId);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAlias()).isEqualTo("Mi Tarjeta JPA");
        assertThat(result.get(0).isLastRegistered()).isTrue();
    }
}
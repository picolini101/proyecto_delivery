package org.jala.edu.payment.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.jala.edu.payment.domain.model.Alias;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.jala.edu.payment.domain.model.PaymentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

// Corre el JPA adapter con H2 en MySQL mode, con el esquema creado por la migración real del módulo Flyway.

@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:payments;MODE=MySQL;DATABASE_TO_LOWER=TRUE")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PaymentMethodRepositoryAdapter.class)
class PaymentMethodRepositoryAdapterTest {

    @Autowired
    private PaymentMethodRepositoryAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByUserIdReadsBackEveryField() {
        // Preparación con inicialización de datos de prueba (AAA)
        UUID userId = UUID.randomUUID();
        PaymentMethod saved = adapter.save(
                PaymentMethod.createNew(userId, new Alias("Mi Tarjeta"), PaymentType.TARJETA, "4111111111111234"));
        detach();

        // Actuar con ejecición y Verificar con validación de resultados
        assertThat(adapter.findByUserId(userId)).singleElement().satisfies(found -> {
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getUserId()).isEqualTo(userId);
            assertThat(found.getAlias().getValue()).isEqualTo("Mi Tarjeta");
            assertThat(found.getType()).isEqualTo(PaymentType.TARJETA);
            assertThat(found.getMaskedNumber()).isEqualTo("**** **** **** 1234");
            assertThat(found.isLastRegistered()).isTrue();
            assertThat(found.isActive()).isTrue();
        });
    }

    @Test
    void onlyTheLastRegisteredMethodKeepsTheFlag() {
        // Preparación de CA1 y CA2
        UUID userId = UUID.randomUUID();
        PaymentMethod first = adapter.save(
                PaymentMethod.createNew(userId, new Alias("Primera"), PaymentType.TARJETA, "1111"));
        detach();

        // Ejecución
        adapter.clearLastRegisteredFlag(userId);
        PaymentMethod second = adapter.save(
                PaymentMethod.createNew(userId, new Alias("Segunda"), PaymentType.TRANSFERENCIA, "2222"));
        detach();

        // Validación
        assertThat(adapter.findLastRegisteredByUserId(userId))
                .hasValueSatisfying(last -> assertThat(last.getId()).isEqualTo(second.getId()));
        assertThat(adapter.findById(first.getId()))
                .hasValueSatisfying(old -> assertThat(old.isLastRegistered()).isFalse());
    }

    @Test
    void findByUserIdIgnoresOtherUsersMethods() {
        // Preparación
        UUID userId = UUID.randomUUID();
        adapter.save(PaymentMethod.createNew(userId, new Alias("Mia"), PaymentType.TARJETA, "1234"));
        adapter.save(PaymentMethod.createNew(UUID.randomUUID(), new Alias("Ajena"), PaymentType.TARJETA, "5678"));
        detach();

        // Ejecución y validación.
        assertThat(adapter.findByUserId(userId)).hasSize(1);
    }

    // Escribe los cambios pendientes y vacia el contexto de la persistencia para que la siguiente lectura consulte a la base de datos.
    private void detach() {
        entityManager.flush();
        entityManager.clear();
    }
}

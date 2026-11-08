package org.jala.edu.system;

import static org.assertj.core.api.Assertions.assertThat;

import org.jala.edu.DeliverySystemApplication;
import org.jala.edu.delivery.application.port.out.CourierRepositoryPort;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Starts the whole application with {@code delivery.persistence.type=jpa} (H2 in MySQL mode, real migrations), so a
 * repository port without a JPA adapter or a bridge fails the pipeline instead of {@code ./mvnw javafx:run -Pjpa}.
 */
@SpringBootTest(classes = DeliverySystemApplication.class, properties = {
        "delivery.persistence.type=jpa",
        "spring.datasource.url=jdbc:h2:mem:delivery-system;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password="})
class JpaModeStartupTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void startsWithEveryRepositoryPortInJpaMode() {
        // Assert: ports without a JPA adapter yet are served by the in-memory bridge
        assertThat(context.getBean(CartRepositoryPort.class)).isNotNull();
        assertThat(context.getBean(CourierRepositoryPort.class)).isNotNull();
        assertThat(context.getBean(OrderRepositoryPort.class)).isNotNull();
    }
}

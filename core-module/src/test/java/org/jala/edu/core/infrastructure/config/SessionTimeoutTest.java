package org.jala.edu.core.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SessionTimeoutTest {

    @Test
    void defaultsToThirtyMinutes() {
        // Arrange
        MockEnvironment environment = new MockEnvironment();

        // Act + Assert
        assertThat(SessionTimeout.from(environment)).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void readsDurationsLikeOneHourAndZeroToDisable() {
        // Arrange
        MockEnvironment oneHour = new MockEnvironment().withProperty(SessionTimeout.PROPERTY, "1h");
        MockEnvironment disabled = new MockEnvironment().withProperty(SessionTimeout.PROPERTY, "0");

        // Act + Assert
        assertThat(SessionTimeout.from(oneHour)).isEqualTo(Duration.ofHours(1));
        assertThat(SessionTimeout.from(disabled)).isEqualTo(Duration.ZERO);
    }
}

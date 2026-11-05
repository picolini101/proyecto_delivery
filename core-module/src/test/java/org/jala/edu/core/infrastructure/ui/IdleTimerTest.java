package org.jala.edu.core.infrastructure.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class IdleTimerTest {

    private final MovableClock clock = new MovableClock(Instant.parse("2026-11-09T10:00:00Z"));

    @Test
    void expiresOnlyAfterTheTimeoutWithoutActivity() {
        // Arrange
        IdleTimer timer = new IdleTimer(Duration.ofMinutes(30), clock);

        // Act + Assert
        clock.advance(Duration.ofMinutes(30));
        assertThat(timer.expired()).isFalse();
        clock.advance(Duration.ofSeconds(1));
        assertThat(timer.expired()).isTrue();
    }

    @Test
    void activityRestartsTheCount() {
        // Arrange
        IdleTimer timer = new IdleTimer(Duration.ofMinutes(30), clock);
        clock.advance(Duration.ofMinutes(20));

        // Act
        timer.touch();
        clock.advance(Duration.ofMinutes(20));

        // Assert
        assertThat(timer.expired()).isFalse();
    }

    @Test
    void aZeroTimeoutNeverExpires() {
        // Arrange
        IdleTimer timer = new IdleTimer(Duration.ZERO, clock);

        // Act
        clock.advance(Duration.ofDays(1));

        // Assert
        assertThat(timer.expired()).isFalse();
    }

    /**
     * A clock the test moves forward by hand.
     */
    static final class MovableClock extends Clock {

        private Instant now;

        MovableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}

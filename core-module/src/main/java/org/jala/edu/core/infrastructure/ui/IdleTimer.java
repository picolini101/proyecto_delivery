package org.jala.edu.core.infrastructure.ui;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Remembers the last time the user did something in the main window and says when the session has been idle for
 * longer than the timeout (RF-1.15). A zero timeout never expires.
 */
final class IdleTimer {

    private final Duration timeout;
    private final Clock clock;
    private volatile Instant lastActivity;

    IdleTimer(Duration timeout, Clock clock) {
        this.timeout = timeout;
        this.clock = clock;
        this.lastActivity = clock.instant();
    }

    void touch() {
        lastActivity = clock.instant();
    }

    boolean expired() {
        return !timeout.isZero() && lastActivity.plus(timeout).isBefore(clock.instant());
    }
}

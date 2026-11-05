package org.jala.edu.core.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

/**
 * How long a signed-in session may stay without activity (RF-1.15), set with {@value #PROPERTY} (for example
 * {@code 30m} or {@code 1h}; {@code 0} keeps the session open until "Sign out"). The main window returns to the login
 * when it passes, and {@code user-module} stops answering for the user.
 */
public final class SessionTimeout {

    public static final String PROPERTY = "delivery.session.idle-timeout";
    public static final Duration DEFAULT = Duration.ofMinutes(30);

    private SessionTimeout() {
    }

    /**
     * @return the configured timeout, {@link #DEFAULT} when it is not set, or {@link Duration#ZERO} when disabled
     */
    public static Duration from(Environment environment) {
        Duration timeout = Binder.get(environment).bind(PROPERTY, Duration.class).orElse(DEFAULT);
        return timeout.isNegative() ? Duration.ZERO : timeout;
    }
}

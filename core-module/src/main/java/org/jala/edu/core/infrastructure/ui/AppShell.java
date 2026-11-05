package org.jala.edu.core.infrastructure.ui;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.infrastructure.config.SessionTimeout;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Owns the primary stage and switches between the login screen ({@link LoginScreen}, when a module provides one)
 * and the main window.
 */
@Component
@RequiredArgsConstructor
public class AppShell {

    static final String MAIN_VIEW = "/org/jala/edu/core/infrastructure/ui/main-view.fxml";
    private static final String STYLESHEET = "/org/jala/edu/core/infrastructure/ui/app.css";

    /** Shown on the login when an operation finds that the session is no longer valid. */
    public static final String SESSION_ENDED = "Your session has ended. Please sign in again.";
    /** Shown on the login when the session was idle for longer than {@link SessionTimeout} (RF-1.15). */
    public static final String SESSION_IDLE = "You were signed out after a period of inactivity.";

    private static final javafx.util.Duration IDLE_CHECK_EVERY = javafx.util.Duration.seconds(15);

    private final SpringFxmlLoader fxmlLoader;
    private final ObjectProvider<LoginScreen> loginScreen;
    private final ApplicationEventPublisher events;
    private final Environment environment;

    private Stage stage;
    private String loginNotice;
    private Timeline idleCheck;

    void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Delivery System");
        if (hasLogin()) {
            BackgroundTasks.whenSessionEnds(() -> signOut(SESSION_ENDED));
            showLogin();
        } else {
            showMainWindow();
        }
        stage.show();
    }

    public boolean hasLogin() {
        return loginScreen.getIfAvailable() != null;
    }

    /**
     * Opens the main window; the login screen calls it once the user has signed in.
     */
    public void showMainWindow() {
        show(fxmlLoader.load(MAIN_VIEW));
        if (hasLogin()) {
            watchIdleTime();
        }
    }

    /**
     * Forgets the current user ({@link SignOutEvent}) and goes back to the login screen.
     */
    public void signOut() {
        signOut(null);
    }

    /**
     * Like {@link #signOut()}, and the login screen shows {@code notice}, e.g. why the session ended.
     */
    public void signOut(String notice) {
        events.publishEvent(new SignOutEvent(this));
        loginNotice = notice;
        showLogin();
    }

    /**
     * @return the notice for the login screen once, e.g. {@value #SESSION_ENDED}; empty afterwards
     */
    public Optional<String> takeLoginNotice() {
        Optional<String> notice = Optional.ofNullable(loginNotice);
        loginNotice = null;
        return notice;
    }

    /**
     * Shows the login screen again, e.g. from a screen it opened with {@link #showScreen(String)}.
     */
    public void showLogin() {
        stopIdleCheck();
        show(fxmlLoader.load(loginScreen.getObject().fxmlPath()));
    }

    /**
     * Shows a full-window view outside the main window, e.g. the sign-up screen reached from the login (#89).
     */
    public void showScreen(String fxmlPath) {
        show(fxmlLoader.load(fxmlPath));
    }

    /**
     * Any key or mouse action in the main window counts as activity; when the session stays idle longer than the
     * timeout, the user is signed out and sees {@link #SESSION_IDLE} on the login.
     */
    private void watchIdleTime() {
        Duration timeout = SessionTimeout.from(environment);
        if (timeout.isZero()) {
            return;
        }
        IdleTimer timer = new IdleTimer(timeout, Clock.systemUTC());
        stage.getScene().addEventFilter(InputEvent.ANY, event -> timer.touch());
        stopIdleCheck();
        idleCheck = new Timeline(new KeyFrame(IDLE_CHECK_EVERY, event -> {
            if (timer.expired()) {
                signOut(SESSION_IDLE);
            }
        }));
        idleCheck.setCycleCount(Animation.INDEFINITE);
        idleCheck.play();
    }

    private void stopIdleCheck() {
        if (idleCheck != null) {
            idleCheck.stop();
            idleCheck = null;
        }
    }

    private void show(Parent root) {
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(STYLESHEET)).toExternalForm());
        stage.setScene(scene);
    }
}

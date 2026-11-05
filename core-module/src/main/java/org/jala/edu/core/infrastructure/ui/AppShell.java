package org.jala.edu.core.infrastructure.ui;

import java.util.Objects;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
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

    private final SpringFxmlLoader fxmlLoader;
    private final ObjectProvider<LoginScreen> loginScreen;
    private final ApplicationEventPublisher events;

    private Stage stage;

    void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Delivery System");
        if (hasLogin()) {
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
    }

    /**
     * Forgets the current user ({@link SignOutEvent}) and goes back to the login screen.
     */
    public void signOut() {
        events.publishEvent(new SignOutEvent(this));
        showLogin();
    }

    /**
     * Shows the login screen again, e.g. from a screen it opened with {@link #showScreen(String)}.
     */
    public void showLogin() {
        show(fxmlLoader.load(loginScreen.getObject().fxmlPath()));
    }

    /**
     * Shows a full-window view outside the main window, e.g. the sign-up screen reached from the login (#89).
     */
    public void showScreen(String fxmlPath) {
        show(fxmlLoader.load(fxmlPath));
    }

    private void show(Parent root) {
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(STYLESHEET)).toExternalForm());
        stage.setScene(scene);
    }
}

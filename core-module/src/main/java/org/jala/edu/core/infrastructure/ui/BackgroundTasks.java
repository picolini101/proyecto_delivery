package org.jala.edu.core.infrastructure.ui;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import org.jala.edu.core.contract.NotAuthenticatedException;

/**
 * Runs blocking work (use cases, I/O) off the JavaFX application thread.
 * Callbacks are invoked back on the JavaFX application thread, so they may update the UI.
 */
public final class BackgroundTasks {

    private static volatile Runnable onSessionEnded;

    private BackgroundTasks() {
    }

    /**
     * When the work fails because nobody is signed in any more ({@link NotAuthenticatedException}, e.g. an
     * administrator deactivated the user), the application goes back to the login instead of calling
     * {@code onFailure}, as long as there is a login screen.
     */
    public static <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> {
            Runnable sessionEnded = onSessionEnded;
            if (sessionEnded != null && endsSession(task.getException())) {
                sessionEnded.run();
            } else {
                onFailure.accept(task.getException());
            }
        });
        Thread.ofVirtual().start(task);
    }

    /**
     * Called by {@link AppShell} once there is a login screen to go back to.
     */
    static void whenSessionEnds(Runnable action) {
        onSessionEnded = action;
    }

    static boolean endsSession(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof NotAuthenticatedException) {
                return true;
            }
        }
        return false;
    }
}

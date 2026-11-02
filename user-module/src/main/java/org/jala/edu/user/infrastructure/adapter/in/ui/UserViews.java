package org.jala.edu.user.infrastructure.adapter.in.ui;

/**
 * Classpath locations of the user module views.
 */
public final class UserViews {

    private static final String BASE = "/org/jala/edu/user/infrastructure/adapter/in/ui/";

    /** Main view of the module, opened by the "Users" navigation button. */
    public static final String MAIN = BASE + "user-main.fxml";
    public static final String CREATE = BASE + "create-user.fxml";
    public static final String LIST = BASE + "list-users.fxml";
    public static final String EDIT = BASE + "edit-user.fxml";
    /** Login screen shown before the main window (CU-USR-06). */
    public static final String LOGIN = BASE + "login.fxml";

    private UserViews() {
    }
}

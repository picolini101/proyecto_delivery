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
    /** Status history of a user (CU-USR-07), shown as a dialog. */
    public static final String STATUS_HISTORY = BASE + "user-status-history.fxml";
    /** Login screen shown before the main window (CU-USR-06). */
    public static final String LOGIN = BASE + "login.fxml";
    /** Sign-up screen for new customers, opened from the login (CU-USR-08). */
    public static final String SIGN_UP = BASE + "sign-up.fxml";
    /** The signed-in user's account and password (CU-USR-09), opened by the "My account" button. */
    public static final String MY_ACCOUNT = BASE + "my-account.fxml";

    private UserViews() {
    }
}

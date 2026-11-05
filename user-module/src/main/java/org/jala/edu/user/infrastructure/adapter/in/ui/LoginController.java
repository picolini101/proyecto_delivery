package org.jala.edu.user.infrastructure.adapter.in.ui;

import static org.jala.edu.core.infrastructure.ui.FormBindings.isBlank;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.infrastructure.ui.AppShell;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.user.application.port.in.LoginCommand;
import org.jala.edu.user.application.usecase.LoginUseCase;
import org.jala.edu.user.infrastructure.adapter.in.contract.SessionCurrentUserService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * JavaFX inbound adapter for CU-USR-06 · Iniciar sesión (view: {@code login.fxml}). On success it signs the user in
 * and opens the main window with the menu of their role.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class LoginController {

    private final LoginUseCase loginUseCase;
    private final SessionCurrentUserService session;
    private final AppShell shell;

    @FXML
    private GridPane form;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signInButton;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        signInButton.disableProperty().bind(isBlank(emailField).or(isBlank(passwordField)));
        shell.takeLoginNotice().ifPresent(notice -> {
            messageLabel.getStyleClass().add("error");
            messageLabel.setText(notice);
        });
    }

    @FXML
    void onSignIn() {
        LoginCommand command = new LoginCommand(emailField.getText(), passwordField.getText());
        form.setDisable(true);
        messageLabel.setText("");
        BackgroundTasks.run(
                () -> loginUseCase.execute(command),
                user -> {
                    session.signIn(user);
                    shell.showMainWindow();
                },
                error -> {
                    passwordField.clear();
                    messageLabel.getStyleClass().add("error");
                    messageLabel.setText(ErrorMessages.of(error, "sign in"));
                    form.setDisable(false);
                });
    }

    @FXML
    void onCreateAccount() {
        shell.showScreen(UserViews.SIGN_UP);
    }
}

package org.jala.edu.user.infrastructure.adapter.in.ui;

import static org.jala.edu.core.infrastructure.ui.FormBindings.isBlank;
import static org.jala.edu.core.infrastructure.ui.FormBindings.optionalText;

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
import org.jala.edu.user.application.port.in.SignUpCommand;
import org.jala.edu.user.application.usecase.SignUpUseCase;
import org.jala.edu.user.infrastructure.adapter.in.contract.SessionCurrentUserService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * JavaFX inbound adapter for CU-USR-08 · Registrarse como cliente (view: {@code sign-up.fxml}), opened from the login
 * screen. On success the new customer is signed in and sees the main window.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class SignUpController {

    private final SignUpUseCase signUpUseCase;
    private final SessionCurrentUserService session;
    private final AppShell shell;

    @FXML
    private GridPane form;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signUpButton;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        signUpButton.disableProperty().bind(isBlank(firstNameField)
                .or(isBlank(lastNameField))
                .or(isBlank(emailField))
                .or(isBlank(passwordField)));
    }

    @FXML
    void onSignUp() {
        SignUpCommand command = new SignUpCommand(
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                optionalText(phoneField),
                passwordField.getText());
        form.setDisable(true);
        messageLabel.setText("");
        BackgroundTasks.run(
                () -> signUpUseCase.execute(command),
                user -> {
                    session.signIn(user);
                    shell.showMainWindow();
                },
                error -> {
                    passwordField.clear();
                    messageLabel.getStyleClass().add("error");
                    messageLabel.setText(ErrorMessages.of(error, "create the account"));
                    form.setDisable(false);
                });
    }

    @FXML
    void onBackToSignIn() {
        shell.showLogin();
    }
}

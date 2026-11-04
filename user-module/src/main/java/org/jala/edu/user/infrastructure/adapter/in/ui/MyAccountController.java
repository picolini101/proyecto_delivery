package org.jala.edu.user.infrastructure.adapter.in.ui;

import static org.jala.edu.core.infrastructure.ui.FormBindings.isBlank;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.DisplayFormat;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.user.application.port.in.ChangePasswordCommand;
import org.jala.edu.user.application.usecase.ChangePasswordUseCase;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * JavaFX inbound adapter for CU-USR-09 · Cambiar contraseña (view: {@code my-account.fxml}), opened by the
 * "My account" button that every role sees.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class MyAccountController {

    private final ChangePasswordUseCase changePasswordUseCase;
    private final CurrentUserService currentUserService;

    @FXML
    private Label nameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private GridPane form;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button changeButton;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        CurrentUser user = currentUserService.currentUser();
        nameLabel.setText(user.displayName());
        roleLabel.setText(DisplayFormat.label(user.role()));
        changeButton.disableProperty().bind(isBlank(currentPasswordField)
                .or(isBlank(newPasswordField))
                .or(newPasswordField.textProperty().isNotEqualTo(confirmPasswordField.textProperty())));
    }

    @FXML
    void onChangePassword() {
        ChangePasswordCommand command = new ChangePasswordCommand(currentUserService.currentUser().id(),
                currentPasswordField.getText(), newPasswordField.getText());
        form.setDisable(true);
        messageLabel.setText("");
        BackgroundTasks.run(
                () -> changePasswordUseCase.execute(command),
                ignored -> {
                    clearForm();
                    showMessage("Password changed. Use the new one the next time you sign in.", "success");
                    form.setDisable(false);
                },
                error -> {
                    clearForm();
                    showMessage(ErrorMessages.of(error, "change the password"), "error");
                    form.setDisable(false);
                });
    }

    private void showMessage(String text, String styleClass) {
        messageLabel.getStyleClass().removeAll("success", "error");
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(text);
    }

    private void clearForm() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
}

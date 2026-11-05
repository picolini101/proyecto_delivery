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
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.DisplayFormat;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.user.application.port.in.ChangePasswordCommand;
import org.jala.edu.user.application.port.in.UpdateMyProfileCommand;
import org.jala.edu.user.application.usecase.ChangePasswordUseCase;
import org.jala.edu.user.application.usecase.FindUserUseCase;
import org.jala.edu.user.application.usecase.UpdateMyProfileUseCase;
import org.jala.edu.user.domain.model.User;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * JavaFX inbound adapter for the signed-in user's own account (view: {@code my-account.fxml}), opened by the
 * "My account" button that every role sees: CU-USR-07 · Consultar mi perfil (RF-1.07), CU-USR-10 · Editar mi perfil
 * (RF-1.08) and CU-USR-09 · Cambiar contraseña.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class MyAccountController {

    private final FindUserUseCase findUserUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final CurrentUserService currentUserService;

    @FXML
    private Label emailLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private GridPane profileForm;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private Button saveProfileButton;

    @FXML
    private Label profileMessageLabel;

    @FXML
    private GridPane passwordForm;

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
        saveProfileButton.disableProperty().bind(isBlank(firstNameField).or(isBlank(lastNameField)));
        changeButton.disableProperty().bind(isBlank(currentPasswordField)
                .or(isBlank(newPasswordField))
                .or(newPasswordField.textProperty().isNotEqualTo(confirmPasswordField.textProperty())));
        profileForm.setDisable(true);
        BackgroundTasks.run(
                () -> findUserUseCase.execute(currentUserService.currentUser().id()).orElseThrow(),
                this::showAccount,
                error -> showMessage(profileMessageLabel, ErrorMessages.of(error, "load your account"), "error"));
    }

    @FXML
    void onSaveProfile() {
        UpdateMyProfileCommand command = new UpdateMyProfileCommand(firstNameField.getText(), lastNameField.getText(),
                optionalText(phoneField));
        profileForm.setDisable(true);
        profileMessageLabel.setText("");
        BackgroundTasks.run(
                () -> updateMyProfileUseCase.execute(command),
                ignored -> {
                    showMessage(profileMessageLabel, "Profile saved. The menu shows the new name the next time you "
                            + "sign in.", "success");
                    profileForm.setDisable(false);
                },
                error -> {
                    showMessage(profileMessageLabel, ErrorMessages.of(error, "save your profile"), "error");
                    profileForm.setDisable(false);
                });
    }

    @FXML
    void onChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        passwordForm.setDisable(true);
        messageLabel.setText("");
        // The session is read inside the background work, so an ended session goes back to the login (#94).
        BackgroundTasks.run(
                () -> changePasswordUseCase.execute(new ChangePasswordCommand(currentUserService.currentUser().id(),
                        currentPassword, newPassword)),
                ignored -> {
                    clearPasswordForm();
                    showMessage(messageLabel, "Password changed. Use the new one the next time you sign in.",
                            "success");
                    passwordForm.setDisable(false);
                },
                error -> {
                    clearPasswordForm();
                    showMessage(messageLabel, ErrorMessages.of(error, "change the password"), "error");
                    passwordForm.setDisable(false);
                });
    }

    private void showAccount(User user) {
        emailLabel.setText(user.getEmail().value());
        roleLabel.setText(DisplayFormat.label(user.getRole()));
        statusLabel.setText(DisplayFormat.label(user.getStatus()));
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        phoneField.setText(user.getPhoneNumber() == null ? "" : user.getPhoneNumber());
        profileForm.setDisable(false);
    }

    private static void showMessage(Label label, String text, String styleClass) {
        label.getStyleClass().removeAll("success", "error");
        label.getStyleClass().add(styleClass);
        label.setText(text);
    }

    private void clearPasswordForm() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
}

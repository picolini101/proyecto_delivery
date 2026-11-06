package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;
import org.jala.edu.catalog.application.port.in.CreateProductCommand;
import org.jala.edu.catalog.application.usecase.CreateProductUseCase;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada de JavaFX para el caso de uso «crear producto» (vista: {@code create-product.fxml}).
 * Se limita a leer el formulario, invocar el caso de uso y mostrar el resultado; las reglas de negocio residen en el dominio.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CreateProductController {

    // TODO(sprint 2): reemplazar por el comercio del usuario autenticado una vez que exista el inicio de sesión.
    private static final UUID CURRENT_COMMERCE = FallbackCatalogService.SAMPLE_COMMERCE;

    private final CreateProductUseCase createProductUseCase;

    @FXML
    private GridPane form;

    @FXML
    private TextField nameField;

    @FXML
    private TextField categoryField;

    @FXML
    private TextField priceField;

    @FXML
    private Label messageLabel;

    @FXML
    void onCreate() {
        CreateProductCommand command;
        try {
            command = new CreateProductCommand(
                    CatalogSession.CURRENT_USER,
                    CURRENT_COMMERCE,
                    nameField.getText(),
                    categoryField.getText(),
                    PriceInput.read(priceField));
        } catch (DomainException e) {
            showMessage(e.getMessage(), "error");
            return;
        }

        form.setDisable(true);
        messageLabel.setText("");
        BackgroundTasks.run(
                () -> createProductUseCase.execute(command),
                productId -> {
                    showMessage("Product " + command.name().strip() + " registered.", "success");
                    clearForm();
                    form.setDisable(false);
                },
                error -> {
                    showMessage(ErrorMessages.of(error, "register the product"), "error");
                    form.setDisable(false);
                });
    }

    private void showMessage(String text, String styleClass) {
        messageLabel.getStyleClass().removeAll("success", "error");
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(text);
    }

    private void clearForm() {
        nameField.clear();
        categoryField.clear();
        priceField.clear();
        nameField.requestFocus();
    }
}
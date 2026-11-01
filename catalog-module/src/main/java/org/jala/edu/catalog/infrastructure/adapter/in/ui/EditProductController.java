package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import static org.jala.edu.core.infrastructure.ui.FormBindings.isBlank;

import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;
import org.jala.edu.catalog.application.port.in.UpdateProductCommand;
import org.jala.edu.catalog.application.port.in.AdjustProductStockCommand;
import org.jala.edu.catalog.application.usecase.UpdateProductUseCase;
import org.jala.edu.catalog.application.usecase.AdjustProductStockUseCase;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.Dialogs;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada JavaFX para el caso de uso «modificar producto»
 * (vista: {@code edit-product.fxml}, mostrada como diálogo).
 * Se limita a leer el formulario, invocar el caso de uso y mostrar el resultado;
 * las reglas de negocio residen en el dominio.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class EditProductController {

    private final UpdateProductUseCase updateProductUseCase;
    private final AdjustProductStockUseCase adjustProductStockUseCase;

    private UUID productId;
    private Runnable onSaved;

    @FXML
    private GridPane form;

    @FXML
    private TextField nameField;

    @FXML
    private TextField categoryField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField stockField;

    @FXML
    private Button saveButton;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        // Nombre y categoría son obligatorios; el precio lo valida el dominio (debe ser mayor que cero)
        saveButton.disableProperty().bind(isBlank(nameField).or(isBlank(categoryField)));
    }

    /**
     * Llena el formulario con los datos de {@code product}; {@code onSaved} se ejecuta tras guardar con éxito.
     */
    public void edit(Product product, Runnable onSaved) {
        this.productId = product.getId().value();
        this.onSaved = onSaved;
        nameField.setText(product.getName());
        categoryField.setText(product.getCategory());
        priceField.setText(product.getPrice().toPlainString());
        stockField.setText(String.valueOf(product.getStock()));
    }

    @FXML
    void onSave() {
        UpdateProductCommand command;
        int stock;

        try {
            command = new UpdateProductCommand(
                    CatalogSession.CURRENT_USER,
                    productId,
                    nameField.getText(),
                    categoryField.getText(),
                    PriceInput.read(priceField));

            stock = Integer.parseInt(stockField.getText().trim());

            if (stock < 0) {
                messageLabel.setText("Stock cannot be negative");
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Stock must be a whole number greater than or equal to zero");
            return;
        } catch (DomainException e) {
            messageLabel.setText(e.getMessage());
            return;
        }

        form.setDisable(true);
        messageLabel.setText("");
        BackgroundTasks.run(
                () -> {
                    updateProductUseCase.execute(command);
                    adjustProductStockUseCase.execute(
                            new AdjustProductStockCommand(
                                    CatalogSession.CURRENT_USER, productId, stock));
                    return null;
                },
                done -> {
                    Dialogs.close(form);
                    onSaved.run();
                },
                error -> {
                    messageLabel.setText(ErrorMessages.of(error, "update the product"));
                    form.setDisable(false);
                });
    }

    @FXML
    void onCancel() {
        Dialogs.close(form);
    }
}
package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import java.math.BigDecimal;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;
import org.jala.edu.catalog.application.dto.CommerceSummary;
import org.jala.edu.catalog.application.port.in.CreateProductCommand;
import org.jala.edu.catalog.application.port.in.ListCommercesQuery;
import org.jala.edu.catalog.application.usecase.CreateProductUseCase;
import org.jala.edu.catalog.application.usecase.ListCommercesUseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada de JavaFX para «crear producto» (vista: {@code create-product.fxml}).
 * El usuario elige el comercio al que pertenecerá el producto entre los comercios creados.
 * Se limita a leer el formulario e invocar el caso de uso; las reglas de negocio residen en el dominio.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CreateProductController {

    private final CreateProductUseCase createProductUseCase;
    private final ListCommercesUseCase listCommercesUseCase;
    private final CurrentUserService currentUserService;

    @FXML
    private GridPane form;

    @FXML
    private ComboBox<CommerceSummary> commerceSelector;

    @FXML
    private TextField nameField;

    @FXML
    private TextField categoryField;

    @FXML
    private TextField priceField;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        commerceSelector.setConverter(new CommerceNameConverter());
        loadCommerces();
    }

    @FXML
    void onCreate() {
        CommerceSummary commerce = commerceSelector.getValue();
        if (commerce == null) {
            MessageLabels.show(messageLabel, "Select the commerce this product belongs to", "error");
            return;
        }

        String name = nameField.getText();
        String category = categoryField.getText();
        BigDecimal price;
        try {
            price = PriceInput.read(priceField);
        } catch (DomainException e) {
            MessageLabels.show(messageLabel, e.getMessage(), "error");
            return;
        }

        form.setDisable(true);
        messageLabel.setText("");
        BackgroundTasks.run(
                () -> createProductUseCase.execute(new CreateProductCommand(
                        currentUserService.currentUser().id(),
                        commerce.id(), name, category, price)),
                productId -> {
                    MessageLabels.show(messageLabel, "Product " + name.strip()
                            + " registered in " + commerce.name() + ".", "success");
                    clearForm();
                    form.setDisable(false);
                },
                error -> {
                    MessageLabels.show(messageLabel,
                            ErrorMessages.of(error, "register the product"), "error");
                    form.setDisable(false);
                });
    }

    /** Carga todos los comercios creados, también los Deshabilitados (necesitan su primer producto). */
    private void loadCommerces() {
        BackgroundTasks.run(
                () -> listCommercesUseCase.execute(ListCommercesQuery.all()),
                commerces -> commerceSelector.getItems().setAll(commerces),
                error -> MessageLabels.show(messageLabel,
                        ErrorMessages.of(error, "load the commerces"), "error"));
    }

    /** Conserva el comercio elegido para poder registrar varios productos seguidos en él. */
    private void clearForm() {
        nameField.clear();
        categoryField.clear();
        priceField.clear();
        nameField.requestFocus();
    }
}
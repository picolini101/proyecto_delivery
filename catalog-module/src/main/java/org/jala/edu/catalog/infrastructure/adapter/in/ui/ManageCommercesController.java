package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.RequiredArgsConstructor;
import org.jala.edu.catalog.application.dto.CommerceSummary;
import org.jala.edu.catalog.application.dto.EnableCommerceResult;
import org.jala.edu.catalog.application.port.in.EnableCommerceCommand;
import org.jala.edu.catalog.application.port.in.ListCommercesQuery;
import org.jala.edu.catalog.application.usecase.EnableCommerceUseCase;
import org.jala.edu.catalog.application.usecase.ListCommercesUseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.DisplayFormat;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada JavaFX para que el administrador vea los comercios y habilite uno
 * (vista: {@code manage-commerces.fxml}).
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ManageCommercesController {

    private final ListCommercesUseCase listCommercesUseCase;
    private final EnableCommerceUseCase enableCommerceUseCase;
    private final CurrentUserService currentUserService;

    @FXML
    private TableView<CommerceSummary> commercesTable;

    @FXML
    private TableColumn<CommerceSummary, String> nameColumn;

    @FXML
    private TableColumn<CommerceSummary, String> addressColumn;

    @FXML
    private TableColumn<CommerceSummary, String> phoneColumn;

    @FXML
    private TableColumn<CommerceSummary, String> statusColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private Button enableButton;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        bind(nameColumn, CommerceSummary::name);
        bind(addressColumn, CommerceSummary::address);
        bind(phoneColumn, CommerceSummary::phone);
        bind(statusColumn, commerce -> DisplayFormat.label(commerce.status()));

        // El botón Habilitar solo se activa cuando hay un comercio seleccionado
        enableButton.disableProperty()
                .bind(commercesTable.getSelectionModel().selectedItemProperty().isNull());

        reload(null, "info");
    }

    @FXML
    void onRefresh() {
        reload(null, "info");
    }

    /**
     * Habilita el comercio seleccionado. Si ya estaba Activo solo se informa, sin modificar nada.
     */
    @FXML
    void onEnable() {
        CommerceSummary selected = commercesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        BackgroundTasks.run(
                () -> enableCommerceUseCase.execute(new EnableCommerceCommand(
                        currentUserService.currentUser().id(), selected.id())),
                result -> {
                    if (result == EnableCommerceResult.ENABLED) {
                        reload("Commerce " + selected.name() + " enabled.", "success");
                    } else {
                        reload("Commerce " + selected.name() + " is already enabled.", "info");
                    }
                },
                error -> MessageLabels.show(messageLabel,
                        ErrorMessages.of(error, "enable the commerce"), "error"));
    }

    /** Carga los comercios; luego muestra {@code message}, o el conteo si es {@code null}. */
    private void reload(String message, String styleClass) {
        refreshButton.setDisable(true);
        BackgroundTasks.run(
                () -> listCommercesUseCase.execute(ListCommercesQuery.all()),
                commerces -> {
                    commercesTable.getItems().setAll(commerces);
                    if (message == null) {
                        MessageLabels.show(messageLabel, count(commerces.size()), "info");
                    } else {
                        MessageLabels.show(messageLabel, message, styleClass);
                    }
                    refreshButton.setDisable(false);
                },
                error -> {
                    MessageLabels.show(messageLabel,
                            ErrorMessages.of(error, "load the commerces"), "error");
                    refreshButton.setDisable(false);
                });
    }

    private static String count(int size) {
        return size == 1 ? "1 commerce" : size + " commerces";
    }

    private static void bind(TableColumn<CommerceSummary, String> column,
                             Function<CommerceSummary, String> value) {
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
    }
}
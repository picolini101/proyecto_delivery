package org.jala.edu.delivery.infrastructure.adapter.in.ui;

import java.util.List;
import java.util.function.Function;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.delivery.application.port.in.ListReadyOrdersQuery;
import org.jala.edu.delivery.application.port.in.ReadyOrderSummary;
import org.jala.edu.delivery.application.port.in.TomarPedidoCommand;
import org.jala.edu.delivery.application.usecase.ListReadyOrdersUseCase;
import org.jala.edu.delivery.application.usecase.TomarPedidoUseCase;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Courier dashboard of orders ready for delivery (view: {@code ready-orders.fxml}).
 * There is no sign-in yet, so the board acts as the sample courier from {@link SampleUsers}.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ReadyOrdersController {

    private final ListReadyOrdersUseCase listReadyOrdersUseCase;
    private final TomarPedidoUseCase tomarPedidoUseCase;

    @FXML
    private TableView<ReadyOrderSummary> ordersTable;

    @FXML
    private TableColumn<ReadyOrderSummary, String> orderIdColumn;

    @FXML
    private TableColumn<ReadyOrderSummary, String> addressColumn;

    @FXML
    private TableColumn<ReadyOrderSummary, String> shippingCostColumn;

    @FXML
    private Button takeButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        bind(orderIdColumn, order -> order.orderId().toString());
        bind(addressColumn, ReadyOrderSummary::deliveryAddress);
        bind(shippingCostColumn, order -> order.shippingCost().toPlainString());

        BooleanBinding noSelection = ordersTable.getSelectionModel().selectedItemProperty().isNull();
        takeButton.disableProperty().bind(noSelection);
        reload(null);
    }

    @FXML
    void onRefresh() {
        reload(null);
    }

    @FXML
    void onTake() {
        ReadyOrderSummary selected = ordersTable.getSelectionModel().getSelectedItem();
        refreshButton.setDisable(true);
        BackgroundTasks.run(
                () -> tomarPedidoUseCase.execute(new TomarPedidoCommand(selected.orderId(), SampleUsers.COURIER)),
                done -> reload("Pedido tomado."),
                error -> {
                    showMessage(ErrorMessages.of(error, "take the order"), "error");
                    refreshButton.setDisable(false);
                });
    }

    private void reload(String successMessage) {
        refreshButton.setDisable(true);
        BackgroundTasks.run(
                () -> listReadyOrdersUseCase.execute(new ListReadyOrdersQuery(SampleUsers.COURIER)),
                orders -> {
                    ordersTable.getItems().setAll(orders);
                    if (successMessage == null) {
                        showMessage(count(orders), "info");
                    } else {
                        showMessage(successMessage, "success");
                    }
                    refreshButton.setDisable(false);
                },
                error -> {
                    showMessage(ErrorMessages.of(error, "load the ready orders"), "error");
                    refreshButton.setDisable(false);
                });
    }

    private void showMessage(String text, String styleClass) {
        messageLabel.getStyleClass().removeAll("info", "success", "error");
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(text);
    }

    private static String count(List<ReadyOrderSummary> orders) {
        if (orders.isEmpty()) {
            return "";
        }
        return orders.size() == 1 ? "1 pedido listo" : orders.size() + " pedidos listos";
    }

    private static void bind(TableColumn<ReadyOrderSummary, String> column, Function<ReadyOrderSummary, String> value) {
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
    }
}

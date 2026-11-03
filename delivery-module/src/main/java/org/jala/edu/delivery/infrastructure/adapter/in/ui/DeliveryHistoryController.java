package org.jala.edu.delivery.infrastructure.adapter.in.ui;

import static org.jala.edu.core.infrastructure.ui.FormBindings.isBlank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.delivery.application.port.in.GetDeliveryHistoryQuery;
import org.jala.edu.delivery.application.usecase.GetDeliveryHistoryUseCase;
import org.jala.edu.delivery.domain.model.CompletedDelivery;
import org.jala.edu.delivery.domain.model.DeliveryHistory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * JavaFX inbound adapter for the "get delivery history" use case (view: {@code delivery-history.fxml}).
 * There is no login yet, so the courier is picked by id; it starts with the sample courier.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class DeliveryHistoryController {

    static final String EMPTY_HISTORY_MESSAGE = "Your completed deliveries will appear in this panel.";
    static final String INVALID_COURIER_ID_MESSAGE = "The courier id must be a valid UUID.";

    private final GetDeliveryHistoryUseCase getDeliveryHistoryUseCase;

    @FXML
    private HBox searchBar;

    @FXML
    private TextField courierIdField;

    @FXML
    private Button consultButton;

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<CompletedDelivery> historyTable;

    @FXML
    private TableColumn<CompletedDelivery, String> orderColumn;

    @FXML
    private TableColumn<CompletedDelivery, String> localityColumn;

    @FXML
    private TableColumn<CompletedDelivery, String> deliveredOnColumn;

    @FXML
    private TableColumn<CompletedDelivery, String> amountColumn;

    @FXML
    private Label totalLabel;

    @FXML
    void initialize() {
        bind(orderColumn, delivery -> delivery.orderId().toString().substring(0, 8));
        bind(localityColumn, CompletedDelivery::locality);
        bind(deliveredOnColumn, delivery -> delivery.deliveredOn().toString());
        bind(amountColumn, delivery -> money(delivery.amount()));

        courierIdField.setText(SampleUsers.COURIER.toString());
        consultButton.disableProperty().bind(isBlank(courierIdField));

        showTotal(BigDecimal.ZERO);
        onConsult();
    }

    @FXML
    void onConsult() {
        UUID courierId;
        try {
            courierId = UUID.fromString(courierIdField.getText().strip());
        } catch (IllegalArgumentException e) {
            showDenied(INVALID_COURIER_ID_MESSAGE);
            return;
        }

        searchBar.setDisable(true);
        BackgroundTasks.run(
                () -> getDeliveryHistoryUseCase.execute(new GetDeliveryHistoryQuery(courierId)),
                history -> {
                    show(history);
                    searchBar.setDisable(false);
                },
                error -> {
                    showDenied(ErrorMessages.of(error, "load the delivery history"));
                    searchBar.setDisable(false);
                });
    }

    private void show(DeliveryHistory history) {
        historyTable.getItems().setAll(history.deliveries());
        showTotal(history.total());
        if (history.isEmpty()) {
            showMessage(EMPTY_HISTORY_MESSAGE, "info");
        } else {
            showMessage(count(history), "info");
        }
    }

    /**
     * Access is denied: nothing from a previous consultation stays on the screen.
     */
    private void showDenied(String message) {
        historyTable.getItems().clear();
        showTotal(BigDecimal.ZERO);
        showMessage(message, "error");
    }

    private void showTotal(BigDecimal total) {
        totalLabel.setText("Total: " + money(total));
    }

    private void showMessage(String text, String styleClass) {
        messageLabel.getStyleClass().removeAll("info", "success", "error");
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(text);
    }

    private static String count(DeliveryHistory history) {
        int size = history.deliveries().size();
        return size == 1 ? "1 delivery" : size + " deliveries";
    }

    private static String money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static void bind(TableColumn<CompletedDelivery, String> column, Function<CompletedDelivery, String> value) {
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
    }
}

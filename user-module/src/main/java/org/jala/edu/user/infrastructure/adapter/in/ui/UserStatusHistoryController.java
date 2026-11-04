package org.jala.edu.user.infrastructure.adapter.in.ui;

import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.Dialogs;
import org.jala.edu.core.infrastructure.ui.DisplayFormat;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.user.application.usecase.ListUserStatusHistoryUseCase;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserStatusChange;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * JavaFX inbound adapter for CU-USR-07 (view: {@code user-status-history.fxml}, shown as a dialog): who activated
 * or deactivated the selected user and when (#82).
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class UserStatusHistoryController {

    private final ListUserStatusHistoryUseCase listUserStatusHistoryUseCase;

    @FXML
    private Label userLabel;

    @FXML
    private TableView<UserStatusChange> historyTable;

    @FXML
    private TableColumn<UserStatusChange, String> dateColumn;

    @FXML
    private TableColumn<UserStatusChange, String> fromColumn;

    @FXML
    private TableColumn<UserStatusChange, String> toColumn;

    @FXML
    private TableColumn<UserStatusChange, String> changedByColumn;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        bind(dateColumn, change -> DisplayFormat.dateTime(change.getCreatedAt()));
        bind(fromColumn, change -> DisplayFormat.label(change.getPreviousStatus()));
        bind(toColumn, change -> DisplayFormat.label(change.getNewStatus()));
        bind(changedByColumn, UserStatusChange::getChangedByName);
    }

    /**
     * Loads the status history of {@code user}, newest first.
     */
    public void show(User user) {
        userLabel.setText(user.fullName() + " · " + user.getEmail().value());
        BackgroundTasks.run(
                () -> listUserStatusHistoryUseCase.execute(user.getId().value()),
                changes -> historyTable.getItems().setAll(changes),
                error -> messageLabel.setText(ErrorMessages.of(error, "load the status history")));
    }

    @FXML
    void onClose() {
        Dialogs.close(historyTable);
    }

    private static void bind(TableColumn<UserStatusChange, String> column,
                             Function<UserStatusChange, String> value) {
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
    }
}

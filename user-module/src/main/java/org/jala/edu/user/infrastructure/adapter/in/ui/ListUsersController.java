package org.jala.edu.user.infrastructure.adapter.in.ui;

import java.util.List;
import java.util.function.Function;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.Dialogs;
import org.jala.edu.core.infrastructure.ui.DisplayFormat;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.core.infrastructure.ui.SpringFxmlLoader;
import org.jala.edu.user.application.port.in.ChangeUserStatusCommand;
import org.jala.edu.user.application.port.in.ListUsersQuery;
import org.jala.edu.user.application.usecase.ChangeUserStatusUseCase;
import org.jala.edu.user.application.usecase.ListUsersUseCase;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.domain.model.UserStatus;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * JavaFX inbound adapter for the "list users" use case (view: {@code list-users.fxml}).
 * Also entry point for editing (dialog), activating or deactivating the selected user and seeing its status history
 * (dialog, #82). Users are never deleted: other modules keep their id (FUN-003).
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ListUsersController {


    private final ListUsersUseCase listUsersUseCase;
    private final ChangeUserStatusUseCase changeUserStatusUseCase;
    private final SpringFxmlLoader fxmlLoader;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<UserRole> roleFilter;

    @FXML
    private ComboBox<UserStatus> statusFilter;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> phoneColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> statusColumn;

    @FXML
    private TableColumn<User, String> createdAtColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private Button editButton;

    @FXML
    private Button statusButton;

    @FXML
    private Button historyButton;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        setUpFilter(roleFilter, UserRole.values(), "All roles");
        setUpFilter(statusFilter, UserStatus.values(), "All statuses");
        bind(nameColumn, User::fullName);
        bind(emailColumn, user -> user.getEmail().value());
        bind(phoneColumn, user -> user.getPhoneNumber() == null ? "" : user.getPhoneNumber());
        bind(roleColumn, user -> DisplayFormat.label(user.getRole()));
        bind(statusColumn, user -> DisplayFormat.label(user.getStatus()));
        bind(createdAtColumn, user -> DisplayFormat.dateTime(user.getCreatedAt()));

        BooleanBinding noSelection = usersTable.getSelectionModel().selectedItemProperty().isNull();
        editButton.disableProperty().bind(noSelection);
        statusButton.disableProperty().bind(noSelection);
        historyButton.disableProperty().bind(noSelection);
        usersTable.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) ->
                statusButton.setText(selected != null && !selected.isActive() ? "Activate" : "Deactivate"));
        usersTable.setRowFactory(table -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    onEdit();
                }
            });
            return row;
        });

        reload(null);
    }

    @FXML
    void onRefresh() {
        reload(null);
    }

    @FXML
    void onSearch() {
        reload(null);
    }

    @FXML
    void onClearSearch() {
        searchField.clear();
        roleFilter.setValue(null);
        statusFilter.setValue(null);
        reload(null);
    }

    @FXML
    void onEdit() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        SpringFxmlLoader.View<EditUserController> view = fxmlLoader.loadView(UserViews.EDIT);
        view.controller().edit(selected, () -> reload("User " + selected.getEmail() + " updated."));
        Dialogs.showModal(usersTable, "Edit user", view.root());
    }

    @FXML
    void onShowHistory() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        SpringFxmlLoader.View<UserStatusHistoryController> view = fxmlLoader.loadView(UserViews.STATUS_HISTORY);
        view.controller().show(selected);
        Dialogs.showModal(usersTable, "Status history", view.root());
    }

    @FXML
    void onToggleStatus() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        UserStatus newStatus = selected.isActive() ? UserStatus.INACTIVE : UserStatus.ACTIVE;
        if (newStatus == UserStatus.INACTIVE && !Dialogs.confirm(usersTable, "Deactivate " + selected.fullName() + "?",
                "The user " + selected.getEmail() + " will not be able to operate until activated again.")) {
            return;
        }
        String action = newStatus == UserStatus.ACTIVE ? "activated" : "deactivated";
        BackgroundTasks.run(
                () -> changeUserStatusUseCase.execute(new ChangeUserStatusCommand(selected.getId().value(), newStatus)),
                done -> reload("User " + selected.fullName() + " " + action + "."),
                error -> showMessage(ErrorMessages.of(error, "change the user status"), "error"));
    }

    /**
     * Loads the users; shows {@code successMessage} afterwards, or the user count when it is {@code null}.
     */
    private void reload(String successMessage) {
        refreshButton.setDisable(true);
        BackgroundTasks.run(
                () -> listUsersUseCase.execute(new ListUsersQuery(searchField.getText(), roleFilter.getValue(),
                        statusFilter.getValue())),
                users -> {
                    usersTable.getItems().setAll(users);
                    boolean searching = !searchField.getText().isBlank() || roleFilter.getValue() != null
                            || statusFilter.getValue() != null;
                    if (searching && users.isEmpty()) {
                        showMessage("No users match the search", "info");
                    } else if (successMessage == null) {
                        showMessage(count(users), "info");
                    } else {
                        showMessage(successMessage, "success");
                    }
                    refreshButton.setDisable(false);
                },
                error -> {
                    showMessage(ErrorMessages.of(error, "load the users"), "error");
                    refreshButton.setDisable(false);
                });
    }

    /**
     * The first item, {@code null}, means "no filter" and shows {@code allLabel}.
     */
    private static <E extends Enum<E>> void setUpFilter(ComboBox<E> filter, E[] values, String allLabel) {
        filter.getItems().add(null);
        filter.getItems().addAll(values);
        filter.setConverter(new StringConverter<>() {
            @Override
            public String toString(E value) {
                return value == null ? allLabel : DisplayFormat.label(value);
            }

            @Override
            public E fromString(String text) {
                throw new UnsupportedOperationException("The filters are not editable");
            }
        });
    }

    private void showMessage(String text, String styleClass) {
        messageLabel.getStyleClass().removeAll("info", "success", "error");
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(text);
    }

    private static String count(List<User> users) {
        return users.size() == 1 ? "1 user" : users.size() + " users";
    }

    private static void bind(TableColumn<User, String> column, Function<User, String> value) {
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
    }
}

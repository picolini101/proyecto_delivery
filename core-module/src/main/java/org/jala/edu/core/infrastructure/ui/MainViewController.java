package org.jala.edu.core.infrastructure.ui;

import javafx.fxml.FXML;
import java.util.Optional;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.CurrentUserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Main window shell: adds one navigation button per module ({@link NavigationItem}) and shows the module's
 * main view in the center when its button is pressed. The pressed button stays highlighted. Only the buttons the
 * {@link NavigationFilter} allows for the current user are shown; with a login, the bar also shows who is signed in
 * and a "Sign out" button.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class MainViewController {

    static final String ACTIVE = "active";

    private final ObjectProvider<NavigationItem> navigationItems;
    private final ObjectProvider<NavigationFilter> navigationFilter;
    private final ObjectProvider<CurrentUserService> currentUserService;
    private final Navigator navigator;
    private final AppShell shell;

    @FXML
    private BorderPane root;

    @FXML
    private HBox navigationBar;

    @FXML
    void initialize() {
        navigator.attach(root);
        Optional<CurrentUser> user = currentUser();
        navigationItems.orderedStream().filter(item -> isVisible(item, user)).forEach(item -> {
            Button button = new Button(item.title());
            button.getStyleClass().add("nav-button");
            button.setOnAction(event -> {
                navigator.show(item.fxmlPath());
                highlight(button);
            });
            navigationBar.getChildren().add(button);
        });
        if (shell.hasLogin()) {
            addSignOut(user);
        }
    }

    private Optional<CurrentUser> currentUser() {
        CurrentUserService service = currentUserService.getIfAvailable();
        return service == null ? Optional.empty() : service.findCurrentUser();
    }

    private boolean isVisible(NavigationItem item, Optional<CurrentUser> user) {
        NavigationFilter filter = navigationFilter.getIfAvailable();
        return filter == null || user.isEmpty() || filter.isVisible(item, user.get());
    }

    private void addSignOut(Optional<CurrentUser> user) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label userLabel = new Label(user.map(CurrentUser::displayName).orElse(""));
        userLabel.getStyleClass().add("nav-user");
        Button signOut = new Button("Sign out");
        signOut.getStyleClass().add("nav-button");
        signOut.setOnAction(event -> shell.signOut());
        navigationBar.getChildren().addAll(spacer, userLabel, signOut);
    }

    private void highlight(Button selected) {
        navigationBar.getChildren().forEach(node -> node.getStyleClass().remove(ACTIVE));
        selected.getStyleClass().add(ACTIVE);
    }
}

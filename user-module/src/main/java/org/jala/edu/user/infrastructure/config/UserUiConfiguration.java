package org.jala.edu.user.infrastructure.config;

import org.jala.edu.core.infrastructure.ui.LoginScreen;
import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.jala.edu.user.infrastructure.adapter.in.ui.UserViews;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Adds the "Users" and "My account" buttons to the main window navigation bar and provides the login screen shown
 * before the main window.
 */
@Configuration
public class UserUiConfiguration {

    @Bean
    @Order(100)
    public NavigationItem userNavigationItem() {
        return new NavigationItem("Users", UserViews.MAIN);
    }

    /**
     * "My account" (#90): every role sees it, because {@code RoleMenu} shows titles it does not list to everyone.
     */
    @Bean
    @Order(190)
    public NavigationItem myAccountNavigationItem() {
        return new NavigationItem("My account", UserViews.MY_ACCOUNT);
    }

    @Bean
    public LoginScreen loginScreen() {
        return new LoginScreen(UserViews.LOGIN);
    }
}

package org.jala.edu.core.infrastructure.ui;

import org.jala.edu.core.contract.CurrentUser;

/**
 * Decides which navigation buttons the current user sees. Without a filter bean every button is shown.
 */
@FunctionalInterface
public interface NavigationFilter {

    boolean isVisible(NavigationItem item, CurrentUser user);
}

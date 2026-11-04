package org.jala.edu.system;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.infrastructure.ui.NavigationFilter;
import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.springframework.stereotype.Component;

/**
 * Menu by role (#61): which navigation buttons each role sees, by the module's button title. Kept here so the
 * modules do not need to know about roles; a title that is not listed is shown to everyone.
 * "My commerce orders" is the merchant's button of {@code order-module} (D-08, #79).
 */
@Component
public class RoleMenu implements NavigationFilter {

    static final Map<String, Set<UserRole>> ROLES_BY_TITLE = Map.of(
            "Users", EnumSet.of(UserRole.ADMIN),
            "Catalog", EnumSet.of(UserRole.ADMIN, UserRole.MERCHANT),
            "Orders", EnumSet.of(UserRole.CUSTOMER),
            "My commerce orders", EnumSet.of(UserRole.MERCHANT),
            "Payments", EnumSet.of(UserRole.CUSTOMER),
            "Deliveries", EnumSet.of(UserRole.COURIER));

    @Override
    public boolean isVisible(NavigationItem item, CurrentUser user) {
        Set<UserRole> roles = ROLES_BY_TITLE.get(item.title());
        return roles == null || roles.contains(user.role());
    }
}

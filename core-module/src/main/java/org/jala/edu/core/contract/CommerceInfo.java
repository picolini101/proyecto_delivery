package org.jala.edu.core.contract;

import java.util.Objects;
import java.util.UUID;

/**
 * A commerce's public data, as other modules see it (#146): its id and name, so an order can show the commerce it
 * was bought from. Never the commerce entity.
 */
public record CommerceInfo(UUID commerceId, String name) {

    public CommerceInfo {
        Objects.requireNonNull(commerceId, "Commerce id is required");
        Objects.requireNonNull(name, "Commerce name is required");
    }
}

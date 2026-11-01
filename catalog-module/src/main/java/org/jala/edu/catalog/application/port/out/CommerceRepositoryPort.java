package org.jala.edu.catalog.application.port.out;

import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.core.application.port.out.CrudRepositoryPort;

public interface CommerceRepositoryPort extends CrudRepositoryPort<Commerce, CommerceId> {
}

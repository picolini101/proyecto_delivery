package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jala.edu.core.infrastructure.persistence.jpa.JpaCrudRepositoryAdapter;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.model.*;

public class OrderStatusHistoryPersistenceAdapter extends JpaCrudRepositoryAdapter<OrderStatusChange, UUID,OrderStatusHistoryJpaEntity,String>
        implements OrderStatusHistoryPort {


}

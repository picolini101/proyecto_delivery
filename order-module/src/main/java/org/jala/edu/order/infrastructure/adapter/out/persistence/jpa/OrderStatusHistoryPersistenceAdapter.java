package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.jala.edu.core.infrastructure.persistence.jpa.JpaCrudRepositoryAdapter;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.model.*;

public class OrderStatusHistoryPersistenceAdapter extends JpaCrudRepositoryAdapter<OrderLine, OrderLineId,OrderLineJpaEntity,String>
        implements OrderStatusHistoryPort {


}

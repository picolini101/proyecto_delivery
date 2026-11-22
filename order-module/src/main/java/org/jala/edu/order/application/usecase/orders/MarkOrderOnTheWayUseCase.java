package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.order.application.port.in.ChangeOrderStatusCommand;
import org.jala.edu.order.application.port.in.MarkOrderCommand;
import org.jala.edu.order.domain.model.OrderStatus;

public class MarkOrderOnTheWayUseCase implements UseCase<MarkOrderCommand, Void> {
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    public MarkOrderOnTheWayUseCase(ChangeOrderStatusUseCase changeOrderStatusUseCase) {
        this.changeOrderStatusUseCase = changeOrderStatusUseCase;
    }
    @Override
    public Void execute(MarkOrderCommand command) {
        ChangeOrderStatusCommand statusCommand =
                new ChangeOrderStatusCommand(
                        command.orderId(),
                        OrderStatus.ON_THE_WAY,
                        command.changedById(),
                        "Transition to OnTheWay");
        return changeOrderStatusUseCase.execute(statusCommand);
    }
}

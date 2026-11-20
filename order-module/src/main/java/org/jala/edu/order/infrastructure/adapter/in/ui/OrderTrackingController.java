package org.jala.edu.order.infrastructure.adapter.in.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.infrastructure.ui.Navigator;
// import org.jala.edu.order.application.dto.OrderTrackingSummary;
import org.jala.edu.order.domain.model.OrderStatus;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class OrderTrackingController {

    private final Navigator navigator;

    @FXML private Label shortIdLabel;
    @FXML private Label productNameLabel;
    @FXML private Label statusMessageLabel;
    @FXML private HBox stepperContainer;

    // Los estados que mostraremos visualmente en la línea de tiempo
    private final List<OrderStatus> timeline = List.of(
            OrderStatus.CONFIRMED,
            OrderStatus.READY_FOR_DELIVERY,
            OrderStatus.ASSIGNED,
            OrderStatus.ON_THE_WAY,
            OrderStatus.DELIVERED
    );

    /**
     * Prepara la vista con los datos que vienen del backend.
     */
    public void prepare(OrderTrackingSummary summary) {
        shortIdLabel.setText("REF: " + summary.shortId());
        productNameLabel.setText(summary.productName());
        statusMessageLabel.setText(getStatusMessage(summary.currentStatus()));

        drawStepper(summary.currentStatus());
    }

    private void drawStepper(OrderStatus currentStatus) {
        stepperContainer.getChildren().clear();

        // Si se canceló, no mostramos la línea de tiempo, solo un mensaje
        if (currentStatus == OrderStatus.CANCELLED) {
            statusMessageLabel.setText("Este pedido fue cancelado.");
            statusMessageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            return;
        }

        if (currentStatus == OrderStatus.CREATED) {
            addStep("Recibido", false, false);
            return;
        }

        // Encontrar en qué paso de la lista estamos
        int currentIndex = timeline.indexOf(currentStatus);
        if (currentIndex == -1) currentIndex = 0;

        for (int i = 0; i < timeline.size(); i++) {
            boolean isCompleted = i <= currentIndex;
            boolean isCurrent = i == currentIndex;
            String label = getStepLabel(timeline.get(i));

            addStep(label, isCompleted, isCurrent);

            // Agregar la línea conectora (si no es el último círculo del progreso)
            if (i < timeline.size() - 1) {
                Line connector = new Line(0, 0, 50, 0);
                connector.setStroke(isCompleted && i < currentIndex ? Color.web("#2563eb") : Color.web("#e0e0e0"));
                connector.setStrokeWidth(2);
                StackPane connectorPane = new StackPane(connector);
                stepperContainer.getChildren().add(connectorPane);
            }
        }
    }

    private void addStep(String labelText, boolean isCompleted, boolean isCurrent) {
        VBox stepBox = new VBox(5);
        stepBox.setAlignment(Pos.CENTER);

        Circle circle = new Circle(8);
        if (isCurrent) {
            circle.setFill(Color.web("#2563eb")); // Azul actual
        } else if (isCompleted) {
            circle.setFill(Color.web("#60a5fa")); // Azul pasado
        } else {
            circle.setFill(Color.TRANSPARENT); // Círculo vacío
            circle.setStroke(Color.web("#cccccc"));
            circle.setStrokeWidth(2);
        }

        Label label = new Label(labelText);
        label.setStyle(isCurrent ? "-fx-font-weight: bold; -fx-text-fill: #222;" : "-fx-text-fill: #888; -fx-font-size: 11px;");
        stepBox.getChildren().addAll(circle, label);
        stepperContainer.getChildren().add(stepBox);
    }

    private String getStepLabel(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "Recibido";
            case READY_FOR_DELIVERY -> "En preparación";
            case ASSIGNED -> "Asignado";
            case ON_THE_WAY -> "En camino";
            case DELIVERED -> "Entregado";
            default -> status.name();
        };
    }

    private String getStatusMessage(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "Hemos recibido tu pedido y está en cola de preparación.";
            case READY_FOR_DELIVERY -> "Tu pedido está listo y esperando a un repartidor.";
            case ASSIGNED -> "Un repartidor ha tomado tu pedido.";
            case ON_THE_WAY -> "El repartidor va en camino a tu dirección.";
            case DELIVERED -> "¡Tu pedido ha sido entregado!";
            case CANCELLED -> "El pedido fue cancelado.";
            default -> "Procesando...";
        };
    }

    @FXML
    void onBack() {
        navigator.show(OrderViews.MAIN);
    }
}
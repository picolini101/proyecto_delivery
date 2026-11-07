package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import javafx.scene.control.Label;

/** Muestra un mensaje de éxito, error o info en una etiqueta, cambiando su estilo. */
final class MessageLabels {

    private MessageLabels() {
    }

    static void show(Label label, String text, String styleClass) {
        label.getStyleClass().removeAll("info", "success", "error");
        label.getStyleClass().add(styleClass);
        label.setText(text);
    }
}
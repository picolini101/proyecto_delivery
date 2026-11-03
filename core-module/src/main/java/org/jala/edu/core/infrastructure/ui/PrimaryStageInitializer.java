package org.jala.edu.core.infrastructure.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Opens the first screen once JavaFX hands over the primary stage.
 */
@Component
@RequiredArgsConstructor
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    private final AppShell shell;

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        shell.start(event.getStage());
    }
}

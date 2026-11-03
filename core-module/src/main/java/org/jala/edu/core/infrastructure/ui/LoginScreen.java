package org.jala.edu.core.infrastructure.ui;

/**
 * The view shown before the main window ({@code fxmlPath}, a classpath resource). The module that provides the
 * login exposes one as a Spring bean; without it the application opens the main window directly.
 */
public record LoginScreen(String fxmlPath) {
}

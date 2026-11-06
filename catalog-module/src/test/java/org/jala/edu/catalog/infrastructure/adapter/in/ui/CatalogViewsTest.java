package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CatalogViewsTest {

    @ParameterizedTest
    @ValueSource(strings = {CatalogViews.MAIN, CatalogViews.CREATE_PRODUCT})
    void everyViewExistsOnTheClasspath(String path) {
        assertThat(getClass().getResource(path)).as("FXML on the classpath: %s", path).isNotNull();
    }
}
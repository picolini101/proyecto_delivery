package org.jala.edu.delivery.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TransportModeTest {

    @Test
    void hasTheExpectedTransportModes() {
        assertThat(TransportMode.values())
                .containsExactly(
                        TransportMode.BICYCLE,
                        TransportMode.MOTORCYCLE,
                        TransportMode.ON_FOOT
                );
    }

    @Test
    void returnsTheCorrectLabelForEachTransportMode() {
        assertThat(TransportMode.BICYCLE.toString()).isEqualTo("Bicicleta");
        assertThat(TransportMode.MOTORCYCLE.toString()).isEqualTo("Motocicleta");
        assertThat(TransportMode.ON_FOOT.toString()).isEqualTo("A pie");
    }
}
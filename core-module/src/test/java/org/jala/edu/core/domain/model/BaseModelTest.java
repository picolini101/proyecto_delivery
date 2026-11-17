package org.jala.edu.core.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

/**
 * Exercises the shared identity and audit behavior with a minimal sample model.
 */
class BaseModelTest {

    private static final Instant CREATED = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void keepsTheIdAndTheTimestamps() {
        // Act
        Note note = new Note("n-1", CREATED, CREATED);

        // Assert
        assertThat(note.getId()).isEqualTo("n-1");
        assertThat(note.getCreatedAt()).isEqualTo(CREATED);
        assertThat(note.getUpdatedAt()).isEqualTo(CREATED);
    }

    @Test
    void rejectsAMissingIdOrTimestamp() {
        // Act
        Throwable noId = catchThrowable(() -> new Note(null, CREATED, CREATED));
        Throwable noCreatedAt = catchThrowable(() -> new Note("n-1", null, CREATED));
        Throwable noUpdatedAt = catchThrowable(() -> new Note("n-1", CREATED, null));

        // Assert
        assertThat(noId).isInstanceOf(NullPointerException.class).hasMessage("Id is required");
        assertThat(noCreatedAt).isInstanceOf(NullPointerException.class).hasMessage("Creation date is required");
        assertThat(noUpdatedAt).isInstanceOf(NullPointerException.class).hasMessage("Update date is required");
    }

    @Test
    void touchMovesOnlyTheUpdateDate() {
        // Arrange
        Note note = new Note("n-1", CREATED, CREATED);

        // Act
        note.edit();

        // Assert
        assertThat(note.getUpdatedAt()).isAfter(CREATED);
        assertThat(note.getCreatedAt()).isEqualTo(CREATED);
    }

    @Test
    void nowHasMicrosecondPrecision() {
        // Act
        Instant now = Note.currentTime();

        // Assert
        assertThat(now).isEqualTo(now.truncatedTo(ChronoUnit.MICROS));
    }

    @Test
    void twoModelsWithTheSameIdAreEqual() {
        // Arrange
        Note original = new Note("n-1", CREATED, CREATED);
        Note edited = new Note("n-1", CREATED, Instant.parse("2026-02-01T10:00:00Z"));

        // Act and Assert
        assertThat(edited).isEqualTo(original).hasSameHashCodeAs(original);
        assertThat(new Note("n-2", CREATED, CREATED)).isNotEqualTo(original);
    }

    private static final class Note extends BaseModel<String> {

        Note(String id, Instant createdAt, Instant updatedAt) {
            super(id, createdAt, updatedAt);
        }

        void edit() {
            touch();
        }

        static Instant currentTime() {
            return now();
        }
    }
}

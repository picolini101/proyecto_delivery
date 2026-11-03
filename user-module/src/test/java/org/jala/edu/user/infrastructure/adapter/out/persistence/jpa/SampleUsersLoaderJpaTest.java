package org.jala.edu.user.infrastructure.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import org.jala.edu.user.infrastructure.config.SampleUsersLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

/**
 * Restarting the application with MySQL must not duplicate the sample users (H2 in MySQL mode, real migrations).
 */
@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:sample-users;MODE=MySQL;DATABASE_TO_LOWER=TRUE")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserPersistenceAdapter.class)
class SampleUsersLoaderJpaTest {

    @Autowired
    private UserPersistenceAdapter adapter;

    @Test
    void loadingTwiceKeepsFiveSampleUsers() {
        // Arrange
        SampleUsersLoader loader = new SampleUsersLoader(adapter);
        loader.run(null);

        // Act (second start)
        loader.run(null);

        // Assert
        assertThat(adapter.findAll()).hasSize(5);
    }
}

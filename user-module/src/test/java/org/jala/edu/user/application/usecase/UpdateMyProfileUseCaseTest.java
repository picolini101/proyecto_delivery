package org.jala.edu.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.jala.edu.user.UserFixtures.NOBODY_SIGNED_IN;
import static org.jala.edu.user.UserFixtures.user;

import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.NotAuthenticatedException;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.user.application.port.in.UpdateMyProfileCommand;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.junit.jupiter.api.Test;

class UpdateMyProfileUseCaseTest {

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();

    @Test
    void changesOnlyTheNameAndPhoneOfTheSignedInUser() {
        // Arrange
        User courier = repository.save(user("ana@example.com", UserRole.COURIER));
        User other = repository.save(user("luis@example.com"));
        UpdateMyProfileUseCase useCase = new UpdateMyProfileUseCase(repository, signedInAs(courier));

        // Act
        useCase.execute(new UpdateMyProfileCommand("Ana Maria", "Lopez", "+59171111111"));

        // Assert
        User stored = repository.findById(courier.getId()).orElseThrow();
        assertThat(stored.fullName()).isEqualTo("Ana Maria Lopez");
        assertThat(stored.getPhoneNumber()).isEqualTo("+59171111111");
        assertThat(stored.getEmail().value()).isEqualTo("ana@example.com");
        assertThat(stored.getRole()).isEqualTo(UserRole.COURIER);
        assertThat(repository.findById(other.getId()).orElseThrow().fullName()).isEqualTo("Ana Perez");
    }

    @Test
    void rejectsABlankNameWithoutChangingTheProfile() {
        // Arrange
        User ana = repository.save(user("ana@example.com"));
        UpdateMyProfileUseCase useCase = new UpdateMyProfileUseCase(repository, signedInAs(ana));

        // Act
        Throwable thrown = catchThrowable(() -> useCase.execute(new UpdateMyProfileCommand(" ", "Lopez", null)));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class);
        assertThat(repository.findById(ana.getId()).orElseThrow().fullName()).isEqualTo("Ana Perez");
    }

    @Test
    void needsASignedInUser() {
        // Arrange
        UpdateMyProfileUseCase useCase = new UpdateMyProfileUseCase(repository, NOBODY_SIGNED_IN);

        // Act
        Throwable thrown = catchThrowable(() -> useCase.execute(new UpdateMyProfileCommand("Ana", "Perez", null)));

        // Assert
        assertThat(thrown).isInstanceOf(NotAuthenticatedException.class);
    }

    private static org.jala.edu.core.contract.CurrentUserService signedInAs(User user) {
        return org.jala.edu.user.UserFixtures.signedInAs(new CurrentUser(user.getId().value(),
                org.jala.edu.core.contract.UserRole.valueOf(user.getRole().name()), user.fullName()));
    }
}

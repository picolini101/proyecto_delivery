package org.jala.edu.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Instant;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CommerceTest {

    @Test
    void registersADisabledCommerceAndStripsTheText() {
        Commerce commerce = Commerce.register(" Pizzeria ", " Calle 1 ", " +573001234567 ", SampleUsers.MERCHANT);

        assertThat(commerce.getName()).isEqualTo("Pizzeria");
        assertThat(commerce.getAddress()).isEqualTo("Calle 1");
        assertThat(commerce.getPhone().value()).isEqualTo("+573001234567");
        assertThat(commerce.getMerchantId()).isEqualTo(SampleUsers.MERCHANT);
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.DISABLED);
        assertThat(commerce.isActive()).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsABlankName(String name) {
        Throwable thrown = catchThrowable(() ->
                Commerce.register(name, CatalogFixtures.ADDRESS, CatalogFixtures.PHONE, SampleUsers.MERCHANT));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Commerce name cannot be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsABlankAddress(String address) {
        Throwable thrown = catchThrowable(() ->
                Commerce.register("Pizzeria", address, CatalogFixtures.PHONE, SampleUsers.MERCHANT));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Commerce address cannot be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsABlankPhone(String phone) {
        Throwable thrown = catchThrowable(() ->
                Commerce.register("Pizzeria", CatalogFixtures.ADDRESS, phone, SampleUsers.MERCHANT));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Commerce phone cannot be blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {"3001234567", "+0123456", "+57 300 123", "abc", "+1234567890123456"})
    void rejectsAPhoneWithAnInvalidFormat(String phone) {
        Throwable thrown = catchThrowable(() ->
                Commerce.register("Pizzeria", CatalogFixtures.ADDRESS, phone, SampleUsers.MERCHANT));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessageContaining("Commerce phone has an invalid format");
    }

    @Test
    void activatesAndDisablesTheCommerce() {
        Commerce commerce = CatalogFixtures.disabledCommerce("Pizzeria");

        commerce.activate();
        assertThat(commerce.isActive()).isTrue();

        commerce.disable();
        assertThat(commerce.isActive()).isFalse();
    }

    @Test
    void restoreRejectsANullStatus() {
        Throwable thrown = catchThrowable(() -> Commerce.restore(
                CommerceId.newId(), "Pizzeria", CatalogFixtures.ADDRESS, CatalogFixtures.PHONE,
                SampleUsers.MERCHANT, null, Instant.now(), Instant.now()));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Commerce status cannot be null");
    }

    @Test
    void rejectsACommerceWithoutMerchant() {
        Throwable thrown = catchThrowable(() -> Commerce.register(
                "Pizzeria", CatalogFixtures.ADDRESS, CatalogFixtures.PHONE, null));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Commerce merchant is required");
    }
}
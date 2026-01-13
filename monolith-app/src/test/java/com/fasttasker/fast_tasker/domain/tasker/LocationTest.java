package com.fasttasker.fast_tasker.domain.tasker;

import com.fasttasker.common.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationTest {
    @Test
    void shouldCreateValidLocation() {
        Location location = Location.builder()
                .latitude(-16.409)
                .longitude(-71.537)
                .address("Plaza de Armas, Arequipa")
                .zip("04001")
                .build();

        assertThat(location).isNotNull();
        assertThat(location.getLatitude()).isEqualTo(-16.409);
        assertThat(location.getZip()).isEqualTo("04001");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-90.1, 90.1, -100.0, 100.0})
    void shouldThrowWhenLatitudeInvalid(double invalidLat) {
        assertThatThrownBy(() -> Location.builder()
                .latitude(invalidLat)
                .longitude(0)
                .address("direction")
                .zip("04001")
                .build()
        )
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("latitude must be between -90 and 90");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-180.1, 180.1, -200.0})
    void shouldThrowWhenLongitudeInvalid(double invalidLon) {
        assertThatThrownBy(() -> Location.builder()
                .latitude(0)
                .longitude(invalidLon)
                .zip("04001")
                .build()
        )
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("longitude must be between -180 and 180");
    }

    @Test
    void shouldThrowWhenZipIsEmpty() {
        assertThatThrownBy(() -> Location.builder()
                .latitude(0)
                .longitude(0)
                .zip("") // Empty
                .build()
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("zip cannot be empty");
    }
}
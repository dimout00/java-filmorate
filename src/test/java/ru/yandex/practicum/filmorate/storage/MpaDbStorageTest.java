package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class})
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void testGetAllMpa() {
        List<Mpa> mpaList = mpaStorage.getAllMpa();

        assertThat(mpaList).hasSize(5);
        assertThat(mpaList.get(0).getName()).isEqualTo("G");
        assertThat(mpaList.get(1).getName()).isEqualTo("PG");
    }

    @Test
    void testGetMpaById() {
        Optional<Mpa> mpa = mpaStorage.getMpaById(1);

        assertThat(mpa).isPresent();
        assertThat(mpa.get().getName()).isEqualTo("G");
        assertThat(mpa.get().getDescription()).isNotNull();
    }
}
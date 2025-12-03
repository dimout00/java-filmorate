package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class})
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    void testGetAllGenres() {
        List<Genre> genres = genreStorage.getAllGenres();

        assertThat(genres).hasSize(6);
        assertThat(genres.get(0).getName()).isEqualTo("Комедия");
        assertThat(genres.get(1).getName()).isEqualTo("Драма");
    }

    @Test
    void testGetGenreById() {
        Optional<Genre> genre = genreStorage.getGenreById(1);

        assertThat(genre).isPresent();
        assertThat(genre.get().getName()).isEqualTo("Комедия");
    }
}
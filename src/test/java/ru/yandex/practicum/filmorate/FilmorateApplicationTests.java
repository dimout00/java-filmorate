package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class FilmorateApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        // Тест загрузки контекста Spring
        assertTrue(true);
    }

    @Test
    public void testCreateFilmWithEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Название не может быть пустым"));
    }

    @Test
    public void testCreateValidUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("validlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
    }

    @Test
    public void testCreateUserWithSpacesInLogin() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("invalid login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ResponseEntity<String> response = restTemplate.postForEntity("/users", user, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Логин не может быть пустым и содержать пробелы"));
    }

    @Test
    public void testCreateFilmWithLongDescription() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("A".repeat(201)); // Описание длиннее 200 символов
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Максимальная длина описания — 200 символов"));
    }

    @Test
    public void testCreateFilmWithEarlyReleaseDate() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(1890, 1, 1)); // Дата до 28.12.1895
        film.setDuration(100);

        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Дата релиза — не раньше 28 декабря 1895 года"));
    }

    @Test
    public void testCreateFilmWithNegativeDuration() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-100); // Отрицательная продолжительность

        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Продолжительность фильма должна быть положительным числом"));
    }
}
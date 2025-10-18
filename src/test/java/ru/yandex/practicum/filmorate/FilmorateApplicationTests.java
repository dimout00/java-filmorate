package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmorateApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    public void testFilmValidation() {
        FilmController filmController = new FilmController();

        // Тест пустого названия
        Film film1 = new Film();
        film1.setName("");
        film1.setDescription("Valid description");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(100);
        assertThrows(ValidationException.class, () -> filmController.addFilm(film1));

        // Тест длинного описания
        Film film2 = new Film();
        film2.setName("Valid name");
        film2.setDescription("A".repeat(201));
        film2.setReleaseDate(LocalDate.of(2000, 1, 1));
        film2.setDuration(100);
        assertThrows(ValidationException.class, () -> filmController.addFilm(film2));

        // Тест ранней даты релиза
        Film film3 = new Film();
        film3.setName("Valid name");
        film3.setDescription("Valid description");
        film3.setReleaseDate(LocalDate.of(1890, 1, 1));
        film3.setDuration(100);
        assertThrows(ValidationException.class, () -> filmController.addFilm(film3));

        // Тест отрицательной продолжительности
        Film film4 = new Film();
        film4.setName("Valid name");
        film4.setDescription("Valid description");
        film4.setReleaseDate(LocalDate.of(2000, 1, 1));
        film4.setDuration(-100);
        assertThrows(ValidationException.class, () -> filmController.addFilm(film4));
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
    public void testUserValidation() {
        UserController userController = new UserController();

        // Тест неверного email
        User user1 = new User();
        user1.setEmail("invalid-email");
        user1.setLogin("validlogin");
        user1.setBirthday(LocalDate.of(2000, 1, 1));
        assertThrows(ValidationException.class, () -> userController.createUser(user1));

        // Тест логина с пробелами
        User user2 = new User();
        user2.setEmail("valid@email.com");
        user2.setLogin("invalid login");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        assertThrows(ValidationException.class, () -> userController.createUser(user2));

        // Тест будущей даты рождения
        User user3 = new User();
        user3.setEmail("valid@email.com");
        user3.setLogin("validlogin");
        user3.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userController.createUser(user3));

        // Тест автоматической подстановки логина вместо пустого имени
        User user4 = new User();
        user4.setEmail("valid@email.com");
        user4.setLogin("testlogin");
        user4.setName("");
        user4.setBirthday(LocalDate.of(2000, 1, 1));
        User createdUser = userController.createUser(user4);
        assertEquals("testlogin", createdUser.getName());
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
}
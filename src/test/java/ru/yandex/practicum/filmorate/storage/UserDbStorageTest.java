package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void testCreateUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(user);

        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("test@mail.ru");
        assertThat(createdUser.getLogin()).isEqualTo("testlogin");
        assertThat(createdUser.getName()).isEqualTo("Test User");
    }

    @Test
    void testGetUserById() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(user);
        Optional<User> foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.get().getEmail()).isEqualTo("test@mail.ru");
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setEmail("test1@mail.ru");
        user1.setLogin("testlogin1");
        user1.setName("Test User 1");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setEmail("test2@mail.ru");
        user2.setLogin("testlogin2");
        user2.setName("Test User 2");
        user2.setBirthday(LocalDate.of(2001, 1, 1));

        userStorage.create(user1);
        userStorage.create(user2);

        List<User> users = userStorage.getAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(user);

        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@mail.ru");

        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@mail.ru");
    }
}
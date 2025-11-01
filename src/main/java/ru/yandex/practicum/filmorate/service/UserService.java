package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        validateUser(user);
        userStorage.getById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + user.getId() + " не найден."));
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(int id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден."));
    }

    public void addFriend(int userId, int friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        userStorage.addFriend(userId, friendId);
        log.info("Пользователь с id={} добавил в друзья пользователя с id={}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        userStorage.removeFriend(userId, friendId);
        log.info("Пользователь с id={} удалил из друзей пользователя с id={}", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        checkUserExists(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        checkUserExists(userId);
        checkUserExists(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Попытка создания пользователя с неверным email: {}", user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Попытка создания пользователя с неверным логином: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы.");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Попытка создания пользователя с датой рождения в будущем: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
    }

    private void checkUserExists(int userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден.");
        }
    }
}
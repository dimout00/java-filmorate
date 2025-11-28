package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("name", user.getName());
        parameters.put("birthday", user.getBirthday());

        Number key = simpleJdbcInsert.executeAndReturnKey(parameters);
        user.setId(key.intValue());
        log.info("Создан пользователь с id: {}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        log.info("Обновлен пользователь с id: {}", user.getId());
        return user;
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser);
        // Загружаем друзей для каждого пользователя
        users.forEach(user -> {
            Map<Integer, FriendshipStatus> friends = loadUserFriends(user.getId());
            user.setFriends(friends);
        });
        return users;
    }

    @Override
    public Optional<User> getById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        User user = users.get(0);
        // Загружаем друзей пользователя
        Map<Integer, FriendshipStatus> friends = loadUserFriends(user.getId());
        user.setFriends(friends);
        return Optional.of(user);
    }

    @Override
    public void addFriend(int userId, int friendId, FriendshipStatus status) {
        String sql = "MERGE INTO friendships (user_id, friend_id, status) KEY (user_id, friend_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, status.toString());
        log.info("Пользователь {} добавил в друзья пользователя {} со статусом {}", userId, friendId, status);
    }

    @Override
    public void confirmFriend(int userId, int friendId) {
        String sql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, FriendshipStatus.CONFIRMED.toString(), friendId, userId);
        log.info("Пользователь {} подтвердил дружбу с пользователем {}", userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.user_id = f1.friend_id " +
                "JOIN friendships f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));

        java.sql.Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            user.setBirthday(birthday.toLocalDate());
        }

        return user;
    }

    private Map<Integer, FriendshipStatus> loadUserFriends(int userId) {
        String sql = "SELECT friend_id, status FROM friendships WHERE user_id = ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, userId);

        Map<Integer, FriendshipStatus> friends = new HashMap<>();
        for (Map<String, Object> row : results) {
            int friendId = (Integer) row.get("friend_id");
            String statusStr = (String) row.get("status");
            FriendshipStatus status = FriendshipStatus.valueOf(statusStr);
            friends.put(friendId, status);
        }
        return friends;
    }
}
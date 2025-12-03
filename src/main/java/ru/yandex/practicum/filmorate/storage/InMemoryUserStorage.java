package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Map<Integer, FriendshipStatus>> friendships = new HashMap<>();
    private int nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        friendships.put(user.getId(), new HashMap<>());
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(int userId, int friendId, FriendshipStatus status) {
        friendships.computeIfAbsent(userId, k -> new HashMap<>()).put(friendId, status);
    }

    @Override
    public void confirmFriend(int userId, int friendId) {
        // Когда пользователь подтверждает дружбу, обновляем статус в обе стороны
        if (friendships.containsKey(userId) && friendships.get(userId).containsKey(friendId)) {
            friendships.get(userId).put(friendId, FriendshipStatus.CONFIRMED);
        }
        friendships.computeIfAbsent(friendId, k -> new HashMap<>()).put(userId, FriendshipStatus.CONFIRMED);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        if (friendships.containsKey(userId)) {
            friendships.get(userId).remove(friendId);
        }
        if (friendships.containsKey(friendId)) {
            friendships.get(friendId).remove(userId);
        }
    }

    @Override
    public List<User> getFriends(int userId) {
        Map<Integer, FriendshipStatus> userFriends = friendships.getOrDefault(userId, Collections.emptyMap());
        return userFriends.keySet().stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> userFriends = friendships.getOrDefault(userId, Collections.emptyMap()).keySet();
        Set<Integer> otherFriends = friendships.getOrDefault(otherId, Collections.emptyMap()).keySet();

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        validateFilm(film);
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
        Film createdFilm = filmStorage.create(film);
        log.info("Создан фильм с id: {}", createdFilm.getId());
        return createdFilm;
    }

    public Film update(Film film) {
        validateFilm(film);
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());

        if (filmStorage.getById(film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден.");
        }

        Film updatedFilm = filmStorage.update(film);
        log.info("Обновлен фильм с id: {}", updatedFilm.getId());
        return updatedFilm;
    }

    public List<Film> getAll() {
        log.debug("Получен запрос на получение всех фильмов");
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        log.debug("Получен запрос на получение фильма с id={}", id);
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден."));
    }

    public void addLike(int filmId, int userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь с id={} поставил лайк фильму с id={}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь с id={} удалил лайк с фильма с id={}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        log.debug("Получен запрос на получение {} популярных фильмов", count);
        return filmStorage.getPopularFilms(count);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Попытка создания фильма с пустым названием");
            throw new ValidationException("Название не может быть пустым.");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Попытка создания фильма с описанием длиной более 200 символов: {}", film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов.");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(minReleaseDate)) {
            log.error("Попытка создания фильма с неверной датой релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года.");
        }
        if (film.getDuration() <= 0) {
            log.error("Попытка создания фильма с отрицательной продолжительностью: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
    }

    private void validateMpa(Mpa mpa) {
        if (mpa == null || mpa.getId() == null) {
            throw new ValidationException("MPA рейтинг обязателен для фильма.");
        }
    }

    private void validateGenres(List<Genre> genres) {
        if (genres != null) {
            // Проверка на дубликаты жанров
            long distinctCount = genres.stream().map(Genre::getId).distinct().count();
            if (distinctCount != genres.size()) {
                throw new ValidationException("Фильм не может содержать дублирующиеся жанры.");
            }
        }
    }

    private void checkFilmExists(int filmId) {
        if (filmStorage.getById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден.");
        }
    }

    private void checkUserExists(int userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден.");
        }
    }
}
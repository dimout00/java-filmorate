## java-filmorate
Filmorate Database Schema
Описание базы данных
База данных Filmorate предназначена для хранения информации о фильмах, пользователях, их предпочтениях и дружеских связях.

Схема базы данных
https://github.com/dimout00/java-filmorate/blob/4c208bdda792f8e9c730ffead22e22bed71500d4/filmorate_database_schema.png

Основные таблицы
users — содержит данные о пользователях

user_id — уникальный идентификатор пользователя (первичный ключ)

email — электронная почта пользователя

login — логин пользователя

name — имя пользователя

birthday — дата рождения пользователя

films — содержит данные о фильмах

film_id — уникальный идентификатор фильма (первичный ключ)

name — название фильма

description — описание фильма

release_date — дата выпуска фильма

duration — продолжительность фильма в минутах

mpa_rating_id — идентификатор рейтинга MPA (внешний ключ)

mpa_ratings — справочник рейтингов MPA

mpa_rating_id — уникальный идентификатор рейтинга (первичный ключ)

name — название рейтинга (G, PG, PG-13, R, NC-17)

description — описание рейтинга

genres — справочник жанров

genre_id — уникальный идентификатор жанра (первичный ключ)

name — название жанра

film_genres — связь между фильмами и жанрами (многие ко многим)

film_id — идентификатор фильма (внешний ключ)

genre_id — идентификатор жанра (внешний ключ)

friendships — дружеские связи между пользователями

user_id — идентификатор пользователя (внешний ключ)

friend_id — идентификатор друга (внешний ключ)

status — статус дружбы ('unconfirmed', 'confirmed')

likes — лайки фильмов от пользователей

film_id — идентификатор фильма (внешний ключ)

user_id — идентификатор пользователя (внешний ключ)

Связи между таблицами
Фильмы связаны с рейтингами MPA через films.mpa_rating_id → mpa_ratings.mpa_rating_id

Фильмы связаны с жанрами через таблицу film_genres

Пользователи связаны друг с другом через таблицу friendships

Лайки связывают пользователей и фильмы через таблицу likes

Примеры запросов
Получить все фильмы

```sql
SELECT * FROM films;
```
#Получить топ N популярных фильмов

```sql
SELECT f.*, COUNT(l.user_id) AS likes_count
FROM films f
LEFT JOIN likes l ON f.film_id = l.film_id
GROUP BY f.film_id
ORDER BY likes_count DESC
LIMIT ?;
```
#Получить список друзей пользователя

```sql
SELECT u.* 
FROM friendships f
JOIN users u ON f.friend_id = u.user_id
WHERE f.user_id = ? AND f.status = 'confirmed';
```
#Получить общих друзей двух пользователей

```sql
WITH friends1 AS (
    SELECT friend_id AS user_id
    FROM friendships
    WHERE user_id = ? AND status = 'confirmed'
),
friends2 AS (
    SELECT friend_id AS user_id
    FROM friendships
    WHERE user_id = ? AND status = 'confirmed'
)
SELECT u.*
FROM friends1 f1
JOIN friends2 f2 ON f1.user_id = f2.user_id
JOIN users u ON f1.user_id = u.user_id;
```
#Добавление нового пользователя

```sql
INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?);
```
#Обновление данных пользователя

```sql
UPDATE users SET email=?, login=?, name=?, birthday=? WHERE user_id=?;
```

#Добавление лайка фильму

```sql
INSERT INTO likes (film_id, user_id) VALUES (?, ?);
```

#Удаление лайка

```sql
DELETE FROM likes WHERE film_id=? AND user_id=?;
```

#Добавление друга (отправка заявки)

```sql
INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'unconfirmed');
```

#Подтверждение дружбы

```sql
-- Удаляем заявку
DELETE FROM friendships WHERE user_id=? AND friend_id=? AND status='unconfirmed';
-- Добавляем две подтвержденные записи
INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'confirmed');
INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'confirmed');
```

#Удаление друга

```sql
DELETE FROM friendships
WHERE (user_id=? AND friend_id=? AND status='confirmed')
   OR (user_id=? AND friend_id=? AND status='confirmed');
```

#Получить жанры фильма

```sql
SELECT g.*
FROM film_genres fg
JOIN genres g ON fg.genre_id = g.genre_id
WHERE fg.film_id = ?;
```

#Получить рейтинг MPA фильма

```sql
SELECT m.*
FROM mpa_ratings m
JOIN films f ON f.mpa_rating_id = m.mpa_rating_id
WHERE f.film_id = ?;
```

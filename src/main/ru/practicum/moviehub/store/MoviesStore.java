package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MoviesStore {
    private final LinkedHashMap<Integer, Movie> movies; // Коллекция фильмов

    public MoviesStore() {
        // Создаем пустую коллекцию фильмов
        this.movies = new LinkedHashMap<>();
    }

    // Добавляем фильм
    public int addMovie(Movie movie) {
        // Если это первый фильм - присваиваем id = 1
        if (movies.isEmpty()) {
            movies.put(1, movie);
            return 1;
        }
        // Иначе получаем последний добавленный id
        int id = movies.lastEntry().getKey() + 1;
        // Добавляем фильм со следующим по порядку id
        movies.put(id, movie);
        return id;
    }

    // Удаление фильма по его id
    public Movie delMovie(Integer id) {
        return movies.remove(id);
    }

    // Удалить все фильмы
    public void delMovies() {
        movies.clear();
    }

    // Получение фильма по его id
    public Movie getMovie(Integer id) {
        return movies.get(id);
    }

    // Получить все фильмы
    public LinkedHashMap<Integer, Movie> getMovies() {
        return movies;
    }

    // Получить все фильмы за определенный год
    public LinkedHashMap<Integer, Movie> getMoviesByYear(Integer year) {
        return movies.entrySet().stream()
                .filter(entry -> entry.getValue().getYear() == year)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}
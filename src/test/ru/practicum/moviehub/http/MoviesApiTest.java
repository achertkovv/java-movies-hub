package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {

    static MoviesServer server;
    static MoviesStore store;

    @BeforeAll
    static void beforeAll() throws IOException {
        store = new MoviesStore();
        server = new MoviesServer(store, 8080);
        server.start();
    }

    @BeforeEach
    void beforeEach() {
        store.addMovie(new Movie("Test1", 1980));
        store.addMovie(new Movie("Test2", 1990));
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        store.delMovies();
        // Создайте HTTP-клиент,
        // укажите таймаут соединения (connectTimeout), равный 2 секундам
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            // создайте объект GET-запроса на эндпоинт /movies
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://localhost:8080/movies"))
                    .build();

            // Обработчик тела запроса
            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            // Отправьте запрос
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            // Допишите проверку кода ответа
            assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

            // Допишите проверку заголовка Content-Type
            String contentTypeHeaderValue =
                    resp.headers().firstValue("Content-Type").orElse("");
            assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                    "Content-Type должен содержать формат данных и кодировку");

            // проверка, что был возвращён пустой массив
            String body = resp.body().trim();
            assertTrue(body.contains("[]"),
                    "Ожидается JSON-массив");
        }
    }

    @Test
    void getMovies_whenNotEmpty_returnsMovies() throws Exception {
        // Создайте HTTP-клиент,
        // укажите таймаут соединения (connectTimeout), равный 2 секундам
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            // создайте объект GET-запроса на эндпоинт /movies
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://localhost:8080/movies"))
                    .build();

            // Обработчик тела запроса
            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            // Отправьте запрос
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            // Допишите проверку кода ответа
            assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

            // Допишите проверку заголовка Content-Type
            String contentTypeHeaderValue =
                    resp.headers().firstValue("Content-Type").orElse("");
            assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                    "Content-Type должен содержать формат данных и кодировку");

            // проверка, что был возвращён массив
            String body = resp.body().trim();
            assertTrue(body.startsWith("[") && body.endsWith("]"),
                    "Ожидается JSON-массив");

            // проверка, что в возвращенных данных есть тестовые данные Test1 и Test2
            assertTrue(body.contains("Test1") && body.contains("Test2"),
                    "В массиве ожидаются тестовые данные");
        }
    }
}
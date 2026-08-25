package ru.practicum.moviehub.http;

import org.junit.jupiter.api.*;
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

    @AfterEach
    void afterEach() {
        store.delMovies();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        store.delMovies();
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://localhost:8080/movies"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

            String contentTypeHeaderValue =
                    resp.headers().firstValue("Content-Type").orElse("");
            assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                    "Content-Type должен содержать формат данных и кодировку");

            String body = resp.body().trim();
            assertTrue(body.contains("[]"),
                    "Ожидается JSON-массив");
        }
    }

    @Test
    void getMovie_whenIDEquals2_returnsMovie() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://localhost:8080/movies?id=2"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(200, resp.statusCode(), "GET /movies?id=2 должен вернуть 200");

            String contentTypeHeaderValue =
                    resp.headers().firstValue("Content-Type").orElse("");
            assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                    "Content-Type должен содержать формат данных и кодировку");

            String body = resp.body().trim();
            assertTrue(body.contains("Test2"),
                    "Ожидается Test2");
        }
    }

    @Test
    void getMovie_whenIDEqualsTwo_returnsError() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://localhost:8080/movies?id=two"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(400, resp.statusCode(), "GET /movies?id=two должен вернуть 400");

            String body = resp.body().trim();
            assertTrue(body.contains("Некорректный ID"),
                    "Ожидается: Некорректный ID");
        }
    }

    @Test
    void getMovie_whenIDEquals0_returnsError() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://localhost:8080/movies?id=0"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(404, resp.statusCode(), "GET /movies?id=0 должен вернуть 404");

            String body = resp.body().trim();
            assertTrue(body.contains("Фильм не найден"),
                    "Ожидается: Фильм не найден");
        }
    }

    @Test
    void getMovies_whenNotEmpty_returnsMovies() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://localhost:8080/movies"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

            String contentTypeHeaderValue =
                    resp.headers().firstValue("Content-Type").orElse("");
            assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                    "Content-Type должен содержать формат данных и кодировку");

            String body = resp.body().trim();
            assertTrue(body.startsWith("[") && body.endsWith("]"),
                    "Ожидается JSON-массив");

            assertTrue(body.contains("Test1") && body.contains("Test2"),
                    "В массиве ожидаются тестовые данные");
        }
    }

    @Test
    void postMovies_whenValidData_returnMovieObject() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/movies"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Test3\", \"year\":2000}"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");

            String contentTypeHeaderValue =
                    resp.headers().firstValue("Content-Type").orElse("");
            assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                    "Content-Type должен содержать формат данных и кодировку");

            String body = resp.body().trim();
            assertTrue(body.contains("Test3") && body.contains("2000") && body.contains("id"),
                    "В массиве ожидаются тестовые данные");
        }
    }

    @Test
    void postMovies_whenValidationEmptyTitle_returnErrorMessage() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/movies"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"\", \"year\":1911}"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

            String body = resp.body().trim();
            assertTrue(body.contains("название не должно быть пустым"),
                    "В массиве c ошибкой ожидается: название не должно быть пустым");
        }
    }

    @Test
    void postMovies_whenValidation100CharsTitle_returnErrorMessage() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/movies"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Testsdfsdfasdfasdfasdfasdfasdfasdf" +
                            "asdfawewqreqwerqwerqwerqwerfsdafasdfqwerqwerqwerqwerfsadfasferwqtethrfyjtru" +
                            "sjrgfhdsijfghdkjfhgdkjfhgkdjhfgkdjhfgkjdhfgkjdhfgkdjfhkjfghjdfghdf3\", \"year\":1911}"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

            String body = resp.body().trim();
            assertTrue(body.contains("название не должно быть длиннее 100 символов"),
                    "В массиве c ошибкой ожидается: название не должно быть длиннее 100 символов");
        }
    }

    @Test
    void postMovies_whenValidationBadYear_returnErrorMessage() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/movies"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Test3\", \"year\":1611}"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

            String body = resp.body().trim();
            assertTrue(body.contains("год должен быть между 1888 и"),
                    "В массиве c ошибкой ожидается: год должен быть между 1888 и");
        }
    }

    @Test
    void postMovies_whenValidationEmptyTitleAndBadYear_returnErrorMessages() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/movies"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"\", \"year\":2029}"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

            String body = resp.body().trim();
            assertTrue(body.contains("название не должно быть пустым") && body.contains("год должен быть между 1888 и"),
                    "В массиве c ошибкой ожидается: название не должно быть пустым && год должен быть между 1888 и");
        }
    }

    @Test
    void postMovies_whenContentTypeIsBad_returnErrorMessage() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/movies"))
                    .header("Content-Type", "application/xml")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Test4\", \"year\":2026}"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(415, resp.statusCode(), "POST /movies должен вернуть 415");
        }
    }

    @Test
    void deleteMovie_whenIDEquals1_returnsEmptyBody() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(URI.create("http://localhost:8080/movies?id=1"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(204, resp.statusCode(), "GET /movies?id=1 должен вернуть 204");
        }
    }

    @Test
    void deleteMovie_whenIDEquals11_returnsError() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(URI.create("http://localhost:8080/movies?id=11"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(404, resp.statusCode(), "GET /movies?id=11 должен вернуть 404");
        }
    }

    @Test
    void deleteMovie_whenIDEqualsTwo_returnsError() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(URI.create("http://localhost:8080/movies?id=two"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(400, resp.statusCode(), "GET /movies?id=two должен вернуть 400");
        }
    }

    @Test
    void getMovies_whenFilterByYear1990_returnsMoviesForThisYear() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // указываем максимальное время ожидания соединения с сервером
                .build()) {

            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://localhost:8080/movies?year=1990"))
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            HttpResponse<String> resp = client.send(req, responseBodyHandler);

            assertEquals(200, resp.statusCode(), "GET /movies?year=1990 должен вернуть 200");

            String body = resp.body().trim();
            assertTrue(body.contains("Test2") && !body.contains("Test1"),
                    "В массиве c ожидается: Test2 и не должно быть Test1");
        }
    }
}
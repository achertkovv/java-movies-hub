package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class MoviesHttpHandler extends BaseHttpHandler {
    private final MoviesStore moviesStore;

    public MoviesHttpHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        // Устанавливаем заголовки ответа
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, 0);

        JsonArray jsonArray = new JsonArray();
        LinkedHashMap<Integer, Movie> movies = moviesStore.getMovies();
        if (movies != null && !movies.isEmpty()) {
            for (Map.Entry<Integer, Movie> entry : movies.entrySet()) {
                JsonObject movieObject = getJsonObjectFromEntry(entry);
                jsonArray.add(movieObject);
            }
        }
        String jsonString = jsonArray.toString(); // Получаем [] для пустого массива
        // Отправляем ответ
        sendResponse(exchange, jsonString);
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        // получаем входящий поток байтов
        InputStream inputStream = exchange.getRequestBody();
        // дожидаемся получения всех данных в виде массива байтов и конвертируем их в строку
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        // создайте экземпляр Gson
        Gson gson = new Gson();
        // Получим Movie из Json строки
        Movie movieDeserialized = gson.fromJson(body, Movie.class);

        if (movieDeserialized.checkTitle().isBlank() &&
                movieDeserialized.checkYear().isBlank()) {
            // Если фильм успешно добавлен:
            // Устанавливаем заголовки ответа
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            // Код статуса — 201 Created.
            exchange.sendResponseHeaders(201, 0);
            // Тело — JSON созданного фильма с присвоенным ID.
            int id = moviesStore.addMovie(movieDeserialized);
            Movie movie = moviesStore.getMovie(id);
            JsonObject movieObject = getJsonObjectFromEntry(Map.entry(id, movie));
            // Отправляем ответ
            sendResponse(exchange, gson.toJson(movieObject));
        } else {
            // Если произошла ошибка валидации:
            // Устанавливаем заголовки ответа
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            // Код статуса — 422 Unprocessable Entity.
            exchange.sendResponseHeaders(422, 0);

            // Тело — объект с информацией об ошибке, содержит следующие поля:
            // error — короткое описание ошибки, например, Ошибка валидации.
            // details — массив строк с деталями проблемы
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("error", "ошибка валидации");
            JsonArray jsonArray = new JsonArray();
            // Заполняем массив данными...
            if (!movieDeserialized.checkTitle().isBlank())
                jsonArray.add(movieDeserialized.checkTitle());
            if (!movieDeserialized.checkYear().isBlank())
                jsonArray.add(movieDeserialized.checkYear());
            jsonObject.add("details", jsonArray);
            // Отправляем ответ
            sendResponse(exchange, gson.toJson(jsonObject));
        }
    }

    @Override
    protected void handleDelete(HttpExchange exchange) throws IOException {

    }

    private JsonObject getJsonObjectFromEntry(Map.Entry<Integer, Movie> entry) {
        JsonObject movieObject = new JsonObject();
        movieObject.addProperty("id", entry.getKey());
        movieObject.addProperty("title", entry.getValue().getTitle());
        movieObject.addProperty("year", entry.getValue().getYear());
        return movieObject;
    }

    private void sendResponse(HttpExchange exchange, String jsonString) throws IOException {
        byte[] responseBytes = jsonString.getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
            os.flush();
        }
    }
}

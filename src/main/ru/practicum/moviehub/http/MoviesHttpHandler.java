package ru.practicum.moviehub.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MoviesHttpHandler extends BaseHttpHandler {
    private final MoviesStore moviesStore;

    public MoviesHttpHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        // Устранение замечаний: "По спецификации доступ к одному фильму идет по пути:
        // GET /movies/{id} и DELETE /movies/{id}."
        String path = exchange.getRequestURI().getPath();
        String[] pathSplit = path.split("/");
        String value = pathSplit[pathSplit.length - 1];
        if (value != null && !value.isEmpty() && !value.equals("movies")) {
            // Получаем id из пути
            int id;
            try {
                id = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // Код статуса — 400 Bad Request.
                sendError(exchange, 400, "Некорректный ID = " + value);
                return;
            }
            // Получаем фильм по его id
            Movie movie = moviesStore.getMovie(id);
            if (movie == null) {
                // Код статуса — 404 Not Found.
                sendError(exchange, 404, "Фильм не найден");
                return;
            }
            // Отправляем ответ 200 OK
            sendResponse(exchange, 200, movie);
        } else if (query != null && !query.isEmpty()) {
            value = query.substring(query.indexOf('=') + 1);
            if (query.contains("year")) {
                // Получаем year из строки запроса
                int year;
                try {
                    year = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    // Код статуса — 400 Bad Request.
                    sendError(exchange, 400, "Некорректный параметр запроса 'year' — " + value);
                    return;
                }
                // Получаем фильмы отсортированные по year
                LinkedHashMap<Integer, Movie> movies = moviesStore.getMoviesByYear(year);
                // Отправляем ответ 200 OK
                sendResponse(exchange, 200, movies);
            } else {
                sendError(exchange, 400, "Некорректный запрос: " + query);
            }
        } else {
            JsonArray jsonArray = new JsonArray();
            LinkedHashMap<Integer, Movie> movies = moviesStore.getMovies();
            if (movies != null && !movies.isEmpty()) {
                for (Map.Entry<Integer, Movie> entry : movies.entrySet()) {
                    JsonObject movieObject = getJsonObjectFromEntry(entry);
                    jsonArray.add(movieObject);
                }
            }
            // Отправляем ответ 200 OK
            sendResponse(exchange, 200, jsonArray);
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        // Восстанавливаем класс из JSON
        Movie movieDeserialized = parseRequestBody(exchange);

        List<String> contentTypeList = exchange.getRequestHeaders().get("Content-Type");
        // Устранение замечаний: проверка Content-Type. contentTypeList.contains(application/json) сравнивает
        // элементы списка целиком, поэтому корректный заголовок application/json; charset=UTF-8 не пройдет проверку
        // и клиент получит 415 на валидный запрос. Тест этого не ловит, так как отправляет заголовок без charset.
        // Сравнивайте через startsWith по первому значению заголовка
        if (contentTypeList == null || !contentTypeList.getFirst().startsWith("application/json")) {
            // Если был получен запрос с неправильным значением заголовка Content-Type:
            sendError(exchange, 415, "неправильное значение заголовка Content-Type");
            // Устранение замечаний: "После отправки 415 нет return, выполнение идет дальше: вы пытаетесь
            // провалидировать фильм и отправить второй ответ в уже завершенный обмен. Это либо исключение,
            // либо порча ответа клиенту.
            return;
        }

        if (movieDeserialized.checkTitle().isBlank() &&
                movieDeserialized.checkYear().isBlank()) {
            // Если фильм успешно добавлен:
            // Тело — JSON созданного фильма с присвоенным ID.
            int id = moviesStore.addMovie(movieDeserialized);
            Movie movie = moviesStore.getMovie(id);
            JsonObject movieObject = getJsonObjectFromEntry(Map.entry(id, movie));
            // Отправляем ответ 201 Created
            sendResponse(exchange, 201, movieObject);
        } else {
            // Если произошла ошибка валидации:
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
            // Отправляем ответ 422 Unprocessable Entity
            sendResponse(exchange, 422, jsonObject);
        }
    }

    @Override
    protected void handleDelete(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && !query.isEmpty()) {
            String value = query.substring(query.indexOf('=') + 1);
            if (query.contains("id")) {
                // Получаем id из строки запроса
                int id;
                try {
                    id = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    // Код статуса — 400 Bad Request.
                    sendError(exchange, 400, "Некорректный ID = " + value);
                    return;
                }
                // Удаляем фильм по его id
                Movie movie = moviesStore.delMovie(id);
                if (movie == null) {
                    // Код статуса — 404 Not Found.
                    sendError(exchange, 404, "Фильм не найден");
                    return;
                }
                // Отправляем ответ 204 No Content.
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(204, -1);
            } else {
                sendError(exchange, 400, "Некорректный запрос: " + query);
            }
        }
    }

    private JsonObject getJsonObjectFromEntry(Map.Entry<Integer, Movie> entry) {
        JsonObject movieObject = new JsonObject();
        movieObject.addProperty("id", entry.getKey());
        movieObject.addProperty("title", entry.getValue().getTitle());
        movieObject.addProperty("year", entry.getValue().getYear());
        return movieObject;
    }
}

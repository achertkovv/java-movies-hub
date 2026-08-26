package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected final Gson gson;
    protected final ErrorResponse errorResponse;

    public BaseHttpHandler() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        this.errorResponse = new ErrorResponse();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // Получаем метод запроса
            String method = exchange.getRequestMethod().toUpperCase();

            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    // Метод не поддерживается
                    errorResponse.sendMethodNotAllowed(exchange, method);
            }
        } catch (Exception e) {
            errorResponse.sendInternalServerError(exchange, e);
        }
    }

    protected abstract void handleGet(HttpExchange exchange) throws IOException;

    protected abstract void handlePost(HttpExchange exchange) throws IOException;

    protected abstract void handleDelete(HttpExchange exchange) throws IOException;

    protected void sendResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String jsonResponse = gson.toJson(response);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        // Устанавливаем заголовки ответа
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        errorResponse.sendError(exchange, statusCode, message, null);
    }

    protected Movie parseRequestBody(HttpExchange exchange) throws IOException {
        // получаем входящий поток байтов
        InputStream inputStream = exchange.getRequestBody();
        // дожидаемся получения всех данных в виде массива байтов и конвертируем их в строку
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        // Получим Movie из Json строки
        return gson.fromJson(body, Movie.class);
    }
}
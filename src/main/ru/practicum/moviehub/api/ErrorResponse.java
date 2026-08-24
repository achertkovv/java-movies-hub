package ru.practicum.moviehub.api;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class ErrorResponse {
    public static void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
        exchange.sendResponseHeaders(code, body.length());
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        }
    }
}
package ru.practicum.moviehub.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorResponse {

    private final Gson gson;

    public ErrorResponse() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public void sendMethodNotAllowed(HttpExchange exchange, String method) throws IOException {
        String message = String.format("Method %s is not allowed for this endpoint", method);
        sendError(exchange, 405, message, method);
    }

    public void sendInternalServerError(HttpExchange exchange, Exception e) throws IOException {
        String message = "Internal server error: " + e.getMessage();
        sendError(exchange, 500, message, null);
    }

    public void sendError(HttpExchange exchange, int statusCode, String message, String additionalInfo) throws IOException {
        ErrorDetails errorDetails = new ErrorDetails(
                statusCode,
                message,
                additionalInfo,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        String jsonResponse = gson.toJson(errorDetails);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    // Внутренний класс для структуры ошибки
    private static class ErrorDetails {
        private final int status;
        private final String error;
        private final String message;
        private final String additionalInfo;
        private final String timestamp;

        public ErrorDetails(int status, String error, String additionalInfo, String timestamp) {
            this.status = status;
            this.error = getErrorName(status);
            this.message = error;
            this.additionalInfo = additionalInfo;
            this.timestamp = timestamp;
        }

        private String getErrorName(int status) {
            return switch (status) {
                case 400 -> "Bad Request";
                case 404 -> "Not Found";
                case 405 -> "Method Not Allowed";
                case 415 -> "Unsupported Media Type";
                case 422 -> "Unprocessable Entity";
                case 500 -> "Internal Server Error";
                default -> "Error";
            };
        }
    }
}
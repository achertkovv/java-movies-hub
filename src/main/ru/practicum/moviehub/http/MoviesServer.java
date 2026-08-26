package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private final HttpServer httpServer;

    public MoviesServer(MoviesStore moviesStore, int port) throws IOException {
        // создали веб-сервер и связываем сервер с портом: передаём настройки сокета и бэклога
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        // связываем конкретный путь и его обработчик
        httpServer.createContext("/movies", new MoviesHttpHandler(moviesStore));
    }

    public void start() {
        httpServer.start(); // запускаем сервер
    }

    public void stop() {
        httpServer.stop(1); // останавливаем сервер
    }
}
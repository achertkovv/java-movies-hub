package ru.practicum.moviehub.api;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PrintWriter {
    private static final String logFileName = "errors.log";

    // Устранение замечаний: перехватывает вообще все, включая IOException от самой отправки ответа, и нигде
    // не логирует причину: диагностировать сбой по такому коду невозможно.
    public void write(Exception ex) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFileName, UTF_8, true))) {
            bw.write(LocalDateTime.now() + ": " + ex.getMessage());
            bw.newLine();
            for (StackTraceElement element : ex.getStackTrace()) {
                bw.write(element.toString());
                bw.write("\n");
            }
        }
    }
}

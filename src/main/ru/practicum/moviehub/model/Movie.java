package ru.practicum.moviehub.model;

import java.time.LocalDate;

public class Movie {
    private final String title; // Название фильма
    private final int year; // Год выхода фильма

    public Movie(String title, int year) {
        this.title = title;
        this.year = year;
    }

    public String checkTitle() {
        // возвращает ошибку при пустом title;
        if (title == null || title.isEmpty())
            return "название не должно быть пустым";
        else if (title.length() > 100) // возвращает ошибку при слишком длинном title (> 100 символов);
            return "название не должно быть длиннее 100 символов";
        else return "";
    }

    public String checkYear() {
        // возвращает ошибку при неверном year (меньше 1888 или больше текущего года + 1);
        if (year < 1888 || year > LocalDate.now().plusYears(1).getYear())
            return "год должен быть между 1888 и " +
                    LocalDate.now().plusYears(1).getYear();
        else return "";
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }
}
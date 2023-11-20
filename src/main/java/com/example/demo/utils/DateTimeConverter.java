package com.example.demo.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeConverter {
   private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm");

    public static LocalTime parseStringTime(String time) {

        try {
            return LocalTime.parse(time, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

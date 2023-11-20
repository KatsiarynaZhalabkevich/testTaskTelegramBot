package com.example.demo.utils;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class DateTimeConverter {

    public static LocalTime parseStringTime(String time) {

        try {
            return LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}

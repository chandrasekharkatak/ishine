package com.apmosys.employeeportal.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateConversionUtil {

    // ===================== LocalDate Formatters =====================

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ===================== String ↔ LocalDate =====================

    public static LocalDate stringToLocalDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateStr, formatter);
    }

    public static LocalDate stringToLocalDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, ISO_FORMATTER);
    }

    public static String localDateToString(LocalDate localDate, String pattern) {
        if (localDate == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDate.format(formatter);
    }

    public static String localDateToString(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.format(ISO_FORMATTER);
    }

    // ===================== LocalDate ↔ java.util.Date =====================

    public static LocalDate utilDateToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        Instant instant = date.toInstant();
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date localDateToUtilDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    // ===================== String ↔ java.util.Date =====================

    public static Date stringToUtilDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected pattern: " + pattern);
        }
    }

    public static String utilDateToString(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    // ===================== Wrapper Methods =====================

    public static Date stringToUtilDateViaLocalDate(String dateStr, String pattern) {
        LocalDate ld = stringToLocalDate(dateStr, pattern);
        return localDateToUtilDate(ld);
    }

    public static String utilDateToStringViaLocalDate(Date date, String pattern) {
        LocalDate ld = utilDateToLocalDate(date);
        return localDateToString(ld, pattern);
    }

    // ===================== SQL Date Handling =====================

    public static java.sql.Date localDateToSqlDate(LocalDate localDate) {
        return localDate == null ? null : java.sql.Date.valueOf(localDate);
    }

    public static LocalDate sqlDateToLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }

    // ===================== Custom Legacy Date Parsing =====================

    public static Date stringToUtilDate(String dateStr) {
        return stringToUtilDate(dateStr, "yyyy-MM-dd");
    }

    public static String utilDateToString(Date date) {
        return utilDateToString(date, "yyyy-MM-dd");
    }
    
    
    
    public static LocalTime stringToLocalTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }

        if (timeStr.trim().length() == 5) { // HH:mm
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }
    
    
    public static String localTimeToString(LocalTime localTime, String pattern) {
        if (localTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localTime.format(formatter);
    }

    
    public static Date combineToUtilDate(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            return null;
        }

        LocalDateTime ldt = LocalDateTime.of(date, time);

        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }


}

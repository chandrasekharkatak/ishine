package com.apmosys.employeeportal.utility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimesheetFormatUtil {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");
    
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");

    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMAT);
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(TIME_FORMAT);
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
    	return dateTime == null ? null : dateTime.format(DATE_TIME_FORMAT);
    }

    public static String formatMinutes(Number minutes) {
        if (minutes == null) return "00:00";
        System.out.println(minutes);
        int totalMinutes = minutes.intValue(); // works for Short, Integer, Long
        int hrs = totalMinutes / 60;
        int mins = totalMinutes % 60;
        System.out.println(hrs + ":" +mins);
        return String.format("%02d:%02d", hrs, mins);
    }
    
}

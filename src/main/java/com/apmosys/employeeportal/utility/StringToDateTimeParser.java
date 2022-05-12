package com.apmosys.employeeportal.utility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

/*By suraj
 * This is custom class to parse string into date, dateTime and to get current dateTime.
 */

@Component
public class StringToDateTimeParser {
	
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	
	public LocalDate getDate(String date)
	{
		return LocalDate.parse(date, dateFormatter);
	}
	
	public LocalDateTime getCurrentDateTime()
	{
		String dateTimeString = LocalDateTime.now().format(dateTimeFormatter);
		return LocalDateTime.parse(dateTimeString, dateTimeFormatter);
	}

}

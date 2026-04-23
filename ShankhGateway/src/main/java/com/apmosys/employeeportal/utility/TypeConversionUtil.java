package com.apmosys.employeeportal.utility;

import java.util.List;
import java.util.stream.Collectors;

public final class TypeConversionUtil {

	private TypeConversionUtil() {
		// prevent instantiation
	}

	public static List<Long> convertToLongList(List<Integer> inputList) {
		return inputList == null
				? null
				: inputList.stream()
						.map(Integer::longValue)
						.collect(Collectors.toList());
	}

	public static Long safeParseLong(Object obj) {
		try {
			if (obj == null)
				return null;
			String str = obj.toString().trim();
			return str.isEmpty() ? null : Long.parseLong(str);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Integer safeParseInt(Object obj) {
		try {
			if (obj == null)
				return null;
			String str = obj.toString().trim();
			return str.isEmpty() ? null : Integer.parseInt(str);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Float safeParseFloat(Object obj) {
		try {
			if (obj == null)
				return null;
			String str = obj.toString().trim();
			return str.isEmpty() ? null : Float.parseFloat(str);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Double safeParseDouble(Object obj) {
		try {
			if (obj == null)
				return null;
			String str = obj.toString().trim();
			return str.isEmpty() ? null : Double.parseDouble(str);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getSafeString(Object obj) {
		return obj == null ? null : obj.toString().trim();
	}

	public static String extractCustomMessage(String fullMessage) {
		if (fullMessage == null)
			return null;

		int colonIndex = fullMessage.indexOf(':');
		int startSearch = colonIndex >= 0 ? colonIndex + 1 : 0;

		int firstQuote = fullMessage.indexOf('"', startSearch);
		int lastQuote = fullMessage.lastIndexOf('"');

		if (firstQuote < 0 || lastQuote <= firstQuote) {
			return fullMessage;
		}

		return fullMessage.substring(firstQuote + 1, lastQuote);
	}

}
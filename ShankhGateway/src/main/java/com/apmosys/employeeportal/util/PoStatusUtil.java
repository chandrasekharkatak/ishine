package com.apmosys.employeeportal.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Date-based PO status for emails (Active / Future / Expired; Unknown when dates are missing).
 */
public final class PoStatusUtil {

	private PoStatusUtil() {
	}

	/**
	 * Inclusive window on {@code startDate} and {@code endDate}. If either boundary is null, returns
	 * {@code Unknown}. If {@code currentDate} is null, {@link LocalDate#now()} is used.
	 */
	public static String getPoStatus(LocalDate currentDate, LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null) {
			return "Unknown";
		}
		LocalDate cur = currentDate != null ? currentDate : LocalDate.now();
		if ((cur.isEqual(startDate) || cur.isAfter(startDate))
				&& (cur.isEqual(endDate) || cur.isBefore(endDate))) {
			return "Active";
		}
		if (cur.isBefore(startDate)) {
			return "Future";
		}
		return "Expired";
	}

	public static LocalDate toLocalDate(LocalDateTime ldt) {
		return ldt == null ? null : ldt.toLocalDate();
	}
}

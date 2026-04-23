package com.apmosys.employeeportal.dto;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One row of the native query used for resources linked to PO numbers (po_no).
 * Field names and {@link JsonProperty} values match the previous {@code Map} JSON shape.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResourceByPoRowDto {

	private Long empId;

	/** Serialized as {@code employementId} for backward compatibility with the legacy API. */
	@JsonProperty("employementId")
	private Long employmentId;

	private String empName;
	private String department;
	private String role;
	private String teamName;
	private String projectManagerName;
	private String projectName;

	/** PO number ({@code po_no}) from {@code project_po_details}. */
	private String poName;

	/**
	 * Maps a native-query {@code Object[]} row (9 columns) to this DTO, or {@code null} if the row is unusable.
	 */
	public static ProjectResourceByPoRowDto fromNativeQueryRow(Object[] row) {
		if (row == null || row.length < 9) {
			return null;
		}
		return new ProjectResourceByPoRowDto(toLong(row[0]), toLong(row[1]), toString(row[2]), toString(row[3]),
				toString(row[4]), toString(row[5]), toString(row[6]), toString(row[7]), toString(row[8]));
	}

	private static Long toLong(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Long) {
			return (Long) o;
		}
		if (o instanceof Integer) {
			return ((Integer) o).longValue();
		}
		if (o instanceof BigInteger) {
			return ((BigInteger) o).longValue();
		}
		if (o instanceof Number) {
			return ((Number) o).longValue();
		}
		try {
			return Long.parseLong(o.toString().trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static String toString(Object o) {
		return o == null ? null : o.toString();
	}
}

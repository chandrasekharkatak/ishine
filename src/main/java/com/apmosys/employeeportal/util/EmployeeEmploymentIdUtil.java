package com.apmosys.employeeportal.util;

/**
 * Central helper for employment ID prefix categories (A-, CS-, AP-, APCS-).
 * Used for duplicate validation routing and display formatting.
 */
public final class EmployeeEmploymentIdUtil {

	public static final String PREFIX_REGULAR = "A-";
	public static final String PREFIX_CONSULTANT = "CS-";
	public static final String PREFIX_APMOSYS_PRODUCT = "AP-";
	public static final String PREFIX_APMOSYS_PRODUCT_CONSULTANT = "APCS-";

	public static final String EMPLOYEE_TYPE_REGULAR = "Regular";
	public static final String EMPLOYEE_TYPE_CONSULTANT = "Consultant";
	public static final String EMPLOYEE_TYPE_APMOSYS_PRODUCT = "Apmosys Product";
	public static final String EMPLOYEE_TYPE_APMOSYS_PRODUCT_CONSULTANT = "Apmosys Product Consultant";
	public static final String EMPLOYEE_TYPE_APPRENTICE = "Apprentice";

	private EmployeeEmploymentIdUtil() {
	}

	public static boolean isTrue(String value) {
		return value != null && "true".equalsIgnoreCase(value.trim());
	}

	public static boolean isApmosysProductConsultant(String isConsultant, String isApmosysProduct) {
		return isTrue(isConsultant) && isTrue(isApmosysProduct);
	}

	public static boolean isConsultantOnly(String isConsultant, String isApmosysProduct) {
		return isTrue(isConsultant) && !isTrue(isApmosysProduct);
	}

	public static boolean isApmosysProductOnly(String isConsultant, String isApmosysProduct) {
		return isTrue(isApmosysProduct) && !isTrue(isConsultant);
	}

	public static String resolvePrefix(String isConsultant, String isApmosysProduct) {
		if (isApmosysProductConsultant(isConsultant, isApmosysProduct)) {
			return PREFIX_APMOSYS_PRODUCT_CONSULTANT;
		}
		if (isTrue(isApmosysProduct)) {
			return PREFIX_APMOSYS_PRODUCT;
		}
		if (isTrue(isConsultant)) {
			return PREFIX_CONSULTANT;
		}
		return PREFIX_REGULAR;
	}

	public static String formatEmploymentId(Long numericId, String isConsultant, String isApmosysProduct) {
		if (numericId == null) {
			return null;
		}
		return resolvePrefix(isConsultant, isApmosysProduct) + numericId;
	}

	public static String resolveEmployeeType(String isConsultant, String isApmosysProduct, String isApprenticeship) {
		if (isApmosysProductConsultant(isConsultant, isApmosysProduct)) {
			return EMPLOYEE_TYPE_APMOSYS_PRODUCT_CONSULTANT;
		}
		if (isTrue(isConsultant)) {
			return EMPLOYEE_TYPE_CONSULTANT;
		}
		if (isTrue(isApprenticeship)) {
			return EMPLOYEE_TYPE_APPRENTICE;
		}
		if (isTrue(isApmosysProduct)) {
			return EMPLOYEE_TYPE_APMOSYS_PRODUCT;
		}
		return EMPLOYEE_TYPE_REGULAR;
	}

	public static String resolveEmployeeTypeFromDto(String employeeType, String isConsultant, String isApmosysProduct,
			String isApprenticeship) {
		if (employeeType != null && !employeeType.isBlank()) {
			if (EMPLOYEE_TYPE_APMOSYS_PRODUCT_CONSULTANT.equalsIgnoreCase(employeeType)) {
				return EMPLOYEE_TYPE_APMOSYS_PRODUCT_CONSULTANT;
			}
			if (isApmosysProductConsultant(isConsultant, isApmosysProduct)) {
				return EMPLOYEE_TYPE_APMOSYS_PRODUCT_CONSULTANT;
			}
			return employeeType;
		}
		return resolveEmployeeType(isConsultant, isApmosysProduct, isApprenticeship);
	}

	/**
	 * Native SQL: formatted employment id for alias {@code e}, column {@code employeement_id}.
	 */
	public static String sqlCaseFormattedEmploymentId(String tableAlias, String idColumn) {
		return "CASE "
				+ "WHEN " + tableAlias + ".is_apmosys_product = 'true' AND " + tableAlias
				+ ".is_consultant = 'true' THEN CONCAT('APCS-', " + tableAlias + "." + idColumn + ") "
				+ "WHEN " + tableAlias + ".is_consultant = 'true' THEN CONCAT('CS-', " + tableAlias + "." + idColumn
				+ ") "
				+ "WHEN " + tableAlias + ".is_apmosys_product = 'true' THEN CONCAT('AP-', " + tableAlias + "."
				+ idColumn + ") "
				+ "ELSE CONCAT('A-', " + tableAlias + "." + idColumn + ") "
				+ "END";
	}

	/** Native SQL with ap2l.ai email treated as AP display (after APCS/CS flag checks). */
	public static String sqlCaseFormattedEmploymentIdWithAp2lEmail(String tableAlias, String idColumn) {
		return "CASE "
				+ "WHEN " + tableAlias + ".is_apmosys_product = 'true' AND " + tableAlias
				+ ".is_consultant = 'true' THEN CONCAT('APCS-', " + tableAlias + "." + idColumn + ") "
				+ "WHEN " + tableAlias + ".is_consultant = 'true' THEN CONCAT('CS-', " + tableAlias + "." + idColumn
				+ ") "
				+ "WHEN " + tableAlias + ".is_apmosys_product = 'true' OR " + tableAlias
				+ ".email LIKE '%ap2l.ai%' THEN CONCAT('AP-', " + tableAlias + "." + idColumn + ") "
				+ "ELSE CONCAT('A-', " + tableAlias + "." + idColumn + ") "
				+ "END";
	}

	/** JPQL/HQL: formatted employment id for entity alias. */
	public static String jpqlCaseFormattedEmploymentId(String entityAlias) {
		return "CASE "
				+ "WHEN " + entityAlias + ".isConsultant = 'true' AND " + entityAlias
				+ ".isApmosysProduct = 'true' THEN CONCAT('APCS-', " + entityAlias + ".employeementId) "
				+ "WHEN " + entityAlias + ".isConsultant = 'true' THEN CONCAT('CS-', " + entityAlias
				+ ".employeementId) "
				+ "WHEN " + entityAlias + ".isApmosysProduct = 'true' THEN CONCAT('AP-', " + entityAlias
				+ ".employeementId) "
				+ "ELSE CONCAT('A-', " + entityAlias + ".employeementId) "
				+ "END";
	}

	public static String formatEmploymentId(String numericId, String isConsultant, String isApmosysProduct) {
		if (numericId == null || numericId.isBlank()) {
			return null;
		}
		try {
			return formatEmploymentId(Long.valueOf(numericId.trim()), isConsultant, isApmosysProduct);
		} catch (NumberFormatException ex) {
			return resolvePrefix(isConsultant, isApmosysProduct) + numericId;
		}
	}

	public static final String APCS_EMAIL_DOMAIN_REQUIRED_MESSAGE =
			"ApMoSys Product Consultant employees must use @ap2l.ai email domain.";

	public static boolean requiresAp2lEmailDomain(String isConsultant, String isApmosysProduct) {
		return isApmosysProductConsultant(isConsultant, isApmosysProduct);
	}

	public static boolean hasAp2lEmailDomain(String email) {
		if (email == null || email.isBlank()) {
			return false;
		}
		return email.trim().toLowerCase().endsWith("@ap2l.ai");
	}

}
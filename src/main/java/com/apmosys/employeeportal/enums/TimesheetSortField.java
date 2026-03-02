package com.apmosys.employeeportal.enums;

import java.util.Arrays;

/**
 * Enum mapping API sort field names to database column expressions.
 * This provides a single source of truth for sorting configuration,
 * making it easy to add, modify, or remove sort options.
 */
public enum TimesheetSortField {

    EMPLOYEE_NAME("employeeName", "e.name"),
    EMPLOYMENT_ID("employeement_Id", "e.employeement_Id"),
    DAY_TYPE("dayType", "dtmn.day_Type"),
    DATE("date", "date"),
    WORK_CHECK_IN("workCheckIn", "work_in_time"),
    WORK_CHECK_OUT("workCheckOut", "work_out_time"),
    LOCATION_COUNT("locationCount", "COUNT(DISTINCT etlm.location_mapping_id)"),
    PROJECT_COUNT("projectCount", "COUNT(DISTINCT ptsn.id.projectId)"),
    PROJECT_NAME("projectName", "p.projectName"),
    CLIENT_NAME("clientName", "c.clientName"),
    TEAM_NAME("teamName", "t.teamName"),
    APPLIED_BY("appliedBy", "ab.name"),
    APPLIED_ON("appliedOn", "etn.createdOn"),
    CREATED_ON("createdOn", "bt.created_on");

    private final String apiField;
    private final String dbColumn;

    TimesheetSortField(String apiField, String dbColumn) {
        this.apiField = apiField;
        this.dbColumn = dbColumn;
    }

    public String getApiField() {
        return apiField;
    }

    public String getDbColumn() {
        return dbColumn;
    }

    /**
     * Converts an API field name to its corresponding database column expression.
     * Returns the default sort column (CREATED_ON) if no match is found.
     *
     * @param apiField the field name from the API request
     * @return the database column expression for sorting
     */
    public static String toDbColumn(String apiField) {
        if (apiField == null || apiField.isBlank()) {
            return CREATED_ON.dbColumn;
        }
        
        return Arrays.stream(values())
                .filter(field -> field.apiField.equals(apiField))
                .map(field -> field.dbColumn)
                .findFirst()
                .orElse(CREATED_ON.dbColumn);
    }

    /**
     * Checks if the given API field is a valid sort field.
     *
     * @param apiField the field name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSortField(String apiField) {
        if (apiField == null || apiField.isBlank()) {
            return true; // null/blank uses default
        }
        return Arrays.stream(values())
                .anyMatch(field -> field.apiField.equals(apiField));
    }
}

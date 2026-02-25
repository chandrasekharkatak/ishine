package com.apmosys.employeeportal.enums;
import java.util.Arrays;
public enum DayTypeCode {

    WORKING("Working", true),
    HALF_DAY_WORKING("Half-day Working", true),
    HOLIDAY("Holiday", false),
    WEEK_OFF("Week Off", false),
    NON_WORKING("Non-Working", true),
    LEAVE("Leave", false),
    CLIENT_HOLIDAY("Client Holiday", false),
    APMOSYS_HOLIDAY("ApMoSys Holiday", false),
    COMP_OFF("Comp Off", false);

    private final String dbValue;
    private final boolean workingDay;

    DayTypeCode(String dbValue, boolean workingDay) {
        this.dbValue = dbValue;
        this.workingDay = workingDay;
    }

    public boolean isWorkingDay() {
        return workingDay;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static DayTypeCode fromDbValue(String dbValue) {
        return Arrays.stream(values())
                .filter(v -> v.dbValue.equalsIgnoreCase(dbValue))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Unsupported dayType in DB: " + dbValue
                        ));
    }
    
    
    
}

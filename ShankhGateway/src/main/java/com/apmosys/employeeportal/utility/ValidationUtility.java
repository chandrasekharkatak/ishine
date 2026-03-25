package com.apmosys.employeeportal.utility;

import java.util.List;

public class ValidationUtility {

    public static boolean isStringNotNullOrEmpty(String string) {
        return string != null && !string.trim().isEmpty();
    }

    public static boolean isListNotNullOrEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }
}

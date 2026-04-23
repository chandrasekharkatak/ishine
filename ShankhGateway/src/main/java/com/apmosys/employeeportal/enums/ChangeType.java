package com.apmosys.employeeportal.enums;

public enum ChangeType {
	ROLE_REMOVED("Role removed from Shankh"),
    COUNT_DECREASED("Role count decreased from Shankh"),
    COUNT_INCREASED("Role count increased from Shankh"),
    NEW_ROLE_ADDED("New role added to Shankh");

    private final String description;

    ChangeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

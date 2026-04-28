package com.apmosys.employeeportal.service.leave;

import com.apmosys.employeeportal.model.DayTypeMasterNew;

/**
 * Bundled output of apply-leave pre-persist validation (orchestrated by {@link ApplyLeaveValidator}).
 */
public final class ApplyLeaveValidationResult {

    private final ParsedLeaveDates dates;
    private final LeaveMapAndType leaveMapAndType;
    private final DayTypeMasterNew leaveDayType;
    private final String logPayload;

    public ApplyLeaveValidationResult(
            ParsedLeaveDates dates,
            LeaveMapAndType leaveMapAndType,
            DayTypeMasterNew leaveDayType,
            String logPayload) {
        this.dates = dates;
        this.leaveMapAndType = leaveMapAndType;
        this.leaveDayType = leaveDayType;
        this.logPayload = logPayload;
    }

    public ParsedLeaveDates dates() {
        return dates;
    }

    public LeaveMapAndType leaveMapAndType() {
        return leaveMapAndType;
    }

    public DayTypeMasterNew leaveDayType() {
        return leaveDayType;
    }

    public String logPayload() {
        return logPayload;
    }
}

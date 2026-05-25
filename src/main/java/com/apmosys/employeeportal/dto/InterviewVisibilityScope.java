package com.apmosys.employeeportal.dto;

import java.util.Collections;
import java.util.List;

/**
 * Resolved row-level visibility for interview list APIs.
 */
public class InterviewVisibilityScope {

    private final boolean viewAll;
    private final List<Long> visibleEmployeeIds;
    private final List<Long> visibleDepartmentIds;

    private InterviewVisibilityScope(boolean viewAll, List<Long> visibleEmployeeIds, List<Long> visibleDepartmentIds) {
        this.viewAll = viewAll;
        this.visibleEmployeeIds = visibleEmployeeIds != null ? visibleEmployeeIds : Collections.emptyList();
        this.visibleDepartmentIds = visibleDepartmentIds != null ? visibleDepartmentIds : Collections.emptyList();
    }

    public static InterviewVisibilityScope all() {
        return new InterviewVisibilityScope(true, null, null);
    }

    public static InterviewVisibilityScope restricted(List<Long> employeeIds, List<Long> departmentIds) {
        return new InterviewVisibilityScope(false, employeeIds, departmentIds);
    }

    public static InterviewVisibilityScope none() {
        return new InterviewVisibilityScope(false, Collections.emptyList(), Collections.emptyList());
    }

    public boolean isViewAll() {
        return viewAll;
    }

    public List<Long> getVisibleEmployeeIds() {
        return visibleEmployeeIds;
    }

    public List<Long> getVisibleDepartmentIds() {
        return visibleDepartmentIds;
    }

    public boolean hasVisibility() {
        return viewAll || !visibleEmployeeIds.isEmpty() || !visibleDepartmentIds.isEmpty();
    }
}

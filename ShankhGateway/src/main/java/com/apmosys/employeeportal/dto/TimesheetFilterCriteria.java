package com.apmosys.employeeportal.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Encapsulates all filter criteria for fetching reportee timesheet requests.
 * This object groups related filter parameters together, making the code
 * more readable and reducing method parameter counts.
 */
@Getter
@Builder
public class TimesheetFilterCriteria {

    private final Long empId;
    private final Boolean clientFilter;
    
    // Employee filters
    private final String employmentId;
    private final String employeeName;
    
    // Time filters
    private final String dayType;
    private final String date;
    /** Optional date range: timesheet date >= startDate when non-null (YYYY-MM-DD). */
    private final String startDate;
    /** Optional date range: timesheet date <= endDate when non-null (YYYY-MM-DD). */
    private final String endDate;
    private final String workCheckIn;
    private final String workCheckOut;
    
    // Project/Client filters
    private final String projectName;
    private final String clientName;
    private final String clientLocation;
    private final String poNo;
    
    // Team/Activity filters
    private final String teamName;
    private final String activity;
    private final String shadowEmpName;
    
    // Count filters
    private final Long locationCount;
    private final Long projectCount;
    
    // Applied by/on filters
    private final String appliedBy;
    private final String appliedOn;
    
    // Status & search filters
    private final int status;
    private final String search;

    /**
     * Factory method to create TimesheetFilterCriteria from the API payload.
     *
     * @param payload the incoming API payload
     * @return a new TimesheetFilterCriteria instance
     */
    public static TimesheetFilterCriteria fromPayload(GetMyReporteesTimesheetRequestsPayload payload) {
        return TimesheetFilterCriteria.builder()
                .empId(payload.getEmpId())
                .clientFilter(Boolean.TRUE.equals(payload.getClientFilter()))
                .employmentId(payload.getEmploymentId())
                .employeeName(payload.getEmployeeName())
                .dayType(payload.getDayType())
                .date(payload.getDate())
                .startDate(payload.getStartDate())
                .endDate(payload.getEndDate())
                .workCheckIn(payload.getWorkCheckIn())
                .workCheckOut(payload.getWorkCheckOut())
                .projectName(payload.getProjectName())
                .clientName(payload.getClientName())
                .clientLocation(payload.getClientLocation())
                .poNo(payload.getPoNo())
                .teamName(payload.getTeamName())
                .activity(payload.getActivity())
                .shadowEmpName(payload.getShadowEmpName())
                .locationCount(payload.getLocationCount())
                .projectCount(payload.getProjectCount())
                .appliedBy(payload.getAppliedBy())
                .appliedOn(payload.getAppliedOn())
                .status(payload.getStatus())
                .search(payload.getSearch())
                .build();
    }
}

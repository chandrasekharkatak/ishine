package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class GetMyReporteesTimesheetRequestsPayload {
	
	private Long empId;
	private Boolean clientFilter;
	
	/* Pagination */
    private Integer page;
    private Integer size;

    /* Sorting */
    private String sortBy = "date";      
    private String sortDir = "DESC";     // ASC / DESC

    /* Search */
    private String employmentId;
    private String employeeName;
    private String dayType;
    private String projectName;
    private String clientName;
    private String clientLocation;
    private String poNo;
    private String shadowEmpName;
    private String teamName;
    private String activity;
    private String date;
    
    private String search;
    private String workCheckIn;
    private String workCheckOut;
    private Long locationCount;
    private Long projectCount;
    private String appliedBy;
    private String appliedOn;
    private int status;

    /** Optional date range filter (YYYY-MM-DD). When both set, only timesheets with date in [startDate, endDate] are returned. */
    private String startDate;
    private String endDate;

}

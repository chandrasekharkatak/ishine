package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class RMGDashboardProjectRequest {

    private String projectStatus;
    private String completionStatus;
    private Long currentUserEmpId;
    private String currentUserType;
    private List<Long> departmentIds;
    private List<String> departmentNames;
    private String type;
    private String expiredProjectFilter;
    private String fixedCostFilter;
    private String employeeGroupKey;
    private String unfilledTimesheetFilter;
    private String fromDate;
    private String toDate;

    private int page;
    private int pageSize;
    private String sortDirection;
    private String sortColumn;
    private String sortColumnType;
    private Map<String, String> projectFilter;
}

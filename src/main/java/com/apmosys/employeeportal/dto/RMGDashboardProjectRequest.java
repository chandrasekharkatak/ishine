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
    private Map<String, String> subKeyKeyMap;

    /**
     * Server-side only: when the project name column filter is resolved through linked projects,
     * native queries restrict rows to these primary project ids. Not intended for client JSON.
     */
    private List<Integer> linkSearchPrimaryProjectIds;

    /**
     * Server-side only: bound value for {@code LOWER(p.project_name) LIKE :linkSearchNameLike} (e.g. {@code %term%}),
     * combined with {@link #linkSearchPrimaryProjectIds} so partial matches on the primary row still appear alongside
     * linked-name resolution. Not intended for client JSON.
     */
    private String linkSearchNameLikeParameter;

}


package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class InterviewDTO {

    private Long id;
    private String title;
    private String date;
    private String time;
    private String client;
    private String role;
    private String project;
    private Long departmentId;
    private Long employeeId;
    private String mode;
    private String interviewStatus;
    private String selectionStatus;
    private String onboardingStatus;
    private String interviewStatusChangeDate;
    private String selectionStatusChangeDate;
    private String onboardingStatusChangeDate;
    private String interviewRemarks;
    private String selectionRemarks;
    private String onboardingRemarks;
    private String jd;
    private String interviewerName;
    private Long scheduledById;
    private String additionalNotes;
    private String resumeFileName;
    private String resumeFilePath;
    private String createdOn;
    private String updatedOn;
    private Long createdBy;
    private Long updatedBy;

    // Display fields (populated via JOINs)
    private String departmentName;
    private String employeeName;
    private String scheduledByName;

    // Filter and pagination fields
    private String startDate;
    private String endDate;
    private List<String> clients;
    private List<Long> departmentIds;
    private List<Long> employeeIds;
    private String sortColumn;
    private String sortDirection;
    private Integer page;
    private Integer size;

    // Column-level filters
    private String titleFilter;
    private String dateFilter;
    private String clientFilter;
    private String roleFilter;
    private String projectFilter;
    private String departmentNameFilter;
    private String employeeNameFilter;
    private String modeFilter;
    private String interviewStatusFilter;
    private String selectionStatusFilter;
    private String onboardingStatusFilter;
    private String jdFilter;
    private String scheduledByNameFilter;
    private String interviewerNameFilter;
}

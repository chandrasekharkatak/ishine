package com.apmosys.employeeportal.dto;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomTimesheetReportDTO {
    private Long employeementId;
    private String employeeName;
    private Date date;
    private String dayType;
    private String description;
    private String status;
    private String totalTime;
    private String createdOn;
    private String updatedOn;
    private String statusUpdatedBy;
    private String teamName;
    private String projectName;
    private String clientName;
    private String officeInTime;
    private String officeOutTime;
    private String totalWorkingHours;
    private String leaveType;
    private String isConsultant;
    private String isApprenticeship;
    private Long timesheetStatusUpdatedBy;
    private Long empId;
    private String isApmosysProduct;
    private String employmentIdAcToET;
}

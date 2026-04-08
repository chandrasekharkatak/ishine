package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GetReporteesTimesheetProjectsDTO {
	
	private Integer projectId;
    private String projectName;
    private String clientName;
    private String clientLocation;
    private String poNo;
    private String shadowEmp;
    private String shadowEmployeementId;
    private Integer status;
    private Integer clientApprovalStatus;
    private String totalClientWorkingMinutes;
    private String description;
	private List<GetReporteesTimesheetActivitiesDTO> activities;
	private List<RejectionDataDTO> rejectionReasons;

}

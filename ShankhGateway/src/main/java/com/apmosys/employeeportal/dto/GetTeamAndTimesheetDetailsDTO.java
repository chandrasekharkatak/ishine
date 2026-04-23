package com.apmosys.employeeportal.dto;
import java.time.LocalDate;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetTeamAndTimesheetDetailsDTO {
    private Long poId;
    private Long poProjectId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String projectName;
    private String departmentName;
    private String listType;
	private Long empId;
}

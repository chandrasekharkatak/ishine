package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

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
public class TimesheetDashboardCountDTO {
	
	private Integer totalApplicableCount;
    private Integer approvedCount;
    private Integer defaulterCount;
    private Integer clientSidePendingCount;
    private Integer totaldefaulterCount;

}

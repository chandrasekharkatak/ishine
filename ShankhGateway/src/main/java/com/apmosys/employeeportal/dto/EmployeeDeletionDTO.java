package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDeletionDTO {
	
	private String employeeName;
    private String teamName;
    private String roleName;

    private LocalDateTime scheduledStartDate;
    
    

}

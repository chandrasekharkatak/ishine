package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class TNMConflictDTO {
	
	private Long empId;
    private String employeeCode;
    private String empName;

    // New project which caused the conflict
    private Integer projectId;
    private String newProjectName;
    private LocalDate startDate;
    
    // Existing active projects (comma separated)
    private String existingProjects;
    
}

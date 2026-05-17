package com.apmosys.employeeportal.dto;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

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
public class PoDeletionImpactDTO {
   
	private String projectName;
    private String deletedPoNumber;

    private boolean projectClosed;

    private List<EmployeeDeletionDTO> deletedEmployees;

    private List<String> deactivatedTeams;
}

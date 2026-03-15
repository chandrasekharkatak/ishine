package com.apmosys.employeeportal.dto;

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

public class DeactivationCandidateDTO {

	private Long employeeTeamMapId;
    private Long empId;
    private Integer projectId;
    
}

package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
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
public class RowData {
	private Long empId;
    private String certificateName;
    private String specialization;
    private Long deptId;
    private Long proficiencyId;
    private String issuingAuthority;
    private LocalDate validFrom;
    private LocalDate expiresOn;
    private String skillsRaw;
    private String driveLink;

}

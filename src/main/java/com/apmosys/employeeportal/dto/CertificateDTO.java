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
public class CertificateDTO {
	
    private Long employeeCertificateId;
    private  String certificationName;
    private Long deptId;
    private String departmentName;
    private String expiresOn;
    private String issuingAuthority;
    private Long proficiencyId;
    private List<EmployeeSkillProficiencyDTO> skills; 
    private String specialization;
    private String validFrom;
    
    private Long empId;
    private Long docId;
    private String certificateStatus;
    
    public CertificateDTO (Long employeeCertificateId,String certificationName,String specialization,
    		String issuingAuthority,LocalDate validFrom,LocalDate expiresOn,String certificateStatus,Long docId) {
    	this.employeeCertificateId= employeeCertificateId;
    	this.certificationName = certificationName;
    	this.specialization = specialization;
    	this.issuingAuthority= issuingAuthority;
    	this.validFrom = validFrom != null ? validFrom.toString() : null;
    	this.expiresOn = expiresOn != null ? expiresOn.toString() : null;
    	this.certificateStatus = certificateStatus;
    	this.docId = docId;
    }
    
}

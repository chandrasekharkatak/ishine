package com.apmosys.employeeportal.dto;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IshineToPoEmpDetailsSharingDTO {
	
	 	private String projectName;
	    private Integer projectId;        
	    private String poNo;
	    private Long poId;

	    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	    private Date startDateOfBilling;
	    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	    private Date endDateOfBilling;

	    private Boolean poStatus;      
	    private Integer clientId;

	    private List<IshineToPoEmployeeDTO> employees;
	    
	    public IshineToPoEmpDetailsSharingDTO(String poNo, Boolean poStatus, Integer clientId) {
	        this.poNo = poNo;
	        this.poStatus = poStatus;
	        this.clientId = clientId;
	    }

	

}

package com.apmosys.employeeportal.mongodb.dto;

import java.util.List;

import com.apmosys.employeeportal.dto.FormFieldDTO;

import lombok.Data;

@Data
public class FormStructureDTO {
	
	    private String id;
	    private String formName;
	    private Integer departmentId;
	    private String parentFormId;
	    private String departmentName;
	    private List<FormFieldDTO> fields;
	    private List<FormStructureDTO> children;
}

package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class DynamicFormStructureDTO {
	private String id;
    private String formName;
    private Long departmentId;
    private List<FormFieldDTO> fields;
}

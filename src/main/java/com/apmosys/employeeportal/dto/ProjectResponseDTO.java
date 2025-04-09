package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class ProjectResponseDTO {

	private Long responseByEmpId;
	private String responseByEmpName;
	private Long responseId;
	private String response;
	private String responseList;
	private Long entityId;
	private String entityType;
	private String uploadedFile;
	private String uploadedFileName;
	private String documentPath;
    private String options;
    private String isDraft;
    private Long processTo;
    private Double marks;
    private String responseType;
}

package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceProofDocumentDTO {

	private Long documentId;
	private String fileName;
	private Integer sortOrder;
}

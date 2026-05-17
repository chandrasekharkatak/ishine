package com.apmosys.employeeportal.dto;

import org.springframework.core.io.Resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceProofDownloadDTO {
	private Resource resource;
	private String downloadFileName;
}

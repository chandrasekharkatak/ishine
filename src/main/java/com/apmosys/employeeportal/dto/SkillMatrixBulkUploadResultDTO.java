package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SkillMatrixBulkUploadResultDTO {

	private int inserted;
	private int failed;
	private List<String> rowErrors = new ArrayList<>();
}

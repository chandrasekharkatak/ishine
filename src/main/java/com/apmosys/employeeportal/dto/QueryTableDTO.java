package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QueryTableDTO {

	private Long queryId;

	private String query;
	private String queryName;
	private Long createdBy;
	private LocalDate createdOn;
	private Long updatedBy;
	private LocalDate updatedOn;
	private boolean publish;
	private String createdByName;
	private String updatedByName;
}

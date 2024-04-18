package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.util.List;

import com.apmosys.employeeportal.model.Notification;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DocumentDTO {
	
	private Long typeId;
	private String typeName;
	private LocalDate createdOn;
	private String createdBy;
	private LocalDate updatedOn;
	private String UpdatedBy;
	private String name;


}

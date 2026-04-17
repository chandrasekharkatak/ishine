package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NewsletterDTO {
	
	private Long documentId;
	private String displayName;
	private String fileName;
	private String type;
	private String readEnabled;
	
	private byte[] fileBytes;
	
	private Long empId;
	private String name;
	private String isAllNewsletterMarkAsRead;
	
	private String createdOn;
	private Integer createdBy;
	private String createdByName;
	private String updatedOn;
	private Integer updatedBy;
	private String updatedByName;
	private String typeName;
	private Long typeId;
	List<CustomFilterDTO> queryList;
}

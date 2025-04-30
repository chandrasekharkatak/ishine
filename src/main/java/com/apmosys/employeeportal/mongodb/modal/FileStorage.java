package com.apmosys.employeeportal.mongodb.modal;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "file")
@Data
public class FileStorage {

	 	@Id
	    private String id;
	    private Long entityId;
	    private String entityType;
	    private Long projectId;
	    private Long questionMasterId;
	
}

package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TagMaster {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long tagId;
	private Long projectId;
	private String tag;
	private String entityType;
	private Long entityId;
	
	//user --> tag created by user   /   nlp --> tag created by system
	private String type;

}

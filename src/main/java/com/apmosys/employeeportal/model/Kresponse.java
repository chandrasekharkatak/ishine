package com.apmosys.employeeportal.model;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class Kresponse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Float response;
	private Long kpiId;
	private Long empId;
	private Long quarterId;
	private String description;
//	private String review;
	
	private Boolean isFixed;
//    private Long rating;
    private String remark;
    private Long progress;
    
	
}

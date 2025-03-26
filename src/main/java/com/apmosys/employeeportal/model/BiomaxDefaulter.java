package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class BiomaxDefaulter {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long defaulterId;
	private Long empId;
	private Long employeementId;
	private Long departmentId;
	private String leaveToDeduct;
	private String defaultedDate;
	private Long groupId;
	private Boolean isDeducted;
	private Boolean isApprovedByManager;
	@Embedded
	public CommonProperties commonProperty = new CommonProperties();

}

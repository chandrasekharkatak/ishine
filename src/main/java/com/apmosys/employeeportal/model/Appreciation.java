package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Appreciation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "appreciation_by")
	private Long appreciationBy;
	
	@Column(name = "appreciation_to")
	private Long appreciationTo;
	
	@Column(name = "manager_name")
	private String managerName;
	
	@Column(name = "appriate_type")
	private String appreciateType;
	
	@Column(name = "comment")
	private String reason;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private LocalDateTime appreciationDate;
	
}

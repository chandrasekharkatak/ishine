package com.apmosys.employeeportal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import antlr.collections.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppreciationEvent {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long appreciationEventid;
	
	private String appreciationEventName;
	
	private String fromDate;
	
	private String toDate;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private LocalDateTime createdOn;
	
	private Long updatedBy;
	
	private String appreciationEventType;
	
	

}

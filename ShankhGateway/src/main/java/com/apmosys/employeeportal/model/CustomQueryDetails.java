package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "CustomQueryDetails")
public class CustomQueryDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long customQueryId;
	
	private Long createdBy;
	
	private String queryName;
	
	private String availableColumns;
	
	private String selectedColumns;

}

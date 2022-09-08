package com.apmosys.employeeportal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "EmployeeTimesheets")
@ToString
public class Timesheet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long timesheetId;

	private Long empId;

	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate date;

	private String dayType;

	@Column(length = 1000)
	private String description;

	private String status;
	private String remarks;
	
	private Float totalTime;
	
	@Embedded
	public CommonProperties commonProperty = new CommonProperties();
	

}

package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="EmployeeTimesheetActivitiesMapping")
public class TimesheetActivityMap {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long timesheetActivityMapId;
	
	private Long timesheetId;
	private Long activityId;
	private Float completionTime;
	private String description;
	private Integer clientLocationId;

}

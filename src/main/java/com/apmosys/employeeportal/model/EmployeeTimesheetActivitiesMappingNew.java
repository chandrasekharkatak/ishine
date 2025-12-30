package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_timesheet_activities_mapping_new")
public class EmployeeTimesheetActivitiesMappingNew {

	    @EmbeddedId
	    private TimesheetActivityMapId id;

	    @Column(name = "description")
	    private String description;

	    @Column(name = "duration_minutes")
	    private Short durationMinutes;

}

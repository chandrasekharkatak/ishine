package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "employee_timesheet_activities_mapping_new")
public class EmployeeTimesheetActivitiesMappingNew {

    @Id	
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "timesheet_id", nullable = false)
    private Long timesheetId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;
    
    @Column(name = "location_mapping_id", nullable = false)
    private Long locationMappingId;

	@Column(name = "description")
	private String description;

	@Column(name = "duration_minutes")
	private Short durationMinutes;

}

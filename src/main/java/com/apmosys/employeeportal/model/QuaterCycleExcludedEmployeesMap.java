package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@Entity
@Audited
@Table(name="quater_cycle_excluded_employees_map")
public class QuaterCycleExcludedEmployeesMap {

	@Id
	@Column(name="map_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long mapId;
	
	@Column(name="quarter_id")
	private Long quarterId;
	
	@Column(name="emp_id")
	private Long empId;
	
	@Column(name="created_by")
	private  Long createdBy;
	
	@Column(name="created_on",columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
}

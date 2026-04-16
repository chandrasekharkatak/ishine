package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "logs")
public class Log {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long logId;
	
	private Long empId;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp recordedOn;
	
	private String event;
	
	private String tableName;
	
	private Long tableEntryId;
	
	@Column(columnDefinition = "TEXT")
	private String payload;
	
	@Column(length = 1000)
	private String remarks;
	

}

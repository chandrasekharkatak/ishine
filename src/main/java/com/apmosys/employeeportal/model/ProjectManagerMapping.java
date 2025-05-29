package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Audited
public class ProjectManagerMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long projectManagerMappingId;
	private Long projectId;
	private Long projectManagerId;
	private Integer active;
	private Long createdBy;
	private Timestamp createdOn;
	private Long updatedBy;
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	
}

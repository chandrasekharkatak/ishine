package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
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
@Table(name = "training_type_master")
public class TrainingTypeMaster {
	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id; 
		@Column(name = "training_type", nullable = false)
	    private String trainingType;

	    @Column(name = "created_on")
	    private LocalDateTime createdOn;

	    @Column(name = "created_by")
	    private Long createdBy;

	    @Column(name = "is_active", nullable = false)
	    private Boolean isActive = true; // Default to active

}

package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Entity
@Data
public class Department {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long deptId;

	private String name;

	// @OneToOne
	// @JoinColumn(name = "hodId")
	private Long hodId;

	@Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
	private Timestamp createdOn;

	private Integer createdBy;

	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;

	private Integer updatedBy;

	@Column(length = 8) // Specify length for the abbreviation
	private String deptAbbreviation; // New field

	private Boolean isBillable;
	private Boolean isTnm;
	@Column
	private String deptColorCode;

	@PrePersist
	public void assignRandomColor() {
		if (this.deptColorCode == null || this.deptColorCode.isEmpty()) {
			Random random = new Random();
			// Generate a random number between 0x000000 and 0xFFFFFF
			int randomColor = random.nextInt(0xFFFFFF + 1);
			// Convert to hex and ensure 6 digits
			this.deptColorCode = String.format("#%06X", randomColor);
		}
	}
}

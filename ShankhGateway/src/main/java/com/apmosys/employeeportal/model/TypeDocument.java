package com.apmosys.employeeportal.model;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.*;


@Getter
@Setter
@ToString
@Entity
public class TypeDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long typeId;
	private String typeName;
	private LocalDate createdOn;
	private String createdBy;
	private LocalDate updatedOn;
	private String UpdatedBy;
}

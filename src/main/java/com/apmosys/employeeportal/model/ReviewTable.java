package com.apmosys.employeeportal.model;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "review_table")
public class ReviewTable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer reviewerId;
	
	private Long employeeId;
	
	private Integer quarter;
	
	private Float rating;
	
	private String remarks;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate reviewDate;
	
	private Float kpiScore;
}

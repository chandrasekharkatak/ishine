package com.apmosys.employeeportal.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class EmployeeRatingPerformance {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long performanceRatingId;
	
	private Long quarterId;
	
	private Long reviewTypeId;
	
	@Column(precision = 3, scale = 1)
    private BigDecimal ratingValue;
	
	private Long empId;
	
	
	

}

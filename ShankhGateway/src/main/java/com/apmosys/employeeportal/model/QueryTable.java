package com.apmosys.employeeportal.model;

import java.time.LocalDate;
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
@Table(name = "query_table")
@Getter
@Setter
public class QueryTable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long queryId;
	private String queryName;
	@Column(length = 5000)
	private String query;

	private Long createdBy;
	private LocalDate createdOn;
	private Long updatedBy;
	private LocalDate updatedOn;
	private boolean publish;

}

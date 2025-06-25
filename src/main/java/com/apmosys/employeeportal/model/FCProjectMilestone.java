package com.apmosys.employeeportal.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "fc_project_milestone")
public class FCProjectMilestone {

	@Id
	private Long id;

	private Long poId;

	private Long projectId;

	private String name;

	@Column(columnDefinition = "varchar(500) default ''")
	private String description;

	private Date startDate;

	private Date endDate;

	private String status;

	@Column(columnDefinition = "varchar(500) default ''")
	private String remarks;

	private Long lineItemId;
}

package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "fc_line_item")
public class FCLineItem {

	@Id
	private Long id;

	private Long poId;

	private Long projectId;
	private Long poProjectId;

	private String name;

	private String status;

}

package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PoHierarchyNodeDTO {

	private Long poId;
	private String poNo;
	private boolean active;

	private boolean deleted;
	private boolean primary;
	private boolean modified;

	private List<PoHierarchyNodeDTO> children = new ArrayList<>();
}

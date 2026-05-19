package com.apmosys.employeeportal.dto.polink;

import java.util.ArrayList;
import java.util.List;

import com.apmosys.employeeportal.dto.PoHierarchyNodeDTO;

import lombok.Getter;
import lombok.Setter;

/**
 * PO tree section for PO-link notification emails.
 */
@Getter
@Setter
public class PoHierarchyDto {

	private List<PoHierarchyNodeDTO> roots = new ArrayList<>();
}

package com.apmosys.employeeportal.dto.polink;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Summary flags and boarding lines for PO-link impact.
 */
@Getter
@Setter
public class ResourceImpactDto {

	private boolean autoResourceAdjustmentApplied;
	private boolean projectDisplayNameChanged;
	private String previousProjectDisplayName;
	private String currentProjectDisplayName;

	private List<String> boardingLines = new ArrayList<>();
}

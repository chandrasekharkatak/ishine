package com.apmosys.employeeportal.dto.polink;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * All structured content needed to render the PO-link completion email.
 */
@Getter
@Setter
public class PoLinkEmailContentDto {

	private String projectDisplayName;

	private List<String> mergedRemovedProjectLines = new ArrayList<>();

	private String whatChangedHtml;

	/** When false, previous PO requirements section is omitted (e.g. Monitoring primary). */
	private boolean includePreviousPoRequirementsSection;

	private List<PreviousPoRequirementsBlockDto> previousRequirementBlocks = new ArrayList<>();

	private List<RequirementDto> currentRequirementRows = new ArrayList<>();

	private List<BoardingTableRowDto> boardingTableRows = new ArrayList<>();

	private ResourceImpactDto resourceImpact = new ResourceImpactDto();
}

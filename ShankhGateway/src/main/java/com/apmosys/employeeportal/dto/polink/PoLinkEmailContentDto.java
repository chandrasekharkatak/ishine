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

	private boolean showNoRequirementDataBanner;
	private String noRequirementBannerNote;

	private String hierarchyTreeHtml;

	private List<PreviousRequirementDto> previousRequirementRows = new ArrayList<>();

	private List<RequirementDto> currentRequirementRows = new ArrayList<>();

	private List<BoardingGroupDto> boardingGroups = new ArrayList<>();

	private ResourceImpactDto resourceImpact = new ResourceImpactDto();
}

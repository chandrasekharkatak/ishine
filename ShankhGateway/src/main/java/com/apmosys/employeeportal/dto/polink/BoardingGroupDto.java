package com.apmosys.employeeportal.dto.polink;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

/** Boarding snapshot grouped by PO (deduplicated resources). */
@Getter
@Builder
public class BoardingGroupDto {

	private String poLabel;
	private String poStateLabel;
	/** True: expired/removed/inactive PO, overboarded context, or other attention needed. */
	private boolean highlightGroup;
	@Builder.Default
	private List<String> memberLines = new ArrayList<>();
}

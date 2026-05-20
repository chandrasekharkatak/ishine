package com.apmosys.employeeportal.dto.polink;

import lombok.Builder;
import lombok.Getter;

/** One row in the PO link email boarding snapshot table. */
@Getter
@Builder
public class BoardingTableRowDto {

	private String poLabel;
	private String status;
	private String resourceName;
	private String team;
	private String remark;
}

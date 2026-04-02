package com.apmosys.employeeportal.dto;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.apmosys.employeeportal.enums.SyncRequestType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeletedPoSyncDTO {

	@NotNull(message = "eventType is required")
	private SyncRequestType eventType;

	@NotNull(message = "projectId is required")
	private Long projectId;

	@NotBlank(message = "projectName is required")
	private String projectName;

	@NotNull(message = "deletedByEmpId is required")
	private Long deletedByEmpId;

	@NotBlank(message = "deletedByEmpName is required")
	private String deletedByEmpName;

	@NotNull(message = "deletedOn is required")
	private Date deletedOn;

	/** Optional; when null or empty, remaining active POs and teams are reconcoded without portal link updates. */
	private List<PoDetailsForProjectPoMappingDTO> associatePos;

	@NotNull(message = "deletedPo is required")
	private PoDetailsForProjectPoMappingDTO deletedPo;

}

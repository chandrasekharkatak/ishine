package com.apmosys.employeeportal.dto;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PoClientAddressUpdateDTO {

	@NotEmpty(message = "PO IDs are required")
	private List<@NotNull Long> poIds;

	@NotNull(message = "Client address ID is required")
	private Long clientAddressId;

	@NotBlank(message = "Client location is required")
	private String clientLocation;

	@NotBlank(message = "Client state is required")
	private String clientState;

	@NotNull(message = "updatedByEmpId is required")
	private Long updatedByEmpId;

	@NotBlank(message = "updatedByEmpName is required")
	private String updatedByEmpName;

	@NotNull(message = "updatedOn is required")
	private Date updatedOn;

}

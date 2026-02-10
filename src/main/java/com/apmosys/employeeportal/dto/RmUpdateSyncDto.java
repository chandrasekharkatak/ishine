package com.apmosys.employeeportal.dto;

import java.util.Date;
import java.util.List;

import com.apmosys.employeeportal.enums.SyncRequestType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RmUpdateSyncDto {
	
	private List<Long> poIds;
    private Long updatedApmosysRmEmpId;
    private String updatedApmosysRmEmpName;
    private String updatedApmosysRmEmail;

}

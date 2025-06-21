package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetDeptIdByRoleDTO {
	
	private String departmentName;
	private Long deptId;
	
	public GetDeptIdByRoleDTO(Long deptId, String departmentName) {
        this.deptId = deptId;
        this.departmentName = departmentName;
    }

}

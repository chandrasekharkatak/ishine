package com.apmosys.employeeportal.dto;

import java.util.Date;

import com.apmosys.employeeportal.utility.TypeConversionUtil;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UnmappedEmployeeProjectDto {

	private Long empId;
	private Long employmentId;
	private String employmentIdStr;
	private String name;
	private String email;
	private Long deptId;
	private String departmentName;
	private String hodMail;
	private String hodName;
	private String reportingManagerMail;
	private String reportingManagerName;
	private Date unmapStartDate;
	private Date unmapEndDate;
	private Integer unmappedDaysCount;

	public UnmappedEmployeeProjectDto(Long empId, Long employmentId, String employmentIdStr, String name, String email,
			Long deptId, String departmentName, String hodMail, String hodName, String reportingManagerMail,
			String reportingManagerName, Date unmapStartDate, Date unmapEndDate, Integer unmappedDaysCount) {
		super();
		this.empId = empId;
		this.employmentId = employmentId;
		this.employmentIdStr = employmentIdStr;
		this.name = name;
		this.email = email;
		this.deptId = deptId;
		this.departmentName = departmentName;
		this.hodMail = hodMail;
		this.hodName = hodName;
		this.reportingManagerMail = reportingManagerMail;
		this.reportingManagerName = reportingManagerName;
		this.unmapStartDate = unmapStartDate;
		this.unmapEndDate = unmapEndDate;
		this.unmappedDaysCount = unmappedDaysCount;
	}

	public static UnmappedEmployeeProjectDto unmappedEmployeeProject(Object[] row) {
		UnmappedEmployeeProjectDto unmappedEmployeeProjectDto = new UnmappedEmployeeProjectDto();
		unmappedEmployeeProjectDto.empId = TypeConversionUtil.safeParseLong(row[0]);
		unmappedEmployeeProjectDto.employmentId = TypeConversionUtil.safeParseLong(row[1]);
		unmappedEmployeeProjectDto.employmentIdStr = TypeConversionUtil.getSafeString(row[2]);
		unmappedEmployeeProjectDto.name = TypeConversionUtil.getSafeString(row[3]);
		unmappedEmployeeProjectDto.email = TypeConversionUtil.getSafeString(row[4]);
		unmappedEmployeeProjectDto.deptId = TypeConversionUtil.safeParseLong(row[5]);
		unmappedEmployeeProjectDto.departmentName = TypeConversionUtil.getSafeString(row[6]);
		unmappedEmployeeProjectDto.hodMail = TypeConversionUtil.getSafeString(row[7]);
		unmappedEmployeeProjectDto.hodName = TypeConversionUtil.getSafeString(row[8]);
		unmappedEmployeeProjectDto.reportingManagerMail = TypeConversionUtil.getSafeString(row[9]);
		unmappedEmployeeProjectDto.reportingManagerName = TypeConversionUtil.getSafeString(row[10]);
		unmappedEmployeeProjectDto.unmapStartDate = row[11] != null ? (Date) row[11] : null;
		unmappedEmployeeProjectDto.unmapEndDate = row[12] != null ? (Date) row[12] : null;
		unmappedEmployeeProjectDto.unmappedDaysCount = TypeConversionUtil.safeParseInt(row[13]);
		return unmappedEmployeeProjectDto;
	}

}

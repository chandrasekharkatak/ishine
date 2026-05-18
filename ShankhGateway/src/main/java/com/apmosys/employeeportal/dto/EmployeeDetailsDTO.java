package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.apmosys.employeeportal.utility.TypeConversionUtil;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeDetailsDTO {

    private Long empId;
    private Long employeementId;
    private String name;
    private String email;
    private Long mobileNo;
    private String employmentstatus;
    private Long jobRoleId;
    private String jobRoleName;
    private Long managerId;
    private String managerName;
    private Long departmentId;
    private String departmentName;
    private String billable;
    private Long teamId;
    private String teamName;
    private Integer projectId;
    private String projectName;
    private String poStartDate;
    private String poEndDate;
    private String poNo;
    private String poProjectType;
    private String employeeRole;
    private String clientName;
    private String clientLocation;
    private Long hodId;
    private String hodName;
    private String hodEmail;
    private Long designationId;
    private String designationName;
    private String billableType;
    private String employeeName;
    private String employeeConfirmationDate;
    private String isConsultant;
    private String dayOnbench;
    private String onbenchDate;
    private String isApprenticeship;
    private String employmentId;
    private String isApmosysProduct;
    private String employmentIdAcToET;
    private String projectManagerName;
    private Long projectManagerId;
    private String apmosysRM;
    private String clientRM;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime effectiveStartDate;

    private List<EmployeeDetailsDTO> expandedRowDetails;
    private boolean expandableRow;
    private String etmStartDate;
    private String etmActive;

    public EmployeeDetailsDTO(Long empId, Long employeementId, String name, String email, String employmentstatus,
            Long mobileNo, String jobRoleName, String departmentName, String isConsultant, String isApprenticeship,
            String isApmosysProduct, Long managerId, String managerName, String billableType) {
        this.empId = empId;
        this.employeementId = employeementId;
        this.name = name;
        this.email = email;
        this.employmentstatus = employmentstatus;
        this.mobileNo = mobileNo;
        this.jobRoleName = jobRoleName;
        this.departmentName = departmentName;
        this.isConsultant = isConsultant;
        this.isApprenticeship = isApprenticeship;
        this.isApmosysProduct = isApmosysProduct;
        this.managerId = managerId;
        this.managerName = managerName;
        this.billableType = billableType;
    }

    public EmployeeDetailsDTO(Object[] row) {
        this.empId = TypeConversionUtil.safeParseLong(row[0]);
        this.name = TypeConversionUtil.getSafeString(row[1]);
        this.employmentIdAcToET = TypeConversionUtil.getSafeString(row[2]);
        this.departmentName = TypeConversionUtil.getSafeString(row[3]);
        this.billable = TypeConversionUtil.getSafeString(row[4]);
        this.billableType = TypeConversionUtil.getSafeString(row[5]);
        this.onbenchDate = TypeConversionUtil.getSafeString(row[6]);
        this.dayOnbench = TypeConversionUtil.getSafeString(row[7]);
        this.projectName = TypeConversionUtil.getSafeString(row[8]);
        this.projectManagerName = TypeConversionUtil.getSafeString(row[9]);
        this.teamName = TypeConversionUtil.getSafeString(row[10]);
        this.employeeRole = TypeConversionUtil.getSafeString(row[11]);
    }

    public EmployeeDetailsDTO(Long empId, Long employeementId, String name, String departmentName, String billable,
            String billableType, String projectName, String clientName, String apmosysRM, String clientRM, String poNo,
            String poProjectType, String poStartDate, String poEndDate) {
        this.empId = empId;
        this.employeementId = employeementId;
        this.name = name;
        this.departmentName = departmentName;
        this.billable = billable;
        this.billableType = billableType;
        this.projectName = projectName;
        this.clientName = clientName;
        this.apmosysRM = apmosysRM;
        this.clientRM = clientRM;
        this.poNo = poNo;
        this.poProjectType = poProjectType;
        this.poStartDate = poStartDate;
        this.poEndDate = poEndDate;
    }

    public static EmployeeDetailsDTO onBenchButProjectMapped(Object[] row) {
        EmployeeDetailsDTO employeeDetailsDTO = new EmployeeDetailsDTO();
        employeeDetailsDTO.empId = TypeConversionUtil.safeParseLong(row[0]);
        employeeDetailsDTO.employmentIdAcToET = TypeConversionUtil.getSafeString(row[1]);
        employeeDetailsDTO.name = TypeConversionUtil.getSafeString(row[2]);
        employeeDetailsDTO.departmentName = TypeConversionUtil.getSafeString(row[3]);
        employeeDetailsDTO.billable = TypeConversionUtil.getSafeString(row[4]);
        employeeDetailsDTO.billableType = TypeConversionUtil.getSafeString(row[5]);
        employeeDetailsDTO.projectName = TypeConversionUtil.getSafeString(row[6]);
        employeeDetailsDTO.clientName = TypeConversionUtil.getSafeString(row[7]);
        employeeDetailsDTO.apmosysRM = TypeConversionUtil.getSafeString(row[8]);
        employeeDetailsDTO.clientRM = TypeConversionUtil.getSafeString(row[9]);
        employeeDetailsDTO.poNo = TypeConversionUtil.getSafeString(row[10]);
        employeeDetailsDTO.poProjectType = TypeConversionUtil.getSafeString(row[11]);
        employeeDetailsDTO.poStartDate = TypeConversionUtil.getSafeString(row[12]);
        employeeDetailsDTO.poEndDate = TypeConversionUtil.getSafeString(row[13]);
        if (row.length > 16) {
            employeeDetailsDTO.projectManagerName = TypeConversionUtil.getSafeString(row[14]);
            employeeDetailsDTO.teamName = TypeConversionUtil.getSafeString(row[15]);
            employeeDetailsDTO.employeeRole = TypeConversionUtil.getSafeString(row[16]);
        }
        return employeeDetailsDTO;
    }

    public static EmployeeDetailsDTO withoutBillability(Object[] row) {
        EmployeeDetailsDTO employeeDetailsDTO = new EmployeeDetailsDTO();
        employeeDetailsDTO.empId = TypeConversionUtil.safeParseLong(row[0]);
        employeeDetailsDTO.employmentIdAcToET = TypeConversionUtil.getSafeString(row[1]);
        employeeDetailsDTO.name = TypeConversionUtil.getSafeString(row[2]);
        employeeDetailsDTO.departmentName = TypeConversionUtil.getSafeString(row[3]);
        employeeDetailsDTO.managerName = TypeConversionUtil.getSafeString(row[4]);
        employeeDetailsDTO.jobRoleName = TypeConversionUtil.getSafeString(row[5]);

        return employeeDetailsDTO;
    }

    public static EmployeeDetailsDTO unfilledTimesheet(Object[] row) {
        EmployeeDetailsDTO employeeDetailsDTO = new EmployeeDetailsDTO();
        employeeDetailsDTO.projectId = TypeConversionUtil.safeParseInt(row[0]);
        employeeDetailsDTO.projectName = TypeConversionUtil.getSafeString(row[1]);
        employeeDetailsDTO.apmosysRM = TypeConversionUtil.getSafeString(row[2]);
        employeeDetailsDTO.clientRM = TypeConversionUtil.getSafeString(row[3]);
        employeeDetailsDTO.poStartDate = TypeConversionUtil.getSafeString(row[4]);
        employeeDetailsDTO.poEndDate = TypeConversionUtil.getSafeString(row[5]);
        employeeDetailsDTO.poNo = TypeConversionUtil.getSafeString(row[6]);
        employeeDetailsDTO.poProjectType = TypeConversionUtil.getSafeString(row[7]);
        employeeDetailsDTO.clientName = TypeConversionUtil.getSafeString(row[8]);
        employeeDetailsDTO.projectManagerName = TypeConversionUtil.getSafeString(row[9]);
        employeeDetailsDTO.teamId = TypeConversionUtil.safeParseLong(row[10]);
        employeeDetailsDTO.teamName = TypeConversionUtil.getSafeString(row[11]);
        employeeDetailsDTO.empId = TypeConversionUtil.safeParseLong(row[12]);
        employeeDetailsDTO.name = TypeConversionUtil.getSafeString(row[13]);
        employeeDetailsDTO.jobRoleName = TypeConversionUtil.getSafeString(row[14]);
        employeeDetailsDTO.departmentName = TypeConversionUtil.getSafeString(row[15]);
        employeeDetailsDTO.mobileNo = TypeConversionUtil.safeParseLong(row[16]);
        employeeDetailsDTO.email = TypeConversionUtil.getSafeString(row[17]);
        employeeDetailsDTO.billable = TypeConversionUtil.getSafeString(row[18]);
        employeeDetailsDTO.billableType = TypeConversionUtil.getSafeString(row[19]);
		employeeDetailsDTO.effectiveStartDate = getLocalDateTime(row[20]);
        employeeDetailsDTO.employeementId = TypeConversionUtil.safeParseLong(row[21]);
        return employeeDetailsDTO;
    }

    // Long empId, Long employeementId, String name, String departmentName, String projectName, String teamName, String clientName, String apmosysRM, String clientRM, String poNo,
        // String poProjectType, String poStartDate, String poEndDate, String etmStartDate (dd-MM-yyyy), Long etmActive
    public static EmployeeDetailsDTO futureStartDateAssigned(Object[] row) {
        EmployeeDetailsDTO employeeDetailsDTO = new EmployeeDetailsDTO();
        employeeDetailsDTO.empId = TypeConversionUtil.safeParseLong(row[0]);
        employeeDetailsDTO.employmentIdAcToET = TypeConversionUtil.getSafeString(row[1]);
        employeeDetailsDTO.name = TypeConversionUtil.getSafeString(row[2]);
        employeeDetailsDTO.departmentName = TypeConversionUtil.getSafeString(row[3]);
        employeeDetailsDTO.projectName = TypeConversionUtil.getSafeString(row[4]);
        employeeDetailsDTO.teamName = TypeConversionUtil.getSafeString(row[5]);
        employeeDetailsDTO.clientName = TypeConversionUtil.getSafeString(row[6]);
        employeeDetailsDTO.apmosysRM = TypeConversionUtil.getSafeString(row[7]);
        employeeDetailsDTO.clientRM = TypeConversionUtil.getSafeString(row[8]);
        employeeDetailsDTO.poNo = TypeConversionUtil.getSafeString(row[9]);
        employeeDetailsDTO.poProjectType = TypeConversionUtil.getSafeString(row[10]);
        employeeDetailsDTO.poStartDate = TypeConversionUtil.getSafeString(row[11]);
        employeeDetailsDTO.poEndDate = TypeConversionUtil.getSafeString(row[12]);
        employeeDetailsDTO.etmStartDate = row[13] != null ? new SimpleDateFormat("dd-MM-yyyy").format((Date) row[13]) : null;
        employeeDetailsDTO.etmActive = getEtmActiveStatus(TypeConversionUtil.safeParseLong(row[14]));
        return employeeDetailsDTO;
    }
    
	private static String getEtmActiveStatus(Long safeParseLong) {
        if (safeParseLong == null) {
            return "Undefined";
        }
        return safeParseLong == 2 ? "Approval Pending" : safeParseLong == 0 ? "Approved" : "Undefined";
    }

    private static LocalDateTime getLocalDateTime(Object obj) {
		if (obj == null) {
			return null;
		}
		Timestamp timestamp = (Timestamp) obj;
		LocalDateTime dateTime = timestamp.toLocalDateTime();
		return dateTime;
	}

}

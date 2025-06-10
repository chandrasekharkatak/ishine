package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportsQueryDTO {
    private List<Long> employeementId;
    private String name;
    private List<Long> deptId;
    private List<Long> jobRoleId;
    private List<Long> managerId;
    private List<Long> teamId;
    private List<Long> projectId;
    private List<Long> clientId;
    private String employmentstatus;
    private String dateOfJoining;
    private String city;
    private String bloodGroup;
    private String gender;
    private List<Long> probationPeriod;
    private List<Long> noticePeriod;
    private String maritalStatus;
    private String bankName;
    private String state;
    private String createdOn;
    private List<Long> createdBy;
    private String experience;
    private String workLocation;
    private Long year;
    private String billableType;
    
}

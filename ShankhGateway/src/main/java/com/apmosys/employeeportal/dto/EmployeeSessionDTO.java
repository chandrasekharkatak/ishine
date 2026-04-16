package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeSessionDTO {
    private Long empId;
    private String empName;
    private String empRole;
    private String sessionId;

    public EmployeeSessionDTO() {
    }
    
    public EmployeeSessionDTO(Long empId, String empName, String empRole, Long sessionId) {
        this.empId = empId;
        this.empName = empName;
        this.empRole = empRole;
        // Convert Long to String if your class field is String
        this.sessionId = sessionId != null ? sessionId.toString() : null;
    }
 
    @Override
    public String toString() {
        return "EmployeeSessionDTO{" +
                "empId=" + empId +
                ", empName='" + empName + '\'' +
                ", empRole='" + empRole + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
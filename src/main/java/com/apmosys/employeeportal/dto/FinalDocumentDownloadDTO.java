package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class FinalDocumentDownloadDTO {
    private Integer projectId;
    private Integer month;
    private Integer year;
    private String projectName;
    private Long empId; 
    private Long selectedEmpId;

}

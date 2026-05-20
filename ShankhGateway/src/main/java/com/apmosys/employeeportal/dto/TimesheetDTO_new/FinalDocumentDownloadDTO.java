package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import lombok.Data;

@Data
public class FinalDocumentDownloadDTO {
    
    private Long empId;
    private String empName;
    private String projectName;
    private String fileUrl;

    public FinalDocumentDownloadDTO(Long empId, String empName, String projectName, String fileUrl) {
        this.empId = empId;
        this.empName = empName;
        this.projectName = projectName;
        this.fileUrl = fileUrl;
    }

}

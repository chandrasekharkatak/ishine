package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import lombok.Data;

@Data
public class FinalDocumentDownloadPayloadDTO {

    private Long empId;
    private Integer month;
    private Integer year;
    private Integer projectId;

    
}

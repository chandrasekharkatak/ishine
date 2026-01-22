package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class FinalDocumentDTO {

    private String employeeName;
    private Integer projectId;
    private String projectName;
    private Integer teamId;
    private String employementId;

    private Long docId;
    private String docMimeType;
    private String docName;
    private Integer finalFlag;

   
}
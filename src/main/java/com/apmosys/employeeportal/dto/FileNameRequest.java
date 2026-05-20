package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileNameRequest {
    private Integer projectId;
    private String extension; 
    private String docType; 
}

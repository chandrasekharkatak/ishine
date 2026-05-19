package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class FileResponseDTO {
    private String status;
    private String contentType;
    private byte[] data;
    
}
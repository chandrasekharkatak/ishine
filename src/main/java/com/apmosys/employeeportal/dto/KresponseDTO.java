package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KresponseDTO {
    private Long id;
    private Float response;
    private String description;
    private Boolean isFixed;
    private String remark; // Temporary field for adding initial remark
    private Long progress;
    private Long empId;
    private Long quarterId;
    
    private List<KresponseRemarkDTO> remarks = new ArrayList<>();
}
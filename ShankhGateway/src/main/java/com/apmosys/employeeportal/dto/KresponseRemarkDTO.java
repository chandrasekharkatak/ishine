package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KresponseRemarkDTO {
    private Long id;
    private String remark;
    private String createdBy;
    private Date createdDate;
    private Long kresponseId;
    private String employeeRole;
    
}
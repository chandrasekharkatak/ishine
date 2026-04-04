package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PoDepartmentMappingDto {

    private Long poDepartmentMapId;
    private Long poId;
    private Long deptId;
    private boolean active;
    private Integer projectId;

    private Long createdBy;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Timestamp createdOn;
    private Long updatedBy;
    @UpdateTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Timestamp updatedOn;


    public PoDepartmentMappingDto(Long poId, Long deptId, Integer projectId, boolean active) {
        this.poId = poId;
        this.deptId = deptId;
        this.projectId = projectId;
        this.active = active;
    }




}

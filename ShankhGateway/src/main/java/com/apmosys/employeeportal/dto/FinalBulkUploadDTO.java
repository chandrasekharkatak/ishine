package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class FinalBulkUploadDTO {
    
    private List<Long> empIds;

    @NotNull(value = "Project id is required")
    private Integer projectId;

    @NotNull(value = "From date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @NotNull(value = "To date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    @NotNull(value = "Created by is required")
    private Long createdBy;

}

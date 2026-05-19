package com.apmosys.employeeportal.dto;

import lombok.Data;
import java.util.List;

@Data
public class TrainingMappingRequestDTO {
    private List<Long> excludedEmployeeIds;
    private List<Long> allEmployeeIds;
    private Long updatedBy;
}


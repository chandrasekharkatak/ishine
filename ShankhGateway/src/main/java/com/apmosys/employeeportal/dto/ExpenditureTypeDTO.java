package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenditureTypeDTO {
    private String expenditureTypeName;
    private String description;
    private Long createdBy;
    private String updatedBy;
}

package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenditureTypeDTO {
    /** Set for update/delete */
    private Long id;
    private String expenditureTypeName;
    private String description;
    private String isActive;
    private Long createdBy;
    private String updatedBy;
}

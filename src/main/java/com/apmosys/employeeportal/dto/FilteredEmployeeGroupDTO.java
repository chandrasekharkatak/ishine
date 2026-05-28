package com.apmosys.employeeportal.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilteredEmployeeGroupDTO {
    private Long id;
    private String name;
    private List<EmpIdAndNameDTO> employees;
}

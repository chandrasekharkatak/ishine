package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class QueryRequestDTO {
    private String customQuery;
    private List<QueryFilterDTO> customQueryFilters;
    private String selectedColumns;
}

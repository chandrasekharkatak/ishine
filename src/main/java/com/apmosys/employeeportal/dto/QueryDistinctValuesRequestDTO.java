package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class QueryDistinctValuesRequestDTO {
    private String customQuery;
    private String column;
    private Integer limit;
    private List<QueryFilterDTO> customQueryFilters;
}


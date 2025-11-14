package com.apmosys.employeeportal.dto;

import java.util.Map;

import lombok.Data;

@Data
public class PageDTO {
    private int page;
    private int size;
    private String sortDirection;
    private String sortColumn;
    private Map<String, String> searchFilter;
    private Map<String, Object> extraFilter;
    private Long currentUserEmpId;
    private String currentUserType;
}

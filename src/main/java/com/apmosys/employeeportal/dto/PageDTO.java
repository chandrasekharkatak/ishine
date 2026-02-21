package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class PageDTO {

    private int page;
    private int size;
    private String sortDirection;
    private String sortColumn;
    private String sortColumnType;
    private Map<String, String> searchFilter;
    private Map<String, Object> extraFilter;
    private Long currentUserEmpId;
    private String currentUserType;
    
    private String searchKeyword;
    private List<Long> filterIdList;

}

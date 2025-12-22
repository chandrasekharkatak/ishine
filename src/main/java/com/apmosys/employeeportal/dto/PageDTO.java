package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class PageDTO {

    private int page;
    private int size;
    private String sortDirection;
    private String sortColumn;
    private String sortColumnType;
    private String searchKeyword;
    private List<Long> filterIdList;

}

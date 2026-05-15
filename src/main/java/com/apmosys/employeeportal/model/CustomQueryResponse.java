package com.apmosys.employeeportal.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomQueryResponse {

    private Long queryId;
    private String queryName;
    private String querySql;
    private String description;
    private int status;
    private Long createdBy;

    private List<Long> roleIds;
}

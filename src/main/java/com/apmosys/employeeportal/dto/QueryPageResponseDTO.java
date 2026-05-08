package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class QueryPageResponseDTO {
    private List<String> headers;
    private List<List<Object>> rows;
    private long totalElements;
    private int page;
    private int size;
}


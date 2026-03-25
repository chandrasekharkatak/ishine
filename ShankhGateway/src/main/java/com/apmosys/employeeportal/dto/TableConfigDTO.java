package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class TableConfigDTO {
	
	private List<TableColumnDTO> columns;
    private int rows;

}

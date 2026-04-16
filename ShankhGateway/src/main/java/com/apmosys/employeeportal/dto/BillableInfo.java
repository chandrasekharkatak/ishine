package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BillableInfo {
    String billableType;
    String billable;
}

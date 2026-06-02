package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChartsCountDTO {
    private String monthName;
    private Integer year;
    private Long resignCount;
    private Long apprenticeCount;
    private Long consultantCount;
    private Long regularCount;
    private Long apmosysProductCount;
    private Long apmosysProductConsultantCount;
    
	
}

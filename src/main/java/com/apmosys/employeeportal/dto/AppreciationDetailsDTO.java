package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppreciationDetailsDTO {

    private String appreciateType;
    private LocalDateTime appreciationDate;
    private String appreciationByName;
    private String fromDate;
    private String toDate;

}

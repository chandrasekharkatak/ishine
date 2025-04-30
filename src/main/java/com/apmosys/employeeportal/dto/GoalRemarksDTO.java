package com.apmosys.employeeportal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class GoalRemarksDTO {
    private Long id;
    private Long remarkBy;
    private String remarkByName;
    private String date;
    private String remarkText;
    
    // Getters and setters
}
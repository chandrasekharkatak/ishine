package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class FilterProjectInsightDTO {

    private List<String> tag;

    private List<String> techStack;

    private List<String> deliveryMode;
    
    private List<String> client;

    private LocalDate createdAt;
}
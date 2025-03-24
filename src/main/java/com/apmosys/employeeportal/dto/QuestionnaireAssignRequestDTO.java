package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionnaireAssignRequestDTO {
    private Long empId;
    private Long templateId;
    private Long quarterId;
}

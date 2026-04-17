package com.apmosys.employeeportal.mongodb.dto;

import java.util.List;
import java.util.Map;

import com.apmosys.employeeportal.model.Question;

import lombok.Data;

@Data
public class FormDataDTO {

    private Map<String, Object> fields;
    private List<ProjectInsightQuestionDTO> questions;
    private List<FormDataDTO> child;
}

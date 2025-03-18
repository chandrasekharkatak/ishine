package com.apmosys.employeeportal.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireDTO {
    private Long questionId;
    private String questionTitle;
    private String questionDescription;
    private String createdBy;
    private List<QuestionDTO> questions;
    private Long quarterId;
}
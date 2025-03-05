package com.apmosys.employeeportal.dto;

import lombok.*;
import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireDTO {
    private Long questionId;
    private String questionTitle;
    private String questionDescription;
    private String createdBy;
    private Long quarterId;
}




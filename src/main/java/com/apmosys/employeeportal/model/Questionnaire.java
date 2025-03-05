package com.apmosys.employeeportal.model;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "questionnaire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Questionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    private String questionTitle;
    
    @Column(name = "question_description")
    private String questionDescription;

    private String createdBy;
    private Long quarterId;
}

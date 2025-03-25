package com.apmosys.employeeportal.model;

import lombok.*;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    private String createdBy;
    
    private Long quarterId;
    
    private String quarter;
    
    private String department;
    
    private Long departmentId;
    
    
    

    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuestionnaire(this);
    }
    private String departmentName;
   
    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setQuestionnaire(null);
    }
}
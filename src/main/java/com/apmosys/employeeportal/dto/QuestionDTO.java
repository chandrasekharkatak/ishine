package com.apmosys.employeeportal.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;
    private String questionText;
    private Float response;
    // Add other fields as needed
}
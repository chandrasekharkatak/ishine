package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class TrainingIdResponsePassStatusDTO {

    private Integer trainingId;
    private String passStatus;
    private Long quizId;
    private Integer marksObtained;

    public TrainingIdResponsePassStatusDTO(Integer trainingId, String passStatus, Long quizId, Integer marksObtained) {
        this.trainingId = trainingId;
        this.passStatus = passStatus;
        this.quizId = quizId;
        this.marksObtained = marksObtained;
    }
    
}

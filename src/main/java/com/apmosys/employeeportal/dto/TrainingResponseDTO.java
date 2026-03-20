package com.apmosys.employeeportal.dto;

import java.util.Date;

import lombok.Data;

@Data
public class TrainingResponseDTO {
    
    private String empId;
    private Integer trainingId;
    private String trainingName;
    private Integer lastCompletedCycleNumber;
    private String empName;
    private String consentGiven;
    private Date lastCompletedOn;

    public TrainingResponseDTO(String empId, Integer trainingId,String trainingName, Integer lastCompletedCycleNumber, String empName, String consentGiven, Date lastCompletedOn) {
        this.empId = empId;
        this.trainingId = trainingId;
        this.trainingName = trainingName;
        this.lastCompletedCycleNumber = lastCompletedCycleNumber;
        this.empName = empName;
        this.consentGiven = consentGiven;
        this.lastCompletedOn = lastCompletedOn;
    }

}

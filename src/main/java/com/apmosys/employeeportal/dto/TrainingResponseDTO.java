package com.apmosys.employeeportal.dto;

import java.util.Date;

import lombok.Data;

@Data
public class TrainingResponseDTO {
    
    private Integer trainingId;
    private Integer lastCompletedCycleNumber;
    private String empName;
    private Date lastCompletedOn;

    public TrainingResponseDTO(Integer trainingId, Integer lastCompletedCycleNumber, String empName, Date lastCompletedOn) {
        this.trainingId = trainingId;
        this.lastCompletedCycleNumber = lastCompletedCycleNumber;
        this.empName = empName;
        this.lastCompletedOn = lastCompletedOn;
    }

}

package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class GetActiveProjectDetailsIfMultipleDTO {

    private Long empId;
    private Integer projectId;
    private String projectName;

    public GetActiveProjectDetailsIfMultipleDTO(Integer projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }

    public GetActiveProjectDetailsIfMultipleDTO(Long empId, Integer projectId, String projectName) {
        this.empId = empId;
        this.projectId = projectId;
        this.projectName = projectName;
    }

}

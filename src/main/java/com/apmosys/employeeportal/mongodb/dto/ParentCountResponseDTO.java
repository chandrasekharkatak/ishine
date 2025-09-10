package com.apmosys.employeeportal.mongodb.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentCountResponseDTO {

    private List<ParentCountDTO> results;
    private long totalProjects;

}

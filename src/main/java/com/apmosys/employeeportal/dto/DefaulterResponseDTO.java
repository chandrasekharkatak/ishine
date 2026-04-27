package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DefaulterResponseDTO {

    private Boolean isDefaulter;

    private List<Map<String, Integer>> months;

    private String heading;

    private String description;
}
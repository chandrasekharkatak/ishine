package com.apmosys.employeeportal.mongodb.dto;

import java.util.List;

import com.apmosys.employeeportal.dto.OptionDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeHubSearchResultObjectField {

    private String type;
    private String label;
    private String name;
    private Object value;
    // private List<String> options;
    private String description;
    private String questionOptions;
}

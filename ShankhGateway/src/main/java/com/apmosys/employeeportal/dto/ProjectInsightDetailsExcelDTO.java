package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectInsightDetailsExcelDTO {

    private String section;
    private String title;
    private String optionType;
    private Object option;
    private Object value;
    private boolean required;
    private boolean isMultiSelect;
    private int fieldWidth;
    private TableConfigDTO tableConfig;

    public ProjectInsightDetailsExcelDTO(String section, String title, String optionType, Object option, Object value,
            boolean required) {
        this.section = section;
        this.title = title;
        this.optionType = optionType;
        this.option = option;
        this.required = required;
        this.value = value;
    }

}

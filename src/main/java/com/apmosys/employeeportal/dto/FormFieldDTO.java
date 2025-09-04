package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class FormFieldDTO {
    private String id;
    private String type;
    private String label;
    private String name;
    private boolean required;
    private String placeholder;
    private String defaultValue;
    private String optionSource;
    private List<OptionDTO> options;
    private int width;
    private int index;
    private int height;
    private int tempCol;
    private int rowPosition;
    private boolean multiple;
    private String apiUrl;
    private String apiLabelKe;
    private String apiValueKey;
    private String parentField;
    private String dependentApiUrl;
    private String dependentLabelKey;
    private String dependentValueKey;
    private String dependentParamName;
    private String parentDynamicId;
    private String hierarchyType;
    private String apiLabelKey;
    private TableConfigDTO tableConfig;
    private String isDynamicallyCreated;
}

package com.apmosys.employeeportal.dto;

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
    private int width;
    private int rowPosition;
}

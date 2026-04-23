package com.apmosys.employeeportal.mongodb.modal;

import java.util.List;
import java.util.Map;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.apmosys.employeeportal.dto.FormFieldDTO;

import lombok.Data;

@Document(collection = "dynamic_form_structure")
@Data
public class DynamicFormStructure {
	
	@Id
    private String id;
    private String formName;
    private Long departmentId;
    private String parentFormId;
    
    @Transient
    private String departmentName;
    private List<FormFieldDTO> fields;
    
    @Transient
    private List<DynamicFormStructure> children;
}

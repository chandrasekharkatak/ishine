package com.apmosys.employeeportal.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.DynamicFormStructureDTO;
import com.apmosys.employeeportal.mongodb.modal.DynamicFormStructure;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public interface FormBuilderService {

	ResponseEntity<ServiceResponse> createDynamicForm(DynamicFormStructureDTO dynamicFormStructureDTO);

	ResponseEntity<ServiceResponse> updateDynamicForm(String id, DynamicFormStructureDTO dynamicFormStructureDTO);

	ResponseEntity<List<DynamicFormStructure>> getAllDynamicForm();

	ResponseEntity<DynamicFormStructure> getByDynamicFormById(String id);

	ResponseEntity<ServiceResponse> deleteFormById(String id);

}

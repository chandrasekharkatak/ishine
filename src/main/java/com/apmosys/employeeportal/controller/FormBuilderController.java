package com.apmosys.employeeportal.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.DynamicFormStructureDTO;
import com.apmosys.employeeportal.mongodb.modal.DynamicFormStructure;
import com.apmosys.employeeportal.service.FormBuilderService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/form")
public class FormBuilderController {
	
	FormBuilderService formBuilderService;
	
	FormBuilderController(FormBuilderService formBuilderService){
		this.formBuilderService = formBuilderService;
	}
	
	@RequestMapping(value = "/createDynamicForm", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> createDynamicForm(@RequestBody DynamicFormStructureDTO dynamicFormStructureDTO){
		return formBuilderService.createDynamicForm(dynamicFormStructureDTO);
	}
	
	@PutMapping("/updateDynamicForm/{id}")
	public ResponseEntity<ServiceResponse> updateDynamicForm(@PathVariable String id, @RequestBody DynamicFormStructureDTO dynamicFormStructureDTO) {
		return formBuilderService.updateDynamicForm(id, dynamicFormStructureDTO);
	}

	@GetMapping("/getAllDynamicForm")
	public ResponseEntity<List<DynamicFormStructure>> getAllForms() {
		return formBuilderService.getAllDynamicForm();
	}

	@GetMapping("/getByDynamicFormById/{id}")
	public ResponseEntity<DynamicFormStructure> getFormById(@PathVariable String id) {
		return formBuilderService.getByDynamicFormById(id);
	}
	
	@GetMapping("/deleteFormById/{id}")
	public ResponseEntity<ServiceResponse> deleteFormById(@PathVariable String id) {
		return formBuilderService.deleteFormById(id);
	}
	
	@PostMapping("/getAllDynamicFormByDepartmentAndType")
	public ResponseEntity<List<DynamicFormStructure>> getAllDynamicFormByDepartmentAndType(@RequestBody DynamicFormStructureDTO dynamicFormStructureDTO) {
		return formBuilderService.getAllDynamicFormByDepartmentAndType(dynamicFormStructureDTO);
	}
	

}

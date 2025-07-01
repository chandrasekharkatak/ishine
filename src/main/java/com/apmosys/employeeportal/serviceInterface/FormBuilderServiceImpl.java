package com.apmosys.employeeportal.serviceInterface;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.Exception.EmployeeNotFoundException;
import com.apmosys.employeeportal.dto.DynamicFormStructureDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.mongodb.modal.DynamicFormStructure;
import com.apmosys.employeeportal.mongodb.repository.DynamicFormStructureRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.service.FormBuilderService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class FormBuilderServiceImpl implements FormBuilderService {
	
	private final DynamicFormStructureRepository dynamicFormStructureRepository;
	private final DepartmentRepository departmentRepository;
	
	@Autowired
	public FormBuilderServiceImpl(DynamicFormStructureRepository dynamicFormStructureRepository,
			DepartmentRepository departmentRepository){
		this.dynamicFormStructureRepository = dynamicFormStructureRepository;
		this.departmentRepository = departmentRepository;
	}

	@Override
	public ResponseEntity<ServiceResponse> createDynamicForm(DynamicFormStructureDTO dynamicFormStructureDTO) {
	    ServiceResponse response = new ServiceResponse();
		try {
	        DynamicFormStructure entity = new DynamicFormStructure();
	        entity.setFormName(dynamicFormStructureDTO.getFormName());
	        entity.setDepartmentId(dynamicFormStructureDTO.getDepartmentId());
	        entity.setParentFormId(dynamicFormStructureDTO.getParentFormId());
	        entity.setFields(dynamicFormStructureDTO.getFields());
	        
	        DynamicFormStructure dbResponse = dynamicFormStructureRepository.save(entity);
	        
	        response.setServiceMessage("Form saved successfully with ID: " + dbResponse.getId());
	        return ResponseEntity.status(200).body(response);
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceMessage("Failed to save form: " + e.getMessage());
	        return ResponseEntity.status(500).body(response);
	    }
	}

	@Override
	public ResponseEntity<ServiceResponse> updateDynamicForm(String id, DynamicFormStructureDTO dynamicFormStructureDTO) {
		ServiceResponse response = new ServiceResponse();
		Optional<DynamicFormStructure> optional = dynamicFormStructureRepository.findById(id);
        if (optional.isPresent()) {
            DynamicFormStructure form = optional.get();
            form.setFormName(dynamicFormStructureDTO.getFormName());
            form.setDepartmentId(dynamicFormStructureDTO.getDepartmentId());
            form.setParentFormId(dynamicFormStructureDTO.getParentFormId());
            form.setFields(dynamicFormStructureDTO.getFields());
            
            DynamicFormStructure dbResponse = dynamicFormStructureRepository.save(form);
            
            if(dbResponse != null) {
            	response.setServiceMessage("Form updated successfully");
            	return ResponseEntity.ok(response);
            }else {
            	response.setServiceMessage("Unable to save Form");
            	return ResponseEntity.status(404).body(response);
            }
        } else {
        	response.setServiceMessage("Form not found");
            return ResponseEntity.status(404).body(response);
        }
	}

	@Override
	public ResponseEntity<List<DynamicFormStructure>> getAllDynamicForm() {
		 List<DynamicFormStructure> forms = dynamicFormStructureRepository.findAll();
		 
		 if(!forms.isEmpty()) {
			 forms.forEach((object) -> {
				 Department deptObj = departmentRepository.getById(object.getDepartmentId());
				 if(deptObj != null) {
					 object.setDepartmentName(deptObj.getName());
				 }
			 });
		 }
		 
	     return ResponseEntity.ok(forms);
	}

	@Override
	public ResponseEntity<DynamicFormStructure> getByDynamicFormById(String id) {
		Optional<DynamicFormStructure> form = dynamicFormStructureRepository.findById(id);
        return form.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.status(404).body(null));
	}

	@Override
	public ResponseEntity<ServiceResponse> deleteFormById(String id) {
		ServiceResponse response = new ServiceResponse();
		Optional<DynamicFormStructure> form = dynamicFormStructureRepository.findById(id);
		if(!form.isEmpty()) {
			this.dynamicFormStructureRepository.deleteById(id);
		}else {
			response.setServiceMessage("Unable to find Form");
			return ResponseEntity.status(404).body(response);
		}
		response.setServiceMessage("Deleted Successfully !!");
		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<List<DynamicFormStructure>> getAllDynamicFormByDepartmentAndType(
	        DynamicFormStructureDTO dynamicFormStructureDTO) {

	    Long departmentId = dynamicFormStructureDTO.getDepartmentId();

	    List<DynamicFormStructure> forms = dynamicFormStructureRepository
	            .findByDepartmentIdAndParentFormIdIsNull(departmentId);

	    return ResponseEntity.ok(forms);
	}



}

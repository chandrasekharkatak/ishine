package com.apmosys.employeeportal.mongodb.repository;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.apmosys.employeeportal.mongodb.modal.DynamicFormStructure;

public interface DynamicFormStructureRepository extends MongoRepository<DynamicFormStructure, String> {

	List<DynamicFormStructure> findByDepartmentIdAndParentFormIdIsNull(Long departmentId);

}

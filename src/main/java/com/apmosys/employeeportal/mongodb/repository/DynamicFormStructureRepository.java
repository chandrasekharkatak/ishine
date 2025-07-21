package com.apmosys.employeeportal.mongodb.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.mongodb.modal.DynamicFormStructure;

@Repository
public interface DynamicFormStructureRepository extends MongoRepository<DynamicFormStructure, String> {

	List<DynamicFormStructure> findByDepartmentIdAndParentFormIdIsNull(Long departmentId);
	
	Optional<DynamicFormStructure> findFirstByParentFormId(String parentFormId);
	
	List<DynamicFormStructure> findAllByParentFormId(String parentFormId);

}

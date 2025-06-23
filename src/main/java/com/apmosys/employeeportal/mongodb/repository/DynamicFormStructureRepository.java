package com.apmosys.employeeportal.mongodb.repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.apmosys.employeeportal.mongodb.modal.DynamicFormStructure;

public interface DynamicFormStructureRepository extends MongoRepository<DynamicFormStructure, String> {

}

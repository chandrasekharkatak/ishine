package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.UserTypeCrudMapping;


public interface UserTypeCrudMappingRepository  {

	public List<UserTypeCrudMapping> findAllByUserTypeId(Long userTypeId);

	

}

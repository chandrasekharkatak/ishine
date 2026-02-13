package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.PoRequirementMapping;
import com.apmosys.employeeportal.model.RoleDetails;


@Repository
public interface RoleDetailsRepository extends JpaRepository<RoleDetails, Long>  {

	Optional<RoleDetails> findByRoleAndDepartmentAndExperience(String role, String department,
			String experience);

}

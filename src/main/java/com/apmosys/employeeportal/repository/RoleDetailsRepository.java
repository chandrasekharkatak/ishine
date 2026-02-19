package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.PoRequirementMapping;
import com.apmosys.employeeportal.model.RoleDetails;


@Repository
public interface RoleDetailsRepository extends JpaRepository<RoleDetails, Long>  {

	
	@Query(value = "SELECT r FROM RoleDetails r WHERE LOWER(TRIM(r.role)) = LOWER(TRIM(:role)) AND LOWER(TRIM(r.department)) = LOWER(TRIM(:department)) AND LOWER(TRIM(r.experience)) = LOWER(TRIM(:experience))")
	Optional<RoleDetails> findByRoleAndDepartmentAndExperience(String role, String department,
			String experience);

}

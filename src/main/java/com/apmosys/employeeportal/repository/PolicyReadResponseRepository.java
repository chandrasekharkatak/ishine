package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.PolicyReadResponse;

public interface PolicyReadResponseRepository  extends JpaRepository<PolicyReadResponse, Long>{

	@Query(nativeQuery = true)
	List<Object[]> getPolicyAllResponsesByPolicyId(Long policyID);

	@Query(nativeQuery = true)
	List<Object[]> getReadPoliciesByEmpId(Long empId);

	long countByEmpId(Long empId);

}

package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementRolePolicy;

public interface ReimbursementRolePolicyRepository extends JpaRepository<ReimbursementRolePolicy, Long> {

	List<ReimbursementRolePolicy> findByExpensePolicyIdOrderByPolicyCategoryAscItemNameAsc(Long expensePolicyId);

	void deleteByExpensePolicyId(Long expensePolicyId);

	@Deprecated
	List<ReimbursementRolePolicy> findByJobRoleIdOrderByPolicyCategoryAscItemNameAsc(Long jobRoleId);

	@Deprecated
	void deleteByJobRoleId(Long jobRoleId);

	@Deprecated
	List<ReimbursementRolePolicy> findByJobRoleIdAndPolicyCategory(Long jobRoleId, String policyCategory);
}

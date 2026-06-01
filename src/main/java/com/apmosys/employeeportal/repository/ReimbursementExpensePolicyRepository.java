package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ReimbursementExpensePolicy;

public interface ReimbursementExpensePolicyRepository extends JpaRepository<ReimbursementExpensePolicy, Long> {

	@Query("SELECT DISTINCT p.expensePolicyId FROM ReimbursementRolePolicy p ORDER BY p.expensePolicyId ASC")
	List<Long> findDistinctExpensePolicyIdsWithRules();
}

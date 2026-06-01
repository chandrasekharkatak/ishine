package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.ReimbursementExpensePolicyJobRole;
import com.apmosys.employeeportal.model.ReimbursementExpensePolicyJobRoleId;

public interface ReimbursementExpensePolicyJobRoleRepository
		extends JpaRepository<ReimbursementExpensePolicyJobRole, ReimbursementExpensePolicyJobRoleId> {

	List<ReimbursementExpensePolicyJobRole> findByExpensePolicyId(Long expensePolicyId);

	void deleteByExpensePolicyId(Long expensePolicyId);

	Optional<ReimbursementExpensePolicyJobRole> findByJobRoleId(Long jobRoleId);

	@Query("SELECT jr.expensePolicyId FROM ReimbursementExpensePolicyJobRole jr WHERE jr.jobRoleId = :jobRoleId")
	Optional<Long> findExpensePolicyIdByJobRoleId(@Param("jobRoleId") Long jobRoleId);

	@Query("SELECT jr.jobRoleId FROM ReimbursementExpensePolicyJobRole jr WHERE jr.expensePolicyId = :expensePolicyId")
	List<Long> findJobRoleIdsByExpensePolicyId(@Param("expensePolicyId") Long expensePolicyId);

	@Query("SELECT jr FROM ReimbursementExpensePolicyJobRole jr WHERE jr.jobRoleId IN :jobRoleIds "
			+ "AND jr.expensePolicyId <> :expensePolicyId")
	List<ReimbursementExpensePolicyJobRole> findConflicts(@Param("jobRoleIds") List<Long> jobRoleIds,
			@Param("expensePolicyId") Long expensePolicyId);

	@Query("SELECT jr FROM ReimbursementExpensePolicyJobRole jr WHERE jr.jobRoleId IN :jobRoleIds")
	List<ReimbursementExpensePolicyJobRole> findByJobRoleIdIn(@Param("jobRoleIds") List<Long> jobRoleIds);
}

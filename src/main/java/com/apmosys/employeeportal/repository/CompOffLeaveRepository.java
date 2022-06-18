package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.CompOffLeave;

public interface CompOffLeaveRepository extends JpaRepository<CompOffLeave, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getPendingCompOffRequestsByManagerId(Integer managerId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllCompOffRequestsByEmpId(Long empId);
}

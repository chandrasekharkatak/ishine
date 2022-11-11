package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.LeaveRevokeApplication;

public interface LeaveRevokeApplicationRepository extends JpaRepository<LeaveRevokeApplication, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getRevokeLeaveApplicationByEmpId(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllMyTeamLeaveRevokeApplicationsByEmpId(Long empId);

}

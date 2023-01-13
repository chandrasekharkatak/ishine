package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.LeaveTypeMaster;

public interface LeaveTypeMasterRepository extends JpaRepository<LeaveTypeMaster, Short> {

	@Query(nativeQuery = true)
	public List<Object[]> getAllLeaveTypesByLeavePolicies(String employmentStatus, String gender);

	public LeaveTypeMaster findByLeaveTypeCode(String leaveTypeCode);

	public Long countByLeaveTypeMasterId(Short leaveTypeMasterId);

	@Query(nativeQuery = true)
	public List<Object[]> findByLeaveTypeMasterId(Short leaveTypeMasterId);
}

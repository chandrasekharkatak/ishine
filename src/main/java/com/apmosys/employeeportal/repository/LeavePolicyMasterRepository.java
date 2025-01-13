package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.LeavePolicyMaster;

@Repository
public interface LeavePolicyMasterRepository extends JpaRepository<LeavePolicyMaster, Short> {

	@Query(value = "FROM LeavePolicyMaster lmp WHERE lmp.employmentStatus = :employmentStatus AND leaveTypeMasterId = :leaveTypeMasterId")
	public Optional<LeavePolicyMaster> findByEmployentStatusAndLeaveTypeMasterId(String employmentStatus,
			Short leaveTypeMasterId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllLeavePolicies();

	public List<LeavePolicyMaster> findByLeaveTypeMasterId(short leaveTypeMasterId);

	public Long countByLeaveTypeMasterId(Short leaveTypeMasterId);

	public LeavePolicyMaster findByLeaveTypeMasterIdAndEmploymentStatus(Short leaveTypeMasterId, String employmentStatus);

	@Query(nativeQuery = true)
	public LeavePolicyMaster findLeavePolicyByMaternityType(String maternityType);

	@Query(value = "FROM LeavePolicyMaster lmp WHERE lmp.employmentStatus = :employmentStatus AND leaveTypeMasterId = :leaveTypeMasterId AND lmp.maternityType = :maternityType")
	public Optional<LeavePolicyMaster> findByEmployentStatusAndLeaveTypeMasterIdAndMaternityType(
			String employmentStatus, Short leaveTypeMasterId, String maternityType);

}
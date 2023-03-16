package com.apmosys.employeeportal.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.CompOffLeave;

public interface CompOffLeaveRepository extends JpaRepository<CompOffLeave, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getPendingCompOffRequestsByManagerId(Integer managerId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllCompOffRequestsByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public Long countPendingCompOffRequestsByManagerId(Integer managerId);

	public List<CompOffLeave> findByLeaveTypeMasterId(Short oldLeaveTypeMasterId);

	public List<CompOffLeave> findByEmpIdAndLeaveStatusIdAndCreatedOnAfterAndCompOffStatus(Long empId, short s,
			Timestamp perv45Day, String compOffStatus);

	public List<CompOffLeave> findByLeaveId(Long leaveId);

	@Query(nativeQuery = true)
	public CompOffLeave findOldestCompOffApplicationByEmpId(Long empId, String compOffStatus);

	@Query(nativeQuery = true)
	public List<CompOffLeave> findAllPendingApplicationByEmpId(Long empId, Timestamp perv45Day, String compOffStatus);
	
}

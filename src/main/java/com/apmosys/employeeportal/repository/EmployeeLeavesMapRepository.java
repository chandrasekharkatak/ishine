package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.projections.Leave;

public interface EmployeeLeavesMapRepository extends JpaRepository<EmployeeLeavesMap, Long> {
	
	
	public List<EmployeeLeavesMap> findAllByEmpId(Long empId);

	public EmployeeLeavesMap findByEmpIdAndLeaveTypeMasterId(Long empId, Short leaveTypeMasterId);

	
	@Query(nativeQuery = true)
	public List<Object[]> getMyLeaveBalancesByEmpId(Long empId);

	public List<EmployeeLeavesMap> findByLeaveTypeMasterId(short leaveTypeMasterId);
	
	public Long countByLeaveTypeMasterId(Short leaveTypeMasterId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllLeaveBalancesByEmpId(Long empId,Short leaveTypeMasterId);

}

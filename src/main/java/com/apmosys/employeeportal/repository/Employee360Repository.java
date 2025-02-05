package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.EmployeeLeave;

@Repository
public interface Employee360Repository extends JpaRepository<EmployeeLeave, Long> {
	
	@Query(nativeQuery = true)
	public List<Object[]> getLeaveDataPerMonthByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getLeaveDataPerMonthByEmpIdFromTo(Long empId,String startDate,String endDate);
	
	

}

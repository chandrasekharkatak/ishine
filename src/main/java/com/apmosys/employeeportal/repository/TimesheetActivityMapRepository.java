package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.model.TimesheetActivityMap;

public interface TimesheetActivityMapRepository extends JpaRepository<TimesheetActivityMap, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> activitiesByTimesheetId(Long timesheetId);
	
	@Transactional
	public void deleteByTimesheetId(Long timesheetId);

}

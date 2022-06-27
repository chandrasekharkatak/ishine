package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.DepartmentHolidayMap;

@Repository
public interface DepartmentHolidayMapRepository extends JpaRepository<DepartmentHolidayMap, Long> {	
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllHolidayListByDeptId(Long deptId);

}

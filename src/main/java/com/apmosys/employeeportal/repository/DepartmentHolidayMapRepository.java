package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.model.DepartmentHolidayMap;

@Repository
public interface DepartmentHolidayMapRepository extends JpaRepository<DepartmentHolidayMap, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getHolidayListByDeptId(Long deptId);

	@Transactional
	public Integer deleteByHolidayId(Short holidayId);

}

package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.DesignationDepartmentMap;

public interface DesignationDepartmentMapRepository extends JpaRepository<DesignationDepartmentMap, Long> {

	List<DesignationDepartmentMap> findByDesignationId(Long designationId);

	DesignationDepartmentMap findByDesignationIdAndDeptId(Long designationId, Long id);

	@Query(nativeQuery = true)
	List<Object[]> findAllDesignationByDeptId(Long deptId);

	@Query(nativeQuery = true)
	List<Object[]> getFilteredDesignation(Set<Long> deptIds, int deptIdsLength);

	List<DesignationDepartmentMap> findByDeptId(Long oldDeptId);

}

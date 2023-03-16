package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectDepartmentMap;

public interface ProjectDepartmentMapRepository extends JpaRepository<ProjectDepartmentMap, Integer> {

	public ProjectDepartmentMap findByProjectIdAndDeptId(Integer projectId, Long deptId);

	public List<ProjectDepartmentMap> findByDeptIdNotInAndProjectId(List<Long> departmentId, Integer projectId);

	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentByProjectId(int projectId);

	public List<ProjectDepartmentMap> findByProjectId(Integer projectId);

}

package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ProjectHierarchyMapping;

public interface ProjectHierarchyMappingRepository extends JpaRepository<ProjectHierarchyMapping, Long> {
	List<ProjectHierarchyMapping> findByParentProjectIdAndChildProjectIdIn(
			Integer parentProjectId,
			List<Integer> childProjectIds
	);

	Optional<ProjectHierarchyMapping> findByChildProjectIdAndActiveTrue(Integer childProjectId);
}

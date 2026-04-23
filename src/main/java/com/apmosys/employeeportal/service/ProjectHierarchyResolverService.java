package com.apmosys.employeeportal.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectHierarchyMapping;
import com.apmosys.employeeportal.repository.ProjectHierarchyMappingRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;

@Service
public class ProjectHierarchyResolverService {

	@Autowired
	private ProjectHierarchyMappingRepository projectHierarchyMappingRepository;

	@Autowired
	private ProjectRepository projectRepository;

	public Integer resolveToPrimaryProjectId(Integer projectId) {
		if (projectId == null) {
			return null;
		}

		Integer current = projectId;
		Set<Integer> seen = new HashSet<>();

		while (current != null) {
			if (!seen.add(current)) {
				// cycle detected
				return projectId;
			}

			Optional<ProjectHierarchyMapping> m = projectHierarchyMappingRepository
					.findByChildProjectIdAndActiveTrue(current);

			if (!m.isPresent()) {
				return current;
			}

			Integer parent = m.get().getParentProjectId();
			if (parent == null) {
				return current;
			}
			current = parent;
		}

		return projectId;
	}

	public String resolveProjectViewId(String projectViewId) {
		if (projectViewId == null || projectViewId.trim().isEmpty()) {
			return projectViewId;
		}

		String trimmed = projectViewId.trim();
		if (trimmed.startsWith("po")) {
			Long poProjectId;
			try {
				poProjectId = Long.parseLong(trimmed.substring(2));
			} catch (Exception e) {
				return projectViewId;
			}

			Project p = projectRepository.findByPoProjectId(poProjectId);
			if (p == null || p.getProjectId() == null) {
				return projectViewId;
			}

			Integer resolvedProjectId = resolveToPrimaryProjectId(p.getProjectId());
			if (resolvedProjectId == null || resolvedProjectId.equals(p.getProjectId())) {
				return projectViewId;
			}

			Project resolved = projectRepository.findByProjectId(resolvedProjectId);
			if (resolved == null || resolved.getPoProjectId() == null) {
				return String.valueOf(resolvedProjectId);
			}

			return "po" + resolved.getPoProjectId();
		}

		Integer numericProjectId;
		try {
			numericProjectId = Integer.parseInt(trimmed);
		} catch (Exception e) {
			return projectViewId;
		}

		Integer resolvedProjectId = resolveToPrimaryProjectId(numericProjectId);
		if (resolvedProjectId == null) {
			return projectViewId;
		}
		return String.valueOf(resolvedProjectId);
	}

	public String resolveProjectNameFromProjectViewId(String projectViewId) {
		if (projectViewId == null || projectViewId.trim().isEmpty()) {
			return null;
		}

		String resolvedViewId = resolveProjectViewId(projectViewId);
		if (resolvedViewId == null || resolvedViewId.trim().isEmpty()) {
			return null;
		}

		String trimmed = resolvedViewId.trim();
		if (trimmed.startsWith("po")) {
			Long poProjectId;
			try {
				poProjectId = Long.parseLong(trimmed.substring(2));
			} catch (Exception e) {
				return null;
			}
			Project p = projectRepository.findByPoProjectId(poProjectId);
			return p != null ? p.getProjectName() : null;
		}

		Integer projectId;
		try {
			projectId = Integer.parseInt(trimmed);
		} catch (Exception e) {
			return null;
		}
		Project p = projectRepository.findByProjectId(projectId);
		return p != null ? p.getProjectName() : null;
	}
}


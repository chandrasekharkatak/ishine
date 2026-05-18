package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.RoleFeatureMap;
import com.apmosys.employeeportal.model.SubFeatureMaster;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.RoleFeatureMapRepository;
import com.apmosys.employeeportal.repository.SubFeatureMasterRepository;

/**
 * Authorizes Skill Matrix APIs using {@link RoleFeatureMap} / {@link SubFeatureMaster},
 * same source of truth as the portal menu (no hardcoded role names).
 */
@Service
public class SkillMatrixAuthorizationService {

	@Autowired
	private SubFeatureMasterRepository subFeatureMasterRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private RoleFeatureMapRepository roleFeatureMapRepository;

	public boolean hasSubFeature(Long empId, String subFeatureName) {
		if (empId == null || subFeatureName == null || subFeatureName.isEmpty()) {
			return false;
		}
		SubFeatureMaster sub = subFeatureMasterRepository.findBySubFeatureName(subFeatureName);
		if (sub == null) {
			return false;
		}
		Employee employee = employeeRepository.findByEmpId(empId);
		if (employee == null || employee.getJobRoleId() == null) {
			return false;
		}
		RoleFeatureMap map = roleFeatureMapRepository.findByJobRoleIdAndSubFeatureMasterId(
				employee.getJobRoleId(), sub.getSubFeatureMasterId());
		return map != null;
	}
}

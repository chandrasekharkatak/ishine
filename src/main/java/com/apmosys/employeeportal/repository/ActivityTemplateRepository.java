package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ActivityTemplate;

public interface ActivityTemplateRepository extends JpaRepository<ActivityTemplate, Long> {

	List<ActivityTemplate> getByDeptIdAndEmployeeRole(Long deptId, String employeeRole);

	List<ActivityTemplate> getByDeptId(Long deptId);

	List<ActivityTemplate> findByActivityTemplateIdNotInAndDeptIdAndEmployeeRole(List<Long> activityId, Long deptId,
			String employeeRole);

}

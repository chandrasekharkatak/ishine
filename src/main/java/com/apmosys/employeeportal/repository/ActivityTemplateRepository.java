package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ActivityTemplate;

public interface ActivityTemplateRepository extends JpaRepository<ActivityTemplate, Long> {

	List<ActivityTemplate> getByDeptIdAndEmployeeRole(Long deptId, String employeeRole);


	List<ActivityTemplate> findByActivityTemplateIdNotInAndDeptIdAndEmployeeRole(List<Long> activityId, Long deptId,
			String employeeRole);

	List<ActivityTemplate> getByDeptIdAndEmployeeRoleIn(Long deptId, String[] employeeRole);

	ActivityTemplate findByTemplateActivityAndDeptIdAndEmployeeRole(String activity, Long deptId, String employeeRole);

	List<ActivityTemplate> getByDeptId(Long deptId);
	
	@Query(nativeQuery=true)
	List<ActivityTemplate> getByDeptIdAndEmployeeRoleType(Long deptId, String employeeRole);
}

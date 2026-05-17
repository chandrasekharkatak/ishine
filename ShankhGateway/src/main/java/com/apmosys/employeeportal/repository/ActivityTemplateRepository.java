package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Set;

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
	
	@Query(value="SELECT at FROM ActivityTemplate at WHERE at.deptId=:deptId AND at.employeeRole =:employeeRole")
	List<ActivityTemplate> getByDeptIdAndEmployeeRoleType(Long deptId, String employeeRole);

	@Query(value="SELECT at FROM ActivityTemplate at WHERE at.deptId IN :deptIds AND at.employeeRole IN :employeeRoles ")
	List<ActivityTemplate> getByDeptIdAndEmployeeRoleTypeIn(Set<Long> deptIds, Set<String> employeeRoles);


    List<ActivityTemplate> getByDeptIdIn(List<Long> deptIds);

}

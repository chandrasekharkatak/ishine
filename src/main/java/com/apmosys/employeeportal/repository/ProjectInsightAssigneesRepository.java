package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectInsightAssignees;

public interface ProjectInsightAssigneesRepository extends JpaRepository<ProjectInsightAssignees, Long> {

	List<ProjectInsightAssignees> getByEntityIdAndEntityType(Long milestoneId, String string);

	@Query(value="Select distinct e.name from ProjectInsightAssignees pia inner join Employee e on e.empId=pia.assignedTo where pia.entityId=:entityId and pia.entityType=:entityType and pia.assignType != 'Tagged' ")
	List<String> getUserNameByEntityIdAndEntityType(Long entityId, String entityType);
	
	@Query(value="Select distinct e.empId from ProjectInsightAssignees pia inner join Employee e on e.empId=pia.assignedTo where pia.entityId=:entityId and pia.entityType=:entityType and pia.assignType != 'Tagged' ")
	List<Long> getAssignedToByEntityIdAndEntityType(Long entityId, String entityType);
		
	@Query(value="Select distinct e.name from ProjectInsightAssignees pia inner join Employee e on e.empId=pia.assignedTo where pia.entityId=:entityId and pia.entityType=:entityType and pia.assignType = 'Tagged' and pia.taggedBy=:taggedBy ")
	List<String> getUserNameByEntityIdAndEntityTypeAndTaggedBy(Long entityId, String entityType,Long taggedBy);

	@Query(value="Select distinct e.empId from ProjectInsightAssignees pia inner join Employee e on e.empId=pia.assignedTo where pia.entityId=:entityId and pia.entityType=:entityType and pia.assignType = 'Tagged' and pia.taggedBy=:taggedBy ")
	List<Long> getUserIdByEntityIdAndEntityTypeAndTaggedBy(Long entityId, String entityType,Long taggedBy);

	@Query(value="Select pia from ProjectInsightAssignees pia where pia.entityId=:entityId and pia.entityType=:entityType and pia.assignedTo =:assignedTo  and pia.taggedBy=:taggedBy and pia.assignType = 'Tagged' ")
	ProjectInsightAssignees getProjectInsightAssigneesByEntityIdAndEntityTypeAndAssignedToHelpTaggedBy(Long entityId, String entityType, Long assignedTo,Long taggedBy);
	

	@Query(value="Select pia from ProjectInsightAssignees pia where pia.entityId=:entityId and pia.entityType=:entityType  and pia.taggedBy=:taggedBy and  pia.assignType = 'Tagged' ")
	List<ProjectInsightAssignees> getProjectInsightAssigneesByEntityIdAndEntityTypeAndHelpTaggedBy(Long entityId, String entityType, Long taggedBy);
	
}

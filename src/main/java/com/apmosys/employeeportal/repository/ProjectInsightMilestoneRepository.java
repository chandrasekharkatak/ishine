package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectInsightMilestone;

public interface ProjectInsightMilestoneRepository extends JpaRepository<ProjectInsightMilestone, Long> {

	@Query(nativeQuery = true)
	List<Object[]> getAllProjectInsight();

	List<ProjectInsightMilestone> getByProjectId(Long projectId);

	boolean existsByProjectId(Long surveyId);

	ProjectInsightMilestone getByMilestoneId(Long milestoneId);

	@Query(value="Select pim from ProjectInsightMilestone pim "
			+ "inner join ProjectInsightAssignees pia on  pim.milestoneId = pia.entityId and pia.entityType = 'Milestone'"
			+ "where pia.assigneeId=:employeeId and pim.projectId=:projectId")
	List<ProjectInsightMilestone> findByProjectIdAndAssignedMilestone(Long projectId, Long employeeId);

	@Query(nativeQuery = true)
	List<Object[]> getAllProjectInsightByUser(Long empId);
	
	@Transactional
	@Modifying
	@Query(value = "delete from ProjectInsightMilestone pim where pim.milestoneId=:milestoneId ")
	void deleteProjectInsightMilestoneByMilestoneId(Long milestoneId);

	@Query(nativeQuery = true,value="SELECT p.project_id,p.project_name,p.project_manager_id,pme.name as 'projectManagerName',pim.created_by,e2.name as 'createdByName',pim.created_on\n"
			+ ",COUNT(*)\n"
			+ "from employee_team_mapping etm \n"
			+ "inner join teams t on t.team_id = etm.team_id \n"
			+ "inner JOIN projects p ON p.project_id=t.project_id \n"
			+ "inner JOIN project_insight_milestone pim ON pim.project_id = p.project_id\n"
			+ "inner JOIN employee pme ON pme.emp_id=p.project_manager_id \n"
			+ "inner JOIN employee e2 ON e2.emp_id=pim.created_by \n"
			+ "where etm.emp_id =:empId  and p.project_id not in(:projectIds) \n"
			+ "and (etm.employee_role like '%Employee%' or etm.employee_role like '%Manager%' or etm.employee_role like '%TeamLead%'\n"
			+ " or etm.employee_role like '%Manager%')\n"
			+ "GROUP BY p.project_id,p.project_name,p.project_manager_id,pme.name ,pim.created_by,e2.name ,pim.created_on")
	List<Object[]> getAllProjectInsightByUserIdForEmployee(Long empId,List<Long> projectIds);
	
}

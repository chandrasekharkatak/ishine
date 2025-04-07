package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectInsightResponse;

public interface ProjectInsightResponseRepository extends JpaRepository<ProjectInsightResponse, Long> {

	ProjectInsightResponse findByQuestionMasterId(Long questionMasterId);

	List<ProjectInsightResponse> findByQuestionMasterIdAndEmpId(Long questionMasterId, Long employeeId);

	List<ProjectInsightResponse> findAllByQuestionMasterId(Long questionMasterId);

	@Query(value="SELECT p.project_id,p.project_name,p.project_manager_id,pme.name as 'projectManagerName',pim.created_by,e2.name as 'createdByName',pim.created_on,pir.process_to \n"
			+ "FROM project_insight_response pir \n"
			+ "INNER JOIN employee e ON e.emp_id=pir.emp_id \n"
			+ "INNER JOIN question_master qm ON qm.question_master_id = pir.question_master_id \n"
			+ "LEFT JOIN project_insight_milestone pim ON pim.milestone_id = qm.entity_id and qm.entity_type = 'Milestone' \n"
			+ "LEFT JOIN project_insight_module pimo ON pimo.module_id = qm.entity_id and qm.entity_type = 'Module' \n"
			+ "LEFT JOIN project_insight_submodule pismo ON pismo.submodule_id = qm.entity_id and qm.entity_type = 'SubModule' \n"
			+ "INNER JOIN projects p ON p.project_id=pim.project_id \n"
			+ "INNER JOIN employee pme ON pme.emp_id=p.project_manager_id \n"
			+ "INNER JOIN employee e2 ON e2.emp_id=pim.created_by  \n"
			+ "INNER JOIN teams t ON t.project_id=p.project_id \n"
			+ "INNER JOIN employee_team_mapping etm ON etm.team_id=t.team_id \n"
			+ "WHERE pir.emp_id=:empId \n"
			+ "and p.project_id not in (select p2.project_id \n"
			+ "FROM project_insight_response pir2 \n"
			+ "INNER JOIN question_master qm2 ON qm2.question_master_id = pir2.question_master_id \n"
			+ "LEFT JOIN project_insight_milestone pim2 ON pim2.milestone_id = qm2.entity_id and qm2.entity_type = 'Milestone' \n"
			+ "LEFT JOIN project_insight_module pimo2 ON pimo2.module_id = qm2.entity_id and qm2.entity_type = 'Module' \n"
			+ "LEFT JOIN project_insight_submodule pismo2 ON pismo2.submodule_id = qm2.entity_id and qm2.entity_type = 'SubModule' \n"
			+ "INNER JOIN projects p2 ON p2.project_id=pim2.project_id \n"
			+ "Where pir2.is_draft = 'Y')"
			+ "GROUP BY p.project_id,p.project_name,p.project_manager_id,pme.name,pim.created_on,pir.process_to",nativeQuery = true)
	List<Object[]> getAllProjectInsightByUser(Long empId);

	ProjectInsightResponse findByEmpIdAndQuestionMasterId(Long empId, Long questionId);
	
	@Query(value="Select pir from ProjectInsightResponse pir  \n"
			+ "where pir.questionMasterId=:questionMasterId and  pir.empId not in  :empIdList  \n ")
	List<ProjectInsightResponse> findAllByQuestionMasterIdAndEmpIdListNotIn(Long questionMasterId,List<Long> empIdList);

}

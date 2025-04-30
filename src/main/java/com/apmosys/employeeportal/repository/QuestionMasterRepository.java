package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.QuestionMaster;

public interface QuestionMasterRepository extends JpaRepository<QuestionMaster, Long>{

	List<QuestionMaster> findByEntityIdAndEntityType(Long milestoneId, String string);

	@Query(value="Select qm from QuestionMaster qm "
			+ "inner join ProjectInsightAssignees pia on qm.entityId = pia.entityId and qm.entityType = pia.entityType "
			+ "where pia.assignedTo=:employeeId and qm.entityId=:entityId and qm.entityType=:entityType")
	List<QuestionMaster> findByEntityIdAndEntityTypeAndAssignedTo(Long entityId, String entityType,Long employeeId);
	
	QuestionMaster findByQuestionMasterId(Long questionId);

//	List<QuestionMaster> findByMilestoneId(Long milestoneId);
	
	@Transactional
	@Modifying
	@Query(value="delete from QuestionMaster qm where qm.questionMasterId=:questionMasterId")
	void deleteAllQuestionsByEntityIdAndEntityType(Long questionMasterId);
	
	@Query(value="SELECT p.project_id,p.project_name,p.project_manager_id,pme.name as 'projectManagerName',CAST(NULL AS char) as created_by,CAST(NULL AS char) as  created_by_name ,CAST(NULL AS char) as  created_on \n"
			+ "FROM question_master qm \n"
			+ "INNER JOIN projects p ON p.project_id=qm.entity_id and qm.entity_type = 'Project' \n"
			+ "INNER JOIN employee pme ON pme.emp_id=p.project_manager_id \n"
			+ "where p.project_id not in(:projectIds)\n"
			+ "GROUP BY p.project_id,p.project_name,p.project_manager_id,pme.name\n"
			+ "",nativeQuery = true)
	List<Object[]> getAllProjectInsight(List<Long> projectIds);

	@Query(value="SELECT\n"
			+ "  q.question_master_id,\n"
			+ "  q.entity_type,\n"
			+ "  CASE\n"
			+ "    WHEN q.entity_type = 'Project' THEN p.project_id\n"
			+ "    WHEN q.entity_type = 'Milestone' THEN m.project_id\n"
			+ "    WHEN q.entity_type = 'Module' THEN mil.project_id\n"
			+ "    WHEN q.entity_type = 'SubModule' THEN smil.project_id\n"
			+ "    WHEN q.entity_type = 'Sub-SubModule' THEN ssmil.project_id\n"
			+ "    ELSE NULL\n"
			+ "  END AS project_id\n"
			+ "FROM question_master q\n"
			+ "\n"
			+ "-- Project\n"
			+ "LEFT JOIN projects p\n"
			+ "  ON q.entity_type = 'Project' AND q.entity_id = p.project_id\n"
			+ "\n"
			+ "-- Milestone\n"
			+ "LEFT JOIN project_insight_milestone m\n"
			+ "  ON q.entity_type = 'Milestone' AND q.entity_id = m.milestone_id\n"
			+ "  LEFT JOIN projects mp\n"
			+ "    ON m.project_id = mp.project_id\n"
			+ "\n"
			+ "-- Module\n"
			+ "LEFT JOIN project_insight_module mo\n"
			+ "  ON q.entity_type = 'Module' AND q.entity_id = mo.module_id\n"
			+ "  LEFT JOIN project_insight_milestone mil\n"
			+ "    ON mo.milestone_id = mil.milestone_id\n"
			+ "  LEFT JOIN projects mop\n"
			+ "    ON mil.project_id = mop.project_id\n"
			+ "\n"
			+ "-- SubModule\n"
			+ "LEFT JOIN project_insight_submodule sm\n"
			+ "  ON q.entity_type = 'SubModule' AND q.entity_id = sm.submodule_id AND sm.sub_module_type = 'SubModule'\n"
			+ "  LEFT JOIN project_insight_module smm\n"
			+ "    ON sm.module_id = smm.module_id\n"
			+ "  LEFT JOIN project_insight_milestone smil\n"
			+ "    ON smm.milestone_id = smil.milestone_id\n"
			+ "  LEFT JOIN projects smp\n"
			+ "    ON smil.project_id = smp.project_id\n"
			+ "\n"
			+ "-- Sub-SubModule\n"
			+ "LEFT JOIN project_insight_submodule ssm\n"
			+ "  ON q.entity_type = 'Sub-SubModule' AND q.entity_id = ssm.submodule_id AND ssm.sub_module_type = 'Sub-SubModule'\n"
			+ "  LEFT JOIN project_insight_module ssmm\n"
			+ "    ON ssm.module_id = ssmm.module_id\n"
			+ "  LEFT JOIN project_insight_milestone ssmil\n"
			+ "    ON ssmm.milestone_id = ssmil.milestone_id\n"
			+ "  LEFT JOIN projects ssmp\n"
			+ "    ON ssmil.project_id = ssmp.project_id\n"
			+ "\n"
			+ "WHERE q.question_master_id =:entityIdOrQuestionMasterId",nativeQuery = true)
	List<Object[]> getProjectInfoByQuestionMasterId(Long entityIdOrQuestionMasterId);
}

package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectTemp;

public interface ProjectTempRepo extends JpaRepository<ProjectTemp, Integer>{

	@Query(value="select distinct p.project_temp_id, p.created_on, p.project_name, p.state, p.client_id, p.po_project_id, p.active, \n"
			+ "	p.sync_project, p.created_by, p.updated_by, p.updated_on, p.is_draft_project, p.po_end_date, p.po_no, \n"
			+ "	p.po_project_type, p.po_start_date, p.apmosysrm, p.clientrm, p.dept_id, p.is_renewable, p.status,\n"
			+ "	p.apmosys_rm_email, p.project_completion_date, p.project_status, p.client_name, \n"
			+ "	CASE\n"
			+ "		WHEN p.is_draft_project = 'true' THEN 'Pending For Approval'\n"
			+ "		WHEN p.is_draft_project = 'false' THEN 'Approved'\n"
			+ "		WHEN p.is_draft_project = 'Rejected' THEN 'Rejected'\n"
			+ "		WHEN p.is_draft_project = 'Completed' THEN 'Completed'\n"
			+ "		WHEN p.is_draft_project = 'Not started' then 'Not Started'\n"
			+ "		ELSE 'Un Mentioned Test Data'\n"
			+ "	END AS draftStatus from project_temp p where p.is_draft_project='Not Started'\n"
			+ "union\n"
			+ "select distinct p.project_id, p.created_on, p.project_name, p.state, p.client_id, p.po_project_id, p.active, \n"
			+ "	p.sync_project, p.created_by, p.updated_by, p.updated_on, p.is_draft_project, p.po_end_date, p.po_no, \n"
			+ " p.po_project_type, p.po_start_date, p.apmosysrm, p.clientrm, p.dept_id, p.is_renewable, p.status,\n"
			+ " p.apmosys_rm_email, p.project_completion_date, p.project_status, p.client_name, \n"
			+ " CASE\n"
			+ "	WHEN p.is_draft_project = 'true' THEN 'Pending For Approval'\n"
			+ "	WHEN p.is_draft_project = 'false' THEN 'Approved'\n"
			+ "	WHEN p.is_draft_project = 'Rejected' THEN 'Rejected'\n"
			+ "	WHEN p.is_draft_project = 'Completed' THEN 'Completed'\n"
			+ "	WHEN p.is_draft_project is null then 'Not Started'\n"
			+ "	ELSE 'Un Mentioned Test Data'\n"
			+ " END AS draftStatus from projects p where p.internal_project_type is not null"
			+ " and is_draft_project is null and p.project_status = 'Not Started'",nativeQuery=true)
	List<Object[]> getAllNotStartedProjectList();
	
	
	@Query(value="select * from project_temp where po_project_Id = :projectId", nativeQuery = true)
	ProjectTemp findByPoProjectId(Long projectId);
}

package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.ResourceRequirementDTO;
import com.apmosys.employeeportal.model.ResourceRequirement;
import com.apmosys.employeeportal.response.ProjectStructureResponse;

public interface ResourceRequirementRepository extends JpaRepository<ResourceRequirement, Long> {

	@Query(nativeQuery = true,value ="select  MAX(resource_overview_id) from resource_requirement where project_id = :projectId and department LIKE CONCAT('%', :departmentName, '%')")
	Long findByProjectIdAndDepartmentName(@Param("departmentName")String departmentName,@Param("projectId")Integer projectId);
	
//	@Query(nativeQuery = true, value="select distinct department from resource_requirement where project_id=:projectId ")
//	List<String> getAllDepartmentsFromProjectId(@Param("projectId") Integer projectId);
	
	@Query(value="select distinct rr.department from ResourceRequirement rr where rr.projectId=:projectId ")
	List<String> getAllDepartmentsFromProjectId(@Param("projectId") Integer projectId);
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.ResourceRequirementDTO(r.role, r.count,r.experience, r.department, r.resourceOverviewId ,r.projectId) \n"
			+ "from ResourceRequirement r  where r.projectId=:projectId")
	List<ResourceRequirementDTO> findByProjectId(@Param("projectId")Integer projectId);
	
	@Query(nativeQuery = true, value="select distinct d.name from projects p\n"
			+ "inner join project_department_map pdm on pdm.project_id = p.project_id\n"
			+ "inner join department d on pdm.dept_id = d.dept_id\n"
			+ "where p.project_id = :projectId ")
	List<String> getAllDepartmentsFromProjectIdInternal(@Param("projectId") Integer projectId);
	
	ResourceRequirement findByProjectIdAndResourceOverviewId(Integer projectId, Long resourceOverviewId);

	void deleteByProjectId(Integer projectId);
	
	Boolean existsByResourceOverviewId(Long resourceOverviewId);
	
	@Query(value= "select distinct c.client_name, p.project_name, p.po_project_type,  \n"
			+ "GROUP_CONCAT(distinct dept_abbreviation SEPARATOR ', ') dept_abbreviation, GROUP_CONCAT(distinct d.name SEPARATOR ', ') dept_name\n"
			+ "from employee e\n"
			+ "inner join employee_team_mapping etm on etm.emp_id = e.emp_id\n"
			+ "inner join teams t on t.team_id = etm.team_id\n"
			+ "inner join projects p on p.project_id = t.project_id\n"
			+ "inner join clients c on c.client_id = p.client_id \n"
			+ "inner join job_role jr on jr.job_role_id = e.job_role_id\n"
			+ "inner join project_department_map pd on pd.project_id = p.project_id\n"
			+ "inner join department d on d.dept_id = jr.dept_id\n"
			+ "where etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' AND  e.employmentstatus != 'InActive' and p.po_project_type = :type \n"
			+ "and d.name in (:deptName) \n"
			+ "group by c.client_name, p.project_name, p.po_project_type ;",nativeQuery = true)
	List<Object[]> getListProjectStructureforFilter(@Param("deptName") String[] deptName,@Param("type") String type);
	
	
	@Query(value= "select distinct c.client_name, p.project_name, p.po_project_type,  \n"
			+ "GROUP_CONCAT(distinct dept_abbreviation SEPARATOR ', ') dept_abbreviation, GROUP_CONCAT(distinct d.name SEPARATOR ', ') dept_name\n"
			+ "from employee e\n"
			+ "inner join employee_team_mapping etm on etm.emp_id = e.emp_id\n"
			+ "inner join teams t on t.team_id = etm.team_id\n"
			+ "inner join projects p on p.project_id = t.project_id\n"
			+ "inner join clients c on c.client_id = p.client_id \n"
			+ "inner join job_role jr on jr.job_role_id = e.job_role_id\n"
			+ "inner join project_department_map pd on pd.project_id = p.project_id\n"
			+ "inner join department d on d.dept_id = jr.dept_id\n"
			+ "where etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' AND  e.employmentstatus != 'InActive' and p.po_project_type = :type \n"
			+ "group by c.client_name, p.project_name, p.po_project_type having dept_name = :deptName;",nativeQuery = true)
	List<Object[]> getListProjectStructure(@Param("deptName") String deptName,@Param("type") String type);
	
	@Query(value="select distinct c.client_name, p.project_name, p.po_project_type,  \n"
			+ "GROUP_CONCAT(distinct dept_abbreviation SEPARATOR ', ') dept_abbreviation, GROUP_CONCAT(distinct d.name SEPARATOR ', ') dept_name\n"
			+ "from employee e\n"
			+ "inner join employee_team_mapping etm on etm.emp_id = e.emp_id\n"
			+ "inner join teams t on t.team_id = etm.team_id\n"
			+ "inner join projects p on p.project_id = t.project_id\n"
			+ "inner join clients c on c.client_id = p.client_id \n"
			+ "inner join job_role jr on jr.job_role_id = e.job_role_id\n"
			+ "inner join project_department_map pd on pd.project_id = p.project_id\n"
			+ "inner join department d on d.dept_id = jr.dept_id\n"
			+ "where etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' AND  e.employmentstatus != 'InActive' and p.po_project_type = :type \n"
			+ "group by c.client_name, p.project_name, p.po_project_type ",nativeQuery = true)
	List<Object[]> getAllProjectStructure(@Param("type") String type);
	
	@Query(value="select distinct c.client_name, p.project_name, p.po_project_type,  \n"
			+ "GROUP_CONCAT(distinct dept_abbreviation SEPARATOR ', ') dept_abbreviation, GROUP_CONCAT(distinct d.name SEPARATOR ', ') dept_name\n"
			+ "from employee e\n"
			+ "inner join employee_team_mapping etm on etm.emp_id = e.emp_id\n"
			+ "inner join teams t on t.team_id = etm.team_id\n"
			+ "inner join projects p on p.project_id = t.project_id\n"
			+ "inner join clients c on c.client_id = p.client_id \n"
			+ "inner join job_role jr on jr.job_role_id = e.job_role_id\n"
			+ "inner join project_department_map pd on pd.project_id = p.project_id\n"
			+ "inner join department d on d.dept_id = jr.dept_id\n"
			+ "where etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' AND  e.employmentstatus != 'InActive' \n"
			+ "group by c.client_name, p.project_name, p.po_project_type\n"
			+ "HAVING dept_name = :deptName \n ",nativeQuery = true)
	List<Object[]> getListAllProjectStructure(@Param("deptName") String deptName);
	
	@Query(value=
			"select distinct c.client_name, p.project_name, p.po_project_type,  \n"
			+ "GROUP_CONCAT(distinct dept_abbreviation SEPARATOR ', ') dept_abbreviation, GROUP_CONCAT(distinct d.name SEPARATOR ', ') dept_name\n"
			+ "from employee e\n"
			+ "inner join employee_team_mapping etm on etm.emp_id = e.emp_id\n"
			+ "inner join teams t on t.team_id = etm.team_id\n"
			+ "inner join projects p on p.project_id = t.project_id\n"
			+ "inner join clients c on c.client_id = p.client_id \n"
			+ "inner join job_role jr on jr.job_role_id = e.job_role_id\n"
			+ "inner join project_department_map pd on pd.project_id = p.project_id\n"
			+ "inner join department d on d.dept_id = jr.dept_id\n"
			+ "where etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' AND  e.employmentstatus != 'InActive' \n"
			+ "and d.name in (:deptName) \n"
			+ "group by c.client_name, p.project_name, p.po_project_type ",nativeQuery = true)
	List<Object[]> getListAllProjectStructureforFilter(@Param("deptName") String[] deptName);
	
	@Query(value="select distinct c.client_name, p.project_name, p.po_project_type,  \n"
			+ "GROUP_CONCAT(distinct dept_abbreviation SEPARATOR ', ') dept_abbreviation, GROUP_CONCAT(distinct d.name SEPARATOR ', ') dept_name\n"
			+ "from employee e\n"
			+ "inner join employee_team_mapping etm on etm.emp_id = e.emp_id\n"
			+ "inner join teams t on t.team_id = etm.team_id\n"
			+ "inner join projects p on p.project_id = t.project_id\n"
			+ "inner join clients c on c.client_id = p.client_id \n"
			+ "inner join job_role jr on jr.job_role_id = e.job_role_id\n"
			+ "inner join project_department_map pd on pd.project_id = p.project_id\n"
			+ "inner join department d on d.dept_id = jr.dept_id\n"
			+ "where etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' AND  e.employmentstatus != 'InActive' \n"
			+ "group by c.client_name, p.project_name, p.po_project_type ",nativeQuery = true)
	List<Object[]> getAllStructure();
}

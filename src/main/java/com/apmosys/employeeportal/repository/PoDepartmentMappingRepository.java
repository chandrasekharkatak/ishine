package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.ProjectPoDepartmentMapDTO;
import com.apmosys.employeeportal.model.PoDepartmentMapping;

@Repository
public interface PoDepartmentMappingRepository extends JpaRepository<PoDepartmentMapping, Long> {

	@Modifying
	@Query("DELETE FROM PoDepartmentMapping")
	void deleteAllRecords();

	@Query(value = "SELECT " +
			"p.project_id, p.client_name, p.client_location, p.state, p.project_name, " +
			"p.description, p.project_manager_id, p.emp_id, p.approved_on, p.created_on, " +
			"p.client_id, p.department_name, p.po_project_id, p.active, p.sync_project, " +
			"p.is_draft_project, p.created_by, p.updated_by, p.updated_on, p.role, " +
			"p.count, p.experience, p.po_start_date, p.po_end_date, p.po_no, " +
			"p.po_project_type, p.apmosys_rm, p.client_rm, p.is_renewable, p.dept_id, " +
			"p.status, p.apmosys_rm_email, p.project_completion_date, p.project_status, " +
			"p.internal_project_type, p.has_client_side_id, p.client_flag, po.po_id " +
			"FROM projects p " +
			"LEFT JOIN project_po_details po ON p.project_id = po.project_id", nativeQuery = true)
	List<ProjectPoDepartmentMapDTO> findAllProjectsWithPoDetails();

	public PoDepartmentMapping findByPoIdAndDeptId(Integer poId, Long deptId);

	public List<PoDepartmentMapping> findByDeptId(Long deptId);

	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentByPoId(int projectId);

	@Query(value = "SELECT pdm.* " +
			"FROM po_department_mapping pdm " +
			"INNER JOIN project_po_details ppo ON " 
			+ "((ppo.po_id IS NOT NULL AND pdm.po_id = ppo.po_id) "
			+ "OR (ppo.po_id IS NULL AND pdm.project_id = ppo.project_id)) " +
			"WHERE ppo.project_id = :projectId AND pdm.dept_id = :deptId", nativeQuery = true)
	List<PoDepartmentMapping> findByProjectIdAndDeptId(@Param("projectId") Integer projectId,
			@Param("deptId") Long deptId);

	@Query(value = "SELECT pdm.* " +
			"FROM po_department_mapping pdm " +
			"INNER JOIN project_po_details ppo ON ((ppo.po_id IS NOT NULL AND pdm.po_id = ppo.po_id) \n"
			+ "OR (ppo.po_id IS NULL AND pdm.project_id = ppo.project_id)) " +
			"WHERE ppo.project_id = :projectId", nativeQuery = true)
	List<PoDepartmentMapping> findByProjectId(@Param("projectId") Integer projectId);

	public List<PoDepartmentMapping> findByPoId(Long poId);

	@Query(value = "SELECT pdm.* " +
			"FROM po_department_mapping pdm " +
			"INNER JOIN project_po_details ppo ON ((ppo.po_id IS NOT NULL AND pdm.po_id = ppo.po_id) "+
			"OR (ppo.po_id IS NULL AND pdm.project_id = ppo.project_id)) "+
			"WHERE ppo.project_id = :projectId AND DATE(ppo.po_end_date) >= CURRENT_DATE ", nativeQuery = true)
	List<PoDepartmentMapping> findByProjectIdAndActive(@Param("projectId") Integer projectId);

	@Query(value = "SELECT Distinct pdm.dept_Id " +
			"FROM po_department_mapping pdm " +
			"INNER JOIN project_po_details ppo ON ((ppo.po_id IS NOT NULL AND pdm.po_id = ppo.po_id) " +
			"OR (ppo.po_id IS NULL AND pdm.project_id = ppo.project_id)) " +
			"WHERE ppo.project_id = :projectId AND (DATE(ppo.po_end_date) >= CURRENT_DATE or :isAllProjects = true )", nativeQuery = true)
	List<Long> findPoDeptIdsByProjectId(@Param("projectId") Integer projectId, boolean isAllProjects);

	List<PoDepartmentMapping> findByPoIdAndActiveTrue(Long poId);
	List<PoDepartmentMapping> findByProjectIdAndActiveTrue(Integer sourceProjectId);
	
	@Query(value ="SELECT DISTINCT e.email\n"
			+ "FROM PoDepartmentMapping pd\n"
			+ "JOIN Department d ON pd.deptId = d.deptId\n"
			+ "JOIN Employee e ON d.hodId = e.empId\n"
			+ "WHERE pd.poId = :poId AND pd.active = true")
	List<String> findHodEmailsByPoId(Long poId);

}

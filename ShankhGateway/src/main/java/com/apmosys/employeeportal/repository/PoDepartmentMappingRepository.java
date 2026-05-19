package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.ProjectPoDepartmentMapDTO;
import com.apmosys.employeeportal.model.PoDepartmentMapping;

@Repository
public interface PoDepartmentMappingRepository extends JpaRepository<PoDepartmentMapping, Long> {
    
	@Transactional
	@Modifying
	@Query("DELETE FROM PoDepartmentMapping")
	void deleteAllRecords();


	public PoDepartmentMapping findByPoIdAndDeptId(Integer poId, Long deptId);

	public List<PoDepartmentMapping> findByDeptId(Long deptId);

	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentByPoId(int projectId);

	@Query(value = "SELECT pdm.po_department_map_id , pdm.active , pdm.dept_id  , pdm.project_id , pdm.po_id\n"
			+ "	        FROM po_department_mapping pdm  \n"
			+ "	        INNER JOIN project_po_details ppo ON  \n"
			+ "	        ((ppo.po_id IS NOT NULL AND pdm.po_id = ppo.po_id)  \n"
			+ "	         OR (ppo.po_id IS NULL AND pdm.project_id = ppo.project_id))  \n"
			+ "	        WHERE pdm.project_id = :projectId  \n"
			+ "	        AND pdm.dept_id = :deptId",
	        nativeQuery = true)
	List<Object[]> findByProjectIdAndDeptId(@Param("projectId") Integer projectId,
	                                        @Param("deptId") Long deptId);


	@Query(value = "SELECT  pdm.dept_id , pdm.po_id , pdm.project_id ,\n"
			+ "         pdm.active \n"
			+ "			FROM po_department_mapping pdm \n"
			+ "			INNER JOIN project_po_details ppo ON ((ppo.po_id IS NOT NULL AND pdm.po_id = ppo.po_id)\n"
			+ "			OR (ppo.po_id IS NULL AND pdm.project_id = ppo.project_id))\n"
			+ "			WHERE ppo.project_id = :projectId", nativeQuery = true)
	List<Object[]> findByProjectId(@Param("projectId") Integer projectId);

	public List<PoDepartmentMapping> findByPoId(Long poId);

	@Query(value = "SELECT pdm.dept_id , pdm.po_id , pdm.project_id , pdm.active " +
			"FROM po_department_mapping pdm " +
			"INNER JOIN project_po_details ppo ON ((ppo.po_id IS NOT NULL AND pdm.po_id = ppo.po_id) "+
			"OR (ppo.po_id IS NULL AND pdm.project_id = ppo.project_id)) "+
			"WHERE ppo.project_id = :projectId AND DATE(ppo.po_end_date) >= CURRENT_DATE ", nativeQuery = true)
	List<PoDepartmentMapping> findByProjectIdAndActive(@Param("projectId") Integer projectId);

	@Query(value = "SELECT Distinct pdm.dept_Id FROM po_department_mapping pdm "
			+ "LEFT JOIN project_po_details ppo ON ((ppo.po_id IS NOT NULL AND pdm.po_id = ppo.po_id) OR (ppo.po_id IS NULL AND pdm.project_id = :projectId)) \n"
			+ "AND (DATE(ppo.po_end_date) >= CURRENT_DATE or :isAllProjects = true ) "
			+ "WHERE pdm.project_id = :projectId ", nativeQuery = true)
	List<Long> findPoDeptIdsByProjectId(@Param("projectId") Integer projectId, boolean isAllProjects);

	List<PoDepartmentMapping> findByPoIdAndActiveTrue(Long poId);
	List<PoDepartmentMapping> findByProjectIdAndActiveTrue(Integer sourceProjectId);
	
	@Query(value ="SELECT DISTINCT e.email\n"
			+ "FROM PoDepartmentMapping pd\n"
			+ "JOIN Department d ON pd.deptId = d.deptId\n"
			+ "JOIN Employee e ON d.hodId = e.empId\n"
			+ "WHERE pd.poId = :poId AND pd.active = true")
	List<String> findHodEmailsByPoId(Long poId);
	
	
	@Query("SELECT DISTINCT e.email " +
		       "FROM PoDepartmentMapping pdm " +
		       "JOIN Department d ON pdm.deptId = d.deptId " +
		       "JOIN Employee e ON d.hodId = e.empId " +
		       "WHERE pdm.projectId = :projectId " +
		       "AND pdm.active = true")
		List<String> findHodEmailsByProjectId(Integer projectId);

}

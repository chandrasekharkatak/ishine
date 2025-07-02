package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO;
import com.apmosys.employeeportal.model.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.DepartmentDTO(dept.deptId,dept.createdBy,dept.createdOn , \n"+
			"dept.name,emp1.name,emp2.name,dept.hodId, dept.updatedOn, \n"+
			"u.name, dept.deptAbbreviation , dept.updatedBy) \n"+
			"FROM Department dept \n"+
			"LEFT JOIN Employee u on dept.updatedBy = u.empId \n"+
			"INNER JOIN Employee emp1 ON dept.createdBy = emp1.empId \n"+
			"INNER JOIN Employee emp2 ON dept.hodId = emp2.empId ")
	public List<DepartmentDTO>  getAllDepartments();

	public Department findByName(String department);

	@Query(nativeQuery = true)
	public List<Object[]> getMappedDepartment(Integer projectId);

	public List<Department> findByDeptIdIn(List<Long> deptIds);

	public List<Department> findByHodId(Long hodId);

	public boolean existsByHodId(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentInfo();

	@Query(nativeQuery = true)
	public List<Object[]> getSegregatedDeptEodDefaulter(LocalDate firstOfMonth, LocalDate currentDate);

	public Department findByDeptId(Long deptId);

	
	@Query(nativeQuery = true , value = "select d.dept_id,d.hod_id,d.name from department d where d.hod_id= :empId")
	public Optional<List<Object>> getDepartmentsByHodId(Long empId);
	
	public Department findByDeptAbbreviation(String deptAbbreviation);
	
	@Query(nativeQuery = true , value = "select * from department where name = :deptname")
	public List<Department> findByDeptName(String deptname);
	
	@Query(value = "select d.deptId from Department d where d.hodId=:hodId")
	 List<Long> findDeptIdsByHodId(@Param("hodId") Long hodId);
	
	@Query(nativeQuery = true,value ="select dept_id from job_role where job_role_id = :jobRoleId")
	Long findDepartmentofCurrentuser(@Param("jobRoleId") Long jobRoleId);
	
	@Query(nativeQuery = true,value ="select name from department where dept_id =:deptId")
	String findDepartmentNameFromDeptId(@Param("deptId") Long deptId);
	
	@Query(value ="select jr.deptId from JobRole jr where jobRoleId =:jobRoleId")
	Long findDepartmentIdOfSpoc(@Param("jobRoleId") Long jobRoleId);
	
	
	 @Query(value = "SELECT \n"
	 		+ "    CASE \n"
	 		+ "        WHEN (\n"
	 		+ "				d.name in ('Admin', 'Resource Management Group', 'Director', 'Super Admin', 'Accounts', 'HR') \n"
	 		+ "					or\n"
	 		+ "                jr.employee_role in ('SuperAdmin', 'Accounts')\n"
	 		+ "			)\n"
	 		+ "        THEN all_depts.dept_ids\n"
	 		+ "        WHEN e.emp_id IN (SELECT hod_id FROM department) \n"
	 		+ "        THEN hod_depts.dept_ids\n"
	 		+ "        ELSE NULL\n"
	 		+ "    END AS dept_id\n"
	 		+ "FROM department d\n"
	 		+ "INNER JOIN job_role jr ON jr.dept_id = d.dept_id\n"
	 		+ "INNER JOIN employee e ON e.job_role_id = jr.job_role_id\n"
	 		+ "LEFT JOIN (\n"
	 		+ "    SELECT GROUP_CONCAT(DISTINCT d2.dept_id) AS dept_ids FROM department d2\n"
	 		+ ") all_depts ON TRUE\n"
	 		+ "LEFT JOIN (\n"
	 		+ "    SELECT hod_id, GROUP_CONCAT(DISTINCT d3.dept_id) AS dept_ids FROM department d3 GROUP BY hod_id\n"
	 		+ ") hod_depts ON hod_depts.hod_id = e.emp_id\n"
	 		+ "WHERE e.emp_id = :empId"
		        , nativeQuery = true)
		    String findAccessibleDeptIdsForEmp(@Param("empId") Long empId);
	 
	 @Query("SELECT new com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO(d.deptId,d.name) FROM Department d WHERE d.deptId != 1")
	 List<GetDeptIdByRoleDTO> findAllExceptId1();
	 
	 @Query("SELECT new com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO(d.deptId,d.name) " +
		       "FROM Department d " +
		       "INNER JOIN JobRole j ON j.deptId = d.deptId " +
		       "INNER JOIN Employee e ON e.jobRoleId = j.jobRoleId " +
		       "INNER JOIN EmployeeTeamMap etm ON etm.empId = e.empId " +
		       "INNER JOIN Team t ON t.teamId = etm.teamId " +
		       "INNER JOIN Project p ON p.projectId = t.projectId " +
		       "INNER JOIN ProjectManagerMapping pmm ON pmm.projectId = p.projectId " +
		       "WHERE pmm.projectManagerId = :empId AND pmm.active = 1 " +
		       "AND e.employmentstatus <> 'InActive' " +
		       "AND etm.active <> 0 AND p.active = 'true' AND t.isActive = 'Y'")
		List<GetDeptIdByRoleDTO> findDeptIdsForProjectManager(@Param("empId") Long empId);

		@Query("SELECT new com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO(d.deptId,d.name) " +
		       "FROM Department d " +
		       "INNER JOIN JobRole j ON j.deptId = d.deptId " +
		       "INNER JOIN Employee e ON e.jobRoleId = j.jobRoleId " +
		       "INNER JOIN EmployeeTeamMap etm ON etm.empId = e.empId " +
		       "INNER JOIN Team t ON t.teamId = etm.teamId " +
		       "INNER JOIN Project p ON p.projectId = t.projectId " +
		       "INNER JOIN ProjectOverheadMapping pom ON pom.projectId = p.projectId " +
		       "WHERE pom.projectOverheadId = :empId AND pom.active = 1 " +
		       "AND e.employmentstatus <> 'InActive' " +
		       "AND etm.active <> 0 AND p.active = 'true' AND t.isActive = 'Y'")
		List<GetDeptIdByRoleDTO> findDeptIdsForProjectOverhead(@Param("empId") Long empId);

		@Query("SELECT t.deptIds FROM Team t WHERE t.teamLeadId = :empId")
		String findDeptIdsForTeamLead(@Param("empId") Long empId);

		@Query("SELECT t.deptIds FROM Team t WHERE t.spocId = :empId")
		String findDeptIdsForSpoc(@Param("empId") Long empId);
		
		@Query("SELECT new com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO(d.deptId, d.name) FROM Department d WHERE d.deptId IN :deptIds")
		List<GetDeptIdByRoleDTO> findDepartmentsByIds(@Param("deptIds") List<Long> deptIds);

		@Query(value = "select new com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO(d.deptId, d.name) from Department d where d.hodId=:hodId")
		List<GetDeptIdByRoleDTO> findDeptIdsByHodId2(@Param("hodId") Long hodId);
		
		@Query(value = "select new com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO(d.deptId, d.name) from Department d\n"
				+ "inner join JobRole j on d.deptId = j.deptId \n"
				+ "inner join Employee e on j.jobRoleId = e.jobRoleId \n"
				+ "where e.empId = :empId")
		List<GetDeptIdByRoleDTO> findDeptsByEmpId(Long empId);
		
		@Query(value = " SELECT COUNT(e) > 0 FROM Employee e\n"
				+ "    WHERE EXISTS (\n"
				+ "        SELECT 1 FROM ProjectManagerMapping pmm \n"
				+ "        WHERE pmm.projectManagerId = :empId AND pmm.active = 1\n"
				+ "    )\n"
				+ "    OR EXISTS (\n"
				+ "        SELECT 1 FROM ProjectOverheadMapping pom \n"
				+ "        WHERE pom.projectOverheadId = :empId AND pom.active = 1\n"
				+ "    )\n"
				+ "    OR EXISTS (\n"
				+ "        SELECT 1 FROM Team t \n"
				+ "        WHERE t.teamLeadId = :empId AND t.isActive = 'Y'\n"
				+ "    )\n"
				+ "    OR EXISTS (\n"
				+ "        SELECT 1 FROM Team t2 \n"
				+ "        WHERE t2.spocId = :empId AND t2.isActive = 'Y'\n"
				+ "    )")
		boolean isUserMappedInAnyRole( Long empId);
		
		@Query("SELECT d.hodId FROM Department d " +
			       "JOIN JobRole jr ON d.deptId = jr.deptId " +
			       "JOIN Employee e ON jr.jobRoleId = e.jobRoleId " +
			       "WHERE e.empId = :empId")
			
		Long findHodIdForEmployee(@Param("empId") Long empId);

}

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
import com.apmosys.employeeportal.dto.PoPortalDTO;
import com.apmosys.employeeportal.dto.PoPortalEmpIdDTO;
import com.apmosys.employeeportal.model.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.DepartmentDTO(dept.deptId,dept.createdBy,dept.createdOn , \n"+
			"dept.name,emp1.name,emp2.name,dept.hodId, dept.updatedOn, \n"+
			"u.name, dept.deptAbbreviation , dept.updatedBy, \n"+
			"CASE WHEN dept.isBillable = true THEN 'Yes' ELSE 'No' END, " +
		    "CASE WHEN dept.isTnm = true THEN 'Yes' ELSE 'No' END) " +
			"FROM Department dept \n"+
			"LEFT JOIN Employee u on dept.updatedBy = u.empId \n"+
			"INNER JOIN Employee emp1 ON dept.createdBy = emp1.empId \n"+
			"INNER JOIN Employee emp2 ON dept.hodId = emp2.empId ")
	public List<DepartmentDTO>  getAllDepartments();

	public Department findByName(String department);
	
	@Query(value = "SELECT * FROM department WHERE dept_id IN (:departmentIdList)", nativeQuery = true)
	List<Object[]> getAllDepartmentsByIdList(@Param("departmentIdList") List<Long> departmentIdList);
	
	@Query(nativeQuery = true)
	public List<Object[]> getMappedDepartment(Integer projectId);

	public List<Department> findByDeptIdIn(List<Long> deptIds);

	public boolean existsByHodId(Long empId);

	@Query(value="SELECT new com.apmosys.employeeportal.dto.PoPortalEmpIdDTO(d.deptId, d.name, e.empId, d.deptAbbreviation, d.isBillable, d.isTnm)  \n"
			+ "FROM Department d \n"
			+ "INNER JOIN Employee e ON e.empId = d.hodId")
	public List<PoPortalEmpIdDTO> getDepartmentInfo();

	@Query(nativeQuery = true)
	public List<Object[]> getSegregatedDeptEodDefaulter(LocalDate firstOfMonth, LocalDate currentDate);

	public Department findByDeptId(Long deptId);

	
	@Query(nativeQuery = true , value = "select d.dept_id,d.hod_id,d.name from department d where d.hod_id= :empId")
	public Optional<List<Object>> getDepartmentsByHodId(Long empId);
	
	public Department findByDeptAbbreviation(String deptAbbreviation);
	

//	@Query(value = "SELECT d.hod_id FROM department d " +
//            "INNER JOIN job_role j ON j.dept_id = d.dept_id " +
//            "INNER JOIN employee e ON e.job_role_id = j.job_role_id " +
//            "WHERE e.emp_id = :empId", 
//    nativeQuery = true)
//	public Long findHodIdByEmpId(@Param("empId") Long empId);
	
	@Query("SELECT d.hodId " +
		       "FROM Department d, JobRole j, Employee e " +
		       "WHERE j.deptId = d.deptId " +
		       "AND e.jobRoleId = j.jobRoleId " +
		       "AND e.empId = :empId")
	public Long findHodIdByEmpId(@Param("empId") Long empId);


	
	@Query(nativeQuery = true,value = "select d.name from department d \n"
			+ "inner join job_role j on j.dept_id = d.dept_id\n"
			+ "inner join employee e on e.job_role_id = j.job_role_id\n"
			+ "inner join user_session u on e.emp_id = u.emp_id")
	public Object findbyEmpId();

	public List<Department> findByHodId(Long empId);
	
	@Query(value = "SELECT name FROM department WHERE dept_id = :deptId", nativeQuery = true)
	public String findNameByDeptId(Long deptId);

	@Query(nativeQuery = true , value = "select * from department where name = :deptname")
	public List<Department> findByDeptName(String deptname);
	
	@Query(value = "SELECT d.dept_id FROM department d WHERE d.hod_id = :hodId", 
		    nativeQuery = true)
		List<Long> findDeptIdsByHodId(@Param("hodId") Long hodId);
	
	@Query(nativeQuery = true,value ="select dept_id from job_role where job_role_id = :jobRoleId")
	Long findDepartmentofCurrentuser(@Param("jobRoleId") Long jobRoleId);
	
	@Query(nativeQuery = true,value ="select name from department where dept_id =:deptId")
	String findDepartmentNameFromDeptId(@Param("deptId") Long deptId);
	
	@Query(value ="select jr.deptId from JobRole jr where jobRoleId =:jobRoleId")
	Long findDepartmentIdOfSpoc(@Param("jobRoleId") Long jobRoleId);
	
	@Query(value ="select jr.deptId from JobRole jr where jobRoleId =:jobRoleId")
	List<Long> findDepartmentIdOfCurrentUser(@Param("jobRoleId") Long jobRoleId);
	
	
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
		List<String> findDeptIdsForTeamLead(@Param("empId") Long empId);

		@Query("SELECT t.deptIds FROM Team t WHERE t.spocId = :empId")
		List<String> findDeptIdsForSpoc(@Param("empId") Long empId);
		
		@Query("SELECT new com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO(d.deptId, d.name) FROM Department d WHERE d.deptId IN :deptIds")
		List<GetDeptIdByRoleDTO> findDepartmentsByIds(@Param("deptIds") List<Long> deptIds);
		
		
		@Query("SELECT new com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO(d.deptId, d.name) FROM Department d")
		List<GetDeptIdByRoleDTO> findAllDepartmentsForSA();
		
		
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
		
		@Query(value ="select distinct deptId from Department")
		List<Long> findAllDepartments();
		
		@Query("SELECT d.hodId FROM Department d " +
			       "JOIN JobRole jr ON d.deptId = jr.deptId " +
			       "JOIN Employee e ON jr.jobRoleId = e.jobRoleId " +
			       "WHERE e.empId = :empId")
			
		Long findHodIdForEmployee(@Param("empId") Long empId);
		
		@Query(nativeQuery = true,value="select e.emp_id, e.name as employee_name \n"
				+ "from employee e \n"
				+ "inner join job_role jr on jr.job_role_id = e.job_role_id \n"
				+ "inner join department d on d.dept_id = jr.dept_id \n"
				+ "inner join employee e1 on e1.emp_id = d.hod_id \n"
				+ "where d.hod_id = :empId \n"
				+ "and e.employmentstatus != 'InActive'")
		List<Long> findAllReporteesOfHod(Long empId);
		
		
		@Query(value="SELECT dept_id\n"
				+ "FROM department\n"
				+ "WHERE REPLACE(LOWER(name), ' ', '') = REPLACE(LOWER(:deptName), ' ', '')",nativeQuery = true)
		Long findByDepartmentnameIgnoreCase(String deptName);


	@Query(value = "SELECT d.name FROM Department d WHERE d.deptId in :deptId")
	public List<String> findDeptNameByDeptIdInd(List<Long> deptId);
	
	@Query(value = "SELECT d FROM Department d ")
	public List<Department> getAllDeptsList();

}

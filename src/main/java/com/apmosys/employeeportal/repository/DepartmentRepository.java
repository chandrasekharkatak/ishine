package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.model.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllDepartments();

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
	
	@Query(nativeQuery = true, value = "select dept_id from department where hod_id = :hodId")
	 List<Long> findDeptIdsByHodId(@Param("hodId") Long hodId);
	
	@Query(nativeQuery = true,value ="select dept_id from job_role where job_role_id = :jobRoleId")
	Long findDepartmentofCurrentuser(@Param("jobRoleId") Long jobRoleId);
	
	@Query(nativeQuery = true,value ="select name from department where dept_id =:deptId")
	String findDepartmentNameFromDeptId(@Param("deptId") Long deptId);
	
	@Query(nativeQuery = true,value ="select dept_id from job_role where job_role_id = :jobRoleId")
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
	
}

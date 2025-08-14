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
	
	@Query(value = "SELECT * FROM department WHERE dept_id IN (:departmentIdList)", nativeQuery = true)
	List<Object[]> getAllDepartmentsByIdList(@Param("departmentIdList") List<Long> departmentIdList);
	
	@Query(nativeQuery = true)
	public List<Object[]> getMappedDepartment(Integer projectId);

	public List<Department> findByDeptIdIn(List<Long> deptIds);

	public boolean existsByHodId(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentInfo();

	@Query(nativeQuery = true)
	public List<Object[]> getSegregatedDeptEodDefaulter(LocalDate firstOfMonth, LocalDate currentDate);

	public Department findByDeptId(Long deptId);

	
	@Query(nativeQuery = true , value = "select d.dept_id,d.hod_id,d.name from department d where d.hod_id= :empId")
	public Optional<List<Object>> getDepartmentsByHodId(Long empId);
	
	public Department findByDeptAbbreviation(String deptAbbreviation);
	

	
	
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

	@Query(value = "SELECT d.name FROM Department d WHERE d.deptId in :deptId")
	public List<String> findDeptNameByDeptIdInd(List<Long> deptId);
	
}

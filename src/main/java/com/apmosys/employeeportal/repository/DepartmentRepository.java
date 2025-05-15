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
	
	@Query(nativeQuery = true,value ="select name from department where dept_id =:deptId")
	String findDepartmentNameFromDeptId(@Param("deptId") Long deptId);
	
	
}

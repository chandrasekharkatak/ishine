package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

}

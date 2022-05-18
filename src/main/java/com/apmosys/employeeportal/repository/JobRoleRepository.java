package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.JobRole;

@Repository
public interface JobRoleRepository extends JpaRepository<JobRole, Long> {
	
	@Query(value = "SELECT j.name as jobrolename ,\r\n"
			+ "e.name as createdby,j.created_on,d.name  as department\r\n"
			+ "FROM job_role j \r\n"
			+ "INNER JOIN employee e ON j.created_by = e.emp_id\r\n"
			+ "INNER JOIN department d ON d.dept_id = j.dept_id",nativeQuery = true)
	public List<Object[]> getAllJobRoles();

}

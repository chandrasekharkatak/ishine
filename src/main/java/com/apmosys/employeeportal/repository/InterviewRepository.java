package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.Interview;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Query(nativeQuery = true, value = "SELECT i.*, d.name as department_name, e.name as employee_name, s.name as scheduled_by_name " +
            "FROM interview_tracker i " +
            "LEFT JOIN department d ON i.department_id = d.dept_id " +
            "LEFT JOIN employee e ON i.employee_id = e.emp_id " +
            "LEFT JOIN employee s ON i.scheduled_by_id = s.emp_id " +
            "WHERE (:startDate IS NULL OR :endDate IS NULL OR i.date BETWEEN :startDate AND :endDate) " +
            "ORDER BY i.date DESC")
    List<Object[]> findAllInterviews(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query(nativeQuery = true, value = "SELECT i.*, d.name as department_name, e.name as employee_name, s.name as scheduled_by_name " +
            "FROM interview_tracker i " +
            "LEFT JOIN department d ON i.department_id = d.dept_id " +
            "LEFT JOIN employee e ON i.employee_id = e.emp_id " +
            "LEFT JOIN employee s ON i.scheduled_by_id = s.emp_id " +
            "WHERE (:startDate IS NULL OR :endDate IS NULL OR i.date BETWEEN :startDate AND :endDate) " +
            "AND (:client IS NULL OR i.client = :client) " +
            "AND (:departmentId IS NULL OR i.department_id = :departmentId) " +
            "AND (:employeeId IS NULL OR i.employee_id = :employeeId) " +
            "ORDER BY i.date DESC")
    List<Object[]> findAllInterviewsFiltered(@Param("startDate") String startDate, @Param("endDate") String endDate,
            @Param("client") String client, @Param("departmentId") Long departmentId, @Param("employeeId") Long employeeId);

    @Query(nativeQuery = true, value = "SELECT DISTINCT client FROM interview_tracker WHERE client IS NOT NULL AND client != '' " +
            "UNION SELECT client_name FROM clients WHERE client_name IS NOT NULL AND client_name != '' " +
            "ORDER BY client")
    List<String> findAllClients();

    @Query(nativeQuery = true, value = "SELECT dept_id, name FROM department WHERE name IS NOT NULL AND name != '' " +
            "ORDER BY name")
    List<Object[]> findAllDepartments();

    @Query(nativeQuery = true, value = "SELECT emp_id, name FROM employee WHERE name IS NOT NULL AND name != '' and employmentstatus!='InActive' " +
            "ORDER BY name")
    List<Object[]> findAllEmployees();

    @Query(nativeQuery = true, value = "SELECT e.emp_id, e.name FROM employee e " +
            "INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id " +
            "WHERE jr.dept_id = :departmentId AND e.name IS NOT NULL AND e.name != '' " +
            "ORDER BY e.name")
    List<Object[]> findEmployeesByDepartmentId(@Param("departmentId") Long departmentId);

    @Query(nativeQuery = true, value = "SELECT DISTINCT project FROM interview_tracker WHERE project IS NOT NULL AND project != '' " +
            "UNION SELECT project_name FROM projects WHERE project_name IS NOT NULL AND project_name != '' " +
            "ORDER BY project")
    List<String> findAllProjects();

    @Query(nativeQuery = true, value = "SELECT p.project_id, p.project_name, c.client_name " +
            "FROM projects p " +
            "LEFT JOIN clients c ON p.client_id = c.client_id " +
            "WHERE p.project_name IS NOT NULL AND p.project_name != '' " +
            "ORDER BY p.project_name")
    List<Object[]> findAllProjectsWithClient();

    @Query(nativeQuery = true, value = "SELECT p.project_id, p.project_name, c.client_name " +
            "FROM projects p " +
            "LEFT JOIN clients c ON p.client_id = c.client_id " +
            "WHERE c.client_name = :clientName AND p.project_name IS NOT NULL AND p.project_name != '' " +
            "ORDER BY p.project_name")
    List<Object[]> findProjectsByClientName(@Param("clientName") String clientName);
}

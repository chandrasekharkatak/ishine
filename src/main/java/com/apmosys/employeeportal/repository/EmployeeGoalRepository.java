package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.EmployeeGoals;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeGoalRepository extends JpaRepository<EmployeeGoals, Long> {
    List<EmployeeGoals> findByEmpId(Long empId);
    
    @Query(nativeQuery = true, value = "SELECT COUNT(eg.emp_id) FROM employee_goals eg WHERE emp_id = :empId AND quarter_id = :quarterId AND goal_status = :goalStatus")
    long countByEmpIdAndQuarterAndGoalStatus(Long empId, Long quarterId, String goalStatus);
    
    @Query(nativeQuery = true, value = "SELECT DISTINCT(eg.template_id) FROM employee_goals eg WHERE eg.template_id = :templateId AND eg.emp_id = :empId")
    Long findByTemplateId(@Param("templateId") Long templateId, @Param("empId") Long empId);
    
    Optional<EmployeeGoals> findByGoalId(Long goalId);
    
}
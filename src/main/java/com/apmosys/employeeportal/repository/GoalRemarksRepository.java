package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.GoalRemarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRemarksRepository extends JpaRepository<GoalRemarks, Long> {
    
    @Query("SELECT gr FROM GoalRemarks gr WHERE gr.employeeGoal.goalId = :goalId ORDER BY gr.createdDate DESC")
    List<GoalRemarks> findByGoalIdOrderByCreatedDateDesc(@Param("goalId") Long goalId);
    
    @Query("SELECT gr FROM GoalRemarks gr WHERE gr.employeeGoal.empId = :empId AND gr.employeeGoal.quarterId = :quarterId ORDER BY gr.createdDate DESC")
    List<GoalRemarks> findByEmpIdAndQuarterIdOrderByCreatedDateDesc(@Param("empId") Long empId, @Param("quarterId") Long quarterId);
    
    @Query("SELECT gr FROM GoalRemarks gr WHERE gr.employeeGoal.goalId = :goalId AND gr.employeeGoal.templateId = :templateId ORDER BY gr.createdDate DESC")
    List<GoalRemarks> findByGoalIdAndTemplateIdOrderByCreatedDateDesc(@Param("goalId") Long goalId, @Param("templateId") Long templateId);
}
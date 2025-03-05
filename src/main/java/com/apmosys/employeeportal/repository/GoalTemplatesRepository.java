package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.GoalTemplates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalTemplatesRepository extends JpaRepository<GoalTemplates, Long> {
	List<GoalTemplates> findByDepartmentId(Long departmentId);
    List<GoalTemplates> findByDepartment(String department);
    List<GoalTemplates> findByCreatedBy(Long createdById);
    List<GoalTemplates> findByIsApproved(Boolean isApproved);
//    List<GoalTemplates> findByDepartmentId(Long departmentId);

}
package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.QuestionMaster;

public interface QuestionMasterRepository extends JpaRepository<QuestionMaster, Long>{

//	List<QuestionMaster> findByMilestoneId(Long milestoneId);

//	QuestionMaster findByQuestionMasterId(Long questionId);

}

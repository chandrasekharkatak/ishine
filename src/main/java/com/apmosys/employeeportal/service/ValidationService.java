package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.SurveyQuestionRepository;

@Service
public class ValidationService {

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	JobRoleRepository jobRoleRepository;
	
	@Autowired
	SurveyQuestionRepository surveyQuestionRepository;
	
	public boolean validateEmpId(Long empId) {

		return (employeeRepository.existsByEmpId(empId)) ? true : false;

	}

	public boolean validateEmploymentId(Long employeementId) {

		return (employeeRepository.existsByEmployeementId(employeementId)) ? true : false;

	}

	public boolean validateManagerId(Long managerId) {

		return (employeeRepository.existsByManagerId(managerId)) ? true : false;

	}

	public boolean validateJobRoleId(Long jobRoleId) {

		return (jobRoleRepository.existsByJobRoleId(jobRoleId)) ? true : false;

	}
	
	public boolean validateSurveyId(Long surveyId) {

		return (surveyQuestionRepository.existsBySurveyId(surveyId)) ? true : false;

	}

}

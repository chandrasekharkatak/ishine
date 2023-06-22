package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, Long>{

	UserSession findByEmpId(Long empId);

	UserSession findBySessionKey(String checkToken);

}

package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.apmosys.employeeportal.model.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, Long>{

	UserSession findByEmpId(Long empId);

	UserSession findBySessionKey(String checkToken);
	    
	
//	Optional<UserSession> findBySessionKey(String sessionKey);


}

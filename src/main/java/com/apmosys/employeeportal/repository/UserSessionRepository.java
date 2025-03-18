package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.EmployeeSessionDTO;
import com.apmosys.employeeportal.model.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, Long>{

	UserSession findByEmpId(Long empId);

	UserSession findBySessionKey(String checkToken);
    
	    @Query("SELECT new com.apmosys.employeeportal.dto.EmployeeSessionDTO(e.empId, e.name, e.role, s.userSessionId) " +
	           "FROM UserSession s " +
	           "INNER JOIN Employee e ON s.empId = e.empId " +
	           "WHERE s.empId = :empId")
	    EmployeeSessionDTO findEmployeeDetailsByEmpId(@Param("empId") Long empId);
	    
	
//	Optional<UserSession> findBySessionKey(String sessionKey);


}

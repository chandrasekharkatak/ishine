package com.apmosys.employeeportal.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.TravelDesk;

public interface TravelDeskRepository extends JpaRepository<TravelDesk, BigInteger>{

	List<TravelDesk> findByEmpId(BigInteger empId);
	
	TravelDesk findByRequestId(BigInteger requestId);
	
	@Query(nativeQuery = true, value = "SELECT * FROM travel_desk ORDER BY request_id desc")
	List<TravelDesk> findAll();
	
	List<TravelDesk> findByApprover1(BigInteger empId);
	
	List<TravelDesk> findByApprover2(BigInteger empId);
}

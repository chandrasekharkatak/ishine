package com.apmosys.employeeportal.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TravelDesk;

public interface TravelDeskRepository extends JpaRepository<TravelDesk, BigInteger>{

	List<TravelDesk> findByEmpId(BigInteger empId );
	
	TravelDesk findByRequestId(BigInteger requestId);
}

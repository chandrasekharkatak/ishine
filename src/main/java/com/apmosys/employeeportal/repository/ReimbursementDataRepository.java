package com.apmosys.employeeportal.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ReimbursementData;
import com.apmosys.employeeportal.model.TravelDesk;

public interface ReimbursementDataRepository extends JpaRepository<ReimbursementData, BigInteger>{
	@Query(nativeQuery = true, value = "SELECT * FROM reimbursement_data WHERE emp_id = :empId order by requestId desc;")
	List<ReimbursementData> findByEmpId(BigInteger empId);


	ReimbursementData findByRequestId(BigInteger requestId);
	
	List<ReimbursementData> findByApprover1(BigInteger empId);
	
	List<ReimbursementData> findByApprover2(String empId);
	
	List<ReimbursementData> findByApprover3(String empId);
}

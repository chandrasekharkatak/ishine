package com.apmosys.employeeportal.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementData;

public interface ReimbursementDataRepository extends JpaRepository<ReimbursementData, BigInteger>{

	List<ReimbursementData> findByEmpId(BigInteger empId);
}

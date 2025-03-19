package com.apmosys.employeeportal.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementData;

public interface ReimbursementDataRepository extends JpaRepository<ReimbursementData, BigInteger>{

}

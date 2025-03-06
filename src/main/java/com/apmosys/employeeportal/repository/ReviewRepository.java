package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReviewTable;

import java.util.*;
public interface ReviewRepository extends JpaRepository<ReviewTable, Long>{
	List<ReviewTable> findByEmployeeId(Long employeeId);
    List<ReviewTable> findByEmployeeIdAndQuarter(Long employeeId, Integer quarter);
}

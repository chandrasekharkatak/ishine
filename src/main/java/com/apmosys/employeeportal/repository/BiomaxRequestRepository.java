package com.apmosys.employeeportal.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.BiomaxRequest;

@Repository
public interface BiomaxRequestRepository extends JpaRepository<BiomaxRequest,Long>{
	 // Fetch by Employee ID
    List<BiomaxRequest> findByEmpId(Long empId);

    // Fetch by Status
    List<BiomaxRequest> findByBiomaxStatus(String biomaxStatus);

    // Fetch by Reporting Manager
    List<BiomaxRequest> findByReportingManagerId(Long reportingManagerId);

    // Fetch all enabled requests
    List<BiomaxRequest> findByIsEnabledTrue();

    // Fetch by date range
    List<BiomaxRequest> findByBiomaxrequestDateBetween(LocalDateTime startDate, LocalDateTime endDate);

}

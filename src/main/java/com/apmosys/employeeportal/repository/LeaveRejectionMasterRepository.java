package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.LeaveRejectionMaster;

public interface LeaveRejectionMasterRepository extends JpaRepository<LeaveRejectionMaster, Long> {

    List<LeaveRejectionMaster> findByIsActiveTrueOrderByRejectionReasonIdAsc();
}
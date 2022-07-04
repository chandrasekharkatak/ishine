package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.LeavePolicyLeaveTypeMap;

@Repository
public interface LeavePolicyLeaveTypeMapRepository extends JpaRepository<LeavePolicyLeaveTypeMap,Long> {

}

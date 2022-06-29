package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.LeavePolicyMaster;

@Repository
public interface LeavePolicyMasterRepository extends JpaRepository<LeavePolicyMaster, Short> {

}

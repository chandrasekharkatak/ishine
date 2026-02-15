package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.RoleDetails;

@Repository
public interface RoleDetailsRepository extends JpaRepository<RoleDetails, Long> {

    
}

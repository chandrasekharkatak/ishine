package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.apmosys.employeeportal.model.LeaveTypeMaster;

public interface LeaveTypeMasterRepository extends JpaRepository<LeaveTypeMaster, Short> {

}

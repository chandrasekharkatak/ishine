package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.DraftEmployee;

public interface DraftEmployeeRepository extends JpaRepository<DraftEmployee,Long>{

}

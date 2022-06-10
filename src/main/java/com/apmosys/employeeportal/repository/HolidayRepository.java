package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, Short> {

}

package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.BirthdayMail;

public interface BirthdayMailRepository extends JpaRepository<BirthdayMail, Long> {

}

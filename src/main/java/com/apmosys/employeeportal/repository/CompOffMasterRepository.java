package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.CompOffMaster;

public interface CompOffMasterRepository extends JpaRepository<CompOffMaster, Short> {

	CompOffMaster findByCompOffId(short reasonId);

}

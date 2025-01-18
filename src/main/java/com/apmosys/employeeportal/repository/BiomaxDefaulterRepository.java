package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.BiomaxDefaulter;

public interface BiomaxDefaulterRepository extends JpaRepository<BiomaxDefaulter, Long> {

	List<BiomaxDefaulter> findByEmployeementId(Long parseLong);

	void deleteAllByEmployeementId(Long employmentId);

}

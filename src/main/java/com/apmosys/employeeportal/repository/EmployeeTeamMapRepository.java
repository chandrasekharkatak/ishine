package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.model.EmployeeTeamMap;

@Repository
public interface EmployeeTeamMapRepository extends JpaRepository<EmployeeTeamMap, Long> {

	@Transactional
	void deleteAllByTeamId(Long teamId);

}

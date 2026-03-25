package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.Help;

public interface HelpRepository extends JpaRepository<Help, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getAllHelpDocument();

	public Help findByHelpDocId(long helpDocId);

}

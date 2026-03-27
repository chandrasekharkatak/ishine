package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.ProjectPoDetails;

@Repository
public interface ProjectPoDetailsRepository extends JpaRepository<ProjectPoDetails, Long>{

	
	@Modifying
	@Query("DELETE FROM ProjectPoDetails")
	void deleteAllRecords();
	
	Optional<ProjectPoDetails> findByPoProjectId(Long poProjectId);
	
	
}

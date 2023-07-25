package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Newsletter;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long >{

	@Query(nativeQuery = true)
	public List<Object[]> getAllNewsletters();
	
	@Query(nativeQuery = true)
	public List<Object[]> findByDocumentId(Long documentId);

	public long countByReadEnabled(String readEnabled);

	public List<Newsletter> findByReadEnabled(String readEnabled);
}

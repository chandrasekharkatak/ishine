package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.UploadPolicy;

@Repository
public interface UploadPolicyRepository extends JpaRepository<UploadPolicy, Long > {
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllDocuments();

	@Query(nativeQuery = true)
	public UploadPolicy findByPolicyID(Long policyID);

}

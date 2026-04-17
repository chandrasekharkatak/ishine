package com.apmosys.employeeportal.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.CertificateDriveLinkMapping;


@Repository
public interface CertificateDriveLinkMapRepository extends JpaRepository<CertificateDriveLinkMapping,Long> {

	
	
	@Modifying
	@Transactional
	@Query("UPDATE CertificateDriveLinkMapping d SET d.drActive = false WHERE d.driveId = :driveId")
	void deactivateDriveLinkById( Long driveId);
}

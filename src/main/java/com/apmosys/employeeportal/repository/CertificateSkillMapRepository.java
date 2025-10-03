package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.CertificateSkillMapping;

@Repository
public interface CertificateSkillMapRepository extends JpaRepository<CertificateSkillMapping,Long>{

	@Query("Select c from CertificateSkillMapping c where c.employeeCertificateId = :employeeCertificateId ")
	public List<CertificateSkillMapping> findByEmployeeCertificateIdAndScActiveTrue(Long employeeCertificateId);
	
	
	@Modifying
	@Transactional
	@Query("UPDATE CertificateSkillMapping c SET c.scActive = false, c.csupdatedBy = :updatedBy, c.csupdatedOn = CURRENT_TIMESTAMP " +
	       "WHERE c.certificateSKillId = :certSkillId")
	void deactivateSkillByCertificateSkillId( Long certSkillId,
	                                          Long updatedBy);


	@Modifying
	@Transactional
	@Query("UPDATE CertificateSkillMapping c SET c.proficiencyId = :proficiencyId, c.csupdatedBy = :updatedBy, c.csupdatedOn = CURRENT_TIMESTAMP " +
	       "WHERE c.certificateSKillId = :certSkillId")
	void updateProficiencyByCertificateSkillId( Long certSkillId,
	                                            Long proficiencyId,
	                                             Long updatedBy);
}

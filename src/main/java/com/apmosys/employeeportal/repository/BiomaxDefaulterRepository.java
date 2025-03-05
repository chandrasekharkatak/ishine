package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.BiomaxDefaulter;

public interface BiomaxDefaulterRepository extends JpaRepository<BiomaxDefaulter, Long> {

	@Query(value = "SELECT * FROM biomax_defaulter WHERE employeement_id = :empId AND created_on >= CURDATE() - INTERVAL 3 DAY", nativeQuery = true)
	List<BiomaxDefaulter> findByEmployeementIdForDefaulterBiomax(@Param("empId") Long empId);


	List<BiomaxDefaulter> findByEmployeementId(Long parseLong);

	void deleteAllByEmployeementId(Long employmentId);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM biomax_defaulter WHERE employeement_id IN :employmentIds", nativeQuery = true)
    void deleteAllByEmployeementIds(List<Long> employmentIds);
	
	
	

}

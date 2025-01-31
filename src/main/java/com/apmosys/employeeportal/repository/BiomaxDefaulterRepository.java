package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.BiomaxDefaulter;

public interface BiomaxDefaulterRepository extends JpaRepository<BiomaxDefaulter, Long> {

	List<BiomaxDefaulter> findByEmployeementId(Long parseLong);

	void deleteAllByEmployeementId(Long employmentId);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM biomax_defaulter WHERE employeement_id IN :employmentIds", nativeQuery = true)
    void deleteAllByEmployeementIds(List<Long> employmentIds);

}

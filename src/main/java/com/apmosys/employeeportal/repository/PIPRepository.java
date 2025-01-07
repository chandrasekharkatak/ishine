package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.PIP;

@Repository
public interface PIPRepository extends JpaRepository<PIP, Long>{

	@Query(nativeQuery = true )
	Optional<List<PIP>> findByEmpId(Long empId);

	@Query(nativeQuery = true)
	PIP findByPipId(Long pipId);

	@Query(nativeQuery = true )
	List<Object[]> findPipReasonByEmpIdAndPipFlag(Long empId);

	@Query(nativeQuery = true)
	List<PIP> getPipDetailsByEmployeeId(Long empId);
	
	

}

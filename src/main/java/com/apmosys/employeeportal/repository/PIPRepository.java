package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.PIP;

@Repository
public interface PIPRepository extends JpaRepository<PIP, Long>{

	@Query(nativeQuery = true , value="Select * FROM pip p WHERE p.emp_id= :empId")
	Optional<List<PIP>> findByEmpId(Long empId);

	@Query(nativeQuery = true , value="Select * FROM pip p WHERE p.pip_id= :pipId")
	PIP findByPipId(Long pipId);

	@Query(nativeQuery = true , value = "SELECT e.name as employeeName,e.employeement_id, p.pip_id,p.pip_reason,p.created_by as createdByName ,"
			+ " p.created_on,p.updated_by as updatedByName,p.updated_on , p.rev_reason,p.pip_flag "
			+ " from pip p "
			+ "left join employee e on e.emp_id=p.emp_id "
			+ "where p.emp_id= :empId and p.pip_flag = :pipFlag")
	List<Object[]> findPipReasonByEmpIdAndPipFlag(Long empId, Boolean pipFlag);
	
	

}

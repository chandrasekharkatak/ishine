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

	@Query(value = "SELECT * FROM db_emp_backup.biomax_defaulter WHERE employeement_id = 3 AND YEAR(defaulted_date) = YEAR(CURDATE()) \n"
			+ "  AND MONTH(defaulted_date) = MONTH(CURDATE()) AND  defaulted_date<=CURDATE()\n"
			+ "  AND is_deducted = FALSE", nativeQuery = true)
	List<BiomaxDefaulter> findByEmployeementIdForDefaulterBiomax(@Param("empId") Long empId);

	@Query(value = "SELECT * FROM biomax_defaulter WHERE employeement_id = :empId AND created_on >= CURDATE() - INTERVAL 3 DAY", nativeQuery = true)
	List<BiomaxDefaulter> findByEmployeementIdAndFromdateAndTodate(@Param("empId") Long empId);


	List<BiomaxDefaulter> findByEmployeementId(Long parseLong);
	
	List<BiomaxDefaulter> findByEmpIdAndIsDeducted(Long empId, boolean isDeducted);
	List<BiomaxDefaulter> findByEmpIdAndIsDeductedAndIsApprovedByManager(Long empId, boolean isDeducted, boolean isApprovedByManager);

	void deleteAllByEmployeementId(Long employmentId);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM biomax_defaulter\n"
			+ "WHERE employeement_id NOT IN (:employmentIds)\n"
			+ "AND MONTH(defaulted_date) != MONTH(CURDATE())\n"
			+ "AND YEAR(defaulted_date) != YEAR(CURDATE())", nativeQuery = true)
    void deleteAllByEmployeementIds(List<Long> employmentIds);

	@Query(value="SELECT MAX(group_id) FROM db_emp_backup.biomax_defaulter WHERE emp_id=:empId",nativeQuery = true)
	Long findMaxDroupIdFromEmpId(Long empId);
	

}

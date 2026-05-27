package com.apmosys.employeeportal.repository;


import com.apmosys.employeeportal.model.EmployeeTrainingMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeTrainingMappingRepository 
        extends JpaRepository<EmployeeTrainingMapping, Long> {
	@Query("SELECT m.empId FROM EmployeeTrainingMapping m " +
		       "WHERE m.trainingMaster.trainingId = :trainingId " +
		       "AND m.activeStatus = 'true'")
		List<Long> findActiveEmpIdsByTrainingId(@Param("trainingId") Integer trainingId);

	@Query("SELECT m FROM EmployeeTrainingMapping m " +
	       "JOIN m.trainingMaster t WHERE t.trainingId = :trainingId")
	List<EmployeeTrainingMapping> findAllByTrainingId(@Param("trainingId") int trainingId);

		@Modifying
		@Query("UPDATE EmployeeTrainingMapping m " +
		       "SET m.activeStatus = 'false', " +
		       "    m.updatedBy = :updatedBy, " +
		       "    m.updatedOn = CURRENT_TIMESTAMP " +
		       "WHERE m.trainingMaster.trainingId = :trainingId " +
		       "AND m.activeStatus = 'true'")
		void deactivateAllByTrainingId(@Param("trainingId") Integer trainingId,
		                               @Param("updatedBy") Long updatedBy);
		@Query("SELECT m.trainingMaster.trainingId FROM EmployeeTrainingMapping m " +
			       "WHERE m.empId = :empId AND m.activeStatus = 'true'")
			List<Integer> findAssignedTrainingIdsByEmpId(@Param("empId") Long empId);
	
		
		
		@Query("SELECT m.empId FROM EmployeeTrainingMapping m " +
			       "WHERE m.trainingMaster.trainingId = :trainingId")
			List<Long> findAllEmpIdsByTrainingId(@Param("trainingId") Integer trainingId);


		@Query("SELECT m.empId FROM EmployeeTrainingMapping m " +
		       "JOIN m.trainingMaster t WHERE t.trainingId = :trainingId " +
		       "AND m.activeStatus = 'false'")
		List<Long> findExcludedEmpIdsByTrainingId(@Param("trainingId") int trainingId);
		
		@Query("SELECT m.trainingMaster.trainingId FROM EmployeeTrainingMapping m " +
			       "WHERE m.empId = :empId AND m.activeStatus = 'false'")
			List<Integer> findExcludedTrainingIdsByEmpId(@Param("empId") Long empId);
		
		@Modifying
		@Query("UPDATE EmployeeTrainingMapping m " +
		       "SET m.activeStatus = 'false' " +
		       "WHERE m.trainingMaster.trainingId = :trainingId " +
		       "AND m.activeStatus = 'true'")
		void updateTrueRecordsToFalseByTrainingId(@Param("trainingId") int trainingId);
		
		
}

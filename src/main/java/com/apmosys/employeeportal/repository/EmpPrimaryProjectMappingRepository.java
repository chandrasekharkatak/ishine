package com.apmosys.employeeportal.repository;

import java.time.LocalDateTime;
import com.apmosys.employeeportal.dto.ProjectNameAndPrjoectIdDTO;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.model.EmpPrimaryProjectMapping;

public interface EmpPrimaryProjectMappingRepository extends JpaRepository<EmpPrimaryProjectMapping, Long>{

	/** @deprecated Prefer {@link #findAllByEmpIdAndIsMappedOrderByMappingIdDesc} — DB may contain duplicate Y rows per emp. */
	@Deprecated
	EmpPrimaryProjectMapping findByEmpIdAndIsMapped(Long empId,String ismapped);

	/** All active primary mappings for an employee (newest first). Use first element when only one logical primary is needed. */
	List<EmpPrimaryProjectMapping> findAllByEmpIdAndIsMappedOrderByMappingIdDesc(Long empId, String ismapped);
	
	 List<EmpPrimaryProjectMapping> findByEmpIdIn(List<Long> empIds);

	 @Query("SELECT epm FROM EmpPrimaryProjectMapping epm WHERE epm.empId IN :empIds AND epm.isMapped = 'Y'")
	 List<EmpPrimaryProjectMapping> findByEmpIdInAndIsMapped(@Param("empIds") List<Long> empIds);
	
	    @Modifying
	    @Transactional
	    @Query(value = "UPDATE emp_primary_project_mapping SET is_mapped = :isMapped, updated_on = :updatedOn WHERE emp_id = :empId", nativeQuery = true)
	    void updateIsMappedOnlyTON(@Param("empId") Long empId,
	                            @Param("isMapped") String isMapped,	                            
	                            @Param("updatedOn") Date updatedOn);
	    
	    @Query(value = "SELECT * FROM emp_primary_project_mapping WHERE emp_id = :empId", nativeQuery = true)
	    Optional<EmpPrimaryProjectMapping> findByEmpIdd(@Param("empId") Long empId);
	    
	    @Modifying
	    @Transactional
	    @Query(value = "UPDATE emp_primary_project_mapping SET primary_project_id = :projectId, primary_project_name = :projectName, is_mapped = :isMapped, updated_on = :updatedOn WHERE emp_id = :empId", nativeQuery = true)
	    void updateMappingDetails(@Param("empId") Long empId,
	                              @Param("projectId") Long projectId,
	                              @Param("projectName") String projectName,
	                              @Param("isMapped") String isMapped,
	                              @Param("updatedOn") Date updatedOn);
	    
	    List<EmpPrimaryProjectMapping> findByEmpIdInAndPrimaryProjectIdInAndIsMapped(List<Long> empIds, List<Long> projectIds, String isMapped);

		@Query("SELECT epm FROM EmpPrimaryProjectMapping epm WHERE epm.empId IN :empIds AND primaryProjectId=:projectId AND epm.isMapped = 'Y' ")
	 	List<EmpPrimaryProjectMapping> findByEmpIdInAndIsMappedAndProjectId(@Param("empIds") List<Long> empIds, @Param("projectId") Long projectId);

        boolean existsByEmpIdAndPrimaryProjectIdAndIsMapped(Long empId, Long projectId, String string);

        List<Long> findEmpIdByEmpIdInAndPrimaryProjectIdAndIsMapped(
        List<Long> empIds,
        Long projectId,
        String isMapped);
        
    @Query("SELECT eppm FROM EmpPrimaryProjectMapping eppm " +
            "WHERE eppm.empId IN :empIds " +
            "AND eppm.primaryProjectId IN :projectIds " +
            "AND eppm.isMapped='Y'")
    List<EmpPrimaryProjectMapping> findActivePrimaryMappings(
            Set<Long> empIds,
            Set<Long> projectIds);

    @Query("SELECT eppm FROM EmpPrimaryProjectMapping eppm WHERE eppm.empId = :empId")
    Optional<EmpPrimaryProjectMapping> findByEmpId(Long empId);
    
    List<EmpPrimaryProjectMapping> findByEmpIdIn(Set<Long> empIds);

	@Query("SELECT epm FROM EmpPrimaryProjectMapping epm WHERE epm.empId IN :empIds AND primaryProjectId !=:projectId AND epm.isMapped = 'Y' ")
	List<EmpPrimaryProjectMapping> findByEmpIdInAndIsMappedAndProjectIdNotIn(@Param("empIds") List<Long> empIds, @Param("projectId") Long projectId);

	@Query("SELECT epm FROM EmpPrimaryProjectMapping epm WHERE epm.empId IN :empIds AND primaryProjectId=:projectId ")
	List<EmpPrimaryProjectMapping> findByEmpIdInAndProjectId(@Param("empIds") List<Long> empIds, @Param("projectId") Long projectId);

	@Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.ProjectNameAndPrjoectIdDTO("
        + "p.projectId, "
        + "p.projectName, "
        + "p.clientFlag, "
        + "p.hasClientSideId, "
        + "p.poProjectType, "
        + "0"
        + ") "
        + "FROM EmpPrimaryProjectMapping eppm "
        + "INNER JOIN Project p ON p.projectId = eppm.primaryProjectId "
        + "WHERE eppm.empId = :empId "
        + "AND eppm.isMapped = 'Y'")
List<ProjectNameAndPrjoectIdDTO> getPrimaryMappedProjects(@Param("empId") Long empId);
}

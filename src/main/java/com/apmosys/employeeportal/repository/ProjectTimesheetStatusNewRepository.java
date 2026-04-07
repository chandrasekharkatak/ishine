package com.apmosys.employeeportal.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.model.ProjectTimesheetStatusId;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;

@Repository
public interface ProjectTimesheetStatusNewRepository extends JpaRepository<ProjectTimesheetStatusNew, ProjectTimesheetStatusId> {
    
    @Query("SELECT p FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId")
    List<ProjectTimesheetStatusNew> findByTimesheetId(@Param("timesheetId") Long timesheetId);
    
    @Query("SELECT p FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId AND p.id.projectId = :projectId AND p.id.locationMappingId = :locationMappingId")
    Optional<ProjectTimesheetStatusNew> findByTimesheetIdAndProjectIdAndLocationMappingId(@Param("timesheetId") Long timesheetId, @Param("projectId") Integer projectId, @Param("locationMappingId") Long locationMappingId);
    
    List<ProjectTimesheetStatusNew> findAllByIdTimesheetIdIn(Set<Long> timesheetIds);
    
    @Query("SELECT pts.id.locationMappingId " +
    	       "FROM ProjectTimesheetStatusNew pts " +
    	       "WHERE pts.id.timesheetId = :timesheetId " +
    	       "AND pts.id.projectId = :projectId")
    	List<Long> findLocationMappingId(@Param("timesheetId") Long timesheetId,
    	                           @Param("projectId") Integer projectId);

    
    @Modifying
	@Transactional
	@Query("UPDATE ProjectTimesheetStatusNew et " +
	       "SET et.status = :statusValue " +
	       "WHERE et.id.timesheetId IN :timesheetIds")
	int processByStatus(@Param("timesheetIds") List<Long> timesheetIds,
	                    @Param("statusValue") int statusValue);
    
    @Modifying
	@Transactional
	@Query("UPDATE ProjectTimesheetStatusNew et " +
	       "SET et.status = :status " +
	       "WHERE et.id.timesheetId = :timesheetId "+
			"AND et.id.projectId = :projectId")
	int processByTSandProject( @Param("timesheetId") Long timesheetId,
	        @Param("projectId") Integer projectId,
	        @Param("status") Integer status);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId")
    void deleteByTimesheetId(@Param("timesheetId") Long timesheetId);
    
    Boolean existsByIdLocationMappingIdAndStatus(
            Long locationMappingId,
            Integer status
    );

    

    @Query("SELECT p FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId AND p.id.locationMappingId = :locationMappingId")
    List<ProjectTimesheetStatusNew> findByTimesheetIdAndLocationTypeId(
            @Param("timesheetId") Long timesheetId,
            @Param("locationMappingId") Long locationMappingId
    );
    
    List<ProjectTimesheetStatusNew> findByIdLocationMappingId(Long locationMappingId);
    
    
    @Modifying
    @Transactional  // Added missing @Transactional
    @Query("DELETE FROM ProjectTimesheetStatusNew p "
        + "WHERE p.id.timesheetId = :timesheetId "
        + "AND p.id.locationMappingId = :locationMappingId "
        + "AND p.id.projectId = :projectId")
    void deleteByTimesheetIdAndLocationMappingIdAndProjectId(
            @Param("timesheetId") Long timesheetId,
            @Param("locationMappingId") Long locationMappingId,
            @Param("projectId") Integer projectId
    );
    
    
    
    @Query("SELECT p FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId AND p.status = :status")
    List<ProjectTimesheetStatusNew> findByTimesheetIdAndStatus(
            @Param("timesheetId") Long timesheetId,
            @Param("status") Integer status
    );
    
    @Query("SELECT p FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId AND p.id.locationMappingId = :locationMappingId")
    List<ProjectTimesheetStatusNew> findByTimesheetIdAndLocationMappingId(
            @Param("timesheetId") Long timesheetId,
            @Param("locationMappingId") Long locationMappingId
    );
    
    @Query("SELECT p FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId AND p.id.locationMappingId = :locationMappingId AND p.id.projectId = :projectId")
    Optional<ProjectTimesheetStatusNew> findByTimesheetIdAndLocationMappingIdAndProjectId(
            @Param("timesheetId") Long timesheetId,
            @Param("locationMappingId") Long locationMappingId,
            @Param("projectId") Integer projectId
    );

    @Query(
    	    "select (count(p) > 0) " +
    	    "from ProjectTimesheetStatusNew p " +
    	    "where p.id.timesheetId = :timesheetId " +
    	    "and p.id.locationMappingId = :locationMappingId " +
    	    "and p.id.projectId = :projectId " +
    	    "and p.status = :status"
    	)
    	boolean existsApprovedProject(
    	        @Param("timesheetId") Long timesheetId,
    	        @Param("locationMappingId") Long locationMappingId,
    	        @Param("projectId") Integer projectId,
    	        @Param("status") Integer status);

    	
    @Query("SELECT ptsn.id.timesheetId, ptsn.id.projectId " +
    	       "FROM ProjectTimesheetStatusNew ptsn " +
    	       "WHERE ptsn.id.timesheetId IN :timesheetIds")
    	List<Object[]> findProjectsForTimesheetIds(
    	        @Param("timesheetIds") List<Long> timesheetIds);

    /**
     * Distinct (timesheet, project) pairs that exist for bulk validation of single/bulk reject flows.
     */
    @Query("SELECT DISTINCT p.id.timesheetId, p.id.projectId FROM ProjectTimesheetStatusNew p "
            + "WHERE p.id.timesheetId IN :timesheetIds AND p.id.projectId IN :projectIds")
    List<Object[]> findDistinctTimesheetProjectPairs(
            @Param("timesheetIds") List<Long> timesheetIds,
            @Param("projectIds") Collection<Integer> projectIds);

        @Query(
                "SELECT p FROM ProjectTimesheetStatusNew p \n" +
                "WHERE p.id.timesheetId IN :timesheetIds \n" +
                "AND p.id.projectId = :projectId \n" +
                "AND p.status IN (1,3)"
        )
        List<ProjectTimesheetStatusNew> findByTimesheetIdsAndProjectIdAndStatusPending(
                @Param("timesheetIds") Set<Long> timesheetIds,
                @Param("projectId") Integer projectId);


        @Query(value = "SELECT ptn.* FROM project_timesheet_status_new ptn\n"
        		+ "JOIN employee_timesheets_new etn \n"
        		+ "  ON etn.timesheet_id = ptn.timesheet_id\n"
        		+ "JOIN projects p \n"
        		+ "on p.project_id = ptn.project_id \n"
        		+ "WHERE ptn.project_id = :projectId\n"
        		+ "  AND etn.emp_id = :empId\n"
        		+ "AND ptn.client_location_id IS NOT NULL \n"
        		+ "AND p.active = 'true'\n"
        		+ "ORDER BY etn.date DESC\n"
        		+ "LIMIT 1", nativeQuery = true)
        ProjectTimesheetStatusNew  findByProjectIdAndEmpId(
           @Param("projectId") Integer projectId, @Param("empId") Long empId);
        
        @Query(value = "select p.project_id,p.client_id,cl.client_location_id from projects p \n"
        		+ "join client_locations cl on cl.client_id = p.client_id \n"
        		+ "join project_department_map pdm on pdm.project_id = p.project_id \n"
        		+ "join job_role jr on jr.dept_id = pdm.dept_id\n"
        		+ "join employee e on e.job_role_id = jr.job_role_id\n"
        		+ "where e.emp_id = :empId \n"
        		+ "and p.project_name LIKE '%bench%'\n"
        		+ "AND p.internal_project_type = 'Bench'\n"
        		+ "and p.active = 'true'\n"
        		+ "AND pdm.active is true order by cl.created_on desc limit 1", nativeQuery = true)
        List<Object[]> getDeptBaseProjectAndClientDataFromEmpId(@Param("empId") Long empId);
        
        @Query("SELECT p FROM ProjectTimesheetStatusNew p WHERE p.shadowEmpId = p.createdBy and p.id.projectId = :projectId")
        List<ProjectTimesheetStatusNew> findShadowForSelf(@Param("projectId") Integer projectId);
}


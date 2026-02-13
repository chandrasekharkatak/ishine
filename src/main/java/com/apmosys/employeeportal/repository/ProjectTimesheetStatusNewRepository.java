package com.apmosys.employeeportal.repository;

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
    
    @Query("SELECT p FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId AND p.id.projectId = :projectId")
    Optional<ProjectTimesheetStatusNew> findByTimesheetIdAndProjectId(@Param("timesheetId") Long timesheetId, @Param("projectId") Integer projectId);
    
    List<ProjectTimesheetStatusNew> findAllByIdTimesheetIdIn(Set<Long> timesheetIds);
    
    @Modifying
	@Transactional
	@Query("UPDATE ProjectTimesheetStatusNew et " +
	       "SET et.status = :statusValue " +
	       "WHERE et.id.timesheetId IN :timesheetIds")
	int processByStatus(@Param("timesheetIds") List<Long> timesheetIds,
	                    @Param("statusValue") int statusValue);
    
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





}


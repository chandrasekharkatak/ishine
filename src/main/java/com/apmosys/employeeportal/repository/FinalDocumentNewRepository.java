package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.FinalDocumentNew;

public interface FinalDocumentNewRepository extends JpaRepository<FinalDocumentNew, Long> {  
	
    @Query("SELECT fd FROM FinalDocumentNew fd WHERE fd.projectId = :projectId")
    List<FinalDocumentNew> findByProjectId(@Param("projectId") Long projectId);
    
//    @Query("SELECT fd FROM FinalDocumentNew fd WHERE fd.finalDocId = :finalDocId")
//    Optional<FinalDocumentNew> findByFinalDocId(@Param("finalDocId") Long finalDocId);

	
 }

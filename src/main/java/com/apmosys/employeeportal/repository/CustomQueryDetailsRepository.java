package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.CustomQueryDetails;

public interface CustomQueryDetailsRepository extends JpaRepository<CustomQueryDetails ,Long> {
	
	@Query("SELECT c FROM CustomQueryDetails c WHERE c.availableColumns = :availableColumns AND c.selectedColumns = :selectedColumns")
	Optional<CustomQueryDetails> findByAvailableAndSelectedColumns(@Param("availableColumns") String availableColumns, @Param("selectedColumns") String selectedColumns);

	@Query(value = "SELECT custom_query_id, query_name, available_columns, selected_columns FROM custom_query_details", nativeQuery = true)
    List<Object[]> findAllCustomQueries();
    
    @Query("SELECT c FROM CustomQueryDetails c WHERE c.queryName = :queryName")
    Optional<CustomQueryDetails> findByQueryName(@Param("queryName") String queryName);

}

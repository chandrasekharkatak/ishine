package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.QueryTable;

@Repository
public interface QueryTableRepository extends JpaRepository<QueryTable, Long>{

	@Query(nativeQuery = true)
	List<Object[]> findAllNonPublishedQuery();
	
	@Query(nativeQuery = true )
	List<Object[]> findAllPublishedQuery();

	QueryTable findByQueryId(Long queryId);

}

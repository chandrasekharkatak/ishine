package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.QueryTable;

@Repository
public interface QueryTableRepository extends JpaRepository<QueryTable, Long>{

	@Query(nativeQuery = true , value = "select qt.query_id,qt.query_name,qt.query,qt.created_by,qt.created_on,e.name as createdByName from query_table qt "
			+ "left join employee e On e.emp_id=qt.created_by where qt.publish=0")
	List<Object[]> findAllNonPublishedQuery();
	
	@Query(nativeQuery = true , value = "select qt.query_id,qt.query_name,qt.query,qt.created_by,qt.created_on,e.name as createdByName, ee.name as updatedByName , qt.updated_on "
			+ "from query_table qt "
			+ " left join employee ee ON qt.updated_by = ee.emp_id "
			+ "left join employee e On e.emp_id=qt.created_by where qt.publish=1")
	List<Object[]> findAllPublishedQuery();

	QueryTable findByQueryId(Long queryId);

}

package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.ReviewType;

@Repository
public interface ReviewTypeRepository extends JpaRepository<ReviewType, Long> {

	  
    @Query(value = "SELECT * FROM review_type WHERE review_type_id = :reviewId", nativeQuery = true)
    ReviewType findByReviewTypeId(@Param("reviewId") Long reviewId);
    
    @Query(value = "SELECT rt.review_label \n"
    		+ "FROM review_type rt\n"
    		+ "inner JOIN quater_cycle qc \n"
    		+ "  ON rt.quarter_id = qc.quarter_id \n"
    		+ "  AND qc.is_enable = 1\n"
    		+ "  AND rt.flag = 1", nativeQuery = true)
	List<Object[]> getReviewDataForQuarter();

	
	 @Query(value = "SELECT r.review_label,r.quarter_id,r.dept_id FROM review_type r where r.flag = true", nativeQuery = true)
	 List<Object[]> getReviewLabelForEveryDepartment();
	


	
	
	

}

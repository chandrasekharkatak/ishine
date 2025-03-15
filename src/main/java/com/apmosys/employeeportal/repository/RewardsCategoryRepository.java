package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Designation;
import com.apmosys.employeeportal.model.RewardsCategory;
import com.apmosys.employeeportal.model.TypeDocument;

@Repository
public interface RewardsCategoryRepository extends JpaRepository<RewardsCategory, Long> {
	
	@Query(value= "select * from rewards_category" , nativeQuery = true)
	public List<Object[]> findAllRewardsCategory();
	
	
	@Query("SELECT rc FROM RewardsCategory rc WHERE LOWER(rc.categoryName) = :rewardCategoryName")
	RewardsCategory findByCategoryNameIgnoreCase(@Param("rewardCategoryName") String rewardCategoryName);

}

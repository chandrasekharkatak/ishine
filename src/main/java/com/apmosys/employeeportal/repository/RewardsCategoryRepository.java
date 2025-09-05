package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Designation;
import com.apmosys.employeeportal.model.RewardsCategory;
import com.apmosys.employeeportal.model.TypeDocument;

@Repository
public interface RewardsCategoryRepository extends JpaRepository<RewardsCategory, Long> {
	
	@Query(value= "select * from rewards_category" , nativeQuery = true)
	public List<Object[]> findAllRewardsCategory();
	
	
	@Query(value ="SELECT * FROM rewards_category WHERE LOWER(category_name) LIKE LOWER(:rewardCategoryName)",nativeQuery= true)
	RewardsCategory findByCategoryNameIgnoreCase(@Param("rewardCategoryName") String rewardCategoryName);


	public List<RewardsCategory> findByRewardCategoryId(Long rewardCategoryId);
	
	@Modifying
	@Transactional
	@Query(value = "update employee_rewards \n"
			+ "set is_quarter_enable = case when ofmonthyear = :ofMonthYear then 1 else 0 end;", nativeQuery = true)
	public int enableQuarter(@Param("ofMonthYear") String ofMonthYear);
	
	@Modifying
	 @Transactional
	 @Query(value = "UPDATE employee_rewards er SET er.is_active = 1 "
	 		+ "WHERE er.ofmonthyear IN :monthyears and er.is_quarter_enable = 1" , nativeQuery = true)
	 int bulkEnableRewardsQuartely(@Param("monthyears") List<String> monthyears);
	
}

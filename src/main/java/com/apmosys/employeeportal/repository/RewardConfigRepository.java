package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.RewardConfig;

public interface RewardConfigRepository extends JpaRepository<RewardConfig, Long> {
	
	RewardConfig findByRewardName(String rewardName);

}

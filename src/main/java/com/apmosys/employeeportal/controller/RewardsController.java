package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.RewardConfigurationDTO;
import com.apmosys.employeeportal.service.RewardsService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class RewardsController {
	
	@Autowired
	RewardsService rewardsService;
	
	@RequestMapping(value = "/getAllRewardsCategory", method= RequestMethod.GET)
	public ServiceResponse getAllRewardsCategory() {
		
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = rewardsService.getAllRewardsCategory();
		return serviceResponse;
	}
	
	@PostMapping("/saveRewardConfiguration")
	public ServiceResponse saveRewardConfiguration(@RequestBody RewardConfigurationDTO rewardConfigurationDTO)
	{
		return rewardsService.saveRewardConfiguration(rewardConfigurationDTO);
	}
	
	@PostMapping("/editRewardConfiguration")
	public ServiceResponse editRewardConfiguration(@RequestBody RewardConfigurationDTO rewardConfigurationDTO)
	{
		return rewardsService.editRewardConfiguration(rewardConfigurationDTO);
	}
	
	@RequestMapping(value = "/getAllRewardsByCategoryId/{categoryId}", method = RequestMethod.GET)
	public ServiceResponse getAllRewardsByCategoryId(@PathVariable("categoryId") Integer categoryId) {
	    ServiceResponse serviceResponse = rewardsService.getAllRewardsByCategoryId(categoryId);
	    return serviceResponse;
	}
	
	@RequestMapping(value = "/showAllRewards", method = RequestMethod.GET)
	public ServiceResponse showAllRewards() {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = rewardsService.showAllRewards();
		return serviceResponse;
	}
	
	@RequestMapping(value = "/fetchEmployeesFromRewardCondition", method = RequestMethod.GET)
	 public ServiceResponse fetchEmployeesFromRewardCondition(@RequestParam Long rewardId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = rewardsService.fetchEmployeesFromRewardCondition(rewardId);
	        return serviceResponse;
	    }

}

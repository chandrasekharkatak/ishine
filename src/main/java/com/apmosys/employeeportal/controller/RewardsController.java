package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeRewardsDTO;
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
	
	@RequestMapping(value = "/getAllRewardsByRewardId/{id}", method = RequestMethod.GET)
	public ServiceResponse getAllRewardsByRewardId(@PathVariable("id") Long id) {
	    ServiceResponse serviceResponse = rewardsService.getAllRewardsByRewardId(id);
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
	
	@DeleteMapping("/deleteRewardsByRewardId/{id}")
    public ServiceResponse deleteRewardsByRewardId(@PathVariable Long id) {
        ServiceResponse serviceResponse = rewardsService.deleteRewardsByRewardId(id);
        return serviceResponse;
    }
	
	@PostMapping("/submitRewardForEmployee")
	public ServiceResponse submitRewardForEmployee(@RequestBody EmployeeRewardsDTO EmployeeRewardsDTO)
	{
		ServiceResponse serviceResponse = rewardsService.submitRewardForEmployee(EmployeeRewardsDTO);
        return serviceResponse;
	}
	
	@PostMapping("/updateRewardsForEmployees")
	public ServiceResponse updateRewardsForEmployees (@RequestBody EmployeeRewardsDTO EmployeeRewardsDTO) {
		ServiceResponse serviceResponse = rewardsService.updateRewardsForEmployees(EmployeeRewardsDTO);
        return serviceResponse;
	}
	
	@PostMapping("/bulkDisableRewards")
	public ServiceResponse bulkDisableRewards() {
	    ServiceResponse serviceResponse = rewardsService.bulkDisableRewards();
	    return serviceResponse;
	}
	
	@PostMapping("/bulkEnableRewards")
	public ServiceResponse bulkEnableRewards(@RequestBody List<String> monthyears) {
	    return rewardsService.bulkEnableRewards(monthyears);
	}
	
	@RequestMapping(value = "/showAllEmployeeRewards", method = RequestMethod.GET)
	public ServiceResponse showAllEmployeeRewards() {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = rewardsService.showAllEmployeeRewards();
		return serviceResponse;
	}
	
	@PostMapping("/getAllActiveTeams")
	public ServiceResponse getAllActiveTeams(@RequestBody RewardConfigurationDTO rewardConfigurationDTO) {
    ServiceResponse serviceResponse = new ServiceResponse();
//    System.out.println("Received request with isTeam: " + rewardConfigurationDTO.getIsTeam());
		serviceResponse = rewardsService.getAllActiveTeams(rewardConfigurationDTO);
		return serviceResponse;
	}
	
	@PostMapping("/isActive")
	public ServiceResponse isActive(@RequestBody EmployeeRewardsDTO employeeRewardsDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = rewardsService.isActive(employeeRewardsDTO);
		return serviceResponse;
	}
	
	
	
	@RequestMapping(value = "/getEmployeeRewardByRewardId/{id}", method = RequestMethod.GET)
	public ServiceResponse getEmployeeRewardByRewardId(@PathVariable("id") Long employeerewardId) { 
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = rewardsService.getEmployeeRewardByRewardIdd(employeerewardId);
		return serviceResponse;
	}
	
	@RequestMapping(value = "/fetchEmployeesForHomepage", method = RequestMethod.GET)
	 public ServiceResponse fetchEmployeesForHomepage() {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = rewardsService.fetchEmployeesForHomepage();
	        return serviceResponse;
	    }
	
//	@RequestMapping(value = "/fetchEmployeesHomepagecurrentmonth/{currentmonth}", method = RequestMethod.GET)
//	 public ServiceResponse fetchEmployeesHomepagecurrentmonth(@PathVariable("currentmonth") String currentmonth ) {
//		ServiceResponse serviceResponse = new ServiceResponse();
//		serviceResponse = rewardsService.fetchEmployeesHomepagecurrentmonth(currentmonth);
//	        return serviceResponse;
//	    }
	
	@DeleteMapping("/deleteEmployeeRewardByRewardId/{id}")
	public ServiceResponse deleteEmployeeRewardByRewardId(@PathVariable Long id) { 
	    ServiceResponse serviceResponse = new ServiceResponse();
	    serviceResponse = rewardsService.deleteEmployeeRewardByRewardId(id);
	    return serviceResponse;
	}
	
	@PostMapping("/fetchEmployeesByTeamId")
	public ServiceResponse fetchEmployeesByTeamId(@PathVariable Long teamId) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    serviceResponse = rewardsService.fetchEmployeesByTeamId(teamId);
	    return serviceResponse;
	}
	
	
}

package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.CustomFilterDTO;
import com.apmosys.employeeportal.dto.DocumentDTO;
import com.apmosys.employeeportal.dto.RewardCategoryDTO;
import com.apmosys.employeeportal.dto.RewardConfigurationDTO;
import com.apmosys.employeeportal.model.RewardConfig;
import com.apmosys.employeeportal.repository.RewardConfigRepository;
import com.apmosys.employeeportal.repository.RewardsCategoryRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class RewardsService {

	@Autowired
	RewardsCategoryRepository rewardsCategoryRepository;

	@Autowired
	RewardConfigRepository rewardConfigRepository;

	public ServiceResponse getAllRewardsCategory() {

		ServiceResponse serviceResponse = new ServiceResponse();

		List<Object[]> getAllRewardsCategory = rewardsCategoryRepository.findAllRewardsCategory();
		List<RewardCategoryDTO> listRewardCategory = new ArrayList<>();

		getAllRewardsCategory.forEach((type) -> {
			RewardCategoryDTO reward = new RewardCategoryDTO();
			reward.setRewardCategoryId(type[0] != null ? Long.parseLong(type[0].toString()) : null);
			reward.setCategoryName(type[1] != null ? type[1].toString() : null);
			listRewardCategory.add(reward);
		});

		if (listRewardCategory != null) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(listRewardCategory);
		} else {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("There is no type present");
		}

		return serviceResponse;

	}

	public ServiceResponse saveRewardConfiguration(RewardConfigurationDTO rewardConfigurationDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();

		try {
			
		    RewardConfig existRewardConfig = rewardConfigRepository.findByRewardName(rewardConfigurationDTO.getRewardName());
			
		    if(existRewardConfig!=null)
		    {
		    	serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("RewardName Already Exist");
				return serviceResponse;
		    }
			
			RewardConfig rewardConfig = new RewardConfig();

			rewardConfig.setCategoryId(rewardConfigurationDTO.getCategoryId());
			rewardConfig.setRewardName(rewardConfigurationDTO.getRewardName());
			rewardConfig.setRewardType(getRewardType(rewardConfigurationDTO.getRewardTypes()));
			rewardConfig.setRewardCondition(getRewardCondition(rewardConfigurationDTO.getCustomFilterDTOList()));

			rewardConfigRepository.save(rewardConfig);

			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse("Reward configuration successfully saved");

		} catch (Exception e) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceError(e.getMessage());
		}

		return serviceResponse;
	}

	private String getRewardCondition(List<CustomFilterDTO> queryList) {

		StringBuilder query = new StringBuilder("");
		System.err.println(" queryList ::   " + queryList);
		for (CustomFilterDTO dto : queryList) {
			if (dto.getOperator() != null && dto.getOperator().equals("like")) {
				dto.setValue("%" + dto.getValue() + "%");
			}

			switch (dto.getColumn()) {
			
			case "Employee Id": {
				query = query.append(" e.employeement_id ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Full Name": {
				query = query.append(" e.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				;
				break;
			}
			case "Employment Status": {
				query = query.append(" e.employmentstatus ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Date Of Joining": {
				query = query.append(" e.date_of_joining ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}

			case "Gender": {
				query = query.append(" e.gender ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				;
				break;
			}

			case "Probation Period": {
				query = query.append(" e.probation_period ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Notice Period": {
				query = query.append(" e.notice_period ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				;
				break;
			}
			case "Department": {
				query = query.append(" d.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				;
				break;
			}
			case "Job Role": {
				query = query.append(" jr.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				;
				break;
			}
			case "Designation": {
				query = query.append(" de.designation_name ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Manager": {
				query = query.append(" m.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Experience": {
				query = query.append(" e.total_experience ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			case "Team Name": {
				query = query.append(" v.team_name ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			case "Project Name": {
				query = query.append("  v.project_name ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			case "Client Name": {
				query = query.append(" v.client_name ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}

			default:
				break;
			}
		}
		return query.toString();

	}

	private String getRewardType(List<String> rewardTypes) {

		return String.join(",", rewardTypes);
	}

}

package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.hibernate.Query;

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
	
	@PersistenceContext
	private EntityManager entityManager;

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
			rewardConfig.setCreatedBy(rewardConfigurationDTO.getCreatedBy());

			rewardConfigRepository.save(rewardConfig);

			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse("Reward configuration successfully saved");

		} catch (Exception e) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceError(e.getMessage());
		}

		return serviceResponse;
	}
	
	public ServiceResponse editRewardConfiguration(RewardConfigurationDTO rewardConfigurationDTO) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        
	        RewardConfig existingRewardConfig = rewardConfigRepository.findById(rewardConfigurationDTO.getId())
	                .orElseThrow(() -> new RuntimeException("Reward configuration not found"));

	        existingRewardConfig.setCategoryId(rewardConfigurationDTO.getCategoryId());
	        existingRewardConfig.setRewardName(rewardConfigurationDTO.getRewardName());
	        existingRewardConfig.setRewardType(getRewardType(rewardConfigurationDTO.getRewardTypes()));
	        existingRewardConfig.setRewardCondition(getRewardCondition(rewardConfigurationDTO.getCustomFilterDTOList()));
	        existingRewardConfig.setUpdatedBy(rewardConfigurationDTO.getUpdatedBy());
	        existingRewardConfig.setUpdatedOn(LocalDateTime.now());
	        
	        rewardConfigRepository.save(existingRewardConfig);

	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse("Reward configuration successfully updated");
	        
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

	public ServiceResponse fetchEmployeesFromRewardCondition(Long rewardId) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    List<RewardConfigurationDTO> reportDTOList = new ArrayList<>();

	    try {
	    	
	        RewardConfig rewardConfig = rewardConfigRepository.findById(rewardId)
	                .orElseThrow(() -> new IllegalArgumentException("Invalid rewardId: " + rewardId));

	        String rewardCondition = rewardConfig.getRewardCondition(); 
	       
	        Session session = entityManager.unwrap(Session.class);
	        
	        try {
	             String q = "SELECT e.name,e.emp_Id, m.name AS manager, m.emp_Id as managerEmpId "
	                    + "FROM employee e "
	                    + "INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id "
	                    + "INNER JOIN department d ON jr.dept_id = d.dept_id "
	                    + "LEFT JOIN designation de ON e.designation_id = de.designation_id "
	                    + "LEFT JOIN employee m ON e.manager_id = m.emp_id "
	                    + "LEFT JOIN employee_team_mapping etm ON etm.emp_id = e.emp_id "
	                    + "WHERE " + rewardCondition;

	            Query query = session.createSQLQuery(q);

	            List<Object[]> resultList = query.getResultList();
	            
	            resultList.forEach((result) -> {
	                RewardConfigurationDTO dto = new RewardConfigurationDTO();
	                dto.setId(rewardId);
	                dto.setEmployeeName(result[0] != null ? result[0].toString() : null);
	                dto.setEmployeeEmpId(result[1] != null 
	                    ? (result[1] instanceof BigInteger ? ((BigInteger) result[1]).toString() : result[1].toString()) 
	                    : null);
	                dto.setManagerName(result[2] != null ? result[2].toString() : null);
	                dto.setManagerEmpId(result[3] != null 
	                    ? (result[3] instanceof BigInteger ? ((BigInteger) result[3]).toString() : result[3].toString()) 
	                    : null);
	                
	                reportDTOList.add(dto);
	            });

	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            if (session != null && session.isOpen()) {
	                session.close();
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    if (reportDTOList != null && !reportDTOList.isEmpty()) {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(reportDTOList);
	    } else {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceResponse("No employees found for the given condition.");
	    }

	    return serviceResponse;
	}

	private String getRewardType(List<String> rewardTypes) {

		return String.join(",", rewardTypes);
	}
	
	public ServiceResponse getAllRewardsByCategoryId(Integer categoryId) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    
	    List<RewardConfig> rewards = rewardConfigRepository.findByCategoryId(categoryId); 
	    List<RewardConfigurationDTO> rewardList = new ArrayList<>();
	    
	    for (RewardConfig reward : rewards) {
	        RewardConfigurationDTO dto = new RewardConfigurationDTO();
	        dto.setId(reward.getId());
	        dto.setRewardName(reward.getRewardName());
	        dto.setCategoryId(reward.getCategoryId());
	        dto.setRewardTypes(Collections.singletonList(reward.getRewardType()));
	        
	        rewardList.add(dto);
	    }
	    
	    if (!rewardList.isEmpty()) {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(rewardList);
	    } else {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceResponse("No rewards found for the provided category ID");
	    }
	    
	    return serviceResponse;
	}
	
	public ServiceResponse getAllRewardsByRewardId(Long id) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    
	    Optional<RewardConfig> optionalReward = rewardConfigRepository.findById(id); 
	    List<RewardConfigurationDTO> rewardList = new ArrayList<>();
	    
	    if (optionalReward.isPresent()) {
	        RewardConfig reward = optionalReward.get();
	        
	        RewardConfigurationDTO dto = new RewardConfigurationDTO();
	        dto.setId(reward.getId() != null ? reward.getId() : null);
	        dto.setRewardName(reward.getRewardName() != null ? reward.getRewardName() : null);
	        dto.setCategoryId(reward.getCategoryId() != null ? reward.getCategoryId() : null);
	        dto.setRewardTypes(reward.getRewardType() != null ? Collections.singletonList(reward.getRewardType()) : Collections.emptyList());
	        
	        rewardList.add(dto);
	        
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(rewardList);
	    } else {
	    	
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceResponse("No rewards found for the provided reward ID");
	    }
	    
	    return serviceResponse;
	}

	public ServiceResponse deleteRewardsByRewardId(Long id) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    
	    Optional<RewardConfig> optionalReward = rewardConfigRepository.findById(id); 
	    
	    if (optionalReward.isPresent()) {
	    	
	    	rewardConfigRepository.deleteById(id);
	        
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse("Reward deleted successfully");
	    } else {
	    	
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceResponse("No rewards found for the provided reward ID");
	    }
	    
	    return serviceResponse;
	}

	public ServiceResponse showAllRewards() {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    List<RewardConfig> rewardsConfig = rewardConfigRepository.findAll();
	    List<RewardCategoryDTO> rewardCategories = rewardsCategoryRepository.findAll()
	        .stream()
	        .map(category -> {
	            RewardCategoryDTO dto = new RewardCategoryDTO();
	            dto.setRewardCategoryId(category.getRewardCategoryId());
	            dto.setCategoryName(category.getCategoryName());
	            return dto;
	        })
	        .collect(Collectors.toList());
	    if (!rewardsConfig.isEmpty()) {
	        List<RewardConfigurationDTO> rewardDTOList = rewardsConfig.stream().map(reward -> {
	            RewardConfigurationDTO rewardDTO = new RewardConfigurationDTO();
	            rewardDTO.setRewardName(reward.getRewardName());
	            rewardDTO.setCategoryId(reward.getCategoryId());
	            rewardDTO.setRewardTypes(Collections.singletonList(reward.getRewardType())); 
	            String categoryName = rewardCategories.stream()
	                .filter(category -> category.getRewardCategoryId().equals((long) reward.getCategoryId()))
	                .map(RewardCategoryDTO::getCategoryName)
	                .findFirst()
	                .orElse(null);
	            rewardDTO.setCategoryName(categoryName); 
	            return rewardDTO;
	        }).collect(Collectors.toList());
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(rewardDTOList);
	    } else {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceResponse("No rewards found");
	    }
	    return serviceResponse;
	}

}

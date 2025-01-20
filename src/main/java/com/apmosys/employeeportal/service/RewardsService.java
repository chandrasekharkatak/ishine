package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.AppreciationDetails;
import com.apmosys.employeeportal.dto.AppreciationDetailsDTO;
import com.apmosys.employeeportal.dto.CustomFilterDTO;
import com.apmosys.employeeportal.dto.EmployeeAppreciationRequest;
import com.apmosys.employeeportal.dto.EmployeeRewardForHomeDTO;
import com.apmosys.employeeportal.dto.EmployeeRewardsDTO;
import com.apmosys.employeeportal.dto.EmployeeRewardsRequest;
import com.apmosys.employeeportal.dto.RewardCategoryDTO;
import com.apmosys.employeeportal.dto.RewardConfigurationDTO;
import com.apmosys.employeeportal.dto.RewardsDetails;
import com.apmosys.employeeportal.model.CommonProperties;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeRewards;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.RewardConfig;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeRewardsRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.RewardConfigRepository;
import com.apmosys.employeeportal.repository.RewardsCategoryRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RewardsService {
	
	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	RewardsCategoryRepository rewardsCategoryRepository;

	@Autowired
	RewardConfigRepository rewardConfigRepository;
	
	@Autowired
    private EmployeeRewardsRepository employeeRewardsRepository;
	
	@Autowired
    private EmployeeRepository employeeRepository;
	
	@Autowired
    private TeamRepository teamRepository;
	
	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;

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

	@Transactional
	public ServiceResponse saveRewardConfiguration(RewardConfigurationDTO rewardConfigurationDTO) {
	    ServiceResponse serviceResponse = new ServiceResponse();

	    try {
	        RewardConfig existRewardConfig = rewardConfigRepository.findByRewardName(rewardConfigurationDTO.getRewardName());

	        if (existRewardConfig != null) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceResponse("RewardName Already Exist");
	            return serviceResponse;
	        }

	        if (rewardConfigurationDTO.getRewardName() == null || rewardConfigurationDTO.getRewardName().isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceResponse("RewardName cannot be empty");
	            return serviceResponse;
	        }

	        RewardConfig rewardConfig = new RewardConfig();
	      
	        rewardConfig.setCategoryId(rewardConfigurationDTO.getCategoryId() != 0 ? rewardConfigurationDTO.getCategoryId() : 0);
	        rewardConfig.setRewardName(rewardConfigurationDTO.getRewardName() != null ? rewardConfigurationDTO.getRewardName() : "");
	        rewardConfig.setRewardType(rewardConfigurationDTO.getRewardTypes() != null ? getRewardType(rewardConfigurationDTO.getRewardTypes()) : null);
	        rewardConfig.setRewardCondition(rewardConfigurationDTO.getCustomFilterDTOList() != null ? getRewardCondition(rewardConfigurationDTO.getCustomFilterDTOList()) : "");
	        
	        List<CustomFilterDTO> customFilterDTOList = rewardConfigurationDTO.getCustomFilterDTOList();
	        
//	        System.out.printf("Custom Filter DTO List: {}", customFilterDTOList);

	        if (customFilterDTOList == null || customFilterDTOList.isEmpty()) {
	            customFilterDTOList = new ArrayList<>();
	            CustomFilterDTO defaultFilter = new CustomFilterDTO();
	            defaultFilter.setColumn("");       
	            defaultFilter.setOperator("");
	            defaultFilter.setValue("");
	            defaultFilter.setConjunction("");
	            defaultFilter.setCustomQuery("");
	            customFilterDTOList.add(defaultFilter);
	        } else {
	                for (CustomFilterDTO filter : customFilterDTOList) {
	                    System.out.println("Custom Filter - Column: " + filter.getColumn() + 
	                                       ", Operator: " + filter.getOperator() + 
	                                       ", Value: " + filter.getValue() + 
	                                       ", Conjunction: " + filter.getConjunction() + 
	                                       ", CustomQuery: " + filter.getCustomQuery());
	                }
	            }
	            
	        ObjectMapper objectMapper = new ObjectMapper();
	        String filtersJson = objectMapper.writeValueAsString(customFilterDTOList);
	        rewardConfig.setFilterConditions(filtersJson);
	        
	        rewardConfig.setIsTeam(rewardConfigurationDTO.getIsTeam() != 0 ? rewardConfigurationDTO.getIsTeam() : 0);
	        
	        CommonProperties commonProperties = new CommonProperties();
		    commonProperties.setCreatedBy(rewardConfigurationDTO.getCreatedBy() != 0 ? rewardConfigurationDTO.getCreatedBy() : null);
//		    commonProperties.setUpdatedOn(LocalDateTime.now());
		    
		    rewardConfig.setCommonProperty(commonProperties);

	        rewardConfigRepository.save(rewardConfig);

	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse("Reward configuration successfully saved");

	    } catch (Exception e) {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceError("Error occurred: " + e.getMessage());
	        e.printStackTrace();
	    }

	    return serviceResponse;
	}
	
	@Transactional
	public ServiceResponse editRewardConfiguration(RewardConfigurationDTO rewardConfigurationDTO) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        RewardConfig existingRewardConfig = rewardConfigRepository.findById(rewardConfigurationDTO.getId())
	                .orElseThrow(() -> new RuntimeException("Reward configuration not found"));

	        existingRewardConfig.setCategoryId(rewardConfigurationDTO.getCategoryId() != 0 ? rewardConfigurationDTO.getCategoryId() : 0);
	        existingRewardConfig.setRewardName(rewardConfigurationDTO.getRewardName() != null ? rewardConfigurationDTO.getRewardName() : "");
	        existingRewardConfig.setRewardType(rewardConfigurationDTO.getRewardTypes() != null ? getRewardType(rewardConfigurationDTO.getRewardTypes()) : null);
	        existingRewardConfig.setRewardCondition(rewardConfigurationDTO.getCustomFilterDTOList() != null ? getRewardCondition(rewardConfigurationDTO.getCustomFilterDTOList()) : "");
	        existingRewardConfig.setIsTeam(rewardConfigurationDTO.getIsTeam() != 0 ? rewardConfigurationDTO.getIsTeam() : 0);
	       

	        ObjectMapper objectMapper = new ObjectMapper();
	        String filtersJson = rewardConfigurationDTO.getCustomFilterDTOList() != null ? objectMapper.writeValueAsString(rewardConfigurationDTO.getCustomFilterDTOList()) : "[]";
	        existingRewardConfig.setFilterConditions(filtersJson);
	        
	        CommonProperties commonProperties = new CommonProperties();
	        
	        commonProperties.setCreatedBy(rewardConfigurationDTO.getCreatedBy() != 0 ? rewardConfigurationDTO.getCreatedBy() : null);
		    commonProperties.setUpdatedBy(rewardConfigurationDTO.getUpdatedBy() != 0 ? rewardConfigurationDTO.getUpdatedBy() : null);
		    commonProperties.setUpdatedOn(LocalDateTime.now());
		    
		    existingRewardConfig.setCommonProperty(commonProperties);
		    
		
		    
		    
	        
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

	    if (queryList == null || queryList.isEmpty()) {
	        System.err.println("queryList is null or empty.");
	        return query.toString();
	    }

//	    System.err.println("queryList: " + queryList);

	    for (CustomFilterDTO dto : queryList) {
	        if (dto == null) {
	            System.err.println("Found null CustomFilterDTO in the list.");
	            continue;
	        }

	        if ("like".equals(dto.getOperator()) && dto.getValue() != null) {
	            dto.setValue("%" + dto.getValue() + "%");
	        }

	        if (dto.getColumn() == null || dto.getOperator() == null || dto.getValue() == null) {
	            System.err.println("Invalid CustomFilterDTO: " + dto);
	            continue;
	        }

	        switch (dto.getColumn()) {
	            case "Employee Id":
	                query.append(" e.employeement_id ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Full Name":
	                query.append(" e.name ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Employment Status":
	                query.append(" e.employmentstatus ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Date Of Joining":
	                query.append(" e.date_of_joining ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Gender":
	                query.append(" e.gender ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Probation Period":
	                query.append(" e.probation_period ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Notice Period":
	                query.append(" e.notice_period ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Department":
	                query.append(" d.name ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Job Role":
	                query.append(" jr.name ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Designation":
	                query.append(" de.designation_name ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Manager":
	                query.append(" m.name ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Experience":
	                query.append(" e.total_experience ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Team Name":
	                query.append(" v.team_name ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Project Name":
	                query.append(" v.project_name ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "Client Name":
	                query.append(" v.client_name ").append(dto.getOperator()).append(" '").append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            default:
	                System.err.println("Unknown column: " + dto.getColumn());
	                break;
	        }
	    }

	    if (query.length() > 0 && query.lastIndexOf(" ") != -1) {
	        int lastSpaceIndex = query.lastIndexOf(" ");
	        query.delete(lastSpaceIndex, query.length());
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
	             String q = "SELECT DISTINCT e.name,e.emp_Id, m.name AS manager, m.emp_Id as managerEmpId "
	                    + "FROM employee e "
	                    + "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id "
	                    + "LEFT JOIN department d ON jr.dept_id = d.dept_id "
	                    + "LEFT JOIN designation de ON e.designation_id = de.designation_id "
	                    + "LEFT JOIN employee m ON e.manager_id = m.emp_id "
	                    + "LEFT JOIN employee_team_mapping etm ON etm.emp_id = e.emp_id "
	                    + "WHERE " + rewardCondition
	                    + "GROUP BY e.name, e.emp_Id, m.name, m.emp_Id";

	            Query query = session.createSQLQuery(q);

	            List<Object[]> resultList = query.getResultList();
	            
	            resultList.forEach((result) -> {
//	            	System.out.println("Query List Data");
//	            	System.out.println(result[0]+ " " +result[1]+ " " +result[2]+ " " +result[3]);
	                RewardConfigurationDTO dto = new RewardConfigurationDTO();
//	                System.out.println("Data setting in dto list");
	                dto.setId(rewardId);
	                dto.setEmployeeName(result[0] != null ? result[0].toString() : null);
	                dto.setEmployeeEmpId(result[1] != null 
	                    ? (result[1] instanceof BigInteger ? ((BigInteger) result[1]).toString() : result[1].toString()) 
	                    : null);
	                dto.setManagerName(result[2] != null ? result[2].toString() : null);
	                dto.setManagerEmpId(result[3] != null 
	                    ? (result[3] instanceof BigInteger ? ((BigInteger) result[3]).toString() : result[3].toString()) 
	                    : null);
	                
//	                System.out.println("Setting DTO values:");
//	                System.out.println("Reward ID: " + dto.getId());
//	                System.out.println("Employee Name: " + dto.getEmployeeName());
//	                System.out.println("Employee Emp ID: " + dto.getEmployeeEmpId());
//	                System.out.println("Manager Name: " + dto.getManagerName());
//	                System.out.println("Manager Emp ID: " + dto.getManagerEmpId());
	                
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
	
	public ServiceResponse getAllActiveTeams(RewardConfigurationDTO rewardConfigurationDTO) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        List<Team> teams = new ArrayList<>();
//	        System.out.println("Incoming request for active teams: isTeam = " + rewardConfigurationDTO.getIsTeam());

	        if (rewardConfigurationDTO.getIsTeam() == 1) {
	            teams = teamRepository.findByIsActiveNot("N");
	        }

	        if (teams != null && !teams.isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(teams);
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceResponse("No active teams found.");
	        }
	    } catch (Exception e) {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceResponse("Error fetching active teams: " + e.getMessage());
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
	        dto.setIsTeam(reward.getIsTeam());
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
	        
	        CommonProperties commonProperties = reward.getCommonProperty();
	        
	        RewardConfigurationDTO dto = new RewardConfigurationDTO();
	        dto.setId(reward.getId());
	        dto.setRewardName(reward.getRewardName());
	        dto.setCategoryId(reward.getCategoryId());
	        dto.setRewardTypes(Arrays.asList(reward.getRewardType().split(",")));
	        dto.setIsTeam(reward.getIsTeam());
	        
	        if(commonProperties.getCreatedBy() == null) {
	        	dto.setCreatedBy(0L);
	        } else {
	        	  dto.setCreatedBy(commonProperties.getCreatedBy());
	        }  
	        
	      
//	        dto.setCreatedOn(commonProperties.getCreatedOn().toLocalDateTime() != null ? commonProperties.getCreatedOn().toLocalDateTime() : null);
	        
	        
	        
	        String filterConditionsJson = reward.getFilterConditions();
	        if (filterConditionsJson != null && !filterConditionsJson.isEmpty()) {
	            ObjectMapper objectMapper = new ObjectMapper();
	            try {
	                List<CustomFilterDTO> filterConditions = objectMapper.readValue(filterConditionsJson, new TypeReference<List<CustomFilterDTO>>() {});
	                dto.setCustomFilterDTOList(filterConditions);
	            } catch (Exception e) {
	                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                serviceResponse.setServiceResponse("Error parsing filter conditions: " + e.getMessage());
	            }
	        } else {
	            dto.setCustomFilterDTOList(Collections.emptyList());  
	        }
	        
	        rewardList.add(dto);
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(rewardList);
	    } else {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceResponse("No rewards found for the provided reward ID");
	    }
	    
	    return serviceResponse;
	}

	@Transactional
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
	    try {
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

	        if (rewardsConfig != null && !rewardsConfig.isEmpty()) {
	            List<RewardConfigurationDTO> rewardDTOList = rewardsConfig.stream()
	                .map(reward -> {
	                	 CommonProperties commonProperties = reward.getCommonProperty();
	                	System.out.println("reward  *****"+reward);
	                	
	                	
	          
	                	
	                    RewardConfigurationDTO rewardDTO = new RewardConfigurationDTO();

	                    
	                    
	                    if (commonProperties.getCreatedOn() == null) {
	                        rewardDTO.setCreatedOn(null);
	                    } else {
	                        rewardDTO.setCreatedOn(commonProperties.getCreatedOn().toLocalDateTime());
	                    }

	                    if (commonProperties.getUpdatedBy() == null) {
	                        rewardDTO.setUpdatedByName(null); // Assuming updatedByName is correct
	                    } else {
	                        rewardDTO.setUpdatedByName(getEmployeeNameByEmpId(commonProperties.getUpdatedBy()));   
	                    } 

	                    if (commonProperties.getUpdatedOn() == null) {
	                        rewardDTO.setUpdatedOn(null);
	                    } else {
	                        rewardDTO.setUpdatedOn(commonProperties.getUpdatedOn());
	                    }

	                    if (commonProperties.getCreatedBy() == null) {
	                        rewardDTO.setCreatedByName(null); 
	                    } else {
	                        rewardDTO.setCreatedByName(getEmployeeNameByEmpId(commonProperties.getCreatedBy()));
	                    }

	                	


	                    
	                    
	                    rewardDTO.setRewardName(reward.getRewardName() != null ? reward.getRewardName() : null);
                    
	                    rewardDTO.setId(reward.getId() != null ? reward.getId() : 0L);  // Default value for long
	                    rewardDTO.setCategoryId(reward.getCategoryId() != null ? reward.getCategoryId() : 0);  // Default value for int
	                    
	                    
                       
	                   

	                    rewardDTO.setRewardTypes(
	                        reward.getRewardType() != null ? Collections.singletonList(reward.getRewardType()) : Collections.emptyList()
	                        		
	                        		
	                        		
	                        		
	                        		
	                        		
	                    );
	                    
	                    
	                    

//	                    try {
//	                        CommonProperties commonProperties = reward.getCommonProperty();
//	                        if (commonProperties != null) {
//	                            rewardDTO.setCreatedBy(commonProperties.getCreatedBy() != null ? commonProperties.getCreatedBy() : null);
//	                            rewardDTO.setUpdatedBy(commonProperties.getUpdatedBy() != null ? commonProperties.getUpdatedBy() : null);
//	                            rewardDTO.setUpdatedOn(commonProperties.getUpdatedOn() != null ? commonProperties.getUpdatedOn() : null);
//	                            rewardDTO.setCreatedOn(commonProperties.getCreatedOn().toLocalDateTime() != null ? commonProperties.getCreatedOn().toLocalDateTime() : null);
//
//	                            rewardDTO.setCreatedByName(getEmployeeNameByEmpId(commonProperties.getCreatedBy()) != null ? getEmployeeNameByEmpId(commonProperties.getCreatedBy()) : null);
//	                            rewardDTO.setUpdatedByName(getEmployeeNameByEmpId(commonProperties.getUpdatedBy()) != null ? getEmployeeNameByEmpId(commonProperties.getUpdatedBy()) : null);
//	                        }
//	                    } catch (Exception e) {
//	                        rewardDTO.setCreatedByName(null);
//	                        rewardDTO.setUpdatedByName(null);
//	                        System.err.println("An error occurred while processing CommonProperties: " + e.getMessage());
//	                    }

	                    String categoryName = rewardCategories.stream()
	                        .filter(category -> category.getRewardCategoryId() != null && reward.getCategoryId() != null &&
	                                            category.getRewardCategoryId().equals((long) reward.getCategoryId()))
	                        .map(RewardCategoryDTO::getCategoryName)
	                        .findFirst()
	                        .orElse(null);
	                    rewardDTO.setCategoryName(categoryName != null ? categoryName : null);

	                    return rewardDTO;
	                }).collect(Collectors.toList());

	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(rewardDTOList);
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceResponse("No rewards found");
	        }
	    } catch (Exception e) {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceResponse("An error occurred while fetching rewards: " + e.getMessage());
	    }
	    return serviceResponse;
	}

	@Transactional
	public ServiceResponse submitRewardForEmployee(EmployeeRewardsDTO employeeRewardsDTO) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    
	    try {
	        if (employeeRewardsDTO.getTeamId() != null && employeeRewardsDTO.getRewardedTo() == null) {
	            List<Long> employeeIds = teamRepository.findEmployeeIdsByTeamId(employeeRewardsDTO.getTeamId());
	            
	            for (Long empId : employeeIds) {
	                EmployeeRewards employeeReward = createEmployeeReward(employeeRewardsDTO, empId);
	                employeeRewardsRepository.save(employeeReward);
	            }
	        } else {
	            EmployeeRewards employeeReward = createEmployeeReward(employeeRewardsDTO, employeeRewardsDTO.getRewardedTo());
	            employeeRewardsRepository.save(employeeReward);
	        }

	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceMessage("Reward(s) submitted successfully.");

	    } catch (Exception e) {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceError(e.getMessage());
	    }

	    return serviceResponse;
	}

		@Transactional
		private EmployeeRewards createEmployeeReward(EmployeeRewardsDTO employeeRewardsDTO, Long empId) {
	    EmployeeRewards employeeRewards = new EmployeeRewards();
	    employeeRewards.setRewardedTo(empId);
	    employeeRewards.setId(employeeRewardsDTO.getId() != null ? employeeRewardsDTO.getId() : null);
	    employeeRewards.setTeamId(employeeRewardsDTO.getTeamId() != null ? employeeRewardsDTO.getTeamId() : 0);
	    employeeRewards.setRewardType(employeeRewardsDTO.getRewardType() != 0 ? employeeRewardsDTO.getRewardType() : 0);
	    employeeRewards.setManagerId(employeeRewardsDTO.getManagerId() != null ? employeeRewardsDTO.getManagerId() : null);
	    employeeRewards.setTeamLeadId(employeeRewardsDTO.getTeamLeadId() != null ? employeeRewardsDTO.getTeamLeadId() : null);
	    employeeRewards.setIsActive(employeeRewards.getIsActive() != 0 ? employeeRewards.getIsActive() : 0);
	    employeeRewards.setFromDate(employeeRewardsDTO.getFromDate() != null ? employeeRewardsDTO.getFromDate() : null);
	    employeeRewards.setToDate(employeeRewardsDTO.getToDate() != null ? employeeRewardsDTO.getToDate() : null);
	    employeeRewards.setRemark(employeeRewardsDTO.getRemark() != null ? employeeRewardsDTO.getRemark() : null);
	    employeeRewards.setRewardTypeName(employeeRewardsDTO.getRewardTypeName() != null ? employeeRewardsDTO.getRewardTypeName() : null);
	    employeeRewards.setOfmonthyear(employeeRewardsDTO.getOfmonthyear()!=null ? employeeRewardsDTO.getOfmonthyear() : null);    
	    CommonProperties commonProperties = new CommonProperties();
	    commonProperties.setCreatedBy(employeeRewardsDTO.getCreatedBy() != null ? employeeRewardsDTO.getCreatedBy() : null);
	    commonProperties.setUpdatedBy(employeeRewardsDTO.getUpdatedBy() != null ? employeeRewardsDTO.getUpdatedBy() : null);
	    commonProperties.setUpdatedOn(LocalDateTime.now());
	    
	    employeeRewards.setCommonProperty(commonProperties);
	    return employeeRewards;
	}
	
	private String getEmployeeNameByEmpId(Long empId) {
        Employee employee = employeeRepository.findByEmpId(empId);
        return employee != null ? employee.getName() : null;
    }

	public ServiceResponse showAllEmployeeRewards() {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    List<EmployeeRewardsDTO> rewardsDTOList = employeeRewardsRepository.findAll().stream()
	        .map(employeeRewards -> {
	        	
	            EmployeeRewardsDTO dto = new EmployeeRewardsDTO();
	            
	            dto.setRewardId(employeeRewards.getRewardId() != null ? employeeRewards.getRewardId() : 0L);
	            dto.setRewardedTo(employeeRewards.getRewardedTo() != null ? employeeRewards.getRewardedTo() : 0L);
	            dto.setRewardType(employeeRewards.getRewardType() != 0 ? employeeRewards.getRewardType() : 0);
	            dto.setRewardTypeName(employeeRewards.getRewardTypeName() != null ? employeeRewards.getRewardTypeName() : null);
	            dto.setManagerId(employeeRewards.getManagerId() != null ? employeeRewards.getManagerId() : null);
	            dto.setTeamLeadId(employeeRewards.getTeamLeadId() != null ? employeeRewards.getTeamLeadId() : null);
	            dto.setIsActive(employeeRewards.getIsActive() != 0 ? employeeRewards.getIsActive() : 0);
	            dto.setFromDate(employeeRewards.getFromDate() != null ? employeeRewards.getFromDate() : null);
	            dto.setToDate(employeeRewards.getToDate() != null ? employeeRewards.getToDate() : null);
	            dto.setRemark(employeeRewards.getRemark() != null ? employeeRewards.getRemark() : "");

	            dto.setCreatedBy(employeeRewards.getCommonProperty().getCreatedBy() != null ? employeeRewards.getCommonProperty().getCreatedBy() : 0L);
	            dto.setUpdatedBy(employeeRewards.getCommonProperty().getUpdatedBy() != null ? employeeRewards.getCommonProperty().getUpdatedBy() : 0L);
	            dto.setUpdatedOn(employeeRewards.getCommonProperty().getUpdatedOn() != null ? employeeRewards.getCommonProperty().getUpdatedOn() : null);
	            dto.setCreatedOn(employeeRewards.getCommonProperty().getCreatedOn().toLocalDateTime() != null ? employeeRewards.getCommonProperty().getCreatedOn().toLocalDateTime() : null);
	            
	            dto.setCreatedByName(getEmployeeNameByEmpId(employeeRewards.getCommonProperty().getCreatedBy()));
	            dto.setUpdatedByName(getEmployeeNameByEmpId(employeeRewards.getCommonProperty().getUpdatedBy()));
	            dto.setManagerName(getEmployeeNameByEmpId(employeeRewards.getManagerId()) != null ? getEmployeeNameByEmpId(employeeRewards.getManagerId()) : null);
	            dto.setRewardedToByName(getEmployeeNameByEmpId(employeeRewards.getRewardedTo()) != null ? getEmployeeNameByEmpId(employeeRewards.getRewardedTo()) : null);
	            return dto;
	        })
	        .collect(Collectors.toList());

	    if (!rewardsDTOList.isEmpty()) {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(rewardsDTOList);
	    } else {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceResponse("No rewards found");
	    }

	    return serviceResponse;
	}

	@Transactional
	public ServiceResponse isActive(EmployeeRewardsDTO employeeRewardsDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();
		
		try {
            Optional<EmployeeRewards> optionalEmployeeRewards = employeeRewardsRepository.findById(employeeRewardsDTO.getRewardId());

            if (optionalEmployeeRewards.isPresent()) {
                EmployeeRewards employeeRewards = optionalEmployeeRewards.get();

                employeeRewards.setIsActive(employeeRewardsDTO.getIsActive() != 0 ? employeeRewardsDTO.getIsActive() : 0);

                employeeRewardsRepository.save(employeeRewards);

                serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            } else {
                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
                serviceResponse.setServiceError("Reward not found with ID: " + employeeRewardsDTO.getRewardId());
            }

		} catch (Exception e) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceError(e.getMessage());
		}
			
		return serviceResponse;
	}
	
	public ServiceResponse getEmployeeRewardByRewardId(Long id) { 
		ServiceResponse serviceResponse = new ServiceResponse();
		
		Optional<EmployeeRewards> optionalEmployeeReward = employeeRewardsRepository.findById(id); 
	    List<EmployeeRewardsDTO> employeeRewardsDTO = new ArrayList<>();
	    
	    if (optionalEmployeeReward.isPresent()) {
	    	EmployeeRewards employeeReward = optionalEmployeeReward.get();
	        
	    	EmployeeRewardsDTO employeeRewards = new EmployeeRewardsDTO();
	        
	    	employeeRewards.setRewardId(employeeReward.getRewardId() != null ? employeeReward.getRewardId() : null);
	    	employeeRewards.setRewardedTo(employeeReward.getRewardedTo() != null ? employeeReward.getRewardedTo() : null);
	    	employeeRewards.setRewardedToByName(getEmployeeNameByEmpId(employeeReward.getRewardedTo()) != null ? getEmployeeNameByEmpId(employeeReward.getRewardedTo()) : null);
			employeeRewards.setRewardTypeName(employeeReward.getRewardTypeName() != null ? employeeReward.getRewardTypeName() : null);
			employeeRewards.setManagerId(employeeReward.getManagerId() != null ? employeeReward.getManagerId() : null);
			employeeRewards.setTeamLeadId(employeeReward.getTeamLeadId() != null ? employeeReward.getTeamLeadId() : null);
			employeeRewards.setIsActive(employeeReward.getIsActive() != 0 ? employeeReward.getIsActive() : 0);
			employeeRewards.setFromDate(employeeReward.getFromDate() != null ? employeeReward.getFromDate() : null);
			employeeRewards.setToDate(employeeReward.getToDate() != null ? employeeReward.getToDate() : null);
			employeeRewards.setRemark(employeeReward.getRemark() != null ? employeeReward.getRemark() : null);
            
			CommonProperties commonProperties = new CommonProperties();
			employeeRewards.setCreatedBy(commonProperties.getCreatedBy() != null ? commonProperties.getCreatedBy() : null);
			employeeRewards.setUpdatedBy(commonProperties.getUpdatedBy() != null ? commonProperties.getUpdatedBy() : null);
			employeeRewards.setUpdatedOn(LocalDateTime.now()); 
			
			employeeRewards.setCreatedByName(getEmployeeNameByEmpId(commonProperties.getCreatedBy()) != null ? getEmployeeNameByEmpId(commonProperties.getCreatedBy()) : null);
			employeeRewards.setUpdatedByName(getEmployeeNameByEmpId(commonProperties.getUpdatedBy()) != null ? getEmployeeNameByEmpId(commonProperties.getUpdatedBy()) : null);
			employeeRewards.setManagerName(getEmployeeNameByEmpId(employeeReward.getManagerId()) != null ? getEmployeeNameByEmpId(employeeReward.getManagerId()) : null);
//			employeeRewards.setUpdatedByName(getEmployeeNameByEmpId(commonProperties.getUpdatedBy()) != null ? getEmployeeNameByEmpId(commonProperties.getUpdatedBy()) : null);
			
	        employeeRewardsDTO.add(employeeRewards);
	        
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(employeeRewardsDTO);
	    } else {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceResponse("No rewards found for the provided reward ID");
	    }
		return serviceResponse;
	}
	
//	public ServiceResponse getEmployeeRewardByRewardId(Long id) { 
//	    ServiceResponse serviceResponse = new ServiceResponse();
//	    Optional<EmployeeRewards> optionalEmployeeReward = employeeRewardsRepository.findById(id); 
//	    List<EmployeeRewardsDTO> employeeRewardsDTO = new ArrayList<>();
//	    
//	    if (optionalEmployeeReward.isPresent()) {
//	        EmployeeRewards employeeReward = optionalEmployeeReward.get();
//	        EmployeeRewardsDTO employeeRewards = new EmployeeRewardsDTO();
//
//	        employeeRewards.setRewardId(employeeReward.getRewardId() != null ? employeeReward.getRewardId() : null);
//	        employeeRewards.setRewardedTo(employeeReward.getRewardedTo() != null ? employeeReward.getRewardedTo() : null);
//	        employeeRewards.setRewardedToByName(getEmployeeNameByEmpId(employeeReward.getRewardedTo()) != null ? getEmployeeNameByEmpId(employeeReward.getRewardedTo()) : null);
//	        employeeRewards.setManagerId(employeeReward.getManagerId() != null ? employeeReward.getManagerId() : null);
//	        employeeRewards.setTeamLeadId(employeeReward.getTeamLeadId() != null ? employeeReward.getTeamLeadId() : null);
//	        employeeRewards.setIsActive(employeeReward.getIsActive() != 0 ? employeeReward.getIsActive() : 0);
//	        employeeRewards.setFromDate(employeeReward.getFromDate() != null ? employeeReward.getFromDate() : null);
//	        employeeRewards.setToDate(employeeReward.getToDate() != null ? employeeReward.getToDate() : null);
//	        employeeRewards.setRemark(employeeReward.getRemark() != null ? employeeReward.getRemark() : null);
//
//	        Optional<RewardConfig> rewardConfigOptional = rewardConfigRepository.findById(employeeReward.getRewardId());
//	        if (rewardConfigOptional.isPresent()) {
//	            RewardConfig rewardConfig = rewardConfigOptional.get();
//	            employeeRewards.setRewardTypeName(rewardConfig.getRewardType() != null ? rewardConfig.getRewardType() : "");
//	        } else {
//	            employeeRewards.setRewardTypeName(""); 
//	        }
//
//
//	        // Setting additional fields
//	        CommonProperties commonProperties = new CommonProperties();
//	        employeeRewards.setCreatedBy(commonProperties.getCreatedBy() != null ? commonProperties.getCreatedBy() : null);
//	        employeeRewards.setUpdatedBy(commonProperties.getUpdatedBy() != null ? commonProperties.getUpdatedBy() : null);
//	        employeeRewards.setUpdatedOn(LocalDateTime.now()); 
//	        employeeRewards.setCreatedByName(getEmployeeNameByEmpId(commonProperties.getCreatedBy()) != null ? getEmployeeNameByEmpId(commonProperties.getCreatedBy()) : null);
//	        employeeRewards.setUpdatedByName(getEmployeeNameByEmpId(commonProperties.getUpdatedBy()) != null ? getEmployeeNameByEmpId(commonProperties.getUpdatedBy()) : null);
//	        employeeRewards.setManagerName(getEmployeeNameByEmpId(employeeReward.getManagerId()) != null ? getEmployeeNameByEmpId(employeeReward.getManagerId()) : null);
//	        
//	        employeeRewardsDTO.add(employeeRewards);
//	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	        serviceResponse.setServiceResponse(employeeRewardsDTO);
//	    } else {
//	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	        serviceResponse.setServiceResponse("No rewards found for the provided reward ID");
//	    }
//	    
//	    return serviceResponse;
//	}

	
	@Transactional
	public ServiceResponse deleteEmployeeRewardByRewardId(Long id) { 
	    ServiceResponse serviceResponse = new ServiceResponse();
	    
	    Optional<EmployeeRewards> optionalEmployeeReward = employeeRewardsRepository.findById(id);
	    
	    if (optionalEmployeeReward.isPresent()) {
	        employeeRewardsRepository.deleteById(id);
	        
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse("Reward successfully deleted.");
	        
	    } else {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceResponse("No rewards found for the provided reward ID.");
	    }
	    
	    return serviceResponse;
	}

	
//	public ServiceResponse fetchEmployeesForHomepage() {
//        ServiceResponse serviceResponse = new ServiceResponse();
//        
//        try {
//            List<List<Object>> employeeRewards = employeeRewardsRepository.fetchEmployeesForHomepage();
//            List<EmployeeRewardForHomeDTO> dtos = new ArrayList<>();
//
//            for (List<Object> rewardData : employeeRewards) {
//            	
//                Long rewardId = ((Number) rewardData.get(0)).longValue();
//                int isActive = (Boolean) rewardData.get(1) ? 1 : 0;
//                Long id = ((Number) rewardData.get(2)).longValue();
//                Long rewardedTo = ((Number) rewardData.get(3)).longValue();
//                String rewardedToByName = (String) rewardData.get(4);
//                 int rewardTypeID = ((Number) rewardData.get(5)).intValue();
//                Integer categoryId = (Integer) rewardData.get(6);
//                String rewardTypeName = (String) rewardData.get(7);
//                Long departmentId = ((Number) rewardData.get(8)).longValue();
//                String department = (String) rewardData.get(9);
//
//                EmployeeRewardForHomeDTO dto = new EmployeeRewardForHomeDTO(
//                    rewardId,
//                    isActive,
//                    id,
//                    rewardedTo,
//                    rewardedToByName,
//                    rewardTypeID,
//                    categoryId,
//                    rewardTypeName,
//                    departmentId,
//                    department
//                );
//
//                dtos.add(dto);
//            }
//
//            if (dtos.isEmpty()) {
//                serviceResponse.setServiceResponse("No data found");
//                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//            } else {
//                serviceResponse.setServiceResponse(dtos);
//                serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            }
//        } catch (Exception e) {
//            e.printStackTrace(); 
//            serviceResponse.setServiceResponse("Error occurred while fetching data");
//            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//        }
//        
//        return serviceResponse;
//    }
	
	public ServiceResponse fetchEmployeesHomepagecurrentmonth( String currentmonth) {
        ServiceResponse serviceResponse = new ServiceResponse();
        
        try {
            List<List<Object>> employeeRewards = employeeRewardsRepository.fetchEmployeesHomepagecurrentmonth(currentmonth);
            List<EmployeeRewardForHomeDTO> dtos = new ArrayList<>();

            for (List<Object> rewardData : employeeRewards) {
            	
                Long rewardId = ((Number) rewardData.get(0)).longValue();
                int isActive = (Boolean) rewardData.get(1) ? 1 : 0;
                Long id = ((Number) rewardData.get(2)).longValue();
                Long rewardedTo = ((Number) rewardData.get(3)).longValue();
                String rewardedToByName = (String) rewardData.get(4);
                 int rewardTypeID = ((Number) rewardData.get(5)).intValue();
                Integer categoryId = (Integer) rewardData.get(6);
                String rewardTypeName = (String) rewardData.get(7);
                Long departmentId = ((Number) rewardData.get(8)).longValue();
                String department = (String) rewardData.get(9);
                String categoryName = (String) rewardData.get(12);
                

                EmployeeRewardForHomeDTO dto = new EmployeeRewardForHomeDTO(
                    rewardId,
                    isActive,
                    id,
                    rewardedTo,
                    rewardedToByName,
                    rewardTypeID,
                    categoryId,
                    rewardTypeName,
                    departmentId,
                    department,
                    categoryName
                );

                dtos.add(dto);
            }

            if (dtos.isEmpty()) {
                serviceResponse.setServiceResponse("No data found");
                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
            } else {
                serviceResponse.setServiceResponse(dtos);
                serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            }
        } catch (Exception e) {
            e.printStackTrace(); 
            serviceResponse.setServiceResponse("Error occurred while fetching data");
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
        }
        
        return serviceResponse;
    }


	
	public ServiceResponse fetchEmployeesByTeamId(Long teamId) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    List<RewardConfigurationDTO> reportDTOList = new ArrayList<>();

	    try {
	        List<EmployeeTeamMap> employeeTeamMaps = employeeTeamMapRepository.findByTeamId(teamId);

	        if (employeeTeamMaps.isEmpty()) {
	            throw new IllegalArgumentException("No employees mapped to the teamId: " + teamId);
	        }

	        Session session = entityManager.unwrap(Session.class);
	        try {
	            for (EmployeeTeamMap map : employeeTeamMaps) {
	                Long empId = map.getEmpId();

	                String q = "SELECT e.name, e.emp_Id, m.name AS manager, m.emp_Id as managerEmpId " +
	                           "FROM employee e " +
	                           "LEFT JOIN employee m ON e.manager_id = m.emp_Id " +
	                           "WHERE e.emp_Id = :empId";

	                Query query = session.createSQLQuery(q);
	                query.setParameter("empId", empId);

	                List<Object[]> resultList = query.getResultList();

	                resultList.forEach(result -> {
	                    RewardConfigurationDTO dto = new RewardConfigurationDTO();
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
	            }

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
	        serviceResponse.setServiceResponse("No employees found for the given teamId.");
	    }

	    return serviceResponse;
	}

//	public RewardsDetails getEmployeeRewardByEmpId(EmployeeRewardsRequest request) {
//		
//		List<EmployeeRewardsDTO> rewardlist = null;
//		
//		if(request.getEmpId() != null) {
//			rewardlist = employeeRewardsRepository.getRewardByEmpId(request.getEmpId());
//		}
//		RewardsDetails rewardsDetails = new RewardsDetails();
//		rewardsDetails.setRewardsDTO(rewardlist);
//		return rewardsDetails;
//	}
	public RewardsDetails getEmployeeRewardByEmpId(EmployeeRewardsRequest request) {
	    List<EmployeeRewardsDTO> rewardList = new ArrayList<>();

	    // Convert String dates to LocalDate
	    LocalDate fromDate = null;
	    LocalDate toDate = null;

	    if (request.getFromDate() != null) {
	        fromDate = LocalDate.parse(request.getFromDate());
	    }
	    if (request.getToDate() != null) {
	        toDate = LocalDate.parse(request.getToDate());
	    }

	    if (request.getEmpId() != null) {
	      
	        List<Object[]> result = employeeRewardsRepository.getRewardByEmpIdWithDateRange(
	                request.getEmpId(), fromDate, toDate
	        );
	        for (Object[] row : result) {
	           
	        	String name = row[0].toString();
	            String rewardTypeName = (String) row[1];
	            Long rewardedTo = null;
	            if (row[2] instanceof BigInteger) {
	                rewardedTo = ((BigInteger) row[2]).longValue();  
	            } else if (row[1] instanceof Long) {
	                rewardedTo = (Long) row[2]; 
	            }
	            LocalDateTime createdOn = null;
	            if (row[3] instanceof Timestamp) {
	                createdOn = ((Timestamp) row[3]).toLocalDateTime();
	            }
	            String remark = row[4].toString();
	            Long updatedBy = null;
	            if (row[5] != null) {
	                updatedBy = ((Number) row[5]).longValue();
	            }
	            String teamName = row[6].toString();
	            EmployeeRewardsDTO dto = new EmployeeRewardsDTO();
	            dto.setRewardTypeName(rewardTypeName);
	            dto.setRewardedTo(rewardedTo);
	            dto.setCreatedOn(createdOn);
	            dto.setRemark(remark);
	            dto.setUpdatedBy(updatedBy);
	            dto.setName(name);
	            dto.setTeamName(teamName);

	            // Add DTO to the list
	            rewardList.add(dto);
	        }
	    }

	    // Return rewards details with the list of DTOs
	    RewardsDetails rewardsDetails = new RewardsDetails();
	    rewardsDetails.setRewardsDTO(rewardList);
	    return rewardsDetails;
	}



//	public RewardsDetails getTeamRewardByEmpId(EmployeeRewardsRequest request) {
//	    List<EmployeeRewardsDTO> rewardList = new ArrayList<>();
//		
//		// Convert String dates to LocalDate
//	    LocalDate fromDate = null;
//	    LocalDate toDate = null;
//
//	    if (request.getFromDate() != null) {
//	        fromDate = LocalDate.parse(request.getFromDate());
//	    }
//	    if (request.getToDate() != null) {
//	        toDate = LocalDate.parse(request.getToDate());
//	    }
//	    if (request.getEmpId() != null) {
//		      
//	        List<Object[]> result = employeeRewardsRepository.getRewardByTeamAndDateRange(
//	                request.getEmpId());
//	        for (Object[] row : result) {
//	           
//	            String name = (String) row[0];
//	            String rewardedTypeName = (String) row[1];
//	            Long rewardedTo = null;
//	            if (row[2] instanceof BigInteger) {
//	                rewardedTo = ((BigInteger) row[2]).longValue();  
//	            } else if (row[2] instanceof Long) {
//	                rewardedTo = (Long) row[2]; 
//	            }
//	            LocalDateTime createdOn = null;
//	            if (row[3] instanceof Timestamp) {
//	                createdOn = ((Timestamp) row[3]).toLocalDateTime();
//	            }
//	            Long createdBy = null;
//	            if (row[4] != null) {
//	            	createdBy = ((Number) row[4]).longValue();
//	            }
//	            Long empId=null;
//	            if (row[5] != null) {
//	            	empId = ((Number) row[5]).longValue();
//	            }
//	            
//	            EmployeeRewardsDTO dto = new EmployeeRewardsDTO();
//	            dto.setName(name);
//	            dto.setRewardTypeName(rewardedTypeName);
//	            dto.setRewardedTo(rewardedTo);
//	            dto.setCreatedOn(createdOn);
//	            dto.setCreatedBy(createdBy);
//	            dto.setEmpId(empId);
//
//	            // Add DTO to the list
//	            rewardList.add(dto);
//	        }
//	    }
//
//		RewardsDetails rewardDetails = new RewardsDetails();
//		rewardDetails.setRewardsDTO(rewardList);
//	    return rewardDetails;
//	}
	
	public RewardsDetails getTeamRewardByEmpId(EmployeeRewardsRequest request) {
	    List<EmployeeRewardsDTO> rewardList = new ArrayList<>();

	    // Convert String dates to LocalDate
	    LocalDate fromDate = parseDate(request.getFromDate());
	    LocalDate toDate = parseDate(request.getToDate());

	    if (request.getEmpId() != null) {
	        List<Object[]> result = employeeRewardsRepository.getRewardByTeamAndDateRange(request.getEmpId());

	        for (Object[] row : result) {
	            EmployeeRewardsDTO dto = mapRowToDTO(row);
	            rewardList.add(dto);
	        }
	    }

	    RewardsDetails rewardDetails = new RewardsDetails();
	    rewardDetails.setRewardsDTO(rewardList);
	    return rewardDetails;
	}

	private LocalDate parseDate(String date) {
	    return date != null ? LocalDate.parse(date) : null;
	}

	private EmployeeRewardsDTO mapRowToDTO(Object[] row) {
	    EmployeeRewardsDTO dto = new EmployeeRewardsDTO();

	    dto.setName(getStringValue(row[0]));
	    dto.setRewardTypeName((String) row[1]);
	    dto.setRewardedTo(getLongValue(row[2]));
	    dto.setCreatedOn(getLocalDateTime(row[3]));
	    dto.setCreatedByName(getStringValue(row[4]));

	    return dto;
	}

	private Long getLongValue(Object value) {
	    if (value instanceof BigInteger) {
	        return ((BigInteger) value).longValue();
	    } else if (value instanceof Long) {
	        return (Long) value;
	    }
	    return null;
	}
	
	private String getStringValue(Object value) {
	    return value != null ? value.toString() : null;
	}

	private LocalDateTime getLocalDateTime(Object value) {
	    if (value instanceof Timestamp) {
	        return ((Timestamp) value).toLocalDateTime();
	    }
	    return null;
	}

}
package com.apmosys.employeeportal.service;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.AppreciationDetails;
import com.apmosys.employeeportal.dto.AppreciationDetailsDTO;
import com.apmosys.employeeportal.dto.CustomFilterDTO;
import com.apmosys.employeeportal.dto.Employee360RewardsDTO;
import com.apmosys.employeeportal.dto.EmployeeAppreciationRequest;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeRewardForHomeDTO;
import com.apmosys.employeeportal.dto.EmployeeRewardsDTO;
import com.apmosys.employeeportal.dto.EmployeeRewardsRequest;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.RewardCategoryDTO;
import com.apmosys.employeeportal.dto.RewardConfigurationDTO;
import com.apmosys.employeeportal.dto.RewardTeamDTO;
import com.apmosys.employeeportal.dto.RewardsDetails;
import com.apmosys.employeeportal.model.CommonProperties;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeRewards;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.RewardConfig;
import com.apmosys.employeeportal.model.RewardsCategory;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeRewardsRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.RewardConfigRepository;
import com.apmosys.employeeportal.repository.RewardsCategoryRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class RewardsService {
	
	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	RewardsCategoryRepository rewardsCategoryRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

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
	
	public ServiceResponse getEmployeeRewardByRewardIdd(Long employeerewardId) {
	    ServiceResponse serviceResponse = new ServiceResponse();

	   
	    List<Object[]> rewardDetailsById = employeeRewardsRepository.getAllEmployeeRewardById(employeerewardId);
	    List<EmployeeRewardsDTO> rewardDtoList = new ArrayList<>();

	    if (rewardDetailsById != null) {
	        rewardDetailsById.forEach(object -> {
	            EmployeeRewardsDTO rewardDto = new EmployeeRewardsDTO();
	            
	            
	            rewardDto.setRewardId(object[0] != null ? Long.valueOf(object[0].toString()) : null);
	            rewardDto.setCategoryId(object[1] != null ? Long.valueOf(object[1].toString()) : null);
	            rewardDto.setCategoryName(object[2] != null ? object[2].toString() : null);
	            rewardDto.setId(object[3] != null ? Long.valueOf(object[3].toString()) : null);
	            rewardDto.setRewardName(object[4] != null ? object[4].toString() : null);
	            
	           
	            if (object[5] != null) {
	                rewardDto.setRewardTypes(Arrays.asList(object[5].toString().split(",")));
	            } else {
	                rewardDto.setRewardTypes(Collections.emptyList());
	            }
	            
	            rewardDto.setRewardTypeName(object[6] != null ? object[6].toString() : null);
	            rewardDto.setRewardedTo(object[7] != null ? Long.valueOf(object[7].toString()) : null);
	            rewardDto.setRewardedToByName(object[8] != null ? object[8].toString() : null);
	            rewardDto.setManagerId(object[9] != null ? Long.valueOf(object[9].toString()) : null);
	            rewardDto.setOfmonthyear(object[10] != null ? object[10].toString() : null);
	            rewardDto.setRemark(object[11] != null ? object[11].toString() : null);
	            
	            rewardDtoList.add(rewardDto);
	        });
	    }

	   
	    serviceResponse.setServiceResponse(rewardDtoList);
	    serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	   

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
	                        rewardDTO.setUpdatedBy(commonProperties.getUpdatedBy());  
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
	                        rewardDTO.setCreatedBy(commonProperties.getCreatedBy());
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

	
//	public ServiceResponse submitRewardForEmployee(EmployeeRewardsDTO employeeRewardsDTO) {
//	    ServiceResponse serviceResponse = new ServiceResponse();
//	    
//	    try {
//	    	EmployeeRewards employeeRewards = new EmployeeRewards();
//		    employeeRewards.setRewardedTo(employeeRewardsDTO.getRewardedTo() != null ? employeeRewardsDTO.getRewardedTo() : null);
//		    employeeRewards.setId(employeeRewardsDTO.getId() != null ? employeeRewardsDTO.getId() : null);
//		    employeeRewards.setRewardCategoryId(employeeRewardsDTO.getRewardCategoryId() != null ? employeeRewardsDTO.getRewardCategoryId():null);	    
//		    employeeRewards.setTeamId(employeeRewardsDTO.getTeamId() != null ? employeeRewardsDTO.getTeamId() : null);
//		    employeeRewards.setRewardType(employeeRewardsDTO.getRewardType() != 0 ? employeeRewardsDTO.getRewardType() : 0);
//		    employeeRewards.setManagerId(employeeRewardsDTO.getManagerId() != null ? employeeRewardsDTO.getManagerId() : null);
//		    employeeRewards.setTeamLeadId(employeeRewardsDTO.getTeamLeadId() != null ? employeeRewardsDTO.getTeamLeadId() : null);
//		    employeeRewards.setIsActive(employeeRewards.getIsActive() != 0 ? employeeRewards.getIsActive() : 0);
//		    employeeRewards.setFromDate(employeeRewardsDTO.getFromDate() != null ? employeeRewardsDTO.getFromDate() : null);
//		    employeeRewards.setToDate(employeeRewardsDTO.getToDate() != null ? employeeRewardsDTO.getToDate() : null);
//		    employeeRewards.setRemark(employeeRewardsDTO.getRemark() != null ? employeeRewardsDTO.getRemark() : null);
//		    employeeRewards.setRewardTypeName(employeeRewardsDTO.getRewardTypeName() != null ? employeeRewardsDTO.getRewardTypeName() : null);
//		    employeeRewards.setOfmonthyear(employeeRewardsDTO.getOfmonthyear()!=null ? employeeRewardsDTO.getOfmonthyear() : null);    
//		    CommonProperties commonProperties = new CommonProperties();
//		    commonProperties.setCreatedBy(employeeRewardsDTO.getCreatedBy() != null ? employeeRewardsDTO.getCreatedBy() : null);
//		    commonProperties.setUpdatedBy(employeeRewardsDTO.getUpdatedBy() != null ? employeeRewardsDTO.getUpdatedBy() : null);
//		    
//		    
//		    employeeRewards.setCommonProperty(commonProperties);
//		    
//		    EmployeeRewards setemployeeRewards=  employeeRewardsRepository.save(employeeRewards);
//		    if(setemployeeRewards!=null) {
//
//	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	        serviceResponse.setServiceMessage("Rewards submitted successfully.");
//		    }else {
//		    	serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//		    	serviceResponse.setServiceResponse("Rewards not submitted ");
//		    }
//
//	    } catch (Exception e) {
//	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	        serviceResponse.setServiceError(e.getMessage());
//	    }
//
//	    return serviceResponse;
//	}
	
	public ServiceResponse submitRewardForEmployee(EmployeeRewardsDTO employeeRewardsDTO) {
	    ServiceResponse serviceResponse = new ServiceResponse();

	    try {
//	        if (employeeRewardsDTO.getRewardCategoryId() != null && 
////	            employeeRewardsDTO.getRewardCategoryId() == 4 && 
//	            employeeRewardsDTO.getOfmonthyear() != null) {
//	            
//	            if (!isValidQuarterlyFormat(employeeRewardsDTO.getOfmonthyear())) {
//	                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	                serviceResponse.setServiceMessage("Invalid quarterly format. Expected format: Q1 2024, Q2 2024, etc.");
//	                return serviceResponse;
//	            }
//	        }

	        EmployeeRewards employeeRewards = new EmployeeRewards();
	        employeeRewards.setRewardedTo(employeeRewardsDTO.getRewardedTo() != null ? employeeRewardsDTO.getRewardedTo() : null);
	        employeeRewards.setId(employeeRewardsDTO.getId() != null ? employeeRewardsDTO.getId() : null);
	        employeeRewards.setRewardCategoryId(employeeRewardsDTO.getRewardCategoryId() != null ? employeeRewardsDTO.getRewardCategoryId() : null);
	        employeeRewards.setTeamId(employeeRewardsDTO.getTeamId() != null ? employeeRewardsDTO.getTeamId() : null);
	        employeeRewards.setRewardType(employeeRewardsDTO.getRewardType() != 0 ? employeeRewardsDTO.getRewardType() : 0);
	        employeeRewards.setManagerId(employeeRewardsDTO.getManagerId() != null ? employeeRewardsDTO.getManagerId() : null);
	        employeeRewards.setTeamLeadId(employeeRewardsDTO.getTeamLeadId() != null ? employeeRewardsDTO.getTeamLeadId() : null);
	        
	        
	        employeeRewards.setFromDate(employeeRewardsDTO.getFromDate() != null ? employeeRewardsDTO.getFromDate() : null);
	        employeeRewards.setToDate(employeeRewardsDTO.getToDate() != null ? employeeRewardsDTO.getToDate() : null);
	        employeeRewards.setRemark(employeeRewardsDTO.getRemark() != null ? employeeRewardsDTO.getRemark() : null);
	        employeeRewards.setRewardTypeName(employeeRewardsDTO.getRewardTypeName() != null ? employeeRewardsDTO.getRewardTypeName() : null);
	        
	        // Handle ofmonthyear - works for both regular months and quarterly format
	        employeeRewards.setOfmonthyear(employeeRewardsDTO.getOfmonthyear() != null ? employeeRewardsDTO.getOfmonthyear() : null);
	        
	        CommonProperties commonProperties = new CommonProperties();
	        commonProperties.setCreatedBy(employeeRewardsDTO.getCreatedBy() != null ? employeeRewardsDTO.getCreatedBy() : null);
	        commonProperties.setUpdatedBy(employeeRewardsDTO.getUpdatedBy() != null ? employeeRewardsDTO.getUpdatedBy() : null);

	        employeeRewards.setCommonProperty(commonProperties);

	        EmployeeRewards setemployeeRewards = employeeRewardsRepository.save(employeeRewards);
	        
	        if (setemployeeRewards != null) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceMessage("Rewards submitted successfully.");
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceResponse("Rewards not submitted");
	        }

	    } catch (Exception e) {
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceError(e.getMessage());
	    }

	    return serviceResponse;
	}
	private boolean isValidQuarterlyFormat(String quarterlyValue) {
	    if (quarterlyValue == null || quarterlyValue.trim().isEmpty()) {
	        return false;
	    }
	    String quarterPattern = "^Q[1-4]\\s+\\d{4}$";
	    return quarterlyValue.matches(quarterPattern);
	}
	private String[] getQuarterDateRange(String quarterlyValue) {
	    if (!isValidQuarterlyFormat(quarterlyValue)) {
	        return null;
	    }
	    
	    String[] parts = quarterlyValue.split("\\s+");
	    String quarter = parts[0];
	    String year = parts[1];
	    
	    String startMonth, endMonth;
	    
	    switch (quarter) {
	        case "Q1":
	            startMonth = year + "-01";
	            endMonth = year + "-03";
	            break;
	        case "Q2":
	            startMonth = year + "-04";
	            endMonth = year + "-06";
	            break;
	        case "Q3":
	            startMonth = year + "-07";
	            endMonth = year + "-09";
	            break;
	        case "Q4":
	            startMonth = year + "-10";
	            endMonth = year + "-12";
	            break;
	        default:
	            return null;
	    }
	    
	    return new String[]{startMonth, endMonth};
	}
	
	public ServiceResponse updateRewardsForEmployees(EmployeeRewardsDTO employeeRewardsDTO) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	   Optional<EmployeeRewards> getemployeeRewardsById = employeeRewardsRepository.findById(employeeRewardsDTO.getRewardId());
	    if(getemployeeRewardsById.isPresent()) {
	    	EmployeeRewards employeeRwardToBeUpdated = getemployeeRewardsById.get();
	    	employeeRwardToBeUpdated.setManagerId(employeeRewardsDTO.getManagerId());
	    	employeeRwardToBeUpdated.setOfmonthyear(employeeRewardsDTO.getOfmonthyear());
	    	employeeRwardToBeUpdated.setRemark(employeeRewardsDTO.getRemark());
	    	employeeRwardToBeUpdated.setRewardedTo(employeeRewardsDTO.getRewardedTo());
	    	employeeRwardToBeUpdated.setRewardTypeName(employeeRewardsDTO.getRewardTypeName());	
	    	CommonProperties commonProperties = employeeRwardToBeUpdated.getCommonProperty();
            if (commonProperties == null) {
                commonProperties = new CommonProperties();
            }
            commonProperties.setUpdatedBy(employeeRewardsDTO.getUpdatedBy());
            commonProperties.setUpdatedOn(LocalDateTime.now());

            employeeRwardToBeUpdated.setCommonProperty(commonProperties);

	    	EmployeeRewards dbResponse = employeeRewardsRepository.save(employeeRwardToBeUpdated);

			if (dbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Rewards Updated Successfully");
				
				
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Rewards Updation Failed.");
				
				
			}
		} else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Rewards Not Found");
			
		}
	} catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		response.setServiceError(e.getMessage());
		
	}
	 return response;   	   
	    
	}
	
	public ServiceResponse bulkDisableRewards() {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        int updatedRows = employeeRewardsRepository.bulkDisableRewards();
	        if (updatedRows > 0) {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Rewards have been disabled successfully.");
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No rewards were updated. Please try again.");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());
	    }
	    return response;
	}
	
	public ServiceResponse bulkEnableRewards(List<String> monthyears) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        int updatedRows = employeeRewardsRepository.bulkEnableRewards(monthyears);
	        if (updatedRows > 0) {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Rewards have been enabled for the following month-years.");
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No rewards were enabled. Please try again.");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());
	    }
	    return response;
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

//	public ServiceResponse showAllEmployeeRewards() {
//	    ServiceResponse serviceResponse = new ServiceResponse();
//	    List<EmployeeRewardsDTO> rewardsDTOList = employeeRewardsRepository.findAll().stream()
//	        .map(employeeRewards -> {
//	        	
//	            EmployeeRewardsDTO dto = new EmployeeRewardsDTO();
//	            
//	            dto.setRewardId(employeeRewards.getRewardId() != null ? employeeRewards.getRewardId() : 0L);
//	            dto.setRewardedTo(employeeRewards.getRewardedTo() != null ? employeeRewards.getRewardedTo() : 0L);
//	            dto.setRewardType(employeeRewards.getRewardType() != 0 ? employeeRewards.getRewardType() : 0);
//	            dto.setRewardTypeName(employeeRewards.getRewardTypeName() != null ? employeeRewards.getRewardTypeName() : null);
//	            dto.setManagerId(employeeRewards.getManagerId() != null ? employeeRewards.getManagerId() : null);
//	            dto.setTeamLeadId(employeeRewards.getTeamLeadId() != null ? employeeRewards.getTeamLeadId() : null);
//	            dto.setIsActive(employeeRewards.getIsActive() != 0 ? employeeRewards.getIsActive() : 0);
//	            dto.setFromDate(employeeRewards.getFromDate() != null ? employeeRewards.getFromDate() : null);
//	            dto.setToDate(employeeRewards.getToDate() != null ? employeeRewards.getToDate() : null);
//	            dto.setRemark(employeeRewards.getRemark() != null ? employeeRewards.getRemark() : "");
//
//	            dto.setCreatedBy(employeeRewards.getCommonProperty().getCreatedBy() != null ? employeeRewards.getCommonProperty().getCreatedBy() : 0L);
//	            dto.setUpdatedBy(employeeRewards.getCommonProperty().getUpdatedBy() != null ? employeeRewards.getCommonProperty().getUpdatedBy() : 0L);
//	            dto.setUpdatedOn(employeeRewards.getCommonProperty().getUpdatedOn() != null ? employeeRewards.getCommonProperty().getUpdatedOn() : null);
//	            dto.setCreatedOn(employeeRewards.getCommonProperty().getCreatedOn().toLocalDateTime() != null ? employeeRewards.getCommonProperty().getCreatedOn().toLocalDateTime() : null);
//	            
//	            dto.setCreatedByName(getEmployeeNameByEmpId(employeeRewards.getCommonProperty().getCreatedBy()));
//	            dto.setUpdatedByName(getEmployeeNameByEmpId(employeeRewards.getCommonProperty().getUpdatedBy()));
//	            dto.setManagerName(getEmployeeNameByEmpId(employeeRewards.getManagerId()) != null ? getEmployeeNameByEmpId(employeeRewards.getManagerId()) : null);
//	            dto.setRewardedToByName(getEmployeeNameByEmpId(employeeRewards.getRewardedTo()) != null ? getEmployeeNameByEmpId(employeeRewards.getRewardedTo()) : null);
//	            return dto;
//	        })
//	        .collect(Collectors.toList());
//
//	    if (!rewardsDTOList.isEmpty()) {
//	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	        serviceResponse.setServiceResponse(rewardsDTOList);
//	    } else {
//	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	        serviceResponse.setServiceResponse("No rewards found");
//	    }
//
//	    return serviceResponse;
//	}
	
//	public ServiceResponse showAllEmployeeRewards() {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("show All EmployeeRewards");
//		apiLogInfo.setApiUrl("/api/showAllEmployeeRewards");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("showAllEmployeeRewards size : "+employeeRewardsRepository.showAllEmployeeRewards());
//
//		try {
//			
//			 List<Object[]> rewardsDTOList = employeeRewardsRepository.showAllEmployeeRewards();
//			    List<EmployeeRewardsDTO> dtolist = new ArrayList<EmployeeRewardsDTO>();
//			        	
//			    	if(!rewardsDTOList.isEmpty()) {
//			    		
//			    		rewardsDTOList.forEach((object) -> {
//			    			 
//			    			EmployeeRewardsDTO dto = new EmployeeRewardsDTO();
//			    		
//			    			dto.setRewardedTo(object[0] != null ? Long.parseLong(object[0].toString()) : null);  			
//			    			dto.setRewardedToByName(object[0] != null ? getEmployeeNameByEmpId(Long.parseLong(object[0].toString())) : null);
//			    			dto.setRewardTypeName(object[1] != null ? object[1].toString() : null);
//			    			dto.setManagerId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
//			    			dto.setManagerName(object[2] != null ? getEmployeeNameByEmpId(Long.parseLong(object[2].toString())) : null);
//			    			dto.setOfmonthyear(object[3] != null ? object[3].toString() : null);
//			    			dto.setRemark(object[4] != null ? object[4].toString() : null);
//			    			dto.setIsActive(object[5] != null ? Integer.parseInt(object[5].toString()) : null);
//			    			dto.setId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
//			    			dto.setCreatedBy(object[7] != null ? Long.parseLong(object[7].toString()) : null);
//			    			dto.setCreatedByName(object[7] != null ? getEmployeeNameByEmpId(Long.parseLong(object[7].toString())) : null);
//			    			dto.setCreatedOn(object[8] != null ? ((Timestamp) object[8]).toLocalDateTime() : null);
//			    			dto.setUpdatedBy(object[9] != null ? Long.parseLong(object[9].toString()) : null);
//			    			dto.setUpdatedByName(object[9] != null ? getEmployeeNameByEmpId(Long.parseLong(object[9].toString())) : null);
//			    			dto.setUpdatedOn(object[10] != null ? ((Timestamp) object[10]).toLocalDateTime() : null);
//			    			dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);		
//			    			dto.setRewardId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
//			    			dto.setRewardCategoryId(object[12] != null ? Long.parseLong(object[12].toString()) : null);
//			    			dto.setRewardCategoryName(object[13] != null ? object[13].toString() : null);		    			
//			    			dtolist.add(dto);
//			    		});
//			    	}
//			            
//			    if (!dtolist.isEmpty()) {
//			    	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			    	response.setServiceResponse(dtolist);
//			    	apiLogInfo.setApiResponse("List fetched of size : "+dtolist.size());
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			    } else {
//			    	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			    	response.setServiceResponse("No rewards found");
//			    	apiLogInfo.setApiResponse("No rewards found");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//
//			    }
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			apiLogInfo.setLogLevel("ERROR");
//			response.setServiceError(e.getMessage());
//		}
//
//	    return response;
//	}
	
	// added quarterly
	
	public ServiceResponse showAllEmployeeRewards() {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("show All EmployeeRewards");
	    apiLogInfo.setApiUrl("/api/showAllEmployeeRewards");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    
	    try {
	        List<Object[]> rewardsDTOList = employeeRewardsRepository.showAllEmployeeRewards();
	        logBuilder.append("showAllEmployeeRewards size : ").append(rewardsDTOList.size());
	        Set<Long> empIds = new HashSet<>();
	        for (Object[] obj : rewardsDTOList) {
	            empIds.add(parseOrNull(obj[0]));
	            empIds.add(parseOrNull(obj[2]));
	            empIds.add(parseOrNull(obj[7]));
	            empIds.add(parseOrNull(obj[9]));
	        }
	        empIds.remove(null);

	        List<Object[]> empResults = employeeRepository.getEmployeeNamesByEmpIds(empIds);
	        Map<Long, String> empNameMap = empResults.stream()
	            .filter(r -> r[0] != null && r[1] != null)
	            .collect(Collectors.toMap(
	                r -> ((Number) r[0]).longValue(),
	                r -> r[1].toString()
	            )); 
	        List<EmployeeRewardsDTO> dtolist = new ArrayList<>();
	        
	        if (!rewardsDTOList.isEmpty()) {
	            
	            for (int index = 0; index < rewardsDTOList.size(); index++) {
	                Object[] object = rewardsDTOList.get(index);
	                
	                try {
	                    EmployeeRewardsDTO dto = new EmployeeRewardsDTO();
	                   
	                    dto.setRewardedTo(parseOrNull(object[0])); 
	                    dto.setRewardedToByName(dto.getRewardedTo() != null ? empNameMap.get(dto.getRewardedTo()) : null);
	                    
	                    dto.setRewardTypeName(object[1] != null ? object[1].toString() : null); 
	                    
	                    dto.setManagerId(parseOrNull(object[2])); 
	                    dto.setManagerName(dto.getManagerId() != null ? empNameMap.get(dto.getManagerId()) : null);
	                    
	                    String ofmonthyearValue = object[3] != null ? object[3].toString() : null; 
	                    dto.setOfmonthyear(ofmonthyearValue);
	                    
	                    dto.setRemark(object[4] != null ? object[4].toString() : null); 
	                    dto.setIsActive(parseIntOrNull(object[5])); 
	                    dto.setId(parseOrNull(object[6])); 
	                    
	                    dto.setCreatedBy(parseOrNull(object[7]));
	                    dto.setCreatedByName(dto.getCreatedBy() != null ? empNameMap.get(dto.getCreatedBy()) : null);
	                    
	                    dto.setCreatedOn(object[8] != null ? ((Timestamp) object[8]).toLocalDateTime() : null); 
	                    
	                    dto.setUpdatedBy(parseOrNull(object[9])); 
	                    dto.setUpdatedByName(dto.getUpdatedBy() != null ? empNameMap.get(dto.getUpdatedBy()) : null);
	                    
	                    dto.setUpdatedOn(object[10] != null ? ((Timestamp) object[10]).toLocalDateTime() : null); 
	                    dto.setEmpId(dto.getRewardedTo()); 
	                    dto.setRewardId(parseOrNull(object[11])); 
	                    dto.setRewardCategoryId(parseOrNull(object[12])); 
	                    dto.setRewardCategoryName(object[13] != null ? object[13].toString() : null); 
	                    
	                    dtolist.add(dto);
	                    
	                } catch (Exception rowException) {
	                  
	                    System.err.println("Error processing row " + index + ": " + rowException.getMessage());
	                    System.err.println("Row data: " + Arrays.toString(object));
	                  
	                }
	            }
	        }
	        
	        if (!dtolist.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(dtolist);
	            apiLogInfo.setApiResponse("List fetched of size : " + dtolist.size());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No rewards found");
	            apiLogInfo.setApiResponse("No rewards found");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        apiLogInfo.setLogLevel("ERROR");
	        response.setServiceError(e.getMessage());
	    }
	    
	    return response;
	}
	private Long parseOrNull(Object value) {
	    if (value == null) {
	        return null;
	    }
	    
	    String stringValue = value.toString().trim();
	    if (stringValue.isEmpty()) {
	        return null;
	    }
	    
	    try {
	    
	        if (stringValue.contains("+") || stringValue.contains("/") || stringValue.contains("=")) {
	            System.err.println("Warning: Attempting to parse what appears to be encrypted data as Long: " + stringValue);
	            return null; 
	        }
	        
	        return Long.parseLong(stringValue);
	    } catch (NumberFormatException e) {
	        System.err.println("Failed to parse Long from value: '" + stringValue + "'. Error: " + e.getMessage());
	        return null;
	    }
	}

	private Integer parseIntOrNull(Object value) {
	    if (value == null) {
	        return null;
	    }
	    
	    String stringValue = value.toString().trim();
	    if (stringValue.isEmpty()) {
	        return null;
	    }
	    
	    try {
	        if (stringValue.contains("+") || stringValue.contains("/") || stringValue.contains("=")) {
	            System.err.println("Warning: Attempting to parse what appears to be encrypted data as Integer: " + stringValue);
	            return null;
	        }
	        
	        return Integer.parseInt(stringValue);
	    } catch (NumberFormatException e) {
	        System.err.println("Failed to parse Integer from value: '" + stringValue + "'. Error: " + e.getMessage());
	        return null;
	    }
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

	
	public ServiceResponse fetchEmployeesForHomepageByCategoryId(RewardCategoryDTO rewardCategoryDTO) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        Long categoryId = rewardCategoryDTO.getRewardCategoryId();
	        List<Object[]> employeeRewards = employeeRewardsRepository.fetchEmployeesForHomepage(categoryId);
	        System.out.println("rewardCategoryDTO.getRewardCategoryId(): " + categoryId);
	        System.out.println("Total records from database: " + employeeRewards.size());
	        
	        List<EmployeeRewardForHomeDTO> dtos = new ArrayList<>();
	        
	        // Handle empty results
	        if (employeeRewards.isEmpty()) {
	            serviceResponse.setServiceResponse("No employee is rewarded for this category");
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            return serviceResponse;
	        }
	        
	    
	        for (Object[] object : employeeRewards) {

	            
	            EmployeeRewardForHomeDTO dto = new EmployeeRewardForHomeDTO();
	            dto.setRewardId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
	            dto.setIsActive(object[1] != null ? Integer.parseInt(object[1].toString()) : null);
	            dto.setId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
	            dto.setRewardedTo(object[3] != null ? Long.parseLong(object[3].toString()) : null);
	            dto.setRewardedToByName(object[4] != null ? object[4].toString() : null);
	            dto.setRewardTypeID(object[5] != null ? Integer.parseInt(object[5].toString()) : null);
	            dto.setRewardTypeName(object[6] != null ? object[6].toString() : null);
	            dto.setDepartmentId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
	            dto.setDepartment(object[8] != null ? object[8].toString() : null);
	            dto.setOfMonthYear(object[9] != null ? object[9].toString() : null);
	            dto.setCategoryName(object[10] != null ? object[10].toString() : null);
	            
	            dtos.add(dto);
	        }
	        
	        System.out.println("Total DTOs created: " + dtos.size());
	        
	        // Final response
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
	
	private String getCurrentQuarter() {
	    LocalDate currentDate = LocalDate.now();
	    int currentMonth = currentDate.getMonthValue();

	    if (currentMonth >= 1 && currentMonth <= 3) {
	        return "Q1";
	    } else if (currentMonth >= 4 && currentMonth <= 6) {
	        return "Q2";
	    } else if (currentMonth >= 7 && currentMonth <= 9) {
	        return "Q3";
	    } else {
	        return "Q4";
	    }
	}
	
//	public ServiceResponse fetchEmployeesHomepagecurrentmonth( String currentmonth) {
//        ServiceResponse serviceResponse = new ServiceResponse();
//        
//        try {
//            List<Object[]> employeeRewards = employeeRewardsRepository.fetchEmployeesHomepagecurrentmonth();
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
//                String categoryName = (String) rewardData.get(12);
//                
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
//                    department,
//                    categoryName
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
	public ServiceResponse getEmployeeRewardByEmpId(EmployeeRewardsRequest request) {
		ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	    	List<Employee360RewardsDTO> rewardList = new ArrayList<>();

		    if (request.getEmpId() != null) {
		      
		        List<Object[]> result = employeeRewardsRepository.getRewardDetailsByEmpId(request.getEmpId(), request.getOfMonthYear());
		        if(result != null) {
		        	for (Object[] row : result) {
		        		Employee360RewardsDTO dto = new Employee360RewardsDTO();
			            dto.setRewardTypeName(row[1] != null ? row[1].toString() : null);
			            dto.setRewardedTo(row[2] != null ? Long.parseLong(row[2].toString()) : null);
			            dto.setCreatedOn(row[3] != null ?getLocalDateTime(row[3]) : null); 
			            dto.setRemark(row[4] != null ? row[4].toString() : null);
			            dto.setCreatedByName(row[5] != null ? getEmployeeNameByEmpId(Long.parseLong(row[5].toString())) : null);
			            dto.setName(row[0] != null ? row[0].toString() : null);
			            dto.setCreatedBY(row[5] != null ? Long.parseLong(row[5].toString()) : null);
			            dto.setRewardCategoryId(row[6] !=null ? Long.parseLong(row[6].toString()) : null);	
			            dto.setRewardCategory(row[7] != null ? row[7].toString() : null);
			            dto.setNameId(row[8] !=null ? Long.parseLong(row[8].toString()) : null);
			            dto.setOfMonthYear(row[9] != null ? row[9].toString() : null);	            
//			            List<RewardTeamDTO> teamList = getTeamsByEmpId(request.getEmpId());
//			            
//			            dto.setTeamlist(teamList);
			            rewardList.add(dto);
			        }
		        }
		    }

		    if (rewardList.isEmpty()) {
	            serviceResponse.setServiceResponse("No data found");
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        } else {
	            serviceResponse.setServiceResponse(rewardList);
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        }
    } catch (Exception e) {
        e.printStackTrace(); 
        serviceResponse.setServiceResponse("Error occurred while fetching data");
        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
    }
    
    return serviceResponse;
	}
	
	
	public List<RewardTeamDTO> getTeamsByEmpId(Long empId) {
		 List<RewardTeamDTO> teamList = new ArrayList<>();
		    List<Object[]> teamResults = employeeRewardsRepository.getTeamsByEmpId(empId); 

		    if (teamResults != null) {
		        for (Object[] row : teamResults) {
		            RewardTeamDTO teamDTO = new RewardTeamDTO();
		            teamDTO.setTeamId(row[0] != null ? Long.parseLong(row[0].toString()) : null);
		            teamDTO.setTeamName(row[1] != null ? row[1].toString() : null);
		            teamList.add(teamDTO);
		        }
		    }
		    return teamList;
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

	    if (request.getEmpId() != null) {
	        List<Object[]> result = employeeRewardsRepository.getRewardByTeamAndDateRange(request.getOfMonthYear(),request.getEmpId());	       
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
	    dto.setRemark(getStringValue(row[4]));
	    dto.setCreatedBy(getLongValue(row[5])); 
	    dto.setCreatedByName(getEmployeeNameByEmpId(getLongValue(row[5])));
	    dto.setRewardCategoryId(getLongValue(row[6]));
	    dto.setRewardCategoryName(getStringValue(row[7]));
	    dto.setId(getLongValue(row[8]));
	    dto.setOfmonthyear(getStringValue(row[9]));  
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
	
	public ServiceResponse saveExcelDataForReward(MultipartFile file , Long createdBy) throws EncryptedDocumentException, InvalidFormatException {
	    ServiceResponse response = new ServiceResponse();
	    List<String> errorMessages = new ArrayList<>();
	    List<Long> inactiveEmployees = new ArrayList<>();

	    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rows = sheet.iterator();

	        if (!rows.hasNext()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("The uploaded file is empty.");
	            return response;
	        }

	        Row headerRow = rows.next();
	        Map<String, Integer> columnIndexMap = new HashMap<>();
	        List<String> requiredColumns = Arrays.asList("Employee Id", "Employee Name", "Reward Category", "Reward Type Name", "Of Month-Year", "Remarks");
	        for (Cell cell : headerRow) {
	            String headerName = cell.getStringCellValue().trim();
	            if (requiredColumns.contains(headerName)) {
	                columnIndexMap.put(headerName, cell.getColumnIndex());
	            }
	        }

	        int rowNum = 1;
	        while (rows.hasNext()) {
	            Row currentRow = rows.next();
	            rowNum++;

	            if (isRowEmpty(currentRow)) {
	                continue;
	            }
	            
	            Long employeeId = null;
	            try {
	                Cell employeeIdCell = currentRow.getCell(columnIndexMap.get("Employee Id"));
	                if (employeeIdCell == null) {
	                    errorMessages.add("Row " + rowNum + ": Employee Id is missing.");
	                    continue;
	                }
	                employeeId = (long) employeeIdCell.getNumericCellValue();
	            } catch (Exception e) {
	                errorMessages.add("Row " + rowNum + ": Invalid EmployeeId.");
	                continue;
	            }
	            
	            String employeeName = null;
	            if (columnIndexMap.containsKey("Employee Name")) {
	                Cell employeeNameCell = currentRow.getCell(columnIndexMap.get("Employee Name"));
	                if (employeeNameCell == null || employeeNameCell.getStringCellValue().trim().isEmpty()) {
	                    errorMessages.add("Row " + rowNum + ": Employee Name is missing.");
	                    continue;
	                }
	                employeeName = employeeNameCell.getStringCellValue().trim();
	            }

	            Optional<Employee> optionalEmployee = Optional.ofNullable(employeeRepository.findByEmployeementId(employeeId));
	            if (!optionalEmployee.isPresent()) {
	                errorMessages.add("Row " + rowNum + ": Employee with ID '" + employeeId + "' not found.");
	                continue;
	            }

	            Employee employee = optionalEmployee.get();
	            
	            
	            if (!employee.getName().equalsIgnoreCase(employeeName)) {
	                errorMessages.add("Row " + rowNum + ": Employee Name does not correspond to Employee ID '" + employeeId + "'.");
	                continue;
	            }
	            

	            if ("InActive".equalsIgnoreCase(employee.getEmploymentstatus())) {
	                inactiveEmployees.add(employeeId);
	                continue;
	            }
	            
	                      
	            

	            String rewardCategoryName = null;
	            if (columnIndexMap.containsKey("Reward Category")) {
	                Cell rewardCategoryCell = currentRow.getCell(columnIndexMap.get("Reward Category"));
	                if (rewardCategoryCell == null || rewardCategoryCell.getStringCellValue().trim().isEmpty()) {
	                    errorMessages.add("Row " + rowNum + ": Reward Category is missing.");
	                    continue;
	                }
	                rewardCategoryName = rewardCategoryCell.getStringCellValue().trim();
	            }

	            Optional<RewardsCategory> rewardCategory = Optional.ofNullable(rewardsCategoryRepository.findByCategoryNameIgnoreCase(rewardCategoryName));
	            if (!rewardCategory.isPresent()) {
	                errorMessages.add("Row " + rowNum + ": Invalid Reward Category '" + rewardCategoryName + "'. Allowed values are 'Monthly', 'Half Yearly', 'Annual'.");
	                continue;
	            }

	            String rewardTypeName = null;
	            if (columnIndexMap.containsKey("Reward Type Name")) {
	                Cell rewardTypeCell = currentRow.getCell(columnIndexMap.get("Reward Type Name"));
	                if (rewardTypeCell == null || rewardTypeCell.getStringCellValue().trim().isEmpty()) {
	                    errorMessages.add("Row " + rowNum + ": Reward Type Name is missing.");
	                    continue;
	                }
	                rewardTypeName = rewardTypeCell.getStringCellValue().trim();
	            }

	            String monthYear = null;
	            if (columnIndexMap.containsKey("Of Month-Year")) {
	                Cell monthYearCell = currentRow.getCell(columnIndexMap.get("Of Month-Year"));
	                if (monthYearCell == null || monthYearCell.getStringCellValue().trim().isEmpty()) {
	                    errorMessages.add("Row " + rowNum + ": Of Month-Year is missing.");
	                    continue;
	                }
	                monthYear = monthYearCell.getStringCellValue().trim();
	            }

//	            String formattedMonthYear = formatMonthYear(monthYear, rowNum, errorMessages);
//	            if (formattedMonthYear == null) continue;
	            String formattedMonthYear = null;
	            String rewardCategoryValue = rewardCategoryName != null ? rewardCategoryName.trim().toLowerCase() : "";

	            if (rewardCategoryValue.equals("quarterly")) {
	                // Quarterly format validation (should be Q1–Q4 + Year)
	                if (!monthYear.matches("(?i)^Q[1-4]\\s\\d{4}$")) {
	                    errorMessages.add("Row " + rowNum + ": Invalid format for 'Of Month-Year'. For Quarterly rewards, use format like 'Q1 2025'.");
	                    continue;
	                }
	                formattedMonthYear = monthYear.toUpperCase().trim();
	            } else {
	                // Regular month-year validation for others
	                formattedMonthYear = formatMonthYear(monthYear, rowNum, errorMessages);
	                if (formattedMonthYear == null) continue;
	            }
	            Long Id = null;
	            if (rewardCategoryName != null) {
	                switch (rewardCategoryName.trim().toLowerCase()) {
	                    case "monthly":
	                    	Id = 2L;
	                        break;
	                    case "half yearly":
	                    	Id = 1L;
	                        break;
	                    case "annual":
	                    	Id = 3L;
	                        break;
	                    case "quarterly":
	                    	Id = 4L;
	                        break;
	                    default:
	                        errorMessages.add("Row " + rowNum + ": Invalid Reward Category '" + rewardCategoryName + "'. Allowed values are 'Monthly', 'Half Yearly', 'Annual', 'Quarterly'.");
	                        continue;
	                }
	            }

	            String remarks = null;
	            if (columnIndexMap.containsKey("Remarks")) {
	                Cell remarksCell = currentRow.getCell(columnIndexMap.get("Remarks"));
	                if (remarksCell != null && !remarksCell.getStringCellValue().trim().isEmpty()) {
	                    remarks = remarksCell.getStringCellValue().trim();
	                }
	            }

	            EmployeeRewards reward = new EmployeeRewards();
	            reward.setRewardedTo(employee.getEmpId());
	            reward.setManagerId(employee.getManagerId());
	            reward.setRewardCategoryId(rewardCategory.get().getRewardCategoryId());
	            reward.setRewardTypeName(rewardTypeName);
	            reward.setOfmonthyear(formattedMonthYear);
	            reward.setRemark(remarks);
	            reward.setId(Id);
	            CommonProperties commonProperties = new CommonProperties();
	            commonProperties.setCreatedBy(createdBy);
	            reward.setCommonProperty(commonProperties);	            
	            reward.setIsActive(0);
	            reward.setRewardType(0);	            
	            employeeRewardsRepository.save(reward);
	        }

	        if (!errorMessages.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse(String.join(", ", errorMessages));
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("File uploaded and processed successfully.");
	        }
	    } catch (IOException e) {
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	    }
	    return response;
	}
	
	@SuppressWarnings("deprecation")
	private boolean isRowEmpty(Row row) {
	    if (row == null) return true;

	    for (Cell cell : row) {
	        if (cell == null) continue;

	        int cellType = cell.getCellType(); // old POI: returns int
	        if (cellType != Cell.CELL_TYPE_BLANK) { // use int constant
	            if (cellType == Cell.CELL_TYPE_STRING && cell.getStringCellValue().trim().isEmpty()) {
	                continue;
	            }
	            return false;
	        }
	    }
	    return true;
	}


	
	private String formatMonthYear(String input, int rowNum, List<String> errorMessages) {
	    try {
	        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
	        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
	        return YearMonth.parse(input, inputFormatter).format(outputFormatter);
	    } catch (Exception e) {
	        errorMessages.add("Row " + rowNum + ": Invalid date format. Expected format is e.g 'January 2025'.");
	        return null;
	    }
	}
	
	public ServiceResponse fetchRewardCategoryForHomePage() {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    
	    try {
	        List<RewardsCategory> rewardCategory = rewardsCategoryRepository.findAll();
	        List<RewardCategoryDTO> dtos = new ArrayList<>();

	        if(rewardCategory.isEmpty()) {
	            serviceResponse.setServiceResponse("No reward category found!");
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            return serviceResponse;
	        } else {
	            for (RewardsCategory category : rewardCategory) {
	                RewardCategoryDTO dto = new RewardCategoryDTO();
	                
	                dto.setRewardCategoryId(category.getRewardCategoryId());
	                dto.setCategoryName(category.getCategoryName());
	                
	                dtos.add(dto);
	            }
	        }

	        if (!dtos.isEmpty()) {
	            serviceResponse.setServiceResponse(dtos);
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            serviceResponse.setServiceResponse("No data found");
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceResponse("Error occurred while fetching data");
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	    }
	    
	    return serviceResponse;
	}
	@Transactional
	public ServiceResponse isQuarterEnable(EmployeeRewardForHomeDTO dto) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("isQuarterEnable");
	    apiLogInfo.setApiUrl("/api/isQuarterEnable");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    
	    try {
	        if (dto.getOfMonthYear() == null || dto.getOfMonthYear().trim().isEmpty()) {
	        	
	            serviceResponse.setServiceResponse("ofMonthYear parameter is required");
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            return serviceResponse;
	        }
	        
	        logBuilder.append("Enabling quarter for: ").append(dto.getOfMonthYear());
	        
	        int updatedRows = rewardsCategoryRepository.enableQuarter(dto.getOfMonthYear());
	       
	        
	        if (updatedRows > 0) {
	            logBuilder.append(" - Successfully updated ").append(updatedRows).append(" records");
	            serviceResponse.setServiceResponse("Quarter enabled successfully for " + dto.getOfMonthYear() + ". Updated " + updatedRows + " records.");
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            logBuilder.append(" - No records found to update");
	            serviceResponse.setServiceResponse("No records found for the specified quarter: " + dto.getOfMonthYear());
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        logBuilder.append(" - Error: ").append(e.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	        
	        serviceResponse.setServiceResponse("Error occurred while enabling quarter for " + dto.getOfMonthYear() + ": " + e.getMessage());
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	    }
	    
	    return serviceResponse;
	}
	
	@Transactional
	public ServiceResponse bulkEnableRewardsQuartely(EmployeeRewardForHomeDTO dto) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("bulkEnableRewardsQuartely");
	    apiLogInfo.setApiUrl("/api/bulkEnableRewardsQuartely");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();

	    try {
	        if (dto.getOfMonthYears() == null || dto.getOfMonthYears().isEmpty()) {
	            serviceResponse.setServiceResponse("ofMonthYears parameter is required");
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            return serviceResponse;
	        }

	        logBuilder.append("Bulk enabling rewards quarterly for: ").append(dto.getOfMonthYears());

	        int updatedRows = rewardsCategoryRepository.bulkEnableRewardsQuartely(dto.getOfMonthYears());

	        if (updatedRows > 0) {
	            logBuilder.append(" - Successfully updated ").append(updatedRows).append(" records");
	            serviceResponse.setServiceResponse("Quarter enabled successfully for given monthyears. Updated " 
	                + updatedRows + " records.");
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            logBuilder.append(" - No records found to update");
	            serviceResponse.setServiceResponse("No records found for the specified monthyears: " 
	                + dto.getOfMonthYears());
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        logBuilder.append(" - Error: ").append(e.getMessage());
	        apiLogInfo.setLogLevel("ERROR");

	        serviceResponse.setServiceResponse("Error occurred while enabling quarter for monthyears " 
	            + dto.getOfMonthYears() + ": " + e.getMessage());
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	    }

	    return serviceResponse;
	}


}

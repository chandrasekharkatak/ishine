package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.DomainDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.SpecializationDTO;
import com.apmosys.employeeportal.model.ActivityTemplate;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Domain;
import com.apmosys.employeeportal.model.EmployeeSpecializationMap;
import com.apmosys.employeeportal.model.Specialization;
import com.apmosys.employeeportal.repository.DomainRepository;
import com.apmosys.employeeportal.repository.EmployeeSpecializationMapRepository;
import com.apmosys.employeeportal.repository.SpecializationRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;


@Service
public class DomainService {
	
	@Autowired
	DomainRepository domainRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	SpecializationRepository specializationRepository;
	
	@Autowired
	EmployeeSpecializationMapRepository employeeSpecializationMapRepository;
	
	@Autowired
	HttpServletRequest httpRequest;
	
	@Autowired
	LogService logService;

	public ServiceResponse createDomain(DomainDTO domainDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setSubFeatureName("create_domain");
		apiLogInfo.setApiUrl("/api/createDomain");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("domainId : "+domainDTO.getDomainId()+", domainName : "+domainDTO.getDomainName());
		try {
			
			List<Domain> existingDomainName = domainRepository.findByDomainnName(domainDTO.getDomainName());
	        if (existingDomainName != null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Domain Name already exists.");
	            apiLogInfo.setApiResponse("Domain Name already exists.");            
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);  
	            apiLogInfo.setLogLevel("ERROR");
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
			
			
			Domain domainObj = new Domain();
			
			domainObj.setDomainName(domainDTO.getDomainName());
			domainObj.setIsActive("true");
			domainObj.setCreatedBy(domainDTO.getCreatedBy());
			
			Domain dbResponse = domainRepository.save(domainObj);
			
			if(dbResponse != null) {
				
				List<Specialization> allSpecialization = new ArrayList<>();
				
				domainDTO.getAllSpecializationList().forEach((domain) -> {
					Specialization specializationObj = new Specialization();
					
					specializationObj.setDomainId(dbResponse.getDomainId());
					specializationObj.setCreatedBy(domainDTO.getCreatedBy());
					specializationObj.setSpecializationName(domain.getSpecializationName());
					specializationObj.setIsActive("true");
					
					allSpecialization.add(specializationObj);
				});
				
				List<Specialization> specializationDbResponse = specializationRepository.saveAll(allSpecialization);
				
				if(!specializationDbResponse.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Domain & Specialization Added successfully.");
					apiLogInfo.setApiResponse("Domain & Specialization Added successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to add Specialization.");
					apiLogInfo.setApiResponse("Unable to add Specialization.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to add New Domain.");
				apiLogInfo.setApiResponse("Unable to add New Domain.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getAllDomain() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setSubFeatureName("get_AllDomain");
		apiLogInfo.setApiUrl("/api/getAllDomain");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getAllDomain size : "+domainRepository.getAllDomain().size());
		try {
			
			List<Object[]> allDomain = domainRepository.getAllDomain();
			List<DomainDTO> dtoList = new ArrayList<>();
			
			if(!allDomain.isEmpty()) {
					for(Object[] object : allDomain) {
						DomainDTO domain = new DomainDTO();
						
						domain.setDomainId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						domain.setDomainName(object[1] != null ? object[1].toString() : null);
						domain.setCreatedBy(object[2] != null ? Long.parseLong(object[2].toString()) : null);
						domain.setCreatedByName(object[3] != null ? object[3].toString() : null);
						domain.setCreatedOn(object[4] != null ? object[4].toString() : null);
						domain.setUpdatedBy(object[5] != null ? Long.parseLong(object[5].toString()) : null);
						domain.setUpdatedByName(object[6] != null ? object[6].toString() : null);
						domain.setUpdatedOn(object[7] != null ? object[7].toString() : null);
						
						dtoList.add(domain);
					}
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("dtoList size : "+dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Domain Found.");
				apiLogInfo.setApiResponse("No Domain Found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getDomainSpecializationByDomainId(DomainDTO domainDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setApiUrl("/api/getDomainSpecializationByDomainId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("domainId : "+domainDTO.getDomainId());
		try {
			
			Domain domainObj = domainRepository.findByDomainId(domainDTO.getDomainId());
			
			if(domainObj != null) {
				DomainDTO domaindto = new DomainDTO();
				
				domaindto.setDomainName(domainObj.getDomainName());
				domaindto.setIsActive("true");
				domaindto.setDomainId(domainObj.getDomainId());
				
				//Get All Specialization
				List<Specialization> allSpecailization = specializationRepository.findByDomainIdAndIsActive(domainObj.getDomainId(), "true");
				List<SpecializationDTO> specDtoList = new ArrayList<SpecializationDTO>();
				
				if(allSpecailization != null) {
					allSpecailization.forEach((object) -> {
						SpecializationDTO dto = new SpecializationDTO();
						
						dto.setSpecializationName(object.getSpecializationName());
						dto.setSpecializationId(object.getSpecializationId());
						specDtoList.add(dto);
					});
				}
				
				domaindto.setAllSpecializationList(specDtoList);
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(domaindto);
				apiLogInfo.setApiResponse("doaminId : "+domainDTO.getDomainId());
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Domain Id not found.");
				apiLogInfo.setApiResponse("Domain Id not found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse updateDomain(DomainDTO domainDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setSubFeatureName("update_domain");
		apiLogInfo.setApiUrl("/api/updateDomain");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(domainDTO.getDomainName());
		try {
			
			Domain domainObj = domainRepository.findByDomainId(domainDTO.getDomainId());
			
			if(domainObj != null) {
				
				domainObj.setDomainName(domainDTO.getDomainName());
				domainObj.setIsActive("true");
				domainObj.setUpdatedBy(domainDTO.getUpdatedBy());
				domainObj.setUpdatedOn(Timestamp.valueOf(stringToDateTimeParser.getCurrentDateTime()));
				
				Domain dbResposne = domainRepository.save(domainObj);
				
				if(dbResposne != null) {
					List<Long> specializationIds = new ArrayList<Long>();
					
					for(SpecializationDTO domain: domainDTO.getAllSpecializationList()) {
						if(domain.getSpecializationId() != null) {
							//update specialization
							Specialization specializationObj = specializationRepository.findBySpecializationId(domain.getSpecializationId());
							if(specializationObj != null) {
								
								specializationObj.setSpecializationName(domain.getSpecializationName());
								specializationObj.setUpdatedBy(domainDTO.getUpdatedBy());
								specializationObj.setIsActive("true");
								specializationObj.setUpdatedOn(Timestamp.valueOf(stringToDateTimeParser.getCurrentDateTime()));
								
								Specialization specDbResponse = specializationRepository.save(specializationObj);
								
								if(specDbResponse != null) {
									
									specializationIds.add(specDbResponse.getSpecializationId());
									
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Domain & Specialization updated successfully.");
									apiLogInfo.setApiResponse("Domain & Specialization updated successfully.");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
								}else {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									response.setServiceResponse("Unable to update specialization(s).");
									apiLogInfo.setApiResponse("Unable to update specialization(s).");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
								}
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Unable to find Domain Specialization(s).");
								apiLogInfo.setApiResponse("Unable to find Domain Specialization(s).");
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							}
						}else {
							//Create New Specialization
								Specialization specializationObj = new Specialization();
								
								specializationObj.setDomainId(domainObj.getDomainId());
								specializationObj.setIsActive("true");
								specializationObj.setCreatedBy(domainObj.getUpdatedBy());
								specializationObj.setSpecializationName(domain.getSpecializationName());
							
							    Specialization specializationDbResponse = specializationRepository.save(specializationObj);
							
                                    if(specializationDbResponse != null) {
									
									specializationIds.add(specializationDbResponse.getSpecializationId());
									
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("New Specialization added successfully.");
									apiLogInfo.setApiResponse("New Specialization added successfully.");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
								}else {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									response.setServiceResponse("Unable to add new specialization(s).");
									apiLogInfo.setApiResponse("Unable to add new specialization(s).");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
								}
						}
					}
					
					//InActivate Specialization
					List<Specialization> specializationtoBeRemove = specializationRepository
							.findBySpecializationIdNotInAndDomainId(specializationIds,domainObj.getDomainId());
					if(!specializationtoBeRemove.isEmpty()) {
						specializationtoBeRemove.forEach((object) -> {
							
							Specialization specializationObj = specializationRepository.findBySpecializationId(object.getSpecializationId());
							
							if(specializationObj != null) {
								
								specializationObj.setIsActive("false");
								specializationObj.setUpdatedBy(domainDTO.getUpdatedBy());
								specializationObj.setUpdatedOn(Timestamp.valueOf(stringToDateTimeParser.getCurrentDateTime()));
								
								Specialization dbResponse = specializationRepository.save(specializationObj);
							}
						});
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to update Domain.");
					apiLogInfo.setApiResponse("Unable to update Domain.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find domain.");
				apiLogInfo.setApiResponse("Unable to find domain.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse deleteDomain(DomainDTO domainDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setSubFeatureName("delete_domain");
		apiLogInfo.setApiUrl("/api/deleteDomain");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("domainName : "+domainDTO.getDomainName());
		try {
			
			Domain domainObj = domainRepository.findByDomainId(domainDTO.getDomainId());
			
			if(domainObj != null) {
				
				domainObj.setIsActive("false");
				domainObj.setUpdatedBy(domainDTO.getUpdatedBy());
				domainObj.setUpdatedOn(Timestamp.valueOf(stringToDateTimeParser.getCurrentDateTime()));
				
				Domain domainResponse = domainRepository.save(domainObj);
				
				if(domainResponse != null) {
					
					List<Specialization> allSpecialization = specializationRepository.findByDomainId(domainResponse.getDomainId());
					List<Specialization> specializationToBeRemoved = new ArrayList<Specialization>();
					
					if(!allSpecialization.isEmpty()) {
						
						allSpecialization.forEach((object) -> {
							
							object.setIsActive("false");
							object.setUpdatedBy(domainDTO.getUpdatedBy());
							object.setUpdatedOn(Timestamp.valueOf(stringToDateTimeParser.getCurrentDateTime()));
							
							specializationToBeRemoved.add(object);
						});
						List<Specialization> dbResponse = specializationRepository.saveAll(specializationToBeRemoved);
						
						if(dbResponse != null) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Domain & its specialization deleted successfully.");
							apiLogInfo.setApiResponse("Domain & its specialization deleted successfully.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Unable to delete Domain & its specialization.");
							apiLogInfo.setApiResponse("Unable to delete Domain & its specialization.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("No specialization found in domain.");
						apiLogInfo.setApiResponse("No specialization found in domain.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to delete domain.");
					apiLogInfo.setApiResponse("Unable to delete domain.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find domain.");
				apiLogInfo.setApiResponse("Unable to find domain.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse checkDomainName(DomainDTO domainDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/checkDomainName");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("domainName : "+domainDTO.getDomainName());
		
		try {
			
			Domain domainObj = domainRepository.findByDomainName(domainDTO.getDomainName());
			if(domainObj != null) {
				if((domainDTO.getDomainId() != null) && !domainDTO.getDomainId().equals(domainObj.getDomainId())) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Domain with same name already exists !!");
					apiLogInfo.setApiResponse("Domain with same name already exists !!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
				if(domainDTO.getDomainId() == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Domain with same name already exists !!");
					apiLogInfo.setApiResponse("Domain with same name already exists !!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getDomainSpecialization(DomainDTO domainDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/getDomainSpecialization");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("domainName : "+domainDTO.getDomainName());
		try {
			List<Long> domainIds = Arrays.asList(domainDTO.getDomainIdList());
			List<Specialization> allSpecialization = specializationRepository.findByDomainIdInAndIsActive(domainIds, "true");
			
			if(!allSpecialization.isEmpty()) {
				List<SpecializationDTO> specDtoList = new ArrayList<SpecializationDTO>();
				
				allSpecialization.forEach((object) -> {
					SpecializationDTO dto = new SpecializationDTO();
					
					dto.setSpecializationName(object.getSpecializationName());
					dto.setSpecializationId(object.getSpecializationId());
					dto.setDomainId(object.getDomainId());
					specDtoList.add(dto);					
				});
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(specDtoList);
				apiLogInfo.setApiResponse("specDtoList size : "+specDtoList.size());
				apiLogInfo.setApiResponse(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Specialization List is empty.");
				apiLogInfo.setApiResponse("Specialization List is empty.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getDomainSpecializationByEmpId(DomainDTO domainDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/getDomainSpecializationByEmpId");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("domainId : "+domainDTO.getDomainId());
		
		try {
			
			List<Object[]> domaninSpecializationList = employeeSpecializationMapRepository.getEmployeeDomainInfo(domainDTO.getEmpId());
			List<DomainDTO> finalList = new ArrayList<>();
			Set<Long> domainIds = new HashSet<Long>();
			Set<Long> specializationIds = new HashSet<Long>();
			
			if(!domaninSpecializationList.isEmpty()) {
				domaninSpecializationList.forEach((object) -> {
					DomainDTO domainObj = new DomainDTO();
					domainIds.add(object[5] != null ? Long.parseLong(object[5].toString()) : null);
					specializationIds.add(object[2] != null ? Long.parseLong(object[2].toString()) : null);
				});
			}
			
			List<Domain> domainObj = domainRepository.findByDomainIdIn(domainIds);
			
			if(!domainObj.isEmpty()) {
				domainObj.forEach((object) -> {
					DomainDTO domainDto = new DomainDTO();
					
					domainDto.setDomainName(object.getDomainName());
					domainDto.setDomainId(object.getDomainId());
					
					List<Specialization> specializationList = specializationRepository
							.findByDomainIdAndSpecializationIdInAndIsActive(object.getDomainId(),specializationIds, "true");
					
					if(!specializationList.isEmpty()) {
						List<SpecializationDTO> specList = new ArrayList<SpecializationDTO>();
						
						specializationList.forEach((specObject) -> {
							SpecializationDTO specDTO = new SpecializationDTO();
							
							specDTO.setSpecializationId(specObject.getSpecializationId());
							specDTO.setSpecializationName(specObject.getSpecializationName());
							
							specList.add(specDTO);
						});
						
						domainDto.setAllSpecializationList(specList);
					}
					finalList.add(domainDto);					
				});
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(finalList);
				apiLogInfo.setApiResponse("finalList size : "+finalList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Domain is mapped with user");
				apiLogInfo.setApiResponse("No Domain is mapped with user");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
}

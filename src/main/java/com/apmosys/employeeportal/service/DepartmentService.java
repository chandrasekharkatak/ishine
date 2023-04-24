package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.datetime.joda.LocalDateTimeParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PoPortalDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class DepartmentService {

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	@Autowired
	JobRoleRepository jobRoleRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Value("${poPortal.api.syncDepartment}")
	private String syncDepartmentApi;

	@Transactional
	public ServiceResponse createDepartment(DepartmentDTO departmentDTO) {
		String message = "";
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("create_department");
		apiLogInfo.setApiUrl("/api/createDepartment");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("departmentName : " +departmentDTO.getName()+ "hodId : " +departmentDTO.getHodId()+ "createdBy : " +departmentDTO.getCreatedBy());
		try {
			Department newDepartment = new Department();
			newDepartment.setName(departmentDTO.getName());
			newDepartment.setHodId(departmentDTO.getHodId());
			newDepartment.setCreatedBy(departmentDTO.getCreatedBy());

			Department newDepartmentCreated = departmentRepository.save(newDepartment);

			if (newDepartmentCreated != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New department created.");
				
				apiLogInfo.setApiResponse("New department created.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);	

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("New department creation Failed.");
				
				apiLogInfo.setApiResponse("New department creation Failed.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);	
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	@Transactional
	public ServiceResponse createDepartmentByList(DepartmentDTO departmentDTO) {
		String message = "";
		ServiceResponse response = new ServiceResponse();
		

		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("create_department");
		apiLogInfo.setApiUrl("/api/createDepartmentByList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("deptId : " +departmentDTO.getDeptId()+"departmentName : " +departmentDTO.getName()+ "hodId : " +departmentDTO.getHodId()+ "createdBy : " +departmentDTO.getCreatedBy());
		try {
			Department newDepartment = new Department();
			newDepartment.setDeptId(departmentDTO.getDeptId());
			newDepartment.setName(departmentDTO.getName());
			newDepartment.setHodId(departmentDTO.getHodId());
			newDepartment.setCreatedBy(departmentDTO.getCreatedBy());

			Department newDepartmentCreated = departmentRepository.save(newDepartment);

			if (newDepartmentCreated != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New department created.");
				
				apiLogInfo.setApiResponse("New department created");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("New department creation Failed.");
				
				apiLogInfo.setApiResponse("New department creation Failed");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		
		return response;
	}

	public ServiceResponse getAllDepartments() {
		ServiceResponse response = new ServiceResponse();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		try {
			List<Object[]> allDepartmentList = departmentRepository.getAllDepartments();
			if (allDepartmentList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department List is Empty.");
			} else {
				List<DepartmentDTO> dtoList = new ArrayList<DepartmentDTO>();
				for (Object[] object : allDepartmentList) {
					DepartmentDTO departmentDTO = new DepartmentDTO();
					departmentDTO.setDeptId(Long.parseLong(object[0].toString()));
					departmentDTO.setCreatedBy(Integer.parseInt(object[1].toString()));
					departmentDTO.setCreatedOn(object[2].toString());
					departmentDTO.setName(object[3].toString());
					departmentDTO.setCreatedByName(object[4].toString());
					departmentDTO.setHodName(object[5].toString());
					departmentDTO.setHodId(Long.parseLong(object[6].toString()));
					departmentDTO.setUpdatedOn(object[7] != null ? object[7].toString(): null);
					departmentDTO.setUpdatedByName(object[8] != null ? object[8].toString() : null);
					dtoList.add(departmentDTO);      
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);

			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateDepartment(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_department");
		apiLogInfo.setApiUrl("/api/updateDepartment");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("deptId : " + departmentDTO.getDeptId());
		try {
			Optional<Department> departmentObject = departmentRepository.findById(departmentDTO.getDeptId());
			if (departmentObject.isPresent()) {
				Department departmentToBeUpdated = departmentObject.get();
				departmentToBeUpdated.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				departmentToBeUpdated.setName(departmentDTO.getName());
				departmentToBeUpdated.setHodId(departmentDTO.getHodId());
				departmentToBeUpdated.setUpdatedBy(departmentDTO.getUpdatedBy());

				Department dbResponse = departmentRepository.save(departmentToBeUpdated);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Department Updated.");
					
					apiLogInfo.setApiResponse("Department Updated.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Department Updation Failed.");
					
					apiLogInfo.setApiResponse("Department Updated.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department Not Found");
				
				apiLogInfo.setApiResponse("Department Not Found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Transactional
	public ServiceResponse deleteDepartment(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("delete_department");
		apiLogInfo.setApiUrl("/api/deleteDepartment");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("deptId : " + departmentDTO.getDeptId());
		
		boolean isDeptUsedInPoPortal = false;
		try {
			Optional<Department> departmentObject = departmentRepository.findById(departmentDTO.getDeptId());
			if (departmentObject.isPresent()) {
				Department departmentToBeDeleted = departmentObject.get();

				Long count = jobRoleRepository.countByDeptId(departmentToBeDeleted.getDeptId());
				
				//check Dept in PoPortal
				try {
					
					final String syncUrl = "http://192.168.21.175:8080/PoPortal/ishine/isDepartmentUsed/{deptId}";
					RestTemplate restTemplate = new RestTemplate();
					String syncResponse = restTemplate.getForObject(syncUrl, String.class, departmentToBeDeleted.getDeptId());
					
					JSONObject json = new JSONObject(syncResponse);
					
					if(json.getInt("httpStatusCode") == 200) {
						
						//0=Not Present
						//1=Present but not used
						//2=Used
						
						if(json.get("message").equals("0")) {
							isDeptUsedInPoPortal = false;
						}
						
						if(json.get("message").equals("1")) {
							//Delete dept from poPortal http://192.168.21.175:8080/PoPortal/ishine/deleteDepartment/{id}
							
							final String deleteUrl = "http://192.168.21.175:8080/PoPortal/ishine/deleteDepartment/{id}";
							RestTemplate deleteRestTemplate = new RestTemplate();
							restTemplate.delete(deleteUrl, departmentToBeDeleted.getDeptId());
						}
						
						if(json.get("message").equals("2")) {
							isDeptUsedInPoPortal = true;
						}
					}
					
				}catch(InternalServerError e) {
					TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
					JSONObject json = new JSONObject(e.getResponseBodyAsString());
					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(json.get("message"));
					
				}catch(HttpClientErrorException e) {
					TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
					
                    JSONObject json = new JSONObject(e.getResponseBodyAsString());
					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(json.get("message"));
				}
				
				if (count == 0 && isDeptUsedInPoPortal == false) {

					departmentRepository.deleteById(departmentToBeDeleted.getDeptId());
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Department deleted.");
					
					apiLogInfo.setApiResponse("Department deleted.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					
					DepartmentDTO dtoObject = new DepartmentDTO();
					
					if(count != 0) {
						dtoObject.setIsDeptUsedInIshine("true");
					}else {
						dtoObject.setIsDeptUsedInIshine("false");
					}
					if(isDeptUsedInPoPortal == true) {
						dtoObject.setIsDeptUsedInPoPortal("true");
					}else {
						dtoObject.setIsDeptUsedInPoPortal("false");
					}
					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(dtoObject);
					
					apiLogInfo.setApiResponse(dtoObject + "Department cannot be deleted as it is mapped to job role(s).");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department Not Found.");
				
				apiLogInfo.setApiResponse("Department Not Found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	@Transactional
	public ServiceResponse changeDepartmentJobRoleMapping(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("delete_department");
		apiLogInfo.setApiUrl("/api/changeDepartmentJobRoleMapping");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("deptId : " + departmentDTO.getDeptId());
		try {
			List<JobRole> jobRoleDepartment = jobRoleRepository.findByDeptId(departmentDTO.getOldDeptId());
			
			if(!jobRoleDepartment.isEmpty()) {
				for(JobRole jobrole: jobRoleDepartment) {
					
					JobRole dbResponse = null;
					if(departmentDTO.getIsDeptUsedInIshine().equals("true")) {
						
						jobrole.setDeptId(departmentDTO.getDeptId());
						dbResponse = jobRoleRepository.save(jobrole);
					}else {
						dbResponse.setName("Not used in Ishine");
					}

					if (dbResponse != null) {
						if(departmentDTO.getIsDeptUsedInPoPortal().equals("true")) {
							
							ServiceResponse syncDeleteResponse = syncDeleteDepartmentWithPoPortal(departmentDTO);
							
							if(syncDeleteResponse.getServiceStatus().equals("Success")) {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse(syncDeleteResponse.getServiceResponse());
							}else {
                                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
								
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse(syncDeleteResponse.getServiceResponse());
							}
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Department Deleted successfully.");
							
							apiLogInfo.setApiResponse("Department Deleted successfully.");			
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Job Role Updation Failed.");
						
						apiLogInfo.setApiResponse("Job Role Updation Failed");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				}
			}else {
				
				if(departmentDTO.getIsDeptUsedInPoPortal().equals("true")) {
					ServiceResponse syncDeleteResponse = syncDeleteDepartmentWithPoPortal(departmentDTO);
					
					if(syncDeleteResponse.getServiceStatus().equals("Success")) {
						
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(syncDeleteResponse.getServiceResponse());
					}else {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
						
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(syncDeleteResponse.getServiceResponse());
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No job role Found");
					
					apiLogInfo.setApiResponse("No job role Found");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);	
				}
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public ServiceResponse syncDeleteDepartmentWithPoPortal(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			//Sync Deleted Dept with PoPortal
			Department deptObj = departmentRepository.findByDeptId(departmentDTO.getDeptId());
			
			if(deptObj != null) {
				Employee empObj = employeeRepository.findByEmpId(deptObj.getHodId());
				
				DepartmentDTO syncObject = new DepartmentDTO();
				syncObject.setDeptId(deptObj.getDeptId());
				syncObject.setDeptName(deptObj.getName());
				syncObject.setHodEmploymentId("A-".concat(empObj.getEmployeementId().toString()));
				
				try {
					
					final String syncUrl = "http://192.168.21.175:8080/PoPortal/ishine/updateDepartment/{id}";
					RestTemplate restTemplate = new RestTemplate();
					String syncResponse = restTemplate.postForObject(syncUrl, syncObject, String.class, departmentDTO.getOldDeptId());
					
					JSONObject json = new JSONObject(syncResponse);
					
					if(json.getInt("httpStatusCode") == 200) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Department Deleted & Synced with PoPortal");
					}
					
				}catch(InternalServerError e) {
					JSONObject json = new JSONObject(e.getResponseBodyAsString());
					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(json.get("message"));
					
				}catch(HttpClientErrorException e) {
                    JSONObject json = new JSONObject(e.getResponseBodyAsString());
					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(json.get("message"));
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department not found.");
			}
			
		}catch(Exception e){
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse checkDepartmentName(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Department deptObj = departmentRepository.findByName(departmentDTO.getName());
			
			if(deptObj != null) {
				if((departmentDTO.getDeptId() != null) && !departmentDTO.getDeptId().equals(deptObj.getDeptId())) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Department with same name already exists !!");
				}
				if(departmentDTO.getDeptId() == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Department with same name already exists !!");
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllDepartmentInfo() {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Object[]> departmentObj = departmentRepository.getDepartmentInfo();
			List<PoPortalDTO> dtoList = new ArrayList<PoPortalDTO>();
			
			if(!departmentObj.isEmpty()) {
				departmentObj.forEach((object) -> {
					PoPortalDTO dto = new PoPortalDTO();
					
					dto.setDeptId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setDeptName(object[1] != null ? object[1].toString() : null);
					dto.setHodId(object[2] != null ? object[2].toString() : null);
					
					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department Info not found.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}

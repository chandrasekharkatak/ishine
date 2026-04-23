package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.AclColumnDTO;
import com.apmosys.employeeportal.dto.BulkBillableUpdateDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeRoleDTO;
import com.apmosys.employeeportal.dto.FeatureMasterDTO;
import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectEmployeeTeamReportDTO;
import com.apmosys.employeeportal.dto.ProjectNamesRequestDTO;
import com.apmosys.employeeportal.dto.MappedSubFeatureDTO;
import com.apmosys.employeeportal.dto.MappedSubFeatureDTO;
import com.apmosys.employeeportal.dto.ProjectInfoDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.EmpPrimaryProjectMapping;
import com.apmosys.employeeportal.model.EmployeeRole;
import com.apmosys.employeeportal.model.FieldAlteration;
import com.apmosys.employeeportal.repository.EmpPrimaryProjectMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeRoleMasterRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.FieldAlterationRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ReportService {
	
	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;
	
	@Autowired
	TimesheetsRepository timesheetsRepository;
	
	@Autowired
	EmployeeRoleMasterRepository employeeRoleMasterRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Autowired
	EmpPrimaryProjectMappingRepository empPrimaryProjectMappingRepository;
	
	@Autowired
	CronJobService cronJobService;
	
	@Autowired
	FieldAlterationRepository fieldAlterationRepository;
	
	
	@Autowired
	private LogService logService;
	
	
	
	
	
	@Autowired
	private HttpServletRequest httpRequest;

	public ServiceResponse leaveReport() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/leaveReport");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			List<Object[]> allLeaveData = employeeLeaveRepository.getLeaveReport();
			logBuilder.append("LeaveReport :" + allLeaveData.size());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (allLeaveData != null) {
				allLeaveData.forEach((object) -> {
					LeaveDTO leavedto = new LeaveDTO();

					leavedto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					leavedto.setEmployeeName(object[1] != null ? object[1].toString() : null);
					leavedto.setLeaveType(object[2] != null ? object[2].toString() : null);
					leavedto.setFromDate(object[3] != null ? object[3].toString() : null);
					leavedto.setToDate(object[4] != null ? object[4].toString() : null);
					leavedto.setNoOfDays(object[5] != null ? Float.parseFloat(object[5].toString()) : null);
					leavedto.setReason(object[6] != null ? object[6].toString() : null);
					leavedto.setStatus(object[7] != null ? object[7].toString() : null);
					leavedto.setManagerName(object[8] != null ? object[8].toString() : null);
					leavedto.setCreatedOn(object[9] != null ? object[9].toString() : null);
					leavedto.setUpdatedOn(object[10] != null ? object[10].toString() : null);
					leavedto.setLeaveStatusUpdatedByName(object[11] != null ? object[11].toString() : null);
					leavedto.setDepartmentName(object[12] != null ? object[12].toString() : null);
					leavedto.setEmploymentStatus(object[13] != null ? object[13].toString() : null);
					leavedto.setFromDateDayType(object[14] != null ? Float.parseFloat(object[14].toString()) : null);
					leavedto.setToDateDayType(object[15] != null ? Float.parseFloat(object[15].toString()) : null);
					leavedto.setManagerId(object[16] != null ? Integer.parseInt(object[16].toString()) : null);
					leavedto.setLeaveStatusUpdatedBy(object[17] != null ? Long.parseLong(object[17].toString()) : null);
					
					dtoList.add(leavedto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("LeaveReport :" + dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("leave Application list is empty.");
				apiLogInfo.setApiResponse("leave Application List is Empty");			
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

	public ServiceResponse timesheetReport() {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        //apiLogInfo.setSubFeatureName("");
        apiLogInfo.setApiUrl("/api/timesheetReport");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
		try {
			List<Object[]> allTimeSheetData = timesheetsRepository.getAllTimesheetData();
       		logBuilder.append("TimeSheetReport:" + allTimeSheetData.size());
			List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();
			
			if(allTimeSheetData != null) {
				allTimeSheetData.forEach((object) -> {
					TimesheetDTO timesheetDto = new TimesheetDTO();
					
					timesheetDto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					timesheetDto.setEmployeeName(object[1] != null ? object[1].toString() : null);
					timesheetDto.setDate(object[2] != null ? object[2].toString() : null);
					timesheetDto.setDayType(object[3] != null ? object[3].toString() : null);
					timesheetDto.setDescription(object[4] != null ? object[4].toString() : null);
					timesheetDto.setStatus(object[5] != null ? object[5].toString() : null);
					timesheetDto.setTotalWorkingHours(object[6] != null ? Float.parseFloat(object[6].toString()) : null);
					timesheetDto.setCreatedOn(object[7] != null ? object[7].toString() : null);
					timesheetDto.setUpdatedOn(object[8] != null ? object[8].toString() : null);
					timesheetDto.setTimesheetStatusUpdatedByName(object[9] != null ? object[9].toString() : null);
					timesheetDto.setOfficeInTime(object[10] != null ? object[10].toString() : null);
					timesheetDto.setOfficeOutTime(object[11] != null ? object[11].toString() : null);
					timesheetDto.setTotalWorkingOfficeHours(object[12] != null ? object[12].toString() : null);
					timesheetDto.setEmploymentstatus(object[10] != null ? object[10].toString() : null);	
					timesheetDto.setTimesheetStatusUpdatedBy(object[11] != null ? Long.parseLong(object[11].toString()) : null);
					
					dtoList.add(timesheetDto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				System.out.println("DTOList :" +dtoList);
                apiLogInfo.setApiResponse("TimeSheetReport: " + dtoList.size());			
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet list is empty.");
                apiLogInfo.setApiResponse("Timesheet list is empty");			
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		}catch(Exception e) {
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

	public ServiceResponse getMappedSubFeatureList(EmployeeDTO employeeDto) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        //apiLogInfo.setSubFeatureName("");
        apiLogInfo.setApiUrl("/api/getMappedSubFeatureList");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("JobRoleId : " + employeeDto.getJobRoleIds());
		try {
	        List<Long> jobRoleIds = employeeDto.getJobRoleIds();
	        if (jobRoleIds == null || jobRoleIds.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Job role list is empty");
	            return response;
	        }
	        
			List<MappedSubFeatureDTO> mappedSubFeatureByJobRoleId = employeeRoleMasterRepository
					.getMappedSubFeatureByJobRoleIds(jobRoleIds);
			
			 if (mappedSubFeatureByJobRoleId == null || mappedSubFeatureByJobRoleId.isEmpty()) {
		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse("Mapped Role List is Empty.");
		            apiLogInfo.setApiResponse("Mapped Role List is Empty.");
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		            
		        } else {
		            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(mappedSubFeatureByJobRoleId);
		            apiLogInfo.setApiResponse("MappedRoleList : " + mappedSubFeatureByJobRoleId.size());
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		        }		
			 
		}catch(Exception e) {
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

	public ServiceResponse getAllSubFeatureList() {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        //apiLogInfo.setSubFeatureName("");
        apiLogInfo.setApiUrl("/api/getAllSubFeatureList");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();

		try {
			
			List<Object[]> accessControlList = employeeRoleMasterRepository
					.getAllSubFeatureList();
        	logBuilder.append("AllSubFeatureList:" + accessControlList.size());
			
			List<EmployeeDTO> dtoList = new ArrayList<>();
			
			if (!accessControlList.isEmpty()) {

				accessControlList.forEach((object) -> {
					EmployeeDTO empDTO = new EmployeeDTO();

					empDTO.setSubFeatureId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					empDTO.setSubFeatureName(object[1] != null ? object[1].toString() : null);
					empDTO.setFeatureId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
					empDTO.setFeatureName(object[3] != null ? object[3].toString() : null);

					dtoList.add(empDTO);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
                apiLogInfo.setApiResponse("Dtolist :" + dtoList.size());
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("ACL (Access control list) is empty.");
                apiLogInfo.setApiResponse("ACL (Access Control List) is empty ");
              apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		}catch(Exception e) {
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

	public ServiceResponse getDefaultMapping(List<AclColumnDTO> aclColumnDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getDefaultMapping");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		//logBuilder.append("");
		try {
			
			List<String> tabNames = null;
		    List<String> featureNames = null;
		    List<String> subFeatureNames = null;

		    for (AclColumnDTO f : aclColumnDTO) {
		        if (f.getValue() == null || f.getValue().isEmpty()) continue;

		        switch (f.getColumn()) {
		            case "Tab Name":
		                tabNames = f.getValue();
		                break;
		            case "Feature":
		                featureNames = f.getValue();
		                break;
		            case "Sub Feature":
		                subFeatureNames = f.getValue();
		                break;
		        }
		    }
		    			
		    List<EmployeeRoleDTO> subFeatureList = employeeRoleMasterRepository.getAllSubFeatureList(tabNames,featureNames,subFeatureNames);
			List<EmployeeRoleDTO> dtoList = new ArrayList<EmployeeRoleDTO>();
			
			if(!subFeatureList.isEmpty()) {
				subFeatureList.forEach((object) -> {
					List<EmployeeRole> employeeRoleMaster = employeeRoleMasterRepository.findBySubFeatureMasterId(object.getSubFeatureId());
					List<FeatureMasterDTO> permissionList = new ArrayList<FeatureMasterDTO>();
					
					if(!employeeRoleMaster.isEmpty()) {
						employeeRoleMaster.forEach((featureObject) -> {
							FeatureMasterDTO featureDto = new FeatureMasterDTO();
							
							featureDto.setEmployeeRole(featureObject.getEmployeeRole());
							featureDto.setPermission(featureObject.getPermission());
							permissionList.add(featureDto);
						});
					}
					
					object.setPermissionList(permissionList);
					dtoList.add(object);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("Sub-Feature & Feature list: " + dtoList.size());			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Sub-Feature & Feature list is empty.");
				apiLogInfo.setApiResponse("Sub-Feature & Feature list is empty");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			}
			
		}catch(Exception e) {
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

//	public ServiceResponse updateDefaultFeatureMapping(JobRoleDTO jobRoleDTO) {
//		
//		String message = "";
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("updateDefaultFeatureMapping");
//		apiLogInfo.setApiUrl("/api/updateDefaultFeatureMapping");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("Update Default Feature Mapping |  Updated By : " + jobRoleDTO.getUpdatedBy());
//		
//		ServiceResponse response = new ServiceResponse();
//		try {
//			
//			if(!jobRoleDTO.getUpdateDefaultFeatureMapping().isEmpty()) {
//				
//				jobRoleDTO.getUpdateDefaultFeatureMapping().forEach((object) -> {
//					
//					if(object.getPermission().equals("true")) {
//						object.setPermission("Y");
//					}else {
//						object.setPermission("N");
//					}
//					
//					EmployeeRole defaultRole = employeeRoleMasterRepository
//							.findBySubFeatureMasterIdAndEmployeeRole(object.getSubFeatureId(), object.getEmployeeRole());
//					
//					if(defaultRole != null) {
//						
//						defaultRole.setPermission(object.getPermission());
//						EmployeeRole dbResponse = employeeRoleMasterRepository.save(defaultRole);
//						
//						if(dbResponse != null) {
//							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//							response.setServiceResponse("Default role sub-feature mapping updated successfully.");
//							
//							apiLogInfo.setApiResponse("Default role sub-feature mapping updated successfully.");			
//							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);	
//						}else {
//							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//							response.setServiceResponse("Unable to update default role and sub-feature mapping.");
//							
//							apiLogInfo.setApiResponse("Unable to update default role and sub-feature mapping.");			
//							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);	
//						}
//					}else {
//						//Add new role mapping
//						EmployeeRole employeeRole = new EmployeeRole();
//
//						employeeRole.setSubFeatureMasterId(object.getSubFeatureId());
//						employeeRole.setSubFeatureName(object.getSubFeatureName());
//						employeeRole.setEmployeeRole(object.getEmployeeRole());
//						employeeRole.setPermission(object.getPermission());
//
//						EmployeeRole newEmployeeRoleMapping = employeeRoleMasterRepository.save(employeeRole);
//						
//						if(newEmployeeRoleMapping != null) {
//							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//							response.setServiceResponse("New Role sub-feature mapping added successfully.");
//							
//							apiLogInfo.setApiResponse("New Role sub-feature mapping added successfully.");			
//							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//						}else {
//							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//							response.setServiceResponse("Unable to add new role sub-feature mapping.");
//							
//							apiLogInfo.setApiResponse("Unable to add new role sub-feature mapping.");		
//							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//						}
//					}
//				});
//			}else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Please select atleast one sub-feature to update default mapping.");
//				
//				apiLogInfo.setApiResponse("Please select atleast one sub-feature to update default mapping.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//		}catch(Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateDefaultFeatureMapping(JobRoleDTO jobRoleDTO) {

	    String message = "";
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("updateDefaultFeatureMapping");
	    apiLogInfo.setApiUrl("/api/updateDefaultFeatureMapping");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Update Default Feature Mapping | Updated By : ").append(jobRoleDTO.getUpdatedBy());

	    ServiceResponse response = new ServiceResponse();

	    try {
	        if (jobRoleDTO == null || jobRoleDTO.getUpdateDefaultFeatureMapping() == null
	                || jobRoleDTO.getUpdateDefaultFeatureMapping().isEmpty()) {

	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Please select at least one sub-feature to update default mapping.");

	            apiLogInfo.setApiResponse("Empty or null updateDefaultFeatureMapping list.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        Set<String> processedKeys = new HashSet<>();

	        jobRoleDTO.getUpdateDefaultFeatureMapping().forEach(object -> {
	            if (object != null && object.getSubFeatureId() != null && object.getEmployeeRole() != null) {

	                if ("true".equalsIgnoreCase(object.getPermission())) {
	                    object.setPermission("Y");
	                } else {
	                    object.setPermission("N");
	                }

	                // Avoid processing duplicates (same subFeatureId + employeeRole combo)
	                String key = object.getSubFeatureId() + "_" + object.getEmployeeRole();
	                if (!processedKeys.add(key)) {
	                    return; // Skip duplicate entry
	                }

	                EmployeeRole defaultRole = employeeRoleMasterRepository
	                        .findBySubFeatureMasterIdAndEmployeeRole(object.getSubFeatureId(), object.getEmployeeRole());

	                if (defaultRole != null) {
	                    // Update existing mapping
	                    defaultRole.setPermission(object.getPermission());
	                    EmployeeRole dbResponse = employeeRoleMasterRepository.save(defaultRole);

	                    if (dbResponse != null) {
	                        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                        response.setServiceResponse("Default role sub-feature mapping updated successfully.");

	                        apiLogInfo.setApiResponse("Default role sub-feature mapping updated successfully.");
	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                    } else {
	                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                        response.setServiceResponse("Unable to update default role and sub-feature mapping.");

	                        apiLogInfo.setApiResponse("Unable to update default role and sub-feature mapping.");
	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                    }

	                } else {
	                    // Add new mapping if not found
	                    EmployeeRole employeeRole = new EmployeeRole();
	                    employeeRole.setSubFeatureMasterId(object.getSubFeatureId());
	                    employeeRole.setSubFeatureName(object.getSubFeatureName());
	                    employeeRole.setEmployeeRole(object.getEmployeeRole());
	                    employeeRole.setPermission(object.getPermission());

	                    EmployeeRole newEmployeeRoleMapping = employeeRoleMasterRepository.save(employeeRole);

	                    if (newEmployeeRoleMapping != null) {
	                        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                        response.setServiceResponse("New role sub-feature mapping added successfully.");

	                        apiLogInfo.setApiResponse("New role sub-feature mapping added successfully.");
	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                    } else {
	                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                        response.setServiceResponse("Unable to add new role sub-feature mapping.");

	                        apiLogInfo.setApiResponse("Unable to add new role sub-feature mapping.");
	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                    }
	                }
	            }
	        });

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("Exception occurred while updating default feature mapping: " + e.getMessage());
	        throw e;
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	public ServiceResponse getPoProjectDetailsBOthPOAndInternal() {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getPoProjectDetailsForPoProjects");
        apiLogInfo.setApiUrl("/api/getPoProjectDetailsForPoProjects");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();

		try {
			List<Object[]> projectInfoList = projectRepository.getPoProjectDetailsBOthPOAndInternal();
        	logBuilder.append("getPoProjectDetailsForPoProjects "+projectInfoList.size());
			List<ProjectInfoDTO> projectObjList = new ArrayList<ProjectInfoDTO>();
			
		    if (projectInfoList != null || !projectInfoList.isEmpty()) {
		    	projectInfoList.forEach((projectInfo) -> {
		    	ProjectInfoDTO projectObj = new ProjectInfoDTO();
		    	
		    	projectObj.setProjectId(projectInfo[0]!=null ? Integer.parseInt(projectInfo[0].toString()) : null);
		    	projectObj.setProjectName(projectInfo[1]!=null ? projectInfo[1].toString() : null);
		        projectObj.setPoNo(projectInfo[2]!=null ? projectInfo[2].toString() : null);
		        projectObj.setStartDate(projectInfo[3]!=null ? projectInfo[3].toString() : null);
		        projectObj.setEndDate(projectInfo[4]!=null ? projectInfo[4].toString() : null);
		        projectObj.setProjectType(projectInfo[5]!=null ? projectInfo[5].toString() : null);
		        projectObj.setId(projectInfo[6]!=null ? Long.parseLong(projectInfo[6].toString()) : null);
		        
		        projectObjList.add(projectObj);
		    	});
	    	}
	        if(projectObjList != null || !projectObjList.isEmpty()) {
	        	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(projectObjList);
	        }else {
	        	response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Data not present !");
	        }
		}catch(Exception e) {
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
	
	
	public ServiceResponse updateBillableType(EmployeeDTO dto) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	    	String oldBillableType = employeeRepository.findBillableTypeByEmpId(dto.getEmpId());
	    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        String updatedOn = LocalDateTime.now().format(formatter);
	        int result = employeeRepository.updateBillableInfo(dto.getEmpId(), dto.getBillableType(), dto.getBillable(), dto.getUpdatedBy(),updatedOn);
	        		if (result > 0) {
	        			cronJobService.triggerBillableTypeChangeMail(dto.getEmpId(), dto.getBillableType(), oldBillableType, dto.getUpdatedBy());
	        			 FieldAlteration alterationLog = new FieldAlteration();
	        	            alterationLog.setEmpId(dto.getEmpId());
	        	            alterationLog.setField("Billable Type");
	        	            alterationLog.setValue(dto.getBillableType());
	        	            alterationLog.setAlteredBy(dto.getUpdatedBy());
	        	            alterationLog.setUpdatedOn(LocalDateTime.now());
	        	            fieldAlterationRepository.save(alterationLog); 
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Billable Type Of Employee Updated successfully");
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Update failed");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceError(e.getMessage());
	        response.setServiceResponse("Something went wrong");
	    }
	    return response;
	}
	
	
//	public ServiceResponse updateBulkBillableEmployeeReport(BulkBillableUpdateDTO bulkBillableUpdateDTO) {
//	    ServiceResponse response = new ServiceResponse();
//
//	    try {
//	    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//	        String updatedOn = LocalDateTime.now().format(formatter);
//	        
//	        Map<Long, String> oldBillableTypes = new HashMap<>();
//	        for (Long empId : bulkBillableUpdateDTO.getEmpIds()) {
//	            String oldType = employeeRepository.findBillableTypeByEmpId(empId);
//	            oldBillableTypes.put(empId, oldType);
//	        }
//	        int updatedRows = employeeRepository.updateBillableTypeForMultiple(
//	        		bulkBillableUpdateDTO.getEmpIds(), bulkBillableUpdateDTO.getBillableType(), bulkBillableUpdateDTO.getBillable(),bulkBillableUpdateDTO.getUpdatedBy(),updatedOn);
//	        if (updatedRows > 0) {
//	        	cronJobService.triggerBulkBillableChangeEmails(
//	                    bulkBillableUpdateDTO.getEmpIds(),
//	                    oldBillableTypes,
//	                    bulkBillableUpdateDTO.getBillableType(),
//	                    bulkBillableUpdateDTO.getUpdatedBy()
//	                );
//	        	List<FieldAlteration> alterationLogs = new ArrayList<>();
//
//	            for (Long empId : bulkBillableUpdateDTO.getEmpIds()) {
//	                FieldAlteration alterationLog = new FieldAlteration();
//	                alterationLog.setEmpId(empId);
//	                alterationLog.setField("Billable Type");
//	                alterationLog.setValue(bulkBillableUpdateDTO.getBillableType());
//	                alterationLog.setAlteredBy(bulkBillableUpdateDTO.getUpdatedBy());
//	                alterationLog.setUpdatedOn(LocalDateTime.now());
//	                alterationLogs.add(alterationLog);
//	            }
//
//	          
//	            fieldAlterationRepository.saveAll(alterationLogs);
//	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	            response.setServiceResponse("Updated Billable Type of " + updatedRows + " employees successfully.");
//	        } else {
//	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	            response.setServiceResponse("No records were updated.");
//	        }
//
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	        response.setServiceResponse("Something went wrong while updating.");
//	        response.setServiceError(e.getMessage());
//	    }
//
//	    return response;
//	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateBulkBillableEmployeeReport(BulkBillableUpdateDTO bulkBillableUpdateDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("update_Bulk_Billable_Employee_Report");
	    apiLogInfo.setApiUrl("/api/updateBulkBillableEmployeeReport");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();

	    try {
	        // Validate input DTO
	        if (bulkBillableUpdateDTO == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Request body cannot be null.");
	            apiLogInfo.setApiResponse("Request body is null.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        if (bulkBillableUpdateDTO.getEmpIds() == null || bulkBillableUpdateDTO.getEmpIds().isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee ID list is empty.");
	            apiLogInfo.setApiResponse("Employee ID list is empty.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        if (bulkBillableUpdateDTO.getBillableType() == null || bulkBillableUpdateDTO.getBillableType().trim().isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Billable Type cannot be null or empty.");
	            apiLogInfo.setApiResponse("Billable Type validation failed.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        if (bulkBillableUpdateDTO.getUpdatedBy() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Updated By cannot be null.");
	            apiLogInfo.setApiResponse("Updated By is null.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // Prepare timestamp
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        String updatedOn = LocalDateTime.now().format(formatter);

	        // Store old billable types for each employee
	        Map<Long, String> oldBillableTypes = new HashMap<>();
	        for (Long empId : bulkBillableUpdateDTO.getEmpIds()) {
	            if (empId == null) continue;
	            String oldType = employeeRepository.findBillableTypeByEmpId(empId);
	            oldBillableTypes.put(empId, oldType != null ? oldType : "N/A");
	        }

	        logBuilder.append("Employees to update: ").append(bulkBillableUpdateDTO.getEmpIds().size()).append(" | ");
	        logBuilder.append("UpdatedBy: ").append(bulkBillableUpdateDTO.getUpdatedBy()).append(" | ");
	        logBuilder.append("New Billable Type: ").append(bulkBillableUpdateDTO.getBillableType()).append(" | ");

	        // Perform bulk update
	        int updatedRows = employeeRepository.updateBillableTypeForMultiple(
	            bulkBillableUpdateDTO.getEmpIds(),
	            bulkBillableUpdateDTO.getBillableType(),
	            bulkBillableUpdateDTO.getBillable(),
	            bulkBillableUpdateDTO.getUpdatedBy(),
	            updatedOn
	        );

	        if (updatedRows > 0) {
	            // Send emails for changed employees
	            cronJobService.triggerBulkBillableChangeEmails(
	                bulkBillableUpdateDTO.getEmpIds(),
	                oldBillableTypes,
	                bulkBillableUpdateDTO.getBillableType(),
	                bulkBillableUpdateDTO.getUpdatedBy()
	            );

	            // Log all field alterations
	            List<FieldAlteration> alterationLogs = new ArrayList<>();
	            for (Long empId : bulkBillableUpdateDTO.getEmpIds()) {
	                if (empId == null) continue;
	                FieldAlteration alterationLog = new FieldAlteration();
	                alterationLog.setEmpId(empId);
	                alterationLog.setField("Billable Type");
	                alterationLog.setValue(bulkBillableUpdateDTO.getBillableType());
	                alterationLog.setAlteredBy(bulkBillableUpdateDTO.getUpdatedBy());
	                alterationLog.setUpdatedOn(LocalDateTime.now());
	                alterationLogs.add(alterationLog);
	            }

	            if (!alterationLogs.isEmpty()) {
	                fieldAlterationRepository.saveAll(alterationLogs);
	            }

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Updated Billable Type of " + updatedRows + " employees successfully.");
	            apiLogInfo.setApiResponse("Bulk Billable Type updated successfully for " + updatedRows + " employees.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No records were updated.");
	            apiLogInfo.setApiResponse("No records were updated.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong while updating.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");

	        // Transaction rollback handled automatically by @Transactional
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}


	
//	public ServiceResponse updateDefaultProjectMappings() {
//	    ServiceResponse response = new ServiceResponse();
//	    try {
//	    	 List<Long> activeEmployees = employeeRepository.findAllActiveEmployees();
//	    	 for(Long empId : activeEmployees) {
//	    		 List<Long> activeProjectIds = employeeTeamMapRepository.findDistinctActiveProjectIdsByEmpId(empId);
//	    		
//	    		 if (activeProjectIds.isEmpty()) {
//	    			 EmpPrimaryProjectMapping existing = empPrimaryProjectMappingRepository.findByEmpId(empId);
//	    			    if (existing != null) {
//	    			    	 empPrimaryProjectMappingRepository.updateIsMappedOnlyTON(empId, "N", new Date());
//	    			    }
//	    			    continue;
//	    		 }
//	    		 
//	    	
//	    		 if (activeProjectIds.size() > 1) {
//	                    continue; 
//	                }
//	    		 
//	    		 Long projectId = activeProjectIds.get(0);
//	             String projectName = employeeTeamMapRepository.getProjectNameById(projectId);
//	             
//	             Optional<EmpPrimaryProjectMapping> existingMappingOpt =
//	            		 empPrimaryProjectMappingRepository.findByEmpIdd(empId);
//	             if (existingMappingOpt.isPresent()) {
//	                    EmpPrimaryProjectMapping existingMapping = existingMappingOpt.get();
//	             
//	             if (Objects.equals(existingMapping.getPrimaryProjectId(), projectId)
//                         && Objects.equals(existingMapping.getPrimaryProjectName(), projectName)
//                         && "Y".equalsIgnoreCase(existingMapping.getIsMapped())) {
//                     continue;
//                 }
//
//                 
//                 empPrimaryProjectMappingRepository.updateMappingDetails(
//                         empId, projectId, projectName, "Y", new Date()
//                 );
//                 
//                 
//	             } else {
//	                    
//	                    EmpPrimaryProjectMapping newMapping = new EmpPrimaryProjectMapping();
//	                    newMapping.setEmpId(empId);
//	                    newMapping.setPrimaryProjectId(projectId);
//	                    newMapping.setPrimaryProjectName(projectName);
//	                    newMapping.setIsMapped("Y");
//	                    newMapping.setUpdatedOn(LocalDateTime.now());
//	                    empPrimaryProjectMappingRepository.save(newMapping);
//	                }
//	            }
//
//	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	            response.setServiceResponse("Employee default project mapping updated successfully.");
//	        } catch (Exception e) {
//	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	            response.setServiceResponse("Error while updating mapping.");
//	            response.setServiceError(e.getMessage());
//	            e.printStackTrace();
//	        }
//
//	        return response;
//	    }
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateDefaultProjectMappings() {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Update Default Project Mappings");
	    apiLogInfo.setApiUrl("/api/updateDefaultProjectMappings");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Initiating default project mapping update process.");

	    try {
	        List<Long> activeEmployees = employeeRepository.findAllActiveEmployees();

	        if (activeEmployees == null || activeEmployees.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No active employees found for default project mapping.");

	            apiLogInfo.setApiResponse("No active employees found.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        for (Long empId : activeEmployees) {
	            if (empId == null) continue; 

	            List<Long> activeProjectIds = employeeTeamMapRepository.findDistinctActiveProjectIdsByEmpId(empId);

	            // If employee has no active project
	            if (activeProjectIds == null || activeProjectIds.isEmpty()) {
	            	Optional<EmpPrimaryProjectMapping> existing = empPrimaryProjectMappingRepository.findByEmpIdd(empId);
	                if (existing != null && !existing.isEmpty()) {
	                    empPrimaryProjectMappingRepository.updateIsMappedOnlyTON(empId, "N", new Date());
	                }
	                continue;
	            }

	            // If employee has multiple active projects, skip to avoid ambiguity
	            if (activeProjectIds.size() > 1) {
	                continue;
	            }

	            Long projectId = activeProjectIds.get(0);
	            if (projectId == null) continue;

	            String projectName = employeeTeamMapRepository.getProjectNameById(projectId);

	            // Handle missing project name
	            if (projectName == null || projectName.trim().isEmpty()) {
	                logBuilder.append("\nSkipped empId: ").append(empId).append(" due to missing project name.");
	                continue;
	            }

	            Optional<EmpPrimaryProjectMapping> existingMappingOpt =
	                    empPrimaryProjectMappingRepository.findByEmpIdd(empId);

	            if (existingMappingOpt.isPresent()) {
	                EmpPrimaryProjectMapping existingMapping = existingMappingOpt.get();

	                // Skip if mapping already correct and active
	                if (Objects.equals(existingMapping.getPrimaryProjectId(), projectId)
	                        && Objects.equals(existingMapping.getPrimaryProjectName(), projectName)
	                        && "Y".equalsIgnoreCase(existingMapping.getIsMapped())) {
	                    continue;
	                }

	                // Update existing mapping
	                empPrimaryProjectMappingRepository.updateMappingDetails(
	                        empId, projectId, projectName, "Y", new Date()
	                );

	            } else {
	                // Create new mapping if none exists
	                EmpPrimaryProjectMapping newMapping = new EmpPrimaryProjectMapping();
	                newMapping.setEmpId(empId);
	                newMapping.setPrimaryProjectId(projectId);
	                newMapping.setPrimaryProjectName(projectName);
	                newMapping.setIsMapped("Y");
	                newMapping.setUpdatedOn(LocalDateTime.now());

	                empPrimaryProjectMappingRepository.save(newMapping);
	            }
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Employee default project mappings updated successfully.");

	        apiLogInfo.setApiResponse("Default project mappings updated successfully.");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
	        e.printStackTrace();

	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Error while updating mapping.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("Exception while updating default project mappings: " + e.getMessage());
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	public ServiceResponse getEmployeesWorkingInProjects(ProjectNamesRequestDTO request) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (request == null || request.getProjectNames() == null || request.getProjectNames().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceError("projectNames is required and must not be empty");
				return response;
			}
			if (request.getStartDate() == null || request.getEndDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceError("startDate and endDate are required (yyyy-MM-dd)");
				return response;
			}
			if (request.getStartDate().isAfter(request.getEndDate())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceError("startDate must not be after endDate");
				return response;
			}
			LocalDateTime rangeStart = request.getStartDate().atStartOfDay();
			LocalDateTime rangeEnd = request.getEndDate().atTime(LocalTime.MAX);
			List<ProjectEmployeeTeamReportDTO> rows = employeeTeamMapRepository
					.findEmployeesInProjectsByProjectNames(request.getProjectNames(), rangeStart, rangeEnd);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(rows);
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}



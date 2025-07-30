package com.apmosys.employeeportal.service;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.EmployeeClientSideIdMappingDTO;
import com.apmosys.employeeportal.dto.FilteredTimesheetDTO;
import com.apmosys.employeeportal.dto.GetEmployeeListByProjectIdDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectClientSideIdDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDocumentDetailsDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeClientSideIdMapping;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.model.TimesheetActivityMap;
import com.apmosys.employeeportal.model.TimesheetApprovalAllocationLogs;
import com.apmosys.employeeportal.model.TimesheetDocumentApproval;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.AuditCustomRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeClientSideIdMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapRepository;
import com.apmosys.employeeportal.repository.TimesheetApprovalAllocationLogsRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentApprovalRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;
import com.apmosys.employeeportal.utility.ToLong_helper;

@EnableAsync
@Service
public class TimesheetService {

	@Autowired
	TimesheetsRepository timesheetsRepository;
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	AuditCustomRepository auditCustomRepository ;

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	ActivitiesRepository activitiesRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	@Autowired
	TimesheetActivityMapRepository timesheetActivityMapRepository;

	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	JobRoleRepository jobRoleRepository;

	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;

	@Autowired
	MailService mailService;

	@Value("${timesheet.lock.days}")
	private Integer timesheetLockDays;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	private EmployeeClientSideIdMappingRepository employeeClientSideIdMappingRepository;
	
	@Autowired
	private TimesheetDocumentDetailsRepository  timesheetDocumentDetailsRepository;
	
	@Autowired
	TimesheetDocumentApprovalRepository timesheetDocumentApprovalRepository;
    
	@Autowired
	TimesheetApprovalAllocationLogsRepository  timesheetApprovalAllocationLogsRepository; 
//	public ServiceResponse getAllProjectsByEmpId(TimesheetDTO timesheetDTO) {
//		ServiceResponse response = new ServiceResponse();
//		try {
//
//			List<Object[]> projectList = employeeTeamMapRepository.findProjectsByTeamId(timesheetDTO.getTeamId());
//			System.out.println("projects :" +projectList.toString());
////			List<Project> projectList = projectRepository.findAll();
//
//			if (projectList.isEmpty()) {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("No projects found for given employee Id.");
//			} else {
//				Type typeList = new TypeToken<List<ProjectDTO>>() {
//				}.getType();
//				List<ProjectDTO> previousEmploymentList = modelMapper.map(projectList, typeList);
//
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(previousEmploymentList);
//
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//		}
//		return response;
//	}

	public ServiceResponse getAllProjectsByEmpId(TimesheetDTO timesheetDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("add_timesheet");
	    apiLogInfo.setApiUrl("/api/getAllProjectsByEmpId");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("empId : " +timesheetDTO.getEmpId());

	    try {

	        List<Object[]> projectList = employeeTeamMapRepository.findProjectsByTeamId(timesheetDTO.getEmpId());
	        List<TimesheetDTO> listDto = new ArrayList<TimesheetDTO>();

	        if (!projectList.isEmpty()) {
	            
	            for (Object[] object : projectList) {
	                
	                String projectType = object[8] != null ? object[8].toString() : "";
	                String poEndDateStr = object[9] != null ? object[9].toString() : null;

	                
	                if ("TNM".equalsIgnoreCase(projectType) || "Fixed Cost".equalsIgnoreCase(projectType)) {
	                    if (poEndDateStr != null) {
	                    	String onlyDateStr = poEndDateStr.contains("T") ? poEndDateStr.split("T")[0] : poEndDateStr;
	                        LocalDate poEndDate = LocalDate.parse(onlyDateStr); 
	                        LocalDate currentDate = LocalDate.now();
//	                        if (poEndDate.isBefore(currentDate)) {
//	                            continue; // skip if poEndDate is in the past
//	                        }
	                    }
	                }
	                
	                TimesheetDTO timesheetDto = new TimesheetDTO();
	                timesheetDto.setClientId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
	                timesheetDto.setClientName(object[1] != null ? object[1].toString() : null);
	                timesheetDto.setClientLocationId(object[2] != null ? Integer.parseInt(object[2].toString()) : null);
	                timesheetDto.setClientLocation(object[3] != null ? object[3].toString() : null);
	                timesheetDto.setProjectId(object[4] != null ? Integer.parseInt(object[4].toString()) : null);
	                timesheetDto.setProjectName(object[5] != null ? object[5].toString() : null);
	                timesheetDto.setTeamName(object[6] != null ? object[6].toString() : null);
	                timesheetDto.setTeamId(object[7] != null ? Long.parseLong(object[7].toString()) : null);

	                listDto.add(timesheetDto);
	            }
	            
	            if (!listDto.isEmpty()) {
	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(listDto);
	                apiLogInfo.setApiResponse("listDto : " + listDto);
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                System.out.println("Project List: " + listDto);
	            } else {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("No valid projects found.");
	                apiLogInfo.setApiResponse("No valid projects found.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            }

	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Project List is empty !!");
	            apiLogInfo.setApiResponse("Project List is empty !!");
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
	public List<TimesheetDTO> getAllProjectsByEmpIdForBioMax(Long employeeCode,String date) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_timesheet");
		apiLogInfo.setApiUrl("/api/getAllProjectsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		List<TimesheetDTO> listDto = new ArrayList<TimesheetDTO>();

		try {

			//List<Object[]> projectList = employeeTeamMapRepository.findProjectsByTeamId(timesheetDTO.getEmpId());
		
			List<Object[]> objectList = timesheetActivityMapRepository
					.activitiesByTimesheetIdforBiomax(employeeCode,date);
			if (!objectList.isEmpty()) {
				TimesheetDTO timesheetDto = new TimesheetDTO();

				for (Object[] object : objectList) {
					timesheetDto = new TimesheetDTO();
					timesheetDto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					timesheetDto.setEmpId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					timesheetDto.setTeamId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
	  				timesheetDto.setTeamName(object[4] != null ? object[4].toString() : null);
	  				timesheetDto.setClientId(object[5] != null ? Integer.parseInt(object[5].toString()) : null);
					timesheetDto.setClientName(object[6] != null ? object[6].toString() : null);
					timesheetDto.setOfficeInTime(object[10] != null ? object[10].toString() : null);
					timesheetDto.setEmployeeName(object[11] != null ? object[11].toString() : null);
					//timesheetDto.setTotalWorkingHours(object[7] != null ? Float.parseFloat(object[7].toString()) : null);
					listDto.add(timesheetDto);
				}
				return listDto;
			} else {
				return listDto;
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
		return listDto;
	}

	public ServiceResponse getAllActivitiesByProjectIdandEmpId(TimesheetDTO timesheetDTO) {

		System.out.println(timesheetDTO);
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_timesheet");
		apiLogInfo.setApiUrl("/api/getAllActivitiesByProjectIdandEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("projectId : " +timesheetDTO.getProjectId()+ "empId : " +timesheetDTO.getEmpId());
		
		try {
			

			List<Object[]> objectList = projectRepository
					.getActivitiesByTeamIdAndEmployeeId(timesheetDTO.getTeamId(), timesheetDTO.getEmpId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No activities found.Activity list is empty");
					
					apiLogInfo.setApiResponse("No activities found.Activity list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<ActivityDTO> dtoList = new ArrayList<ActivityDTO>();

					list.forEach((object) -> {
						String[] employeeRoleInTeam = (object[4] != null ? object[4].toString() : null).split(",");
						boolean contains = Arrays.stream(employeeRoleInTeam).anyMatch((object[3] != null ? object[3].toString() : null)::equals);

						if(contains) {
							ActivityDTO dto = new ActivityDTO();

							dto.setActivityId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
							dto.setActivity(object[1] != null ? object[1].toString() : null);
							dto.setTeamId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
							dto.setDepartmentList(object[5] != null ? object[5].toString().split(",") : null);
//							dto.setDeptIds(object[5] != null ? object[5].toString() : null);
							dtoList.add(dto);
						}
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No activities found.Activity list is null");
				
				apiLogInfo.setApiResponse("No activities found.Activity list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse addTimesheet(TimesheetDTO timesheetDTO, MultipartFile doc) {
		ServiceResponse response = new ServiceResponse();
		System.out.println("timesheetDTO currentManagerId : "+timesheetDTO.getCurrentManagerId());
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_timesheet");
		apiLogInfo.setApiUrl("/api/addTimesheet");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +timesheetDTO.getEmpId()+ "dayType:" +timesheetDTO.getDayType());
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		try {

			List<ActivityDTO> allTimesheetActivities = timesheetDTO.getAllTimesheetActivities();
			Timesheet newTimesheet = new Timesheet();

			newTimesheet.setEmpId(timesheetDTO.getEmpId());
			newTimesheet.setDate(stringToDateTimeParser.getDate(timesheetDTO.getDate(), "yyyy-MM-dd"));
			newTimesheet.setDayType(timesheetDTO.getDayType());
			newTimesheet.setProjectId(timesheetDTO.getProjectId());
			newTimesheet.setClientSideId(timesheetDTO.getClientSideId() != null ? timesheetDTO.getClientSideId() : null);
			newTimesheet.setClientApprovalStatus(timesheetDTO.getClientApprovalStatus() != null ? timesheetDTO.getClientApprovalStatus() : null);
			newTimesheet.setHasClientSideId(timesheetDTO.getClientSideId() != null ? timesheetDTO.getHasClientSideId() : null);
			newTimesheet.setIsShadowTimesheet(timesheetDTO.getIsShadowTimesheet() != null ? (Boolean) timesheetDTO.getIsShadowTimesheet() : null);
			newTimesheet.setShadowEmpId(timesheetDTO.getShadowEmpId() != null ? Long.parseLong(timesheetDTO.getShadowEmpId().toString()) : null);
			
			System.out.println("timesheetDTO.getCurrentmanagerId() ==> "+timesheetDTO.getCurrentManagerId());

			newTimesheet.setCurrentManagerId(timesheetDTO.getCurrentManagerId());	
			if (timesheetDTO.getDayType().equals("Public Holiday") || timesheetDTO.getDayType().equals("Week Off") || timesheetDTO.getDayType().equals("Leave")) {
				
				newTimesheet.setDescription(timesheetDTO.getDescription());
				newTimesheet.setTotalTime((float)0);
				newTimesheet.setTotalWorkingHours("0");
			} else {
				
				// LocalDateTime dateTime = LocalDateTime.parse(timesheetDTO.getOfficeInTime(), formatter);
				
				newTimesheet.setOfficeInTime(LocalDateTime.parse(timesheetDTO.getOfficeInTime(), formatter));
				newTimesheet.setOfficeOutTime(LocalDateTime.parse(timesheetDTO.getOfficeOutTime(), formatter));
				newTimesheet.setTotalWorkingHours(timesheetDTO.getTotalWorkingOfficeHours());

				newTimesheet.setClientInTime(timesheetDTO.getClientInTime() != null ? timesheetDTO.getClientInTime() : null);
				newTimesheet.setClientOutTime(timesheetDTO.getClientOutTime() != null ? timesheetDTO.getClientOutTime() : null);
				newTimesheet.setTotalClientWorkingHours(timesheetDTO.getTotalClientWorkingHours() != null ? timesheetDTO.getTotalClientWorkingHours() : null);
				
				String description = "";
				if (allTimesheetActivities.isEmpty()) {
					description = "No activity available in timesheet";
				} else {
					for (ActivityDTO activity : allTimesheetActivities) {

						description = description.concat(activity.getActivity() + "<br>");
					

					}
				}
				newTimesheet.setDescription(description);
			}
			newTimesheet.setStatus("Pending");
			newTimesheet.setIsNightShift(timesheetDTO.getIsNightShift());
			newTimesheet.getCommonProperty().setCreatedBy(timesheetDTO.getCreatedBy());
			
			
//			added by sakti
			
//			Timesheet existing = timesheetsRepository.findByEmpIdAndDate(timesheetDTO.getEmpId(), stringToDateTimeParser.getDate(timesheetDTO.getDate(), "yyyy-MM-dd"));
//			if (existing != null) {
//			    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			    response.setServiceResponse("Timesheet already exists for this date.");
//			    
//			    apiLogInfo.setApiResponse("Duplicate timesheet entry attempt");
//			    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			    logService.logMyInfo(httpRequest, apiLogInfo);
//			    
//			    return response;
//			}

			
			System.err.println("Time sheet checked "+newTimesheet);

			Timesheet newTimesheetCreated = timesheetsRepository.save(newTimesheet);
			
			if(timesheetDTO.getClientApprovalStatus().equalsIgnoreCase("pending") || timesheetDTO.getClientApprovalStatus().equalsIgnoreCase("approved")) {
				
				TimesheetDocumentDetails docData = addTimesheetDocument(timesheetDTO.getDocumentData(),"Create",doc);
				docData.setTimesheetId(newTimesheetCreated.getTimesheetId());
				docData.setEmpId(timesheetDTO.getEmpId());
//				docData.setDocData(doc.getBytes());
				
				if (docData != null) {
				    try {
				    	System.out.println(docData);
				        TimesheetDocumentDetails docu = timesheetDocumentDetailsRepository.save(docData);

				        if (docu == null) {
				            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				            response.setServiceResponse("Timesheet added, but document not saved.");

				            apiLogInfo.setApiResponse("Timesheet added, but document not saved.");
				            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				        } else {
				            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				            response.setServiceResponse("Timesheet added successfully");

				            apiLogInfo.setApiResponse("Timesheet added successfully");
				            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				        }
				    } catch (Exception e) {
				        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				        response.setServiceResponse("Timesheet added, but document save failed due to an error.");

				        apiLogInfo.setApiResponse("Exception while saving document: " + e.getMessage());
				        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				        e.printStackTrace(); 
				    }
				}
			}	

			if (!timesheetDTO.getDayType().equals("Public Holiday") && !timesheetDTO.getDayType().equals("Week Off") && !timesheetDTO.getDayType().equals("Leave")) {				
				Optional.ofNullable(newTimesheetCreated.getEmpId()).ifPresentOrElse((timesheet) -> {
					
					List<TimesheetActivityMap> mapList = new ArrayList<TimesheetActivityMap>();
					allTimesheetActivities.forEach((activity) -> {

						TimesheetActivityMap map = new TimesheetActivityMap();
						map.setActivityId(activity.getActivityId());
						map.setCompletionTime(activity.getCompletionTime());
						if(activity.getDescription() == null) {
							Activity activityObj = activitiesRepository.getById(activity.getActivityId());
							map.setDescription(activityObj.getActivity());
						}else {
							map.setDescription(activity.getDescription());
						}
						map.setTimesheetId(newTimesheetCreated.getTimesheetId());
						map.setClientLocationId(activity.getClientLocationId());
						
						mapList.add(map);

						Float savedTime = newTimesheetCreated.getTotalTime() != null ? newTimesheetCreated.getTotalTime() : 0;
						Float totalTime = activity.getCompletionTime() + savedTime;
						newTimesheet.setTotalTime(totalTime);
						
						System.err.println("Time sheet checked2 "+newTimesheet);
						timesheetsRepository.save(newTimesheet);
					});
					
					
					System.err.println("Time sheet mapList "+mapList);

					List<TimesheetActivityMap> activityMapped = timesheetActivityMapRepository.saveAll(mapList);

					if (activityMapped.isEmpty()) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Timesheet added , but activity not mapped.");
						
						apiLogInfo.setApiResponse("Timesheet added , but activity not mapped.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Timesheet added successfully");
						
						apiLogInfo.setApiResponse("Timesheet added successfully");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						
					}

				}, () -> {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet not generated");
					
					apiLogInfo.setApiResponse("Timesheet not generated");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				});
			} else {
				if (newTimesheetCreated == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet not generated");
					
					apiLogInfo.setApiResponse("Timesheet not generated");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Timesheet added successfully");
					
					apiLogInfo.setApiResponse("Timesheet added successfully");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
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

	public ServiceResponse getAllMyTimesheetsByEmpId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_timesheet");
		apiLogInfo.setApiUrl("/api/getAllMyTimesheetsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("startDate : " +timesheetDTO.getStartDate()+ " ,endDate : " +timesheetDTO.getEndDate() );
		try {

			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());

			List<Object[]> timesheetList = timesheetsRepository
					.getAllMyTimesheets(timesheetDTO.getEmpId(), start, end);

			Optional.ofNullable(timesheetList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet list is empty");
					
					apiLogInfo.setApiResponse("Timesheet list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {

					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO dto = new TimesheetDTO();
						Long timesheetId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
						dto.setTimesheetId(timesheetId);
						dto.setDate(object[1] != null ? object[1].toString() : null);
						dto.setDayType(object[2] != null ? object[2].toString() : null);
						dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
						dto.setDescription(object[4] != null ? object[4].toString() : null);
						dto.setTotalTime(object[5] != null ? Float.parseFloat(object[5].toString() ) : null);
						dto.setStatus(object[6] != null ? object[6].toString() : null);
						dto.setCreatedByName(object[7] != null ? object[7].toString() : null);
						dto.setCreatedOn(object[8] != null ? object[8].toString() : null);
						dto.setEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
						dto.setRemarks(object[10] != null ? object[10].toString() : null);
						dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
						dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
						dto.setTotalWorkingOfficeHours(object[13] != null ? object[13].toString() : null);
						dto.setIsNightShift(object[14] != null ? object[14].toString() : null);
						dto.setLeaveType(object[15] != null ? object[15].toString() : null);
						dto.setClientInTime(object[16] != null ? ((Timestamp) object[16]).toLocalDateTime() : null);
						dto.setClientOutTime(object[17] != null ? ((Timestamp) object[17]).toLocalDateTime() : null);
						dto.setClientSideId(object[18] != null ? object[18].toString() : null);
						dto.setTotalClientWorkingHours(object[19] != null ? object[19].toString() : null);
						dto.setProjectId(object[20] != null ? Integer.parseInt(object[20].toString()) : null);
						dto.setClientApprovalStatus(object[21] != null ? object[21].toString() : null);
						dto.setHasClientSideId(object[22] != null ? (Boolean) object[22] : null);
						dto.setEmploymentId(object[22] != null ? employeeRepository.fetchEmploymentIdByEmpId(Long.parseLong(object[9].toString())) : null);
						dto.setIsShadowTimesheet(object[23] != null ? (Boolean) object[23] : null);
						dto.setShadowEmpId(object[24] != null ? Long.parseLong(object[24].toString()) : null);
						if(timesheetId != null)
						dto.setDocId(timesheetDocumentDetailsRepository.findDocIdByTimesheetId(timesheetId));
						// Get InActive Activities In Timesheet
						if(dto.getStatus().equals("Pending")) {
							List<Object[]> inactiveActivityList = timesheetsRepository
									.getInactiveActivitiesByTimesheetId(dto.getTimesheetId());
							
							List<ActivityDTO> inactiveDtoList = new ArrayList<ActivityDTO>();
							
							if(!inactiveActivityList.isEmpty()) {
								inactiveActivityList.forEach((actObject) -> {
									ActivityDTO actDto = new ActivityDTO();
									actDto.setTimesheetActivityMapId(actObject[0]!= null ? Long.parseLong(actObject[0].toString()) : null);
									
									inactiveDtoList.add(actDto);
								});
							
								dto.setInactiveTimesheetActivities(inactiveDtoList);						}							
						}
						
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}
			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet list is null");
				
				apiLogInfo.setApiResponse("Timesheet list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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
	
	
	public ServiceResponse getAllMyTeamTimesheets(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation Configuration");
		apiLogInfo.setApiUrl("/api/getAllMyTeamTimesheets");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("startDate : " +timesheetDTO.getStartDate()+ " ,endDate : " +timesheetDTO.getEndDate() );
		try {

			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());

			List<Object[]> timesheetList = timesheetsRepository
					.getAllMyTeamTimesheets(timesheetDTO.getCreatedBy(), start, end);

			Optional.ofNullable(timesheetList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet list is empty");
					
					apiLogInfo.setApiResponse("Timesheet list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO dto = new TimesheetDTO();
						Long timesheetId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
						dto.setTimesheetId(timesheetId);
						dto.setDate(object[1] != null ? object[1].toString() : null);
						dto.setDayType(object[2] != null ? object[2].toString() : null);
						dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
						dto.setDescription(object[4] != null ? object[4].toString() : null);
						dto.setTotalTime(object[5] != null ? Float.parseFloat(object[5].toString() ) : null);
						dto.setStatus(object[6] != null ? object[6].toString() : null);
						dto.setCreatedByName(object[7] != null ? object[7].toString() : null);
						dto.setCreatedOn(object[8] != null ? object[8].toString() : null);
						dto.setEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
						dto.setRemarks(object[10] != null ? object[10].toString() : null);
						dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
						dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
						dto.setTotalWorkingOfficeHours(object[13] != null ? object[13].toString() : null);
						dto.setIsNightShift(object[14] != null ? object[14].toString() : null);
						dto.setLeaveType(object[15] != null ? object[15].toString() : null);
						if(timesheetId != null)
						dto.setDocumentEntityData(timesheetDocumentDetailsRepository.findByTimesheetId(timesheetId));
						// Get InActive Activities In Timesheet
						if(dto.getDayType().equals("Working") && (dto.getStatus().equals("Pending") || dto.getStatus().equals("Rejected"))) {
							List<Object[]> inactiveActivityList = timesheetsRepository
									.getInactiveActivitiesByTimesheetId(dto.getTimesheetId());
							
							List<ActivityDTO> inactiveDtoList = new ArrayList<ActivityDTO>();
							
							if(!inactiveActivityList.isEmpty()) {
								inactiveActivityList.forEach((actObject) -> {
									ActivityDTO actDto = new ActivityDTO();
									actDto.setTimesheetActivityMapId(actObject[0] != null ? Long.parseLong(actObject[0].toString()) : null);
									
									inactiveDtoList.add(actDto);
								});
							
								dto.setInactiveTimesheetActivities(inactiveDtoList);						}							
						}	
						
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList size: " +dtoList.size());			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					

				}
			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet list is null");
				
				apiLogInfo.setApiResponse("Timesheet list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	public ServiceResponse getAllMyActivitiesByTimesheetId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("update_timesheet");
		apiLogInfo.setApiUrl("/api/getAllMyActivitiesByTimesheetId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId());
		
		try {

			List<Object[]> objectList = timesheetActivityMapRepository
					.activitiesByTimesheetId(timesheetDTO.getTimesheetId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No activities found.Activity list is empty");
					
					apiLogInfo.setApiResponse("No activities found.Activity list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<ActivityDTO> dtoList = new ArrayList<ActivityDTO>();

					list.forEach((object) -> {

						ActivityDTO dto = new ActivityDTO();

						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setActivity(object[1] != null ? object[1].toString() : null);
						dto.setEta(object[2] != null ? Float.parseFloat(object[2].toString()) : null);
						dto.setDescription(object[3] != null ? object[3].toString() : null);
						dto.setCompletionTime(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
						dto.setProjectName(object[5] != null ? object[5].toString() : null);
						dto.setClientName(object[6] != null ? object[6].toString() : null);
						dto.setClientLocation(object[7] != null ? object[7].toString() : null);
						dto.setTeamName(object[8] != null ? object[8].toString() : null);
						dto.setEmployeeName(object[9] != null ? object[9].toString() : null);
						dto.setManagerName(object[10] != null ? object[10].toString() : null);
						dto.setActivityId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
						dto.setProjectId(object[12] != null ? Integer.parseInt(object[12].toString()) : null);
						dto.setTimesheetActivityMapId(object[13] != null ? Long.parseLong(object[13].toString()) : null);
						dto.setClientId(object[14] != null ? Integer.parseInt(object[14].toString()) : null);
						dto.setClientLocationId(object[15] != null ? Integer.parseInt(object[15].toString()) : null);
						dto.setTeamId(object[16] != null ? Long.parseLong(object[16].toString()) : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No activities found.Activity list is null");
				
				apiLogInfo.setApiResponse("No activities found.Activity list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

//	public ServiceResponse getMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
//		ServiceResponse response = new ServiceResponse();
//		
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("view_my_teams_timesheets_requests");
//		apiLogInfo.setApiUrl("/api/getMyReporteesTimesheetRequests");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("managerId : " +timesheetDTO.getManagerId()+ " ,status : " +timesheetDTO.getStatus());
//		try {
//
//			List<Object[]> objectList = timesheetsRepository
//					.getMyReporteesTimesheetRequests(timesheetDTO.getManagerId(), timesheetDTO.getStatus());
//
//			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {
//
//				if (list.isEmpty()) {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("No timesheets found. List is empty.");
//					
//					apiLogInfo.setApiResponse("No timesheets found. List is empty.");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				} else {
//					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();
//
//					list.forEach((object) -> {
//
//						TimesheetDTO dto = new TimesheetDTO();
//						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
//						dto.setDate(object[1] != null ? object[1].toString() : null);
//						dto.setDayType(object[2] != null ? object[2].toString() : null);
//						dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
//						dto.setDescription(object[4] != null ? object[4].toString() : null);
//						dto.setStatus(object[5] != null ? object[5].toString() : null);
//						dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
//						dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
//						dto.setEmployeementId(object[8] != null ? Long.parseLong(object[8].toString()) : null);
//						dto.setTotalTime(object[9] != null ? Float.parseFloat(object[9].toString()) : null);
//						dto.setEmail(object[10] != null ? object[10].toString() : null);
//						dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
//						dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
//						dto.setTotalWorkingOfficeHours(object[13] != null ? object[13].toString() : null);
//						dto.setIsNightShift(object[14] != null ? object[14].toString() : null);
//						dtoList.add(dto);
//					});
//
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse(dtoList);
//					
//					apiLogInfo.setApiResponse("dtoList : " +dtoList );			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				}
//
//			}, () -> {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("No  timesheets found. List is null.");
//				
//				apiLogInfo.setApiResponse("No  timesheets found. List is null.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			});
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
//	these changes are added for temporary , we have to add one more field that is manager id in employee_timesheets table 

	public ServiceResponse getMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_my_teams_timesheets_requests");
		apiLogInfo.setApiUrl("/api/getMyReporteesTimesheetRequests");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +timesheetDTO.getManagerId()+ " ,status : " +timesheetDTO.getStatus());
		try {

			Employee employeeData = employeeRepository.findByEmpId(timesheetDTO.getManagerId()); 

			List<Object[]> objectList = timesheetsRepository
					.getMyReporteesTimesheetRequests(timesheetDTO.getManagerId(), timesheetDTO.getStatus(),employeeData.getDateOfJoining());

			
			
			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No timesheets found. List is empty.");
					
					apiLogInfo.setApiResponse("No timesheets found. List is empty.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO dto = new TimesheetDTO();
						Long timesheetId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
						dto.setTimesheetId(timesheetId);
						dto.setDate(object[1] != null ? object[1].toString() : null);
						dto.setDayType(object[2] != null ? object[2].toString() : null);
						dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
						dto.setDescription(object[4] != null ? object[4].toString() : null);
						dto.setStatus(object[5] != null ? object[5].toString() : null);
						dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
						dto.setCreatedBy(object[7] != null ? Long.parseLong(object[7].toString()) : null);
						dto.setCreatedOn(object[8] != null ? object[8].toString() : null);
						dto.setEmployeementId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
						dto.setTotalTime(object[10] != null ? Float.parseFloat(object[10].toString()) : null);
						dto.setEmail(object[11] != null ? object[11].toString() : null);
						dto.setOfficeInTime(object[12] != null ? object[12].toString() : null);
						dto.setOfficeOutTime(object[13] != null ? object[13].toString() : null);
						dto.setTotalWorkingOfficeHours(object[14] != null ? object[14].toString() : null);
						dto.setIsNightShift(object[15] != null ? object[15].toString() : null);
						dto.setIsConsultant(object[17] != null ? object[17].toString() : null);
						dto.setIsApprenticeship(object[18] != null ? object[18].toString() : null);
						dto.setEmpId(object[19] != null ? Long.parseLong(object[19].toString()) : null);
						dto.setClientInTime(object[20] != null ? ((Timestamp) object[20]).toLocalDateTime() : null);
						dto.setClientOutTime(object[21] != null ? ((Timestamp) object[21]).toLocalDateTime() : null);
						dto.setClientSideId(object[22] != null ? object[22].toString() : null);
						dto.setTotalClientWorkingHours(object[23] != null ? object[23].toString() : null);
						dto.setProjectId(object[24] != null ? Integer.parseInt(object[24].toString()) : null);
						dto.setClientApprovalStatus(object[25] != null ? object[25].toString() : null);
						dto.setHasClientSideId(object[26] != null ? (Boolean) object[26] : null);
						dto.setEmploymentId(object[19] != null ? employeeRepository.fetchEmploymentIdByEmpId(Long.parseLong(object[19].toString())) : null);
						dto.setIsShadowTimesheet(object[27] != null ? (Boolean) object[27] : null);
						dto.setShadowEmpId(object[28] != null ? Long.parseLong(object[28].toString()) : null);
						if(timesheetId != null)dto.setDocId(timesheetDocumentDetailsRepository.findDocIdByTimesheetId(timesheetId));
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList );			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No  timesheets found. List is null.");
				
				apiLogInfo.setApiResponse("No  timesheets found. List is null.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	
//	public ServiceResponse countMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
//		ServiceResponse response = new ServiceResponse();
//		
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("view_all_team_requests");
//		apiLogInfo.setApiUrl("/api/countMyReporteesTimesheetRequests");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("managerId : " +timesheetDTO.getManagerId());
//		try {
//
//			Long applicationCount = timesheetsRepository.countMyReporteesTimesheetRequests(timesheetDTO.getManagerId());
//
//			if (applicationCount == 0) {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("No timesheet request(s) found.");
//				
//				apiLogInfo.setApiResponse("No timesheet request(s) found.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//
//			} else {
//				timesheetDTO = new TimesheetDTO();
//				timesheetDTO.setApplicationCount(applicationCount);
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(timesheetDTO);
//				
//				apiLogInfo.setApiResponse("timesheetDTO : " +timesheetDTO);			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			}
//
//		} catch (Exception e) {
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

	public ServiceResponse countMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_all_team_requests");
		apiLogInfo.setApiUrl("/api/countMyReporteesTimesheetRequests");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +timesheetDTO.getManagerId());
		try {

			Employee employeeData = employeeRepository.findByEmpId(timesheetDTO.getManagerId()); 
			
			Long applicationCount = timesheetsRepository.countMyReporteesTimesheetRequests(timesheetDTO.getManagerId(),employeeData.getDateOfJoining());

			if (applicationCount == 0) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No timesheet request(s) found.");
				
				apiLogInfo.setApiResponse("No timesheet request(s) found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			} else {
				timesheetDTO = new TimesheetDTO();
				timesheetDTO.setApplicationCount(applicationCount);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(timesheetDTO);
				
				apiLogInfo.setApiResponse("timesheetDTO : " +timesheetDTO);			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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
	public ServiceResponse updateTimesheetRequestById(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_request_status");
		apiLogInfo.setApiUrl("/api/updateTimesheetRequestById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId());
		try { 

			Optional<Timesheet> timesheetobject = timesheetsRepository.findById(timesheetDTO.getTimesheetId());
            if(timesheetDTO.getClientSideId() !=null) {
	
            	ServiceResponse response1 = timesheetDocumentApproval(timesheetDTO);
            	 if (ServiceResponse.STATUS_FAIL.equals(response1.getServiceStatus())
                         || ServiceResponse.SOMETHING_WENT_WRONG.equals(response1.getServiceStatus())) {
                     throw new RuntimeException("Timesheet Document Failed To Approve");
                 } 

            }
			timesheetobject.ifPresentOrElse((timesheet) -> {

				timesheet.setStatus(timesheetDTO.getStatus());
				timesheet.setTimesheetStatusUpdatedBy(timesheetDTO.getTimesheetStatusUpdatedBy());
				timesheet.setRemarks(timesheetDTO.getRejectReason());

				Timesheet updatedTimesheet = timesheetsRepository.save(timesheet);

            	 ServiceResponse response2 = timesheetDocumentApprovalLogs(timesheetDTO);
            	 if (ServiceResponse.STATUS_FAIL.equals(response2.getServiceStatus())
                         || ServiceResponse.SOMETHING_WENT_WRONG.equals(response2.getServiceStatus())) {
                     throw new RuntimeException("Timesheet Document Failed To Approve");
                 }
				if (updatedTimesheet.getEmpId() != null) {
					System.out.println("updatedTimesheet.getStatus ()  : "+ updatedTimesheet.getStatus());
					
					if (updatedTimesheet.getStatus().equals("Approved")) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Timesheet status Approved.");
						System.out.println("updatedTimesheet.getStatus ()  : "+ updatedTimesheet.getStatus());
						
						apiLogInfo.setApiResponse("Timesheet status Approved.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Timesheet status Rejected.");
						
						apiLogInfo.setApiResponse("Timesheet status Rejected.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

						try {
//							mailService.sendMail(timesheetDTO.getEmail(),
//									"Regarding Timesheet Rejection ", "Employee Id"+" A-"+timesheetDTO.getEmployeementId()+
//									" "+ " <br> "+" Employee Name -"+" "+timesheetDTO.getEmployeeName()+
//									" <br> "+"Your Timesheet has been rejected "+timesheetDTO.getRejectReason());
							
							mailService.sendMailWithCC(timesheetDTO.getEmail(), timesheetDTO.getManagerEmail(), "Regarding Timesheet Request Rejection", 
									"Dear "+ timesheetDTO.getEmployeeName()+","+
							"<br> "
							+" &nbsp;"+" &nbsp;"+" "+"Your timesheet application has been rejected by "+ timesheetDTO.getManagerName() +"."+
							"<br>"+"<br>"+"<b>"+"Timesheet Details :"+"<b>"+
							"<br>"+
							"EmpID :"+" "+ timesheetDTO.getEmployeementId()+
							"<br>"+
							"Name :"+" "+ timesheetDTO.getEmployeeName()+
							"<br>"+
							" Date :"+" "+ timesheetDTO.getDate() +
							"<br>"+
							" Day Type : "+" "+ timesheetDTO.getDayType() 
							+"<br>"+
							"Total Working Hours :"+" "+timesheetDTO.getTotalWorkingOfficeHours()+" "+"(hrs)"+
							"<br>"+
							"Rejection reason :"+" "+timesheetDTO.getRejectReason());
							
						} catch (Exception e) {
							e.printStackTrace();
							response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
							response.setServiceResponse("Something Went Wrong.");
							response.setServiceError(e.getMessage());
							
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							apiLogInfo.setLogLevel("ERROR");
						}
					}

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet status updation failed.");
					
					apiLogInfo.setApiResponse("Timesheet status updation failed.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found");
				
				apiLogInfo.setApiResponse("Timesheet not found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateTimesheet(TimesheetDTO timesheetDTO,MultipartFile doc) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_timesheet");
		apiLogInfo.setApiUrl("/api/updateTimesheet");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId()+ " ,dayType : " +timesheetDTO.getDayType());
		
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		try {

			Optional<Timesheet> timesheet = timesheetsRepository.findById(timesheetDTO.getTimesheetId());

			if (timesheet.isPresent()) {

				Timesheet existingTimesheet = timesheet.get();
                existingTimesheet.setCurrentManagerId(timesheetDTO.getCurrentManagerId());
				existingTimesheet.getCommonProperty().setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				existingTimesheet.getCommonProperty().setUpdatedBy(timesheetDTO.getCreatedBy());
				existingTimesheet.setDate(stringToDateTimeParser.getDate(timesheetDTO.getDate(), "yyyy-MM-dd"));
				existingTimesheet.setIsNightShift(timesheetDTO.getIsNightShift());
				/*
				 * Only pending/rejected timesheet can be updated by employee. So even if
				 * employee is updating pending timesheet or rejected timesheet the status
				 * should be set to "pending" in db. Hence we have hard coded "status" of
				 * timesheet being updated to be "pending",
				 * existingTimesheet.setStatus("Pending"); this way both pending/rejected
				 * timesheet would be set to "pending" status by default.
				 * 
				 */
				existingTimesheet.setStatus("Pending");
				existingTimesheet.setTotalTime((float) 0);

				existingTimesheet.setProjectId(timesheetDTO.getProjectId() != null ? timesheetDTO.getProjectId() : null);
				existingTimesheet.setClientSideId(timesheetDTO.getClientSideId() != null ? timesheetDTO.getClientSideId() : null);
				existingTimesheet.setClientApprovalStatus(timesheetDTO.getClientApprovalStatus() != null ? timesheetDTO.getClientApprovalStatus() : null);
				existingTimesheet.setHasClientSideId(timesheetDTO.getClientSideId() != null ? timesheetDTO.getHasClientSideId() : null);
				existingTimesheet.setIsShadowTimesheet(timesheetDTO.getIsShadowTimesheet() != null ? (Boolean) timesheetDTO.getIsShadowTimesheet() : null);
				existingTimesheet.setShadowEmpId(timesheetDTO.getShadowEmpId() != null ? Long.parseLong(timesheetDTO.getShadowEmpId().toString()) : null);
				

				if (timesheetDTO.getDayType().equals("Public Holiday") || timesheetDTO.getDayType().equals("Week Off") || timesheetDTO.getDayType().equals("Leave")) {
					timesheetActivityMapRepository.deleteByTimesheetId(timesheetDTO.getTimesheetId());
					existingTimesheet.setDescription(timesheetDTO.getDescription());
					existingTimesheet.setDayType(timesheetDTO.getDayType());
					existingTimesheet.setOfficeInTime(null);
					existingTimesheet.setOfficeOutTime(null);
					existingTimesheet.setTotalWorkingHours("0");

				} else {
					existingTimesheet.setDayType(timesheetDTO.getDayType());
					existingTimesheet.setOfficeInTime(LocalDateTime.parse(timesheetDTO.getOfficeInTime(), formatter));
					existingTimesheet.setOfficeOutTime(LocalDateTime.parse(timesheetDTO.getOfficeOutTime(), formatter));
					existingTimesheet.setTotalWorkingHours(timesheetDTO.getTotalWorkingOfficeHours());
					
					existingTimesheet.setClientInTime(timesheetDTO.getClientInTime() != null ? timesheetDTO.getClientInTime() : null);
					existingTimesheet.setClientOutTime(timesheetDTO.getClientOutTime() != null ? timesheetDTO.getClientOutTime() : null);
					existingTimesheet.setTotalClientWorkingHours(timesheetDTO.getTotalClientWorkingHours() != null ? timesheetDTO.getTotalClientWorkingHours() : null);

					List<ActivityDTO> updatedTimesheetActivities = timesheetDTO.getAllTimesheetActivities();

					String description = "";

					if (updatedTimesheetActivities.isEmpty()) {
						description = "No activity available in timesheet";
					} else {
						for (ActivityDTO activity : updatedTimesheetActivities) {

							description = description.concat(activity.getActivity() + "<br>");

						}
					}

					existingTimesheet.setDescription(description);

					timesheetDTO.getUpdatedTimesheetActivities().stream()
							.filter(activities -> activities.getTimesheetActivityMapId() == null)
							.forEach((activity) -> {

								TimesheetActivityMap map = new TimesheetActivityMap();
								map.setActivityId(activity.getActivityId());
								map.setCompletionTime(activity.getCompletionTime());
								if(activity.getDescription() == null) {
									Activity activityObj = activitiesRepository.getById(activity.getActivityId());
									map.setDescription(activityObj.getActivity());
								}else {
									map.setDescription(activity.getDescription());
								}
								map.setTimesheetId(timesheetDTO.getTimesheetId());
								map.setClientLocationId(activity.getClientLocationId());
								timesheetActivityMapRepository.save(map);

							});
					timesheetDTO.getUpdatedTimesheetActivities().stream()
							.filter(activities -> activities.getTimesheetActivityMapId() != null)
							.forEach((activity) -> {

								timesheetActivityMapRepository.deleteById(activity.getTimesheetActivityMapId());

							});

					timesheetDTO.getAllTimesheetActivities().stream()
							.filter(activities -> activities.getTimesheetActivityMapId() != null)
							.forEach((activity) -> {

								Optional<TimesheetActivityMap> existingMap = timesheetActivityMapRepository
										.findById(activity.getTimesheetActivityMapId());

								if (existingMap.isPresent()) {
									TimesheetActivityMap map = existingMap.get();

									map.setActivityId(activity.getActivityId());
									map.setCompletionTime(activity.getCompletionTime());
									if(activity.getDescription() == null) {
										Activity activityObj = activitiesRepository.getById(activity.getActivityId());
										map.setDescription(activityObj.getActivity());
									}else {
										map.setDescription(activity.getDescription());
									}
									map.setClientLocationId(activity.getClientLocationId());
									timesheetActivityMapRepository.save(map);
								}
							});

				}

				Float totalTime = (float) 0;

				List<TimesheetActivityMap> timesheetActivityMapObj = timesheetActivityMapRepository
						.findByTimesheetId(timesheetDTO.getTimesheetId());
				for (TimesheetActivityMap mappingFound : timesheetActivityMapObj) {
					totalTime += mappingFound.getCompletionTime();
				}
				existingTimesheet.setTotalTime(totalTime);

				Timesheet updatedTimesheet = timesheetsRepository.save(existingTimesheet);
				TimesheetDocumentDetails docData = addTimesheetDocument(timesheetDTO.getDocumentData(),"Update",doc);
				docData.setTimesheetId(updatedTimesheet.getTimesheetId());
				docData.setEmpId(timesheetDTO.getEmpId());
//				docData.setDocData(doc.getBytes());
				
				if (docData != null) {
				    try {
				    	System.out.println(docData);
				        TimesheetDocumentDetails docu = timesheetDocumentDetailsRepository.save(docData);

				        if (docu == null) {
				            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				            response.setServiceResponse("Timesheet added, but document not saved.");

				            apiLogInfo.setApiResponse("Timesheet added, but document not saved.");
				            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				        } else {
				            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				            response.setServiceResponse("Timesheet added successfully");

				            apiLogInfo.setApiResponse("Timesheet added successfully");
				            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				        }
				    } catch (Exception e) {
				        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				        response.setServiceResponse("Timesheet added, but document save failed due to an error.");

				        apiLogInfo.setApiResponse("Exception while saving document: " + e.getMessage());
				        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				        e.printStackTrace(); 
				    }
				}

				if (updatedTimesheet.getTimesheetId() != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Timesheet updated.");
					apiLogInfo.setApiResponse("Timesheet updated");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet updation failed.");
					apiLogInfo.setApiResponse("timesheet updation failed");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found.");
				apiLogInfo.setApiResponse("timesheet not found");			
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

	public ServiceResponse getMyReporteesApprovedTimesheets(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_my_teams_timesheets");
		apiLogInfo.setApiUrl("/api/getMyReporteesApprovedTimesheets");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +timesheetDTO.getManagerId()+ " ,startDate : " +timesheetDTO.getStartDate()+ " ,endDate : " +timesheetDTO.getEndDate());
		try {
			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());

			List<Object[]> objectList = timesheetsRepository.getMyReporteesApprovedTimesheets(
					timesheetDTO.getManagerId(), start, end);

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No timesheets found. List is empty.");
					
					apiLogInfo.setApiResponse("Appreciation not Enabled");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO dto = new TimesheetDTO();

						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setDate(object[1] != null ? object[1].toString() : null);
						dto.setDayType(object[2] != null ? object[2].toString() : null);
						dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
						dto.setDescription(object[4] != null ? object[4].toString() : null);
						dto.setStatus(object[5] != null ? object[5].toString() : null);
						dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
						dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
						dto.setTotalTime(object[8] != null ? Float.parseFloat(object[8].toString()) : null);
						dto.setRemarks(object[9] != null ? object[9].toString() : null);
						dto.setEmployeementId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
						dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
						dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
						dto.setTotalWorkingOfficeHours(object[13] != null ? object[13].toString() : null);
						dto.setIsNightShift(object[14] != null ? object[14].toString() : null);
						dto.setLeaveType(object[15] != null ? object[15].toString() : null);
						dto.setEmpId(object[16] != null ? Long.parseLong(object[16].toString()) : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No  timesheets found. List is null.");
				
				apiLogInfo.setApiResponse("No  timesheets found. List is null.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	public ServiceResponse getLast7DaysTimesheetsByEmpId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation Configuration");
		apiLogInfo.setApiUrl("/api/getLast7DaysTimesheetsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +timesheetDTO.getEmpId());
		try {

			List<Object[]> objectList = timesheetsRepository.getLast7DaysTimesheetsByEmpId(timesheetDTO.getEmpId(),
					LocalDate.now().minusDays(timesheetLockDays));

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No timesheets found. List is empty.");
					
					apiLogInfo.setApiResponse("No timesheets found. List is empty.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
				} else {
					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO dto = new TimesheetDTO();

						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setDate(object[1] != null ? object[1].toString() : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList size : " +dtoList.size());			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No timesheets found. List is null.");
				
				apiLogInfo.setApiResponse("No timesheets found. List is null.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	public ServiceResponse addClientAndProjectByList(ProjectDTO projectDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation Configuration");
		apiLogInfo.setApiUrl("/api/addClientAndProjectByList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +projectDTO.getProjectManagerId()+ " ,projectId : " +projectDTO.getProjectId() );
		try {

			Optional<Employee> EmpId = Optional
					.ofNullable(employeeRepository.findByEmployeementId(projectDTO.getProjectManagerId()));
			if (EmpId.isPresent()) {

				Employee employee = EmpId.get();
				Long primaryId = employee.getEmpId();

				Project project = new Project();

				project.setProjectId(projectDTO.getProjectId());
				project.setClientLocation(projectDTO.getClientLocation());
				project.setClientName(projectDTO.getClientName());
				project.setProjectManagerId(primaryId);
				project.setProjectName(projectDTO.getProjectName());
				project.setState(projectDTO.getState());

				project.setCreatedOn(projectDTO.getCreatedOn());

				Project dbresponse = projectRepository.save(project);

				if (dbresponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project and Client added.");
					
					apiLogInfo.setApiResponse("Project and Client added.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Creation of Project and client failed");
					
					apiLogInfo.setApiResponse("Creation of Project and client failed");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
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

	public ServiceResponse getTimesheetsForHomePageByEmpId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_timesheet_display");
		apiLogInfo.setApiUrl("/api/getTimesheetsForHomePageByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("startDate : " +timesheetDTO.getStartDate()+ " ,endDate : " +timesheetDTO.getEndDate() );
		try {

			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());

			List<Object[]> objectList = timesheetsRepository.getTimesheetsForHomePageByEmpId(timesheetDTO.getEmpId(),
					start, end);

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No timesheets found. List is empty.");
					apiLogInfo.setApiResponse("No timesheets found. List is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
					
				} else {
					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO dto = new TimesheetDTO();

						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setDate(object[1] != null ? object[1].toString() : null);
						dto.setWeekDayName(object[2] != null ? object[2].toString() : null);
						dto.setTotalWorkingHours(object[3] != null ? Float.parseFloat(object[3].toString()) : 0);
						dto.setStatus(object[4] != null ? object[4].toString() : null);
						dto.setDayType(object[5] != null ? object[5].toString() : null);
						dto.setDescription(
							    object[6] != null 
							        ? object[6].toString().replaceAll("(?i)<br>\\s*$", "").trim() 
							        : null
							);

						dto.setActivity(object[7] != null ? object[7].toString() : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No  timesheets found. List is null.");
				
				apiLogInfo.setApiResponse("No  timesheets found. List is null.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
			});

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

	public ServiceResponse revokeApprovedTimesheet(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("revoke_reportee_timesheet");
		apiLogInfo.setApiUrl("/api/revokeApprovedTimesheet");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId() );
		try {

			Optional<Timesheet> timesheetObj = timesheetsRepository.findById(timesheetDTO.getTimesheetId());

			timesheetObj.ifPresentOrElse((timesheetFound) -> {

				timesheetFound.setStatus("Pending");
				timesheetsRepository.save(timesheetFound);

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Timesheet revoked successfully");
				
				apiLogInfo.setApiResponse("Timesheet revoked successfully");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found.");
				
				apiLogInfo.setApiResponse("Timesheet not found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);				
				
			});

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
	
	public ServiceResponse bulkApproveTimesheetRequest(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("BulkApproveTimesheetRequest");
		apiLogInfo.setApiUrl("/api/bulkApproveTimesheetRequest");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BulkApprovedList : " + timesheetDTO.getBulkApprovedList().size());
	
		try {
			
			for (TimesheetDTO timesheet : timesheetDTO.getBulkApprovedList()) {
				
				timesheet.setStatus(timesheetDTO.getStatus());
				timesheet.setTimesheetStatusUpdatedBy(timesheetDTO.getTimesheetStatusUpdatedBy());
				response = updateTimesheetRequestById(timesheet);
			    
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
	
	public ServiceResponse bulkRejectTimesheetRequest(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("BulkRejectTimesheetRequest");
		apiLogInfo.setApiUrl("/api/bulkRejectTimesheetRequest");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BulkRejectList : " + timesheetDTO.getBulkRejectList().size());
		try {
			
			for (TimesheetDTO timesheet : timesheetDTO.getBulkRejectList()) {
				
				timesheet.setStatus(timesheetDTO.getStatus());
				timesheet.setTimesheetStatusUpdatedBy(timesheetDTO.getTimesheetStatusUpdatedBy());
				timesheet.setRejectReason(timesheetDTO.getRejectReason());
				response = updateTimesheetRequestById(timesheet);   
				System.out.println("   timesheet Reject reason __" +timesheetDTO.getRejectReason());
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
	
	
	public ServiceResponse getAllLeaveTimesheetsWithoutLeaveApplication(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllLeaveTimesheetsWithoutLeaveApplication");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("startDate : " +timesheetDTO.getStartDate()+ "endDate : " +timesheetDTO.getEndDate() );
		try {
			List<Object[]> timesheetList = new ArrayList<>();

			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());
			
			Employee employee = employeeRepository.findByEmpId(timesheetDTO.getCurrentUser());
			JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());

			String role = jobRole.getEmployeeRole();
			String name = jobRole.getName();
			
			if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
					|| name.equalsIgnoreCase("Super Admin")) {
				 timesheetList = timesheetsRepository
							.getAllLeaveTimesheetsWithoutLeaveApplication(start, end);
			}
			else if(departmentRepository.existsByHodId(timesheetDTO.getCurrentUser())) {
				List<Long> deptIds = departmentRepository.findDeptIdsByHodId(timesheetDTO.getCurrentUser());
				timesheetList = timesheetsRepository
						.getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWise(start, end,deptIds);
			}else {
				Employee employeee = employeeRepository.findByEmpId(timesheetDTO.getCurrentUser());
				Long deptId = departmentRepository.findDepartmentofCurrentuser(employeee.getJobRoleId());
				timesheetList = timesheetsRepository
						.getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise(start, end,deptId);
				
			}
			
			

		 

			Optional.ofNullable(timesheetList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet list is empty");
					
					apiLogInfo.setApiResponse("Timesheet list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {

					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO timesheetDto = new TimesheetDTO();
						timesheetDto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						timesheetDto.setEmployeeName(object[1] != null ? object[1].toString() : null);
						timesheetDto.setDate(object[2] != null ? object[2].toString() : null);
						timesheetDto.setDayType(object[3] != null ? object[3].toString() : null);
						timesheetDto.setDescription(object[4] != null ? object[4].toString() : null);
						timesheetDto.setStatus(object[5] != null ? object[5].toString() : null);
						timesheetDto.setLeaveType(object[6] != null ? object[6].toString() : null);
						timesheetDto.setManagerName(object[7] != null ? object[7].toString() : null);
						timesheetDto.setDepartmentName(object[8] != null ? object[8].toString() : null);
						timesheetDto.setCreatedOn(object[9] != null ? object[9].toString() : null);
						timesheetDto.setUpdatedOn(object[10] != null ? object[10].toString() : null);
						timesheetDto.setTimesheetStatusUpdatedByName(object[11] != null ? object[11].toString() : null);
						timesheetDto.setIsConsultant(object[12] != null ? object[12].toString() : null);
						timesheetDto.setIsApprenticeship(object[13] != null ? object[13].toString() : null);
						timesheetDto.setManagerId(object[14] != null ? Long.parseLong(object[14].toString()) : null);
						timesheetDto.setTimesheetStatusUpdatedBy(object[15] != null ? Long.parseLong(object[15].toString()) : null);
						timesheetDto.setEmpId(object[16] != null ? Long.parseLong(object[16].toString()) : null);
						dtoList.add(timesheetDto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList size : " +dtoList.size());			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}
			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet list is null");
				
				apiLogInfo.setApiResponse("Timesheet list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

//	@Transactional
//	public void updateCurrentManagerInTimesheets() {
//	    List<Object[]> empAndManagerList = timesheetsRepository.findEmployeesAndTheirManagers();
//
//	    for (Object[] empAndManager : empAndManagerList) {
//	        Long empId = ((BigInteger) empAndManager[0]).longValue();
//	        Long managerId = ((BigInteger) empAndManager[1]).longValue();
//
//	        // Update current_manager_id in the employee_timesheets table
//	        timesheetsRepository.updateCurrentManagerId(empId, managerId);
//	    }
//	}
	
//	public List<Object[]> getManagerIdUpdates(Long empId) {
//	    String query = "SELECT e.manager_id, MAX(e.updated_on) AS latest_update " +
//	                   "FROM employee_aud e " +
//	                   "WHERE e.emp_id = :emp_id " +
//	                   "GROUP BY e.manager_id " +
//	                   "ORDER BY latest_update DESC " +
//	                   "LIMIT 2";
//	    List<Object[]> results = auditCustomRepository.readAuditCustomNativeQueryy(query, empId);
//	    
//	    // Convert the Timestamp to LocalDateTime
//	    for (Object[] result : results) {
//	        if (result[1] instanceof Timestamp) {
//	            result[1] = ((Timestamp) result[1]).toLocalDateTime();
//	        }
//	    }
//	    
//	    return results;
//	}
//
//	@Async
//	@Scheduled(cron = "0 05 17 * * ?")
//	public void updateTimesheetManagerIds() {
//		System.err.println("--cron started----")	;    
//		List<Long> employeeIds = timesheetsRepository.findDistinctEmpIds(); // Method to fetch distinct empIds
//		
//	    for (Long empId : employeeIds) {
//	        List<Object[]> managerUpdates = getManagerIdUpdates(empId);
//
//	        if (managerUpdates.isEmpty() || managerUpdates.get(0)[0] == null) {
//	            continue; // No manager updates found or the first managerId is null, skip this employee
//	        }
//
//	        // Convert BigInteger to Long
//	        Long firstManagerId = ((BigInteger) managerUpdates.get(0)[0]).longValue();
//	        LocalDateTime latestUpdate = (LocalDateTime) managerUpdates.get(0)[1];
//	        Long secondManagerId = managerUpdates.size() > 1 ? ((BigInteger) managerUpdates.get(1)[0]).longValue() : null;
//
//	        List<Timesheet> timesheets = timesheetsRepository.findTimesheetsByEmpIdOrderByCreatedOn(empId);
//
//	        for (Timesheet timesheet : timesheets) {
//	            LocalDateTime createdOn = timesheet.getCommonProperty().getCreatedOn().toLocalDateTime(); // Access createdOn
//
//	            if (latestUpdate == null || createdOn.isBefore(latestUpdate)) {
//	                timesheetsRepository.updateCurrentManagerId(timesheet.getTimesheetId(), 
//	                                                            secondManagerId != null ? secondManagerId : firstManagerId);
//	            } else {
//	                timesheetsRepository.updateCurrentManagerId(timesheet.getTimesheetId(), firstManagerId);
//	            }
//	        }
//	    }
//	    System.err.println("--------cron ended---");
//	}

	public ServiceResponse getAllOrDeptWiseEmployeeTimesheetReport(FilteredTimesheetDTO filteredTimesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        
        
        apiLogInfo.setSubFeatureName("fetch_timesheets");
        apiLogInfo.setApiUrl("/api/getAllOrDeptWiseEmployeeTimesheetReport");
        apiLogInfo.setLogLevel("INFO");
        
        String deptId = filteredTimesheetDTO.getDeptId();
        String startDate = filteredTimesheetDTO.getStartDate();
        String endDate = filteredTimesheetDTO.getEndDate();
        
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Requested Department ID: ").append(deptId);

        try {
            List<Object[]> timesheetList;
            if ("all".equalsIgnoreCase(deptId)) {
                timesheetList = timesheetsRepository.getAllEmployeeTimesheetsBetweenDates(startDate, endDate);
            } else {
                timesheetList = timesheetsRepository.getTimesheetsByDepartmentAndDateRange(
                    Long.parseLong(deptId), startDate, endDate);
            }

            if (timesheetList == null || timesheetList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No timesheets found.");
                apiLogInfo.setApiResponse("No timesheets found.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            } else {
                List<TimesheetDTO> dtoList = timesheetList.stream()
                        .map(this::mapToTimesheetDTO)
                        .collect(Collectors.toList());

                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
                apiLogInfo.setApiResponse("Timesheet data retrieved.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something went wrong.");
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }

        apiLogInfo.setApiRequest(logBuilder.toString());
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }
	
	private TimesheetDTO mapToTimesheetDTO(Object[] object) {
        TimesheetDTO dto = new TimesheetDTO();
        dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
        dto.setDate(object[1] != null ? object[1].toString() : null);
        dto.setDayType(object[2] != null ? object[2].toString() : null);
        dto.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()) : null);        
        dto.setEmployeeName(object[4] != null ? object[4].toString() : null);
        dto.setTotalTime(object[5] != null ? Float.parseFloat(object[5].toString()) : null);
        dto.setStatus(object[6] != null ? object[6].toString() : null);
        dto.setCreatedByName(object[7] != null ? object[7].toString() : null);
        dto.setCreatedOn(object[8] != null ? object[8].toString() : null);
        dto.setCreatedByEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
        dto.setRemarks(object[10] != null ? object[10].toString() : null);
        dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
        dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
        dto.setIsNightShift(object[13] != null ? object[13].toString() : null);
        dto.setLeaveType(object[14] != null ? object[14].toString() : null);
        dto.setActivityTimesheetId(object[15] != null ? Long.parseLong(object[15].toString()) : null);
        dto.setActivity(object[16] != null ? object[16].toString() : null);
        dto.setDescription(object[18] != null ? object[18].toString() : null);
        dto.setProjectName(object[19] != null ? object[19].toString() : null);
        dto.setClientName(object[20] != null ? object[20].toString() : null);
        dto.setClientLocation(object[21] != null ? object[21].toString() : null);
        dto.setTeamName(object[22] != null ? object[22].toString() : null);
        dto.setManagerName(object[23] != null ? object[23].toString() : null);
        dto.setActivityId(object[24] != null ? Long.parseLong(object[24].toString()) : null);
        dto.setProjectId(object[25] != null ? Integer.parseInt(object[25].toString()) : null);
        dto.setIsApprenticeship(object[31] != null ? object[31].toString() : null);
        dto.setIsConsultant(object[30] != null ? object[30].toString() : null);
        dto.setDepartmentName(object[32] != null ? object[32].toString() : null);
        dto.setManagerId(object[33] != null ? Long.parseLong(object[33].toString()) : null);
        dto.setEmpId(object[34] != null ? Long.parseLong(object[34].toString()) : null);        
        return dto;
    }
	
	
//	public ServiceResponse getLastFilledTimesheetByEmpId(Long empId) {
//	    ServiceResponse response = new ServiceResponse();
//	    LogDTO apiLogInfo = new LogDTO();
//	    apiLogInfo.setApiUrl("/api/getLastFilledTimesheetByEmpId");
//	    apiLogInfo.setLogLevel("INFO");
//	    ToLong_helper toLong_helper = new ToLong_helper();
//
//	    try {
//	    	List<Object[]> resultList = timesheetsRepository.getLastFilledTimesheet(empId);
//
//	    	if (!resultList.isEmpty()) {
//	    	    Object[] object = resultList.get(0); 
//
//	    	    if (object.length < 19) {
//	    	        throw new RuntimeException("Expected 19 columns, got: " + object.length);
//	    	    }
//	    	    TimesheetDTO dto = new TimesheetDTO();
//
//	    	    dto.setTimesheetId(toLong_helper.safeParseLong(object[0]));             
//	    	    dto.setDate(toLong_helper.getSafeString(object[1]));                    
//	    	    dto.setDayType(toLong_helper.getSafeString(object[2]));                
//	    	    dto.setOfficeInTime(toLong_helper.getSafeString(object[3]));            
//	    	    dto.setOfficeOutTime(toLong_helper.getSafeString(object[4]));           
//	    	    dto.setTotalWorkingOfficeHours(toLong_helper.getSafeString(object[5])); 
//	    	    dto.setDescription(toLong_helper.getSafeString(object[8]));             
//
//	    	    dto.setTotalTime(toLong_helper.safeParseFloat(object[7]));              
//	    	    dto.setActivity(toLong_helper.getSafeString(object[8]));               
//
//	    	    dto.setActivityId(toLong_helper.safeParseLong(object[9]));              
//	    	    dto.setActivity(toLong_helper.getSafeString(object[10]));               
//
//	    	    dto.setTeamId(toLong_helper.safeParseLong(object[11]));                 
//	    	    dto.setTeamName(toLong_helper.getSafeString(object[12]));               
//	    	    dto.setTeamLeadName(toLong_helper.getSafeString(object[13]));          
//
//	    	    dto.setProjectId(toLong_helper.safeParseInt(object[14]));               
//	    	    dto.setProjectName(toLong_helper.getSafeString(object[15]));            
//	    	    dto.setClientId(toLong_helper.safeParseInt(object[16]));                
//	    	    dto.setClientLocationId(toLong_helper.safeParseInt(object[17]));       
//	    	    dto.setClientLocation(toLong_helper.getSafeString(object[18]));        
//	    	    dto.setClientName(toLong_helper.getSafeString(object[19]));           
//	    	   	  response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	    	    response.setServiceResponse(dto);
//	    	    apiLogInfo.setApiResponse("Last timesheet found");
//	    	    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//	    	} else {
//	    	    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	    	    response.setServiceResponse("No timesheet found for employee.");
//	    	    apiLogInfo.setApiResponse("No timesheet found");
//	    	    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	    	}
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	        response.setServiceResponse("Something went wrong.");
//	        response.setServiceError(e.getMessage());
//	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	        apiLogInfo.setLogLevel("ERROR");
//	    }
//
//	    logService.logMyInfo(httpRequest, apiLogInfo);
//	    return response;
//	}
	
	
	public ServiceResponse getLastFilledTimesheetByEmpId(Long empId) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getLastFilledTimesheetByEmpId");
	    apiLogInfo.setLogLevel("INFO");
	    ToLong_helper toLong_helper = new ToLong_helper();

	    try {
	        List<Object[]> activeCheckList = timesheetsRepository.checkEmployeeActiveOrNot(empId);

	        // Case 1: No records found in the timesheet - employee never filled any timesheet
	        if (activeCheckList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(Collections.emptyList());
	            response.setServiceMessage("Employee has never filled any timesheet.");
	            response.setServiceResponse1("No Timesheet");

	            apiLogInfo.setApiResponse("No timesheet record found for employee.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // Determine active status from the activeCheckList result
	        boolean isActive = Integer.parseInt(String.valueOf(activeCheckList.get(0)[1])) == 1;
	        response.setServiceResponse1(isActive ? "Active" : "Not Active");

	        if (!isActive) {
	            // Case 2: Employee was in a project but is not currently active
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(Collections.emptyList());
	            response.setServiceMessage("Employee is not active on previous project.");

	            apiLogInfo.setApiResponse("Employee is Not Active");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // Case 3 or 4: Employee is active, check for last filled timesheet
	        List<Object[]> resultList = timesheetsRepository.getLastFilledTimesheet(empId);
	        if (!resultList.isEmpty()) {
	            Object[] object = resultList.get(0);

	            if (object.length < 20) {
	                throw new RuntimeException("Expected 20 columns, got: " + object.length);
	            }

	            TimesheetDTO dto = new TimesheetDTO();
	            dto.setTimesheetId(toLong_helper.safeParseLong(object[0]));
	            dto.setDate(toLong_helper.getSafeString(object[1]));
	            dto.setDayType(toLong_helper.getSafeString(object[2]));
	            dto.setOfficeInTime(toLong_helper.getSafeString(object[3]));
	            dto.setOfficeOutTime(toLong_helper.getSafeString(object[4]));
	            dto.setTotalWorkingOfficeHours(toLong_helper.getSafeString(object[5]));
	            dto.setTotalTime(toLong_helper.safeParseFloat(object[7]));
	            dto.setDescription(toLong_helper.getSafeString(object[8]));

	            dto.setActivityId(toLong_helper.safeParseLong(object[9]));
	            dto.setActivity(toLong_helper.getSafeString(object[10]));

	            dto.setTeamId(toLong_helper.safeParseLong(object[11]));
	            dto.setTeamName(toLong_helper.getSafeString(object[12]));
	            dto.setTeamLeadName(toLong_helper.getSafeString(object[13]));

	            dto.setProjectId(toLong_helper.safeParseInt(object[14]));
	            dto.setProjectName(toLong_helper.getSafeString(object[15]));

	            dto.setClientId(toLong_helper.safeParseInt(object[16]));
	            dto.setClientLocationId(toLong_helper.safeParseInt(object[17]));
	            dto.setClientLocation(toLong_helper.getSafeString(object[18]));
	            dto.setClientName(toLong_helper.getSafeString(object[19]));

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(dto);
	            response.setServiceMessage("Last timesheet found for employee.");

	            apiLogInfo.setApiResponse("Last timesheet found for active employee");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            // Case 4: Active employee but no timesheet found
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(Collections.emptyList());
	            response.setServiceMessage("No timesheet found for employee.");

	            apiLogInfo.setApiResponse("No timesheet found");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	public ServiceResponse getActiveProjectsByEmpId(Long empId) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getActiveProjectsByEmpId");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
	        List<ProjectDTO> activeProjectList = timesheetsRepository.getActiveProjectsByEmpId(empId);
	        
	        if (activeProjectList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Please contact to the RMG team to provide you active project mapping!");
	            response.setServiceMessage("Employee has no active project mapping ! For EmpId: " + empId);
	            
	            apiLogInfo.setApiResponse("Employee has no active project mapping ! For EmpId: " + empId);
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(activeProjectList);
            response.setServiceMessage("Project List fetched successfully!");

            apiLogInfo.setApiResponse("Project list where employee has active = 1 in Employee Team Mapping table fetched successfully!");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage()); 
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	public ServiceResponse getClientSideIdByProjectId(Long projectId) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getClientSideIdByProjectId");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
	        String clientSideId = employeeClientSideIdMappingRepository.findClientSideIdByProjectId(projectId);
	        
	        if (clientSideId.isEmpty() || clientSideId.equals("")) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No Client Side Id fetched for the selected Project!");
	            response.setServiceMessage("No Client Side Id fetched for the selected Project! For ProjectId: " + projectId);
	            
	            apiLogInfo.setApiResponse("No Client Side Id fetched for the selected Project! For ProjectId: " + projectId);
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(clientSideId);
            response.setServiceMessage("Client Side Id fetched successfully!");

            apiLogInfo.setApiResponse("Client Side Id fetched successfully!");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage()); 
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	public ServiceResponse fetchEmploymentIdByEmpId(Long empId) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/fetchEmploymentIdByEmpId");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
	        String clientSideId = employeeRepository.fetchEmploymentIdByEmpId(empId);
	        
	        if (clientSideId.isEmpty() || clientSideId.equals("")) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No Emp Id fetched!");
	            response.setServiceMessage("No Emp Id fetched for the employee with EmpId: " + empId);
	            
	            apiLogInfo.setApiResponse("No Emp Id fetched for the employee with EmpId: " + empId);
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(clientSideId);
            response.setServiceMessage("Emp Id fetched successfully!");

            apiLogInfo.setApiResponse("Emp Id fetched successfully!");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage()); 
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	public TimesheetDocumentDetails addTimesheetDocument(TimesheetDocumentDetailsDTO timesheetDocumentDetailsDTO, String oprType, MultipartFile doc) throws IOException {
		TimesheetDocumentDetails data = new TimesheetDocumentDetails();
		if("Create".equalsIgnoreCase(oprType)) {
			timesheetDocumentDetailsDTO.setActive(true);
			timesheetDocumentDetailsDTO.setCreatedOn(LocalDateTime.now());
		}
		else if("Update".equalsIgnoreCase(oprType)) {
			data = timesheetDocumentDetailsRepository.findByDocIdAndActive(timesheetDocumentDetailsDTO.getDocId(),true);
			timesheetDocumentDetailsDTO.setUpdatedOn(LocalDateTime.now());
		}
		if(doc != null) timesheetDocumentDetailsDTO.setDocFile(doc);
		else throw new DataIntegrityViolationException("No Document found...!!");
		data.setDocName(timesheetDocumentDetailsDTO.getDocName());
		data.setDocData(timesheetDocumentDetailsDTO.getDocFile().getBytes());
		data.setTimesheetId(timesheetDocumentDetailsDTO.getTimesheetId());
		data.setEmpId(timesheetDocumentDetailsDTO.getTimesheetId());
		if(timesheetDocumentDetailsDTO.getCreatedOn() != null)
			data.setCreatedOn(timesheetDocumentDetailsDTO.getCreatedOn());
		if(timesheetDocumentDetailsDTO.getCreatedBy() != null)
			data.setCreatedBy(timesheetDocumentDetailsDTO.getCreatedBy());
		if(timesheetDocumentDetailsDTO.getUpdatedBy() != null)
			data.setUpdatedBy(timesheetDocumentDetailsDTO.getUpdatedBy());
		if(timesheetDocumentDetailsDTO.getUpdatedOn() != null)
			data.setUpdatedOn(timesheetDocumentDetailsDTO.getUpdatedOn());
		data.setClientApprovalStatus(timesheetDocumentDetailsDTO.getClientApprovalStatus());
	    data.setRmApprovalStatus("Pending");
	    data.setFinalFlag(timesheetDocumentDetailsDTO.getFinalFlag());
	    data.setDocMimeType(timesheetDocumentDetailsDTO.getDocFile().getContentType());
	    data.setActive(true);
	    return data;
		
	}
	
	public TimesheetDocumentDetailsDTO fetchTimesheetDocument(Long docId, Long timesheetId) {
	   TimesheetDocumentDetails entity = new TimesheetDocumentDetails();

	    if (docId != null) {
	    	entity = timesheetDocumentDetailsRepository.findByDocIdAndActive(docId,true);
	    } else if (timesheetId != null) {
	    	entity = timesheetDocumentDetailsRepository.findTopByTimesheetIdAndActive(timesheetId,true);
	    } else {
	        throw new IllegalArgumentException("Either docId or timesheetId must be provided.");
	    }
	    TimesheetDocumentDetailsDTO dto = new TimesheetDocumentDetailsDTO();
	    if(entity != null) {
	    dto.setDocId(entity.getDocId());
	    dto.setDocName(entity.getDocName());
	    dto.setTimesheetId(entity.getTimesheetId());
	    dto.setEmpId(entity.getEmpId());
	    dto.setCreatedOn(entity.getCreatedOn());
	    dto.setCreatedBy(entity.getCreatedBy());
	    dto.setUpdatedOn(entity.getUpdatedOn());
	    dto.setUpdatedBy(entity.getUpdatedBy());
	    dto.setActive(entity.getActive());
	    dto.setClientApprovalStatus(entity.getClientApprovalStatus());
	    dto.setRmApprovalStatus(entity.getRmApprovalStatus());
	    dto.setFinalFlag(entity.getFinalFlag());

	    if (entity.getDocData() != null) {
	        dto.setDocDataBase64(Base64.getEncoder().encodeToString(entity.getDocData()));
	        dto.setMimeType(entity.getDocMimeType());
	        }
	    }
	    else dto = null;
	    return dto;
	}

	
	public ServiceResponse createClientSideIdMapping(EmployeeClientSideIdMappingDTO empClientDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("create_client_side_id_mapping");
	    apiLogInfo.setApiUrl("/api/createClientSideIdMapping");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("empId: ").append(empClientDTO.getEmpId())
	              .append(", projectId: ").append(empClientDTO.getProjectId());

	    try {
	        Optional<EmployeeClientSideIdMapping> existing = employeeClientSideIdMappingRepository.findByProjectIdAndActive(empClientDTO.getProjectId(), true);
	        if (existing.isPresent()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("An active mapping already exists for this project.");
	            return response;
	        }

	        EmployeeClientSideIdMapping newEmpClient = new EmployeeClientSideIdMapping();
	        newEmpClient.setEmpId(empClientDTO.getEmpId());
	        newEmpClient.setProjectId(empClientDTO.getProjectId());
	        newEmpClient.setClientSideId(empClientDTO.getClientSideId());
	        newEmpClient.setCreatedBy(empClientDTO.getEmpId());
	        newEmpClient.setCreatedOn(LocalDateTime.now());
	        newEmpClient.setActive(true);

	        EmployeeClientSideIdMapping newMapping = employeeClientSideIdMappingRepository.save(newEmpClient);

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Saved Successfully!");
	        apiLogInfo.setApiResponse("New Mapping created for project id : "
	            + newMapping.getProjectId() + " for client side id: " + newMapping.getClientSideId());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

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
	
	public ServiceResponse updateClientSideIdMapping(EmployeeClientSideIdMappingDTO empClientDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("updateClientSideIdMapping");
		apiLogInfo.setApiUrl("/api/updateClientSideIdMapping");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +empClientDTO.getEmpId()+ "projectId:" +empClientDTO.getProjectId());
		try {

			Optional<EmployeeClientSideIdMapping> existingEmpClientMap = employeeClientSideIdMappingRepository.findByProjectIdAndActive(empClientDTO.getProjectId(),true);

			if (!existingEmpClientMap.isPresent()) {

	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No Client Side Id found for this project!");
	            apiLogInfo.setApiResponse("No mapping found for projectId: " + empClientDTO.getProjectId());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

	        } else {

	        	EmployeeClientSideIdMapping existingEmpClient = existingEmpClientMap.get();
	        	
	            existingEmpClient.setEmpId(empClientDTO.getEmpId());
	            existingEmpClient.setProjectId(empClientDTO.getProjectId());
	            existingEmpClient.setClientSideId(empClientDTO.getClientSideId());
	            existingEmpClient.setUpdatedBy(empClientDTO.getEmpId());
	            existingEmpClient.setUpdatedOn(LocalDateTime.now());

	            EmployeeClientSideIdMapping updatedMapping = employeeClientSideIdMappingRepository.save(existingEmpClient);

	            if (updatedMapping == null) {
	            	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Unable to update the Client Side Id!");
	                apiLogInfo.setApiResponse("Failed to save Client Side Id for projectId: "
	                    + empClientDTO.getProjectId());
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            } else {
	            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse("Updated Successfully!");
	                apiLogInfo.setApiResponse("Mapping updated for projectId: "
	                    + updatedMapping.getProjectId() + ", clientSideId: " + updatedMapping.getClientSideId());
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            }
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
	
	public ServiceResponse getActiveProjectsAndClientSideIdByEmpId(Long empId) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getActiveProjectsByEmpId");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
	        List<ProjectClientSideIdDTO> activeProjectList = timesheetsRepository.getActiveProjectsAndClientSideIdByEmpId(empId);
	        
	        if (activeProjectList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Please contact to the RMG team to provide you active project mapping!");
	            response.setServiceMessage("Employee has no active project mapping ! For EmpId: " + empId);
	            
	            apiLogInfo.setApiResponse("Employee has no active project mapping ! For EmpId: " + empId);
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(activeProjectList);
            response.setServiceMessage("Project List fetched successfully!");

            apiLogInfo.setApiResponse("Project list where employee has active = 1 in Employee Team Mapping table fetched successfully!");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage()); 
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	

	public ServiceResponse getEmployeeListByProjectId(Integer projectId, Long currentUser) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
	    	List<GetEmployeeListByProjectIdDTO> empList = employeeRepository.getEmployeeListByProjectId(projectId,currentUser);
	        
	    	if (empList == null || empList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No Active employees found for this Project!");
	            response.setServiceMessage("No Active employees found for the Project with ProjectId: " + projectId);
	            
	            apiLogInfo.setApiResponse("No Active employees found for the Project with ProjectId: " + projectId);
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(empList);
            response.setServiceMessage("Employee List fetched successfully!");

            apiLogInfo.setApiResponse("Employee List fetched successfully!");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage()); 
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	public ServiceResponse getClientSideIdByProjectIdAndEmpId(Long projectId, Long empId) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getClientSideIdByProjectIdAndEmpId");
	    apiLogInfo.setLogLevel("INFO");
	    
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +empId+ "projectId:" +projectId+"\n");
	    
	    try {
	        String clientSideId = employeeClientSideIdMappingRepository.getClientSideIdByProjectIdAndEmpId(projectId,empId);
	        
	        if (clientSideId.isEmpty() || clientSideId.equals("")) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No Client Side Id fetched for the selected Project!");
	            response.setServiceMessage("No Client Side Id fetched for the selected Project! For ProjectId: " + projectId);
	            
	            apiLogInfo.setApiResponse("No Client Side Id fetched for the selected Project! For ProjectId: " + projectId);
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(clientSideId);
            response.setServiceMessage("Client Side Id fetched successfully!");
    		logBuilder.append("empId : " +empId+ "Client Side Id :" +clientSideId+"\n");

            apiLogInfo.setApiResponse("Client Side Id fetched successfully!");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage()); 
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}



	public List<TimesheetDTO> getTimesheetForEmployee(Long empId, String fromDate, String toDate) {
	    try {
	        List<Object[]> records = timesheetsRepository.findByEmpIdAndDateBetween(empId, fromDate, toDate);

	        return records.stream().map(record -> {
	            TimesheetDTO dto = new TimesheetDTO();
	            dto.setEmployeeName((String) record[0]); 
	            dto.setProjectName((String) record[1]);  
	            dto.setTeamName((String) record[2]);     
	            // dto.set((String) record[3]); 
	            dto.setDate(record[4] != null ? record[4].toString() : null); 
	            dto.setDayType((String) record[5]);     
	            dto.setTotalTime(record[6] != null ? Float.valueOf(record[6].toString()) : null); 
	            dto.setActivity((String) record[7]);     
	            dto.setDescription((String) record[8]);  
	            dto.setManagerName((String) record[9]); 
	            return dto;
	        }).collect(Collectors.toList());

	    } catch (Exception e) {
	        System.err.println("Error fetching timesheet data: " + e.getMessage());
	        e.printStackTrace(); 
	        return new ArrayList<>(); 
	    }
	}


	
	public ServiceResponse getDocumentDataByDocId(Long docId) {
		ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getDocumentDataByDocId");
	    apiLogInfo.setLogLevel("INFO");
	    
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append( "docId:" +docId+"\n");
		 try {
			 TimesheetDocumentDetails docDetails = new TimesheetDocumentDetails();
			 docDetails = timesheetDocumentDetailsRepository.findByDocIdAndActive(docId,true);
			 if(docDetails == null) {
				 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse("Document not found...!!");
		            response.setServiceMessage("Document not found...!!" + docId);
		            
		            apiLogInfo.setApiResponse("Document not found...!!" + docId);
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		            logService.logMyInfo(httpRequest, apiLogInfo);
		            return response;
			 }else {
				 	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(docDetails);
//		            response.setServiceMessage("Client Side Id fetched successfully!");
		            apiLogInfo.setApiResponse("Document details fetched successfully");
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			 }
		 }
		 catch (Exception e) {
		        e.printStackTrace();
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        response.setServiceResponse("Something went wrong.");
		        response.setServiceError(e.getMessage());

		        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		        apiLogInfo.setApiResponse(e.getMessage()); 
		        apiLogInfo.setLogLevel("ERROR");
		    }
		 logService.logMyInfo(httpRequest, apiLogInfo);
		 return response;
	}
	
	public ServiceResponse checkIfProjectRequiresClientId(Integer projectId) {
		ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/checkIfProjectRequiresClientId");
	    apiLogInfo.setLogLevel("INFO");
	    
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append( "projectId: " +projectId+"\n");
		 try {
			 Project projObj = projectRepository.getByProjectId(projectId);
			 
			 if(projObj == null) {
				 
				 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	             response.setServiceResponse("No such project found in the system !");
	             apiLogInfo.setApiResponse("No such project found in the system !");
	             apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	             
			 } else {
				 
				 Boolean flag = projObj.getHasClientSideId();
				 
				 if(flag != null) {
					 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		             response.setServiceResponse(flag);
		             apiLogInfo.setApiResponse("Client Side Id status fetched successfully");
		             apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		             
		    		 logService.logMyInfo(httpRequest, apiLogInfo);
		             return response;
				 }
			 }
		 }
		 catch (Exception e) {
		        e.printStackTrace();
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        response.setServiceResponse("Something went wrong.");
		        response.setServiceError(e.getMessage());

		        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		        apiLogInfo.setApiResponse(e.getMessage()); 
		        apiLogInfo.setLogLevel("ERROR");
		    }
		 logService.logMyInfo(httpRequest, apiLogInfo);
		 return response;
	}
	
	@Transactional
	public ServiceResponse timesheetDocumentApproval(TimesheetDTO timesheetDTO) {
         ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("timesheetDocumentApproval");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId());
		try {
			
			TimesheetDocumentApproval dbResponse = null;
			TimesheetDocumentApproval timesheetDetailsForDocumentApproval =  timesheetDocumentApprovalRepository.findByTimesheetId(timesheetDTO.getTimesheetId());
			if(timesheetDetailsForDocumentApproval == null) {
				TimesheetDocumentApproval timesheetDetails = new TimesheetDocumentApproval();
				timesheetDetails.setTimesheetId(timesheetDTO.getTimesheetId());
				timesheetDetails.setApproverId(timesheetDTO.getTimesheetStatusUpdatedBy());	
				timesheetDetails.setApprovalStatus(timesheetDTO.getStatus());
				timesheetDetails.setLevelId(timesheetDTO.getLevelId());
				timesheetDetails.setPreviousLevelId(timesheetDTO.getPreviousLevelId());
				timesheetDetails.setPreviousApproverId(timesheetDTO.getPreviousApproverId());
				timesheetDetails.setRejectionId(timesheetDTO.getRejectionId());
				timesheetDetails.setCreatedBy(timesheetDTO.getEmpId());
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
				LocalDateTime createdOn = LocalDateTime.parse(timesheetDTO.getCreatedOn(), formatter);
				timesheetDetails.setCreatedOn(createdOn);
				
				dbResponse = timesheetDocumentApprovalRepository.save(timesheetDetails);
			}else {
				timesheetDetailsForDocumentApproval.setApproverId(timesheetDTO.getTimesheetStatusUpdatedBy());	
				timesheetDetailsForDocumentApproval.setApprovalStatus(timesheetDTO.getStatus());
				timesheetDetailsForDocumentApproval.setLevelId(timesheetDTO.getLevelId());
				timesheetDetailsForDocumentApproval.setPreviousLevelId(timesheetDetailsForDocumentApproval.getLevelId());
				timesheetDetailsForDocumentApproval.setPreviousApproverId(timesheetDetailsForDocumentApproval.getApproverId());
				timesheetDetailsForDocumentApproval.setRejectionId(timesheetDetailsForDocumentApproval.getRejectionId());
				timesheetDetailsForDocumentApproval.setUpdatedBy(timesheetDTO.getTimesheetStatusUpdatedBy());
				timesheetDetailsForDocumentApproval.setUpdatedOn(LocalDateTime.now());
				dbResponse = timesheetDocumentApprovalRepository.save(timesheetDetailsForDocumentApproval);
			}

			 if(dbResponse == null) {
				 
							    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					            response.setServiceResponse("Timesheet Document Failed To Approve");
					            response.setServiceMessage("Timesheet Document Failed To Approve");
					            
					            apiLogInfo.setApiResponse("Timesheet Document Failed To Approve");
					            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					            logService.logMyInfo(httpRequest, apiLogInfo);
					            return response;
						 }else {
							    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					            response.setServiceResponse(dbResponse);
					            response.setServiceMessage("Timesheet Document Approved!");
					            apiLogInfo.setApiResponse("Timesheet Document Approved!");
					            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						 }
			
		}catch(Exception e) {
			  e.printStackTrace();
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        response.setServiceResponse("Something went wrong.");
		        response.setServiceError(e.getMessage());

		        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		        apiLogInfo.setApiResponse(e.getMessage()); 
		        apiLogInfo.setLogLevel("ERROR");
			
		}
		return response;
	}
	
	@Transactional
	public ServiceResponse timesheetDocumentApprovalLogs(TimesheetDTO timesheetDTO) {
		 ServiceResponse response = new ServiceResponse();
			
			LogDTO apiLogInfo = new LogDTO();
			apiLogInfo.setSubFeatureName("timesheetDocumentApprovalLogs");
			apiLogInfo.setLogLevel("INFO");
			StringBuilder logBuilder = new StringBuilder();
			logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId());
			try {
				
				TimesheetApprovalAllocationLogs dbResponse = null;
				TimesheetDocumentApproval timesheetDetailsForDocumentApproval =  timesheetDocumentApprovalRepository.findByTimesheetId(timesheetDTO.getTimesheetId());
				if(timesheetDetailsForDocumentApproval == null) {
					TimesheetApprovalAllocationLogs timesheetDetails = new TimesheetApprovalAllocationLogs();
					timesheetDetails.setTimesheetId(timesheetDTO.getTimesheetId());
					timesheetDetails.setApproverId(timesheetDTO.getTimesheetStatusUpdatedBy());	
					timesheetDetails.setApprovalStatus(timesheetDTO.getStatus());
					timesheetDetails.setLevelId(timesheetDTO.getLevelId());
					timesheetDetails.setPreviousLevelId(timesheetDTO.getPreviousLevelId());
					timesheetDetails.setPreviousApproverId(timesheetDTO.getPreviousApproverId());
					timesheetDetails.setRejectionId(timesheetDTO.getRejectionId());
					timesheetDetails.setCreatedBy(timesheetDTO.getEmpId());
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
					LocalDateTime createdOn = LocalDateTime.parse(timesheetDTO.getCreatedOn(), formatter);
					timesheetDetails.setCreatedOn(createdOn);
					timesheetDetails.setAllocId(timesheetDTO.getAllocId());					
					dbResponse = timesheetApprovalAllocationLogsRepository.save(timesheetDetails);
				}else {
					TimesheetApprovalAllocationLogs timesheetDetails = new TimesheetApprovalAllocationLogs();
					timesheetDetails.setTimesheetId(timesheetDetailsForDocumentApproval.getTimesheetId());
					timesheetDetails.setApproverId(timesheetDetailsForDocumentApproval.getApproverId());	
					timesheetDetails.setApprovalStatus(timesheetDetailsForDocumentApproval.getApprovalStatus());
					timesheetDetails.setLevelId(timesheetDetailsForDocumentApproval.getLevelId());
					timesheetDetails.setPreviousLevelId(timesheetDetailsForDocumentApproval.getPreviousLevelId());
					timesheetDetails.setPreviousApproverId(timesheetDetailsForDocumentApproval.getPreviousApproverId());
					timesheetDetails.setRejectionId(timesheetDetailsForDocumentApproval.getRejectionId());
					timesheetDetails.setCreatedBy(timesheetDTO.getEmpId());
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
					LocalDateTime createdOn = LocalDateTime.parse(timesheetDTO.getCreatedOn(), formatter);
					timesheetDetails.setCreatedOn(createdOn);
					timesheetDetails.setAllocId(timesheetDetailsForDocumentApproval.getAllocId());					
					dbResponse = timesheetApprovalAllocationLogsRepository.save(timesheetDetails);
					
				}
				
				 if(dbResponse == null) {
					 
								    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						            response.setServiceResponse("Timesheet Logs Failed To Save");
						            response.setServiceMessage("Timesheet Logs Failed To Save");
						            
						            apiLogInfo.setApiResponse("Timesheet Logs Failed To Save");
						            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						            logService.logMyInfo(httpRequest, apiLogInfo);
						            return response;
							 }else {
								    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						            response.setServiceResponse(dbResponse);
						            response.setServiceMessage("Timesheet logs Saved!");
						            apiLogInfo.setApiResponse("Timesheet logs Saved!");
						            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
							 }
				
			
				}catch(Exception e) {
				  e.printStackTrace();
			        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			        response.setServiceResponse("Something went wrong.");
			        response.setServiceError(e.getMessage());

			        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			        apiLogInfo.setApiResponse(e.getMessage()); 
			        apiLogInfo.setLogLevel("ERROR");
				
			}
			return response;
			}
	
	
	
//	public ServiceResponse totalVmsFilledCount(String clientApprovalStatus) {
//	    ServiceResponse response = new ServiceResponse();
//	    LogDTO apiLogInfo = new LogDTO();
//	    apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
//	    apiLogInfo.setLogLevel("INFO");
//	    
//	    try {
//	    	List<TimesheetDTO> timesheetList = timesheetsRepository.getTotalVmsFilledCount(clientApprovalStatus);
//	        
//	    	if (timesheetList == null || timesheetList.isEmpty()) {
//	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	            response.setServiceResponse("No VMS Filled!");
//	            response.setServiceMessage("No VMS Filled!"); 
//	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	            logService.logMyInfo(httpRequest, apiLogInfo);
//	            return response;
//	        }
//	        
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            response.setServiceResponse(timesheetList);
//            response.setServiceMessage("TimesheetList List fetched successfully!");
//
//            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//	        
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	        response.setServiceResponse("Something went wrong.");
//	        response.setServiceError(e.getMessage());
//
//	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	        apiLogInfo.setApiResponse(e.getMessage()); 
//	        apiLogInfo.setLogLevel("ERROR");
//	    }
//
//	    logService.logMyInfo(httpRequest, apiLogInfo);
//	    return response;
//	}
//	
//	
//	
//	public ServiceResponse totalIshineFilledCount(String status) {
//	    ServiceResponse response = new ServiceResponse();
//	    LogDTO apiLogInfo = new LogDTO();
//	    apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
//	    apiLogInfo.setLogLevel("INFO");
//	    
//	    try {
//	    	List<TimesheetDTO> ishineTimesheetList = timesheetsRepository.totalIshineFilledCount(status);
//	        
//	    	if (ishineTimesheetList == null || ishineTimesheetList.isEmpty()) {
//	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	            response.setServiceResponse("No Timesheet Filled!");
//	            response.setServiceMessage("No Timesheet Filled!"); 
//	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	            logService.logMyInfo(httpRequest, apiLogInfo);
//	            return response;
//	        }
//	        
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            response.setServiceResponse(ishineTimesheetList);
//            response.setServiceMessage("TimesheetList List fetched successfully!");
//
//            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//	        
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	        response.setServiceResponse("Something went wrong.");
//	        response.setServiceError(e.getMessage());
//
//	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	        apiLogInfo.setApiResponse(e.getMessage()); 
//	        apiLogInfo.setLogLevel("ERROR");
//	    }
//
//	    logService.logMyInfo(httpRequest, apiLogInfo);
//	    return response;
//	}

	
	
}

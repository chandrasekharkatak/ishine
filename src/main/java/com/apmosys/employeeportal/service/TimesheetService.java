package com.apmosys.employeeportal.service;


import java.io.IOException;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.ClientDetailsForActivityDTO;
import com.apmosys.employeeportal.dto.ClientLocationDTO;
import com.apmosys.employeeportal.dto.EmpIdAndNameDTO;
import com.apmosys.employeeportal.dto.EmployeeClientSideIdMappingDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeInfoDTO;
import com.apmosys.employeeportal.dto.EmployeeViewForClientAttendanceStatusDTO;
import com.apmosys.employeeportal.dto.FilteredTimesheetDTO;
import com.apmosys.employeeportal.dto.GetClientDetailsByProjectIdAndEmpIdDTO;
import com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO;
import com.apmosys.employeeportal.dto.GetEmployeeListByProjectIdDTO;
import com.apmosys.employeeportal.dto.GetEmployeeSummaryOnExportDTO;
import com.apmosys.employeeportal.dto.GetEmployeeTimesheetAsCalenderByProjectIdDTO;
import com.apmosys.employeeportal.dto.GetEmployeeTimesheetAsCalenderDTO;
import com.apmosys.employeeportal.dto.GetEmployeeViewForClientAttendanceStatusDTO;
import com.apmosys.employeeportal.dto.GetProjectByMonthRangeAndEmpIdDTO;
import com.apmosys.employeeportal.dto.GetProjectListForDateAndEmpIdPayload;
import com.apmosys.employeeportal.dto.GetProjectViewForClientAttendanceStatusDTO;
import com.apmosys.employeeportal.dto.LastTimesheetFieldDto;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectClientSideIdDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectDetailsForActivityDTO;
import com.apmosys.employeeportal.dto.ProjectNameAndPrjoectIdDTO;
import com.apmosys.employeeportal.dto.ProjectTimesheetInfoDTO;
import com.apmosys.employeeportal.dto.TeamIdTeamNameDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDashboardCountDTO;
import com.apmosys.employeeportal.dto.TimesheetDocumentApprovalDTO;
import com.apmosys.employeeportal.dto.TimesheetDocumentDetailsDTO;
import com.apmosys.employeeportal.dto.TimesheetRejectionReasonsMasterDTO;
import com.apmosys.employeeportal.exception.UnauthorizedAccessException;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.DayTypeMasterNew;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeClientSideIdMapping;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.FinalDocument;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.model.TimesheetActivityMap;
import com.apmosys.employeeportal.model.TimesheetApprovalAllocationLogs;
import com.apmosys.employeeportal.model.TimesheetDataDTO;
import com.apmosys.employeeportal.model.TimesheetDocumentApproval;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.apmosys.employeeportal.model.TimesheetRejectionReasonsMaster;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.AuditCustomRepository;
import com.apmosys.employeeportal.repository.DayTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeClientSideIdMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.FinalDocumentRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapRepository;
import com.apmosys.employeeportal.repository.TimesheetApprovalAllocationLogsRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentApprovalRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsRepository;
import com.apmosys.employeeportal.repository.TimesheetRejectionReasonsMasterRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.service.validator.TimesheetValidatorService;
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
	private EmployeeLeaveRepository employeeLeaveRepository;
	
	@Autowired
	private HolidayRepository holidayRepository;
	
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
	
	@Autowired
	TimesheetRejectionReasonsMasterRepository  timesheetRejectionReasonsMasterRepository;
	
	@Autowired
	FinalDocumentRepository finalDocumentRepository;
	
	@Autowired
	TimesheetValidatorService timesheetValidatorService;
	
	@Autowired
	TimesheetDocumentService timesheetDocumentService;
	
	@Autowired
	TimesheetApprovalService timesheetApprovalService;
	
	@Autowired
	TimesheetDashboardService timesheetDashboardService;
	
	@Autowired
	TimesheetQueryService timesheetQueryService;
	
	@Autowired
	DayTypeMasterNewRepository dayTypeMasterNewRepository; 
	
	@Value("${maximum.timesheetCanBeFilledByMember}")
	private String maximumTimesheetCanBeFilledByTeamMember;
	
	@Value("${timesheet.check.period}")
	private String timesheetCheckPeriod;
	
	@PersistenceContext
    private EntityManager entityManager;

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
		return timesheetQueryService.getAllActivitiesByProjectIdandEmpId(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetQueryService
	private ServiceResponse getAllActivitiesByProjectIdandEmpIdInternal(TimesheetDTO timesheetDTO) {

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

//	@Transactional(rollbackFor = Exception.class)
//	public ServiceResponse addTimesheet(TimesheetDTO timesheetDTO, MultipartFile doc1, MultipartFile doc2) throws Exception {
//		ServiceResponse response = new ServiceResponse();
//		System.out.println("timesheetDTO currentManagerId : "+timesheetDTO.getCurrentManagerId());
//		
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("add_timesheet");
//		apiLogInfo.setApiUrl("/api/addTimesheet");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("empId : " +timesheetDTO.getEmpId()+ "dayType:" +timesheetDTO.getDayType());
//		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//		try {
//
//			List<ActivityDTO> allTimesheetActivities = timesheetDTO.getAllTimesheetActivities();
//			Timesheet newTimesheet = new Timesheet();
//
//			newTimesheet.setEmpId(timesheetDTO.getEmpId());
//			newTimesheet.setDate(stringToDateTimeParser.getDate(timesheetDTO.getDate(), "yyyy-MM-dd"));
//			newTimesheet.setDayType(timesheetDTO.getDayType());
//			newTimesheet.setProjectId(timesheetDTO.getProjectId());
//			newTimesheet.setClientSideId(timesheetDTO.getClientSideId() != null ? timesheetDTO.getClientSideId() : null);
//			newTimesheet.setClientApprovalStatus(timesheetDTO.getClientApprovalStatus() != null ? timesheetDTO.getClientApprovalStatus() : null);
//			newTimesheet.setHasClientSideId(timesheetDTO.getClientSideId() != null ? timesheetDTO.getHasClientSideId() : null);
//			newTimesheet.setIsShadowTimesheet(timesheetDTO.getIsShadowTimesheet() != null ? (Boolean) timesheetDTO.getIsShadowTimesheet() : null);
//			newTimesheet.setShadowEmpId(timesheetDTO.getShadowEmpId() != null ? Long.parseLong(timesheetDTO.getShadowEmpId().toString()) : null);
//			
//			System.out.println("timesheetDTO.getCurrentmanagerId() ==> "+timesheetDTO.getCurrentManagerId());
//
//			newTimesheet.setCurrentManagerId(timesheetDTO.getCurrentManagerId());	
//			if (timesheetDTO.getDayType().equals("Public Holiday") || timesheetDTO.getDayType().equals("Week Off") || timesheetDTO.getDayType().equals("Leave") || timesheetDTO.getDayType().equals("Client Holiday")) {
//				
//				newTimesheet.setDescription(timesheetDTO.getDescription());
//				newTimesheet.setTotalTime((float)0);
//				newTimesheet.setTotalWorkingHours("0");
//			} else {
//				
//				// LocalDateTime dateTime = LocalDateTime.parse(timesheetDTO.getOfficeInTime(), formatter);
//				
//				newTimesheet.setOfficeInTime(LocalDateTime.parse(timesheetDTO.getOfficeInTime(), formatter));
//				newTimesheet.setOfficeOutTime(LocalDateTime.parse(timesheetDTO.getOfficeOutTime(), formatter));
//				newTimesheet.setTotalWorkingHours(timesheetDTO.getTotalWorkingOfficeHours());
//
//				newTimesheet.setClientInTime(timesheetDTO.getClientInTime() != null ? timesheetDTO.getClientInTime() : null);
//				newTimesheet.setClientOutTime(timesheetDTO.getClientOutTime() != null ? timesheetDTO.getClientOutTime() : null);
//				newTimesheet.setTotalClientWorkingHours(timesheetDTO.getTotalClientWorkingHours() != null ? timesheetDTO.getTotalClientWorkingHours() : null);
//				
//				String description = "";
//				if (allTimesheetActivities.isEmpty()) {
//					description = "No activity available in timesheet";
//				} else {
//					for (ActivityDTO activity : allTimesheetActivities) {
//
//						description = description.concat(activity.getActivity() + "<br>");
//					
//
//					}
//				}
//				newTimesheet.setDescription(description);
//			}
//			newTimesheet.setStatus("Pending");
//			newTimesheet.setIsNightShift(timesheetDTO.getIsNightShift());
//			newTimesheet.getCommonProperty().setCreatedBy(timesheetDTO.getCreatedBy());
//			
//			
////			added by sakti
//			
////			Timesheet existing = timesheetsRepository.findByEmpIdAndDate(timesheetDTO.getEmpId(), stringToDateTimeParser.getDate(timesheetDTO.getDate(), "yyyy-MM-dd"));
////			if (existing != null) {
////			    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
////			    response.setServiceResponse("Timesheet already exists for this date.");
////			    
////			    apiLogInfo.setApiResponse("Duplicate timesheet entry attempt");
////			    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
////			    logService.logMyInfo(httpRequest, apiLogInfo);
////			    
////			    return response;
////			}
//
//			
//			System.err.println("Time sheet checked "+newTimesheet);
//
//			Timesheet newTimesheetCreated = timesheetsRepository.save(newTimesheet);
//			if(Boolean.FALSE.equals(timesheetDTO.getIsShadowTimesheet()) && Boolean.TRUE.equals(timesheetDTO.getHasClientSideId())
//					&& ("Working".equalsIgnoreCase(timesheetDTO.getDayType()) || "Non-working".equalsIgnoreCase(timesheetDTO.getDayType()))) {
//			if(timesheetDTO.getClientApprovalStatus() == null) {
//		    	throw new IllegalArgumentException("Client approval status is null");
//	    	}
//		    if (doc1 != null && "pending".equalsIgnoreCase(timesheetDTO.getClientApprovalStatus())) {
//		        handleDocumentUpload(timesheetDTO, newTimesheetCreated, doc1, false); // may throw exception
//		    }else {
//		    	if("pending".equalsIgnoreCase(timesheetDTO.getClientApprovalStatus())) {
//		    	throw new IllegalArgumentException("In case of pending the document is missing");
//		    	}
//		    }
//		    
//
//		    if (doc2 != null && doc1 != null && "approved".equalsIgnoreCase(timesheetDTO.getClientApprovalStatus())) {
//		        handleDocumentUpload(timesheetDTO, newTimesheetCreated, doc1, false); // unapproved
//		        handleDocumentUpload(timesheetDTO, newTimesheetCreated, doc2, true);  // approved
//		    }
//		    else {
//		    	if("approved".equalsIgnoreCase(timesheetDTO.getClientApprovalStatus()))
//		    	throw new IllegalArgumentException("In case of approved one of the document is missing");
//		    }
//			}
//
//			if (!timesheetDTO.getDayType().equals("Public Holiday") && !timesheetDTO.getDayType().equals("Week Off") && !timesheetDTO.getDayType().equals("Leave") && !timesheetDTO.getDayType().equals("Client Holiday")) {				
//				Optional.ofNullable(newTimesheetCreated.getEmpId()).ifPresentOrElse((timesheet) -> {
//					
//					List<TimesheetActivityMap> mapList = new ArrayList<TimesheetActivityMap>();
//					allTimesheetActivities.forEach((activity) -> {
//
//						TimesheetActivityMap map = new TimesheetActivityMap();
//						map.setActivityId(activity.getActivityId());
//						map.setCompletionTime(activity.getCompletionTime());
//						if(activity.getDescription() == null) {
//							Activity activityObj = activitiesRepository.getById(activity.getActivityId());
//							map.setDescription(activityObj.getActivity());
//						}else {
//							map.setDescription(activity.getDescription());
//						}
//						map.setTimesheetId(newTimesheetCreated.getTimesheetId());
//						map.setClientLocationId(activity.getClientLocationId());
//						
//						mapList.add(map);
//
//						Float savedTime = newTimesheetCreated.getTotalTime() != null ? newTimesheetCreated.getTotalTime() : 0;
//						Float totalTime = activity.getCompletionTime() + savedTime;
//						newTimesheet.setTotalTime(totalTime);
//						
//						System.err.println("Time sheet checked2 "+newTimesheet);
//						timesheetsRepository.save(newTimesheet);
//					});
//					
//					
//					System.err.println("Time sheet mapList "+mapList);
//
//					List<TimesheetActivityMap> activityMapped = timesheetActivityMapRepository.saveAll(mapList);
//
//					if (activityMapped.isEmpty()) {
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse("Timesheet added , but activity not mapped.");
//						
//						apiLogInfo.setApiResponse("Timesheet added , but activity not mapped.");			
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//					} else {
//						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//						response.setServiceResponse("Timesheet added successfully");
//						
//						apiLogInfo.setApiResponse("Timesheet added successfully");			
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//						
//					}
//
//				}, () -> {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Timesheet not generated");
//					
//					apiLogInfo.setApiResponse("Timesheet not generated");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				});
//			} else {
//				if (newTimesheetCreated == null) {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Timesheet not generated");
//					
//					apiLogInfo.setApiResponse("Timesheet not generated");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("Timesheet added successfully");
//					
//					apiLogInfo.setApiResponse("Timesheet added successfully");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				}
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
//			throw e;
//		}
//		
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse addTimesheet(TimesheetDTO timesheetDTO, MultipartFile doc1, MultipartFile doc2)
			throws Exception {
		ServiceResponse response = new ServiceResponse();
		System.out.println("timesheetDTO currentManagerId : " + timesheetDTO.getCurrentManagerId());

		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_timesheet");
		apiLogInfo.setApiUrl("/api/addTimesheet");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + timesheetDTO.getEmpId() + " dayType:" + timesheetDTO.getDayType());

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		try {
			
			// Validate employee authorization
			timesheetValidatorService.validateEmployeeAuthorization(timesheetDTO);
			
			// Parse and validate timesheet date
			LocalDate timesheetDate = stringToDateTimeParser.getDate(timesheetDTO.getDate(), "yyyy-MM-dd");
			timesheetValidatorService.validateTimesheetDateNotInFuture(timesheetDate);
			
			// Validate timesheet lock period
			ServiceResponse lockResponse = timesheetValidatorService.validateTimesheetLockPeriod(
					timesheetDTO.getEmpId(), timesheetDate);
			if (lockResponse != null) {
				return lockResponse;
			}
			
			// Validate client-side documents
			timesheetValidatorService.validateClientSideDocuments(timesheetDTO, doc1, doc2);

			Timesheet existingTimesheet = timesheetsRepository.findByEmpIdAndDate(timesheetDTO.getEmpId(),
					timesheetDate);

			Timesheet newTimesheet;
			boolean isUpdate = false;

			// If day type is "Not Working" and a record already exists, update it(Like if Sunday ,satur day)
			if (existingTimesheet != null && "Non-working".equalsIgnoreCase(timesheetDTO.getDayType())) {
				newTimesheet = existingTimesheet;
				isUpdate = true;
				System.out.println("Updating existing timesheet for Non Working day");
			} else {
				newTimesheet = new Timesheet();
				newTimesheet.setEmpId(timesheetDTO.getEmpId());
				newTimesheet.setDate(timesheetDate);
			}

			newTimesheet.setDayType(timesheetDTO.getDayType());
			newTimesheet.setProjectId(timesheetDTO.getProjectId());
			newTimesheet.setIsShadowTimesheet(timesheetDTO.getIsShadowTimesheet());
			newTimesheet.setShadowEmpId(
					timesheetDTO.getShadowEmpId() != null ? Long.parseLong(timesheetDTO.getShadowEmpId().toString())
							: null);
			newTimesheet.setCurrentManagerId(timesheetDTO.getCurrentManagerId());

			if (timesheetDTO.getDayType().equals("Public Holiday") || timesheetDTO.getDayType().equals("Week Off")
					|| timesheetDTO.getDayType().equals("Leave")
					|| timesheetDTO.getDayType().equals("Client Holiday")) {

				newTimesheet.setDescription(timesheetDTO.getDescription());
				newTimesheet.setTotalTime(0f);
				newTimesheet.setTotalWorkingHours("0");
			} else {
				// Validate office in/out time
				LocalDateTime officeIn = LocalDateTime.parse(timesheetDTO.getOfficeInTime(), formatter);
				LocalDateTime officeOut = LocalDateTime.parse(timesheetDTO.getOfficeOutTime(), formatter);
				timesheetValidatorService.validateOfficeTime(officeIn, officeOut, now);
				
				newTimesheet.setOfficeInTime(officeIn);
				newTimesheet.setOfficeOutTime(officeOut);
				newTimesheet.setTotalWorkingHours(timesheetDTO.getTotalWorkingOfficeHours());

				String description = "";
				if (timesheetDTO.getAllTimesheetActivities().isEmpty()) {
					description = "No activity available in timesheet";
				} else {
					for (ActivityDTO activity : timesheetDTO.getAllTimesheetActivities()) {
						description += activity.getActivity() + "<br>";
					}
				}
				newTimesheet.setDescription(description);
			}

			newTimesheet.setStatus("Pending");
			newTimesheet.setIsNightShift(timesheetDTO.getIsNightShift());
			if (!isUpdate) {
				newTimesheet.getCommonProperty().setCreatedBy(timesheetDTO.getCreatedBy());
			} else {
				newTimesheet.getCommonProperty().setUpdatedBy(timesheetDTO.getCreatedBy());
				newTimesheet.getCommonProperty().setUpdatedOn(LocalDateTime.now());
			}

			if (Boolean.FALSE.equals(timesheetDTO.getIsShadowTimesheet())
					&& Boolean.TRUE.equals(timesheetDTO.getHasClientSideId())
					&& ("Working".equalsIgnoreCase(timesheetDTO.getDayType())
							|| "Non-working".equalsIgnoreCase(timesheetDTO.getDayType()))) {

				newTimesheet.setClientSideId(timesheetDTO.getClientSideId());
				newTimesheet.setClientApprovalStatus(timesheetDTO.getClientApprovalStatus());
				newTimesheet.setHasClientSideId(timesheetDTO.getHasClientSideId());
				newTimesheet.setClientInTime(timesheetDTO.getClientInTime());
				newTimesheet.setClientOutTime(timesheetDTO.getClientOutTime());
				newTimesheet.setTotalClientWorkingHours(timesheetDTO.getTotalClientWorkingHours());

				// Validate client-side time constraints
				ServiceResponse clientTimeResponse = timesheetValidatorService.validateClientSideTimeConstraints(
						timesheetDTO, now);
				if (clientTimeResponse != null) {
					return clientTimeResponse;
				}

				// Validate documents for approval status
				ServiceResponse docResponse = timesheetValidatorService.validateDocumentsForApprovalStatus(
						timesheetDTO, doc1, doc2);
				if (docResponse != null) {
					return docResponse;
				}
			}

			Timesheet savedTimesheet = timesheetsRepository.save(newTimesheet);

			if (Boolean.TRUE.equals(timesheetDTO.getHasClientSideId())
					&& ("Working".equalsIgnoreCase(timesheetDTO.getDayType())
							|| "Non-working".equalsIgnoreCase(timesheetDTO.getDayType()))) {

				// Validate client approval status
				ServiceResponse statusResponse = timesheetValidatorService.validateClientApprovalStatus(
						timesheetDTO.getClientApprovalStatus());
				if (statusResponse != null) {
					return statusResponse;
				}

				// Validate and handle documents
				if (doc1 != null && "pending".equalsIgnoreCase(timesheetDTO.getClientApprovalStatus())) {
					timesheetDocumentService.handleDocumentUpload(timesheetDTO, savedTimesheet, doc1, false);
				} else if ("pending".equalsIgnoreCase(timesheetDTO.getClientApprovalStatus())) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("In case of pending the document is missing");
					return response;
				}

				if (doc2 != null && doc1 != null
						&& "approved".equalsIgnoreCase(timesheetDTO.getClientApprovalStatus())) {
					timesheetDocumentService.handleDocumentUpload(timesheetDTO, savedTimesheet, doc1, false);
					timesheetDocumentService.handleDocumentUpload(timesheetDTO, savedTimesheet, doc2, true);
				} else if ("approved".equalsIgnoreCase(timesheetDTO.getClientApprovalStatus())) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("In case of approved one of the documents is missing");
					return response;
				}
			}

			if (!timesheetDTO.getDayType().equals("Public Holiday") && !timesheetDTO.getDayType().equals("Week Off")
					&& !timesheetDTO.getDayType().equals("Leave")
					&& !timesheetDTO.getDayType().equals("Client Holiday")) {

				List<TimesheetActivityMap> mapList = new ArrayList<>();
				for (ActivityDTO activity : timesheetDTO.getAllTimesheetActivities()) {
					TimesheetActivityMap map = new TimesheetActivityMap();
					map.setActivityId(activity.getActivityId());
					map.setCompletionTime(activity.getCompletionTime());
					map.setDescription(activity.getDescription() != null ? activity.getDescription()
							: activitiesRepository.getById(activity.getActivityId()).getActivity());
					map.setTimesheetId(savedTimesheet.getTimesheetId());
					map.setClientLocationId(activity.getClientLocationId());
					mapList.add(map);

					Float savedTime = savedTimesheet.getTotalTime() != null ? savedTimesheet.getTotalTime() : 0;
					savedTimesheet.setTotalTime(activity.getCompletionTime() + savedTime);
				}
				timesheetsRepository.save(savedTimesheet);
				List<TimesheetActivityMap> activityMapped = timesheetActivityMapRepository.saveAll(mapList);

				if (activityMapped.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet added, but activity not mapped.");
					apiLogInfo.setApiResponse("Timesheet added, but activity not mapped.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(
							isUpdate ? "Timesheet updated successfully" : "Timesheet added successfully");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(
						isUpdate ? "Timesheet updated successfully" : "Timesheet added successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public List<EmployeeDTO> getAllTeamMemberView(Long empId) {
	    LocalDate date = LocalDate.now().minusDays(Long.parseLong(timesheetCheckPeriod));
	    List<Object[]> list = employeeRepository.getAllTeamMemberView(empId);
	    List<EmployeeDTO> dtoList = new ArrayList<>();

	    if (list == null || list.isEmpty()) {
	        return Collections.emptyList(); // Return empty list if no team members found
	    }

	    list.forEach((object) -> {
	        EmployeeDTO dto = new EmployeeDTO();
	        dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
	        dto.setName(object[1] != null ? object[1].toString() : null);
	        dto.setEmail(object[2] != null ? object[2].toString() : null);
	        dto.setJobRoleName(object[3] != null ? object[3].toString() : null);
	        dto.setMobileNo(object[4] != null ? Long.parseLong(object[4].toString()) : null);
	        dto.setEmployeementId(object[5] != null ? Long.parseLong(object[5].toString()) : null);
	        dto.setEmploymentstatus(object[6] != null ? object[6].toString() : null);
	        dto.setManagerId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
	        dto.setManagerName(object[8] != null ? object[8].toString() : null);
	        dto.setManagerEmail(object[9] != null ? object[9].toString() : null);
	        dto.setHodId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
	        dto.setHodName(object[11] != null ? object[11].toString() : null);
	        dto.setHodEmail(object[12] != null ? object[12].toString() : null);
	        dto.setDepartmentId(object[13] != null ? Long.parseLong(object[13].toString()) : null);
	        dto.setIsTimesheetLockCheckEnable(object[14] != null ? object[14].toString() : null);
	        dto.setReportingManagerId(object[15] != null ? Long.parseLong(object[15].toString()) : null);
	        dto.setApprovalsTo(object[16] != null ? object[16].toString() : null);
	        dto.setReportingManagerName(object[17] != null ? object[17].toString() : null);
	        dto.setReportingManagerEmail(object[18] != null ? object[18].toString() : null);
	        dto.setDateOfJoining(object[19] != null ? object[19].toString() : null);
	        dto.setProbationPeriod(object[20] != null ? Short.parseShort(object[20].toString()) : null);
	        dto.setIsConsultant(object[21] != null ? object[21].toString() : null);
	        dto.setIsApprenticeship(object[22] != null ? object[22].toString() : null);
	        dto.setIsApmosysProduct(object[23] != null ? object[23].toString() : null);

	        // Employment ID formatting
	        String employmentId = dto.getEmployeementId() != null ? dto.getEmployeementId().toString() : null;
	        String isApmosysProduct = dto.getIsApmosysProduct();

	        if (employmentId != null) {
	            if ("true".equalsIgnoreCase(isApmosysProduct)) {
	                dto.setEmploymentIdAcToET("AP-" + employmentId);
	            } else {
	                dto.setEmploymentIdAcToET("A-" + employmentId);
	            }
	        }

	        dto.setClientSideId(object[24] != null ? object[24].toString() : null);
	        dto.setEmploymentId(object[25] != null ? object[25].toString() : null);

	        // Timesheet fill check
	        Long memberEmpId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
	        List<Object[]> timesheetFilledByMember = timesheetsRepository.getTimesheetFilledByMemberOLD(memberEmpId, date);

	        boolean isFilled = timesheetFilledByMember.size() >= Long.parseLong(maximumTimesheetCanBeFilledByTeamMember);
	        dto.setIsTimesheetFilledByMember(isFilled ? "true" : "false");

	        dtoList.add(dto);
	    });

	    return dtoList;
	}



	
	private void handleDocumentUpload(TimesheetDTO timesheetDTO, Timesheet newTimesheetCreated, MultipartFile file,
			boolean isFinal) throws IOException {
		timesheetDocumentService.handleDocumentUpload(timesheetDTO, newTimesheetCreated, file, isFinal);
	}

	public ServiceResponse getAllMyTimesheetsByEmpId(TimesheetDTO timesheetDTO) {
		return timesheetQueryService.getAllMyTimesheetsByEmpId(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetQueryService
	private ServiceResponse getAllMyTimesheetsByEmpIdInternal(TimesheetDTO timesheetDTO) {
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
					.getAllMyTimesheetsOLD(timesheetDTO.getEmpId(), start, end);

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
						dto.setProjectName(object[21] != null ? object[21].toString() : null);
						dto.setClientApprovalStatus(object[22] != null ? object[22].toString() : null);
						dto.setHasClientSideId(object[23] != null ? (Boolean) object[23] : null);
						dto.setEmploymentId(object[23] != null ? employeeRepository.fetchEmploymentIdByEmpId(Long.parseLong(object[9].toString())) : null);
						dto.setIsShadowTimesheet(object[24] != null ? (Boolean) object[24] : null);
						dto.setShadowEmpId(object[25] != null ? Long.parseLong(object[25].toString()) : null);
						dto.setRejectReason(object[26] != null ? object[26].toString() : null);
						
						
						if(timesheetId != null) {
							 List<TimesheetDocumentDetailsDTO> details =timesheetDocumentDetailsRepository.findAllDocIdByTimesheetId(timesheetId);
							 if(details.size()>1) {
								 dto.setBulkApprovedDocId(null);					
								 for (TimesheetDocumentDetailsDTO doc : details) {
							     if (Boolean.TRUE.equals(doc.getFinalFlag())) {
							         dto.setApprovedDocument(doc.getDocId());
							     }
							     if(Boolean.FALSE.equals(doc.getFinalFlag())) {
							    	 dto.setFilledDocument(doc.getDocId());  	 
							     }
							 } 
							}else if(details.size()==1) {
								dto.setBulkApprovedDocId(details.get(0).getBulkApprovedDocId());								
								if(Boolean.TRUE.equals(details.get(0).getFinalFlag()) && details.get(0).getBulkApprovedDocId()!=null) {
									dto.setFilledDocument(details.get(0).getDocId());
							        dto.setApprovedDocument(details.get(0).getBulkApprovedDocId());
							        }
							else {
								dto.setFilledDocument(details.get(0).getDocId());
								
							}
							}
						};
						// Get InActive Activities In Timesheet
						if(dto.getStatus() != null && dto.getStatus().equals("Pending")) {
							List<Object[]> inactiveActivityList = timesheetsRepository
									.getInactiveActivitiesByTimesheetIdOLD(dto.getTimesheetId());
							
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
	
	
	public ServiceResponse getAllMyTeamTimesheets(TimesheetDTO timesheetDTO) {
		return timesheetQueryService.getAllMyTeamTimesheets(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetQueryService
	private ServiceResponse getAllMyTeamTimesheetsInternal(TimesheetDTO timesheetDTO) {
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
					.getAllMyTeamTimesheetsOLD(timesheetDTO.getCreatedBy(), start, end);

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
						dto.setRejectReason(object[25] != null ? object[25].toString() : null);				
						
						
						 if(timesheetId != null) {
							 List<TimesheetDocumentDetailsDTO> details =timesheetDocumentDetailsRepository.findAllDocIdByTimesheetId(timesheetId);
							 for (TimesheetDocumentDetailsDTO doc : details) {
							     if (Boolean.TRUE.equals(doc.getFinalFlag())) {
							         dto.setApprovedDocument(doc.getDocId());
							     }
							     if(Boolean.FALSE.equals(doc.getFinalFlag())) {
							    	 dto.setFilledDocument(doc.getDocId());  	 
							     }
							 }

							 
						}
						// Get InActive Activities In Timesheet
						if(dto.getDayType().equals("Working") && (dto.getStatus().equals("Pending") || dto.getStatus().equals("Rejected"))) {
							List<Object[]> inactiveActivityList = timesheetsRepository
									.getInactiveActivitiesByTimesheetIdOLD(dto.getTimesheetId());
							
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
//	public ServiceResponse getMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
//		return timesheetApprovalService.getMyReporteesTimesheetRequests(timesheetDTO);
//	}


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
		return timesheetApprovalService.countMyReporteesTimesheetRequests(timesheetDTO);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateTimesheetRequestById(TimesheetDTO timesheetDTO) {
		return timesheetApprovalService.updateTimesheetRequestById(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetApprovalService
	private ServiceResponse updateTimesheetRequestByIdInternal(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_request_status");
		apiLogInfo.setApiUrl("/api/updateTimesheetRequestById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId());
		try { 

			Optional<Timesheet> timesheetobject = timesheetsRepository.findById(timesheetDTO.getTimesheetId());
			if (timesheetobject.isPresent()) {
			    Timesheet timesheet = timesheetobject.get();

			    Timestamp createdOnTimestamp = timesheet.getCommonProperty().getCreatedOn();

			    if (createdOnTimestamp != null) {
			      
			        LocalDateTime createdOnLDT = createdOnTimestamp.toLocalDateTime();

			        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			        String formattedCreatedOn = createdOnLDT.format(formatter);

			        timesheetDTO.setCreatedOn(formattedCreatedOn);
			    } else {
			        timesheetDTO.setCreatedOn(null);
			    }
			}

		    
            if(timesheetDTO.getClientSideId() !=null) {
	
//            	ServiceResponse response1 = timesheetDocumentApproval(timesheetDTO);
            	ServiceResponse response1 = bulkTimesheetDocumentApproval(
            		    Collections.singletonList(timesheetDTO)
            		);
            	 if (ServiceResponse.STATUS_FAIL.equals(response1.getServiceStatus())
                         || ServiceResponse.SOMETHING_WENT_WRONG.equals(response1.getServiceStatus())) {
                     throw new RuntimeException("Timesheet Document Failed To Approve");
                 } 

            }
			timesheetobject.ifPresentOrElse((timesheet) -> {

				timesheet.setStatus(timesheetDTO.getStatus());
				timesheet.setTimesheetStatusUpdatedBy(timesheetDTO.getTimesheetStatusUpdatedBy());
				timesheet.setRemarks(timesheetDTO.getRejectReason());
				timesheet.setRejectionId(timesheetDTO.getRejectionId() != null 
                	    ? timesheetDTO.getRejectionId().longValue() 
                	    : null);
				Timesheet updatedTimesheet = timesheetsRepository.save(timesheet);

//            	 ServiceResponse response2 = timesheetDocumentApprovalLogs(timesheetDTO);
				ServiceResponse response2 = bulkTimesheetDocumentApprovalLogs(
					    Collections.singletonList(timesheetDTO)
					);
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
	public ServiceResponse updateTimesheet(TimesheetDTO timesheetDTO,MultipartFile doc1 , MultipartFile doc2) {
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
		LocalDateTime now = LocalDateTime.now();
		try {

			// Validate employee authorization
			timesheetValidatorService.validateEmployeeAuthorization(timesheetDTO);
			
			Optional<Timesheet> timesheet = timesheetsRepository.findById(timesheetDTO.getTimesheetId());
			
			List<TimesheetDocumentDetailsDTO> details =timesheetDocumentDetailsRepository.findAllDocIdByTimesheetId(timesheetDTO.getTimesheetId());
		if (details != null && !details.isEmpty()) {
			for (TimesheetDocumentDetailsDTO doc : details) {
			    if (
			        Boolean.TRUE.equals(doc.getFinalFlag()) &&
			        "Pending".equalsIgnoreCase(timesheetDTO.getClientApprovalStatus()) && (doc.getBulkApprovedDocId()== null)
			    ) {
			    	Optional<TimesheetDocumentDetails> optionalEntity =
			                timesheetDocumentDetailsRepository.findById(doc.getDocId());

			        if (optionalEntity.isPresent()) {
			            TimesheetDocumentDetails entity = optionalEntity.get();

			            timesheetDocumentDetailsRepository.delete(entity);
			        }			    }
			}
		}
			if (timesheet.isPresent()) {

				Timesheet existingTimesheet = timesheet.get();
                existingTimesheet.setCurrentManagerId(timesheetDTO.getCurrentManagerId());
				existingTimesheet.getCommonProperty().setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				existingTimesheet.getCommonProperty().setUpdatedBy(timesheetDTO.getCreatedBy());
				
				// Validate timesheet date
				LocalDate updateDate = stringToDateTimeParser.getDate(timesheetDTO.getDate(), "yyyy-MM-dd");
				timesheetValidatorService.validateTimesheetDateNotInFuture(updateDate);
				existingTimesheet.setDate(updateDate);
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
				

				if (timesheetDTO.getDayType().equals("Public Holiday") || timesheetDTO.getDayType().equals("Week Off") || timesheetDTO.getDayType().equals("Leave") || timesheetDTO.getDayType().equals("Client Holiday")) {
					timesheetActivityMapRepository.deleteByTimesheetId(timesheetDTO.getTimesheetId());
					existingTimesheet.setDescription(timesheetDTO.getDescription());
					existingTimesheet.setDayType(timesheetDTO.getDayType());
					existingTimesheet.setOfficeInTime(null);
					existingTimesheet.setOfficeOutTime(null);
					existingTimesheet.setTotalWorkingHours("0");

				} else {
					
					if (Boolean.FALSE.equals(timesheetDTO.getIsShadowTimesheet())
		                    && Boolean.TRUE.equals(timesheetDTO.getHasClientSideId())
		                    && ("Working".equalsIgnoreCase(timesheetDTO.getDayType())
		                            || "Non-working".equalsIgnoreCase(timesheetDTO.getDayType()))) {

		                // Validate client-side time constraints
		                ServiceResponse clientTimeResponse = timesheetValidatorService.validateClientSideTimeConstraints(
		                        timesheetDTO, now);
		                if (clientTimeResponse != null) {
		                    return clientTimeResponse;
		                }

		            }
					
					
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
				if (timesheetDTO.getTotalWorkingOfficeHours() != null && !timesheetDTO.getTotalWorkingOfficeHours().isEmpty()) {
					try {
						String[] parts = timesheetDTO.getTotalWorkingOfficeHours().split(":");
						if (parts.length == 2) {
							float hours = Float.parseFloat(parts[0]);
							float minutes = Float.parseFloat(parts[1]);
							float rawDecimal = hours + (minutes / 60.0f);

							float roundedTime = (float) (Math.round(rawDecimal * 100.0) / 100.0);

							existingTimesheet.setTotalTime(roundedTime);
						}
					} catch (Exception e) {
						System.err.println("Error parsing working hours: " + timesheetDTO.getTotalWorkingOfficeHours());
					}
				}
				Timesheet updatedTimesheet = timesheetsRepository.save(existingTimesheet);

			if(doc1 != null && "pending".equalsIgnoreCase(timesheetDTO.getClientApprovalStatus())){
					//TimesheetDocumentDetailsDTO document = new TimesheetDocumentDetailsDTO();
					//This is for normal flow but there is one test case If he had created a time sheet using new code and then updated it
				   //-> First he had uploaded both the documents and the client approval status is approved , but then he edited it and make it 
				  //pending and upload only one doc the, filled doc for this we have to handle it differently.

					TimesheetDocumentDetailsDTO document = getSingleNonFinalDocument(timesheetDTO);
					
					TimesheetDocumentDetails existing = getSingleExistingDocument(document.getDocId());

					// Case 1 If it is pending previously, or it is approved and created by using old code 
					if(!existing.getFinalFlag()) {	
				        	savePendingDocument(existing, document, doc1, timesheetDTO, updatedTimesheet, response, apiLogInfo);
						  }
						//This is for when updating client approve status from approved to pending and this is created by new code.
						else {
							  // Case 2: Approved → Pending rollback (new code)
					        rollbackApprovedToPending(existing, document, timesheetDTO, response, apiLogInfo,doc1);
							}
					
				}	

					if(doc2 != null && "Approved".equalsIgnoreCase(timesheetDTO.getClientApprovalStatus())) {
//					
						
						TimesheetDocumentDetailsDTO document = getSingleFinalDocument(timesheetDTO);
					//Doc id not null means when creating the time sheet it was created as approved and both document were there.
						if(document.getDocId()!=null) {
							
						TimesheetDocumentDetails existing = getSingleExistingDocument(document.getDocId());

//						
						if(existing.getBulkApprovedDocId()!=null){
							// Bulk approved doc id is not null means this timesheet is created by the new code.
//							
								replaceApprovedDocumentNewCode(
					                    doc2, document, existing, timesheetDTO, response, apiLogInfo
					            );
				            
						}
					else if(existing.getBulkApprovedDocId()==null) {
					// This timesheet  is created by the old code
						
				        saveApprovedDocumentOldCode(
				                doc2, document, timesheetDTO, updatedTimesheet, response, apiLogInfo
				        );


				}
						
					}
						
					else if(document.getDocId()==null) {
						
					    handleFirstTimeApprovedDocument(
					            doc2, document, timesheetDTO, response, apiLogInfo
					    );
					//This means it does not had the approved document previously.
							
					}
						
						
				}
				
					if(doc1 != null && "approved".equalsIgnoreCase(timesheetDTO.getClientApprovalStatus())){
						TimesheetDocumentDetailsDTO document = new TimesheetDocumentDetailsDTO();
						List<TimesheetDocumentDetailsDTO> nonFinalDocuments = timesheetDTO.getDocumentData()
							    .stream()
							    .filter(doc -> Boolean.FALSE.equals(doc.getFinalFlag()))
							    .collect(Collectors.toList());
						if(nonFinalDocuments.size() == 1) {
							document = nonFinalDocuments.get(0);
							}
						else {
							throw new IllegalArgumentException("Sending multiple Unapproved file data");
						}
						//first find out that this timesheet is created using old code or new.
						List<Long> docIdsFromDB = timesheetDocumentDetailsRepository.findDocIdsByTimesheetId(timesheetDTO.getTimesheetId());
						if(docIdsFromDB.size()>1) {
							// this time sheet is created based on previous code.
							
						TimesheetDocumentDetails docData = addTimesheetDocument(document,"Update",doc1);
						docData.setTimesheetId(updatedTimesheet.getTimesheetId());
						docData.setEmpId(timesheetDTO.getEmpId());
						docData.setCreatedBy(timesheetDTO.getEmpId());
//						docData.setDocData(doc.getBytes());
						
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
						else if(docIdsFromDB.size() == 1) {
							//either it is created by the new code
							byte[] fileBytes = doc1.getBytes();
							String fileName = doc1.getOriginalFilename();
							String contentType = doc1.getContentType();
							try {
							List<TimesheetDocumentDetails> res = timesheetDocumentDetailsRepository.findByDocId(docIdsFromDB.get(0));
							TimesheetDocumentDetails existedTdd = new TimesheetDocumentDetails();
							if(res!=null && !res.isEmpty()) {
								existedTdd = res.get(0);
							}
							existedTdd.setUpdatedBy(timesheetDTO.getEmpId());
							existedTdd.setDocName(fileName);
							existedTdd.setDocData(fileBytes);
							existedTdd.setDocMimeType(contentType);
							existedTdd.setUpdatedOn(LocalDateTime.now());
							
							 TimesheetDocumentDetails finalResponse = timesheetDocumentDetailsRepository.save(existedTdd);
					            if(finalResponse==null || res==null) {
					            	 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							         response.setServiceResponse("Timesheet added, but document not saved.");
							         apiLogInfo.setApiResponse("Timesheet added, but document not saved.");
							         apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					            }
					            else {
					            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						            response.setServiceResponse("Timesheet added successfully");

						            apiLogInfo.setApiResponse("Timesheet added successfully");
						            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					            	
					            }
							}
							catch (Exception e) {
						        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						        response.setServiceResponse("Timesheet added, but document save failed due to an error.");

						        apiLogInfo.setApiResponse("Exception while saving document: " + e.getMessage());
						        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

						        e.printStackTrace(); 
						    }
					            
						}
					}
				
//				TimesheetDocumentDetails docData = addTimesheetDocument(timesheetDTO.getDocumentData(),"Update",doc);
//				docData.setTimesheetId(updatedTimesheet.getTimesheetId());
//				docData.setEmpId(timesheetDTO.getEmpId());
////				docData.setDocData(doc.getBytes());
//				
//				if (docData != null) {
//				    try {
//				    	System.out.println(docData);
//				        TimesheetDocumentDetails docu = timesheetDocumentDetailsRepository.save(docData);
//
//				        if (docu == null) {
//				            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				            response.setServiceResponse("Timesheet added, but document not saved.");
//
//				            apiLogInfo.setApiResponse("Timesheet added, but document not saved.");
//				            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				        } else {
//				            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				            response.setServiceResponse("Timesheet added successfully");
//
//				            apiLogInfo.setApiResponse("Timesheet added successfully");
//				            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				        }
//				    } catch (Exception e) {
//				        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				        response.setServiceResponse("Timesheet added, but document save failed due to an error.");
//
//				        apiLogInfo.setApiResponse("Exception while saving document: " + e.getMessage());
//				        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//
//				        e.printStackTrace(); 
//				    }
//				}

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

	public ServiceResponse getMyReportees(TimesheetDTO timesheetDTO) {
		return timesheetQueryService.getMyReportees(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetQueryService
	private ServiceResponse getMyReporteesInternal(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_my_teams_timesheets");
		apiLogInfo.setApiUrl("/api/getMyReportees");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +timesheetDTO.getManagerId()+ " ,startDate : " +timesheetDTO.getStartDate()+ " ,endDate : " +timesheetDTO.getEndDate());
		try {

			List<TimesheetDTO> objectList = timesheetsRepository.getMyReportees(
					timesheetDTO.getManagerId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No reportees found. List is empty.");
					
					apiLogInfo.setApiResponse("Appreciation not Enabled");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(objectList);
					
					apiLogInfo.setApiResponse("dtoList : " +objectList);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No  reportees found. List is null.");
				
				apiLogInfo.setApiResponse("No  reportees found. List is null.");			
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
	
	public ServiceResponse getMyReporteesApprovedTimesheets(TimesheetDTO timesheetDTO) {
		return timesheetQueryService.getMyReporteesApprovedTimesheets(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetQueryService
	private ServiceResponse getMyReporteesApprovedTimesheetsInternal(TimesheetDTO timesheetDTO) {
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

			List<Object[]> objectList = timesheetsRepository.getMyReporteesApprovedTimesheetsOLD(
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
						dto.setIsApmosysProduct(object[17] != null ? object[17].toString() : null);
						
						String employmentId = dto.getEmployeementId() != null ? dto.getEmployeementId().toString() : null;
//					    String isConsultant = timesheetDto.getIsConsultant();
					    String isApmosysProduct = dto.getIsApmosysProduct();

					    if (employmentId != null) {
					        if ("true".equalsIgnoreCase(isApmosysProduct)) {
					        	dto.setEmploymentIdAcToET("AP-" + employmentId);
					        }else {
					        	dto.setEmploymentIdAcToET("A-" + employmentId);
					        }
					    }

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
	
	public ServiceResponse getMyReporteesApprovedTimesheets2(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_my_teams_timesheets_requests");
		apiLogInfo.setApiUrl("/api/getMyReporteesTimesheetRequests");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +timesheetDTO.getManagerId()+ " ,status : " +timesheetDTO.getStatus());
		try {
			
			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());
			
			List<Long> empIds = timesheetDTO.getEmpIds();  
             Employee employeeData = employeeRepository.findByEmpId(timesheetDTO.getManagerId());
             List<Object[]> objectList = timesheetsRepository
					.getMyReporteesApprovedTimesheetRequests2OLD(timesheetDTO.getManagerId(),timesheetDTO.getStatus(), start, end,empIds);
			

			
			
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

						dto.setIsApmosysProduct(object[29] != null ? object[29].toString() : null);		
						
						String employmentId = dto.getEmployeementId() != null ? dto.getEmployeementId().toString() : null;
//					    String isConsultant = timesheetDto.getIsConsultant();
					    String isApmosysProduct = dto.getIsApmosysProduct();

					    if (employmentId != null) {
					        if ("true".equalsIgnoreCase(isApmosysProduct)) {
					        	dto.setEmploymentIdAcToET("AP-" + employmentId);
					        }else {
					        	dto.setEmploymentIdAcToET("A-" + employmentId);
					        }
					    }

						
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
						if(timesheetId != null) {
							 List<TimesheetDocumentDetailsDTO> details =timesheetDocumentDetailsRepository.findAllDocIdByTimesheetId(timesheetId);
							 for (TimesheetDocumentDetailsDTO doc : details) {
							     if (Boolean.TRUE.equals(doc.getFinalFlag())) {
							         dto.setApprovedDocument(doc.getDocId());
							     }
							     if(Boolean.FALSE.equals(doc.getFinalFlag())) {
							    	 dto.setFilledDocument(doc.getDocId());  	 
							     }
							 }

							 
						}
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
	

	public ServiceResponse getLast7DaysTimesheetsByEmpId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation Configuration");
		apiLogInfo.setApiUrl("/api/getLast7DaysTimesheetsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +timesheetDTO.getEmpId());
		try {

			List<Object[]> objectList = timesheetsRepository.getLast7DaysTimesheetsByEmpIdOLD(timesheetDTO.getEmpId(),
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

			List<Object[]> objectList = timesheetsRepository.getTimesheetsForHomePageByEmpIdOLD(timesheetDTO.getEmpId(),
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
		return timesheetApprovalService.revokeApprovedTimesheet(timesheetDTO);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse bulkApproveTimesheetRequest(TimesheetDTO timesheetDTO) {
		return timesheetApprovalService.bulkApproveTimesheetRequest(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetApprovalService
	private ServiceResponse bulkApproveTimesheetRequestInternal(TimesheetDTO timesheetDTO) {
	    ServiceResponse response = new ServiceResponse();
	    List<TimesheetDTO> timesheetList = timesheetDTO.getBulkApprovedList();

	    try {
	        Map<Long, TimesheetDTO> dtoMap = timesheetList.stream()
	                .collect(Collectors.toMap(TimesheetDTO::getTimesheetId, Function.identity()));

	        List<Long> timesheetIds = new ArrayList<>(dtoMap.keySet());
	        List<Timesheet> timesheets = timesheetsRepository.findAllById(timesheetIds);

	        // Update in batch
	        for (Timesheet timesheet : timesheets) {
	            if (timesheet != null) {
	                timesheet.setStatus(timesheetDTO.getStatus());
	                timesheet.setTimesheetStatusUpdatedBy(timesheetDTO.getUpdatedBy());
	                timesheet.setRemarks(timesheetDTO.getRejectReason());
	            }
	        }

	        timesheetsRepository.saveAll(timesheets);

	        List<TimesheetDTO> withClientSideId = timesheetList == null
	                ? Collections.emptyList()
	                : timesheetList.stream()
	                      .filter(t -> t.getClientSideId() != null)
	                      .collect(Collectors.toList());
	        if (!withClientSideId.isEmpty()) {
	            ServiceResponse approvalResp = bulkTimesheetDocumentApproval(withClientSideId);
	            if (!ServiceResponse.STATUS_SUCCESS.equals(approvalResp.getServiceStatus())) {
	                throw new RuntimeException("Bulk Timesheet Document Approval Failed");
	            }
	        }

	        ServiceResponse logsResp = bulkTimesheetDocumentApprovalLogs(timesheetList);
	        if (!ServiceResponse.STATUS_SUCCESS.equals(logsResp.getServiceStatus())) {
	            throw new RuntimeException("Bulk Timesheet Document Approval Logs Failed");
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Bulk Timesheet Approved Successfully!");

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong in bulk approval.");
	        response.setServiceError(e.getMessage());
	    }

	    return response;
	}

	
//	public ServiceResponse bulkApproveTimesheetRequest(TimesheetDTO timesheetDTO) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("BulkApproveTimesheetRequest");
//		apiLogInfo.setApiUrl("/api/bulkApproveTimesheetRequest");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("BulkApprovedList : " + timesheetDTO.getBulkApprovedList().size());
//	
//		try {
//			
//			for (TimesheetDTO timesheet : timesheetDTO.getBulkApprovedList()) {
//				
//				timesheet.setStatus(timesheetDTO.getStatus());
//				timesheet.setTimesheetStatusUpdatedBy(timesheetDTO.getUpdatedBy());
//				response = updateTimesheetRequestById(timesheet);
//			    
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//		
//	}
//	
//	public ServiceResponse bulkRejectTimesheetRequest(TimesheetDTO timesheetDTO) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("BulkRejectTimesheetRequest");
//		apiLogInfo.setApiUrl("/api/bulkRejectTimesheetRequest");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("BulkRejectList : " + timesheetDTO.getBulkRejectList().size());
//		try {
//
//			for (TimesheetDTO timesheet : timesheetDTO.getBulkRejectList()) {
//
//				timesheet.setStatus(timesheetDTO.getStatus());
//				timesheet.setTimesheetStatusUpdatedBy(timesheetDTO.getUpdatedBy());
//				timesheet.setRejectReason(timesheetDTO.getRejectReason());
//				if (timesheetDTO.getRejectionId() != null) {
//					timesheet.setRejectionId(timesheetDTO.getRejectionId());
//				}
//				response = updateTimesheetRequestById(timesheet);
//				System.out.println("   timesheet Reject reason __" + timesheetDTO.getRejectReason());
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//
//	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse bulkRejectTimesheetRequest(TimesheetDTO timesheetDTO) {
		return timesheetApprovalService.bulkRejectTimesheetRequest(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetApprovalService
	private ServiceResponse bulkRejectTimesheetRequestInternal(TimesheetDTO timesheetDTO) {

	    ServiceResponse response = new ServiceResponse();
	    List<TimesheetDTO> rejectList = timesheetDTO.getBulkRejectList();

	    try {
	        Map<Long, TimesheetDTO> dtoMap = rejectList.stream()
	                .collect(Collectors.toMap(TimesheetDTO::getTimesheetId, Function.identity()));

	        List<Long> timesheetIds = new ArrayList<>(dtoMap.keySet());

	        List<Timesheet> timesheets = timesheetsRepository.findAllById(timesheetIds);

	        for (Timesheet t : timesheets) {
	            TimesheetDTO dto = dtoMap.get(t.getTimesheetId());
	            if (dto != null) {
	                t.setStatus(timesheetDTO.getStatus());  // REJECTED
	                t.setTimesheetStatusUpdatedBy(timesheetDTO.getUpdatedBy());
	                t.setRemarks(timesheetDTO.getRejectReason());
	                t.setRejectionId(timesheetDTO.getRejectionId() != null 
	                	    ? timesheetDTO.getRejectionId().longValue() 
	                	    : null);
	            }
	        }

	        timesheetsRepository.saveAll(timesheets);

	        // BULK document approval only for records having clientSideId
	        List<TimesheetDTO> withClientSideId =
	                rejectList == null ? Collections.emptyList()
	                : rejectList.stream()
	                      .filter(t -> t.getClientSideId() != null)
	                      .collect(Collectors.toList());
	        
	        if (!withClientSideId.isEmpty()) {
	            ServiceResponse approvalResp = bulkTimesheetDocumentApproval(withClientSideId);

	            if (!ServiceResponse.STATUS_SUCCESS.equals(approvalResp.getServiceStatus())) {
	                throw new RuntimeException("Bulk Timesheet Document Approval Failed");
	            }
	        }

	        ServiceResponse logsResp = bulkTimesheetDocumentApprovalLogs(rejectList);
	        if (!ServiceResponse.STATUS_SUCCESS.equals(logsResp.getServiceStatus())) {
	            throw new RuntimeException("Bulk Timesheet Document Approval Logs Failed");
	        }

	        for (TimesheetDTO dto : rejectList) {
	            try {
	                mailService.sendMailWithCC(
	                        dto.getEmail(),
	                        dto.getManagerEmail(),
	                        "Regarding Timesheet Request Rejection",
	                        "Dear " + dto.getEmployeeName() + "," +
	                                "<br><br>Your timesheet application has been rejected by " + dto.getManagerName() + "." +
	                                "<br><br><b>Timesheet Details:</b>" +
	                                "<br>EmpID: " + dto.getEmployeementId() +
	                                "<br>Name: " + dto.getEmployeeName() +
	                                "<br>Date: " + dto.getDate() +
	                                "<br>Day Type: " + dto.getDayType() +
	                                "<br>Total Working Hours: " + dto.getTotalWorkingOfficeHours() + " (hrs)" +
	                                "<br><br><b>Rejection Reason:</b> " + dto.getRejectReason()
	                );
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Bulk Timesheets Rejected Successfully!");

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong in bulk rejection.");
	        response.setServiceError(e.getMessage());
	    }

	    return response;
	}


	public ServiceResponse getAllLeaveTimesheetsWithoutLeaveApplication(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllLeaveTimesheetsWithoutLeaveApplication");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("startDate: ").append(timesheetDTO.getStartDate())
				.append(", endDate: ").append(timesheetDTO.getEndDate())
				.append(", page: ").append(timesheetDTO.getPage())
				.append(", size: ").append(timesheetDTO.getSize())
				.append(", sortBy: ").append(timesheetDTO.getSortByForTimesheetLeaveReport())
				.append(", sortDirection: ").append(timesheetDTO.getSortDirection());

		try {
			if (timesheetDTO.getStartDate() == null || timesheetDTO.getEndDate() == null) {
				throw new IllegalArgumentException("Start date and End date are required.");
			}
			if (timesheetDTO.getCurrentUser() == null) {
				throw new IllegalArgumentException("Current user ID is required.");
			}

			LocalDate start;
			LocalDate end;
			try {
				start = LocalDate.parse(timesheetDTO.getStartDate());
				end = LocalDate.parse(timesheetDTO.getEndDate());
			} catch (DateTimeParseException ex) {
				throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd");
			}

			int page = (timesheetDTO.getPage() != null) ? timesheetDTO.getPage() : 0;
			int size = (timesheetDTO.getSize() != null) ? timesheetDTO.getSize() : 10;

			List<String> sortBy = (timesheetDTO.getSortByForTimesheetLeaveReport() != null
					&& !timesheetDTO.getSortByForTimesheetLeaveReport().isEmpty())
							? timesheetDTO.getSortByForTimesheetLeaveReport()
							: Collections.singletonList("date");

			Sort.Direction sortDir = "desc".equalsIgnoreCase(timesheetDTO.getSortDirection())
					? Sort.Direction.DESC
					: Sort.Direction.ASC;

			Pageable pageable = null;
			if (Boolean.TRUE.equals(timesheetDTO.getExportAll())) {
			    pageable = Pageable.unpaged();
			} else {
			    pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy.toArray(new String[0])));
			}

			Map<String, String> filters = (timesheetDTO.getFilters() != null)
					? timesheetDTO.getFilters()
					: new HashMap<>();

			Long empId = null;
			String empIdStr = null;

			if (filters.containsKey("employmentIdAcToET") && !filters.get("employmentIdAcToET").isEmpty()) {
			    String empValue = filters.get("employmentIdAcToET").trim();

			    if (empValue.matches("\\d{6,}")) {
			        empId = Long.parseLong(empValue);
			    } else {
			        empIdStr = "%" + empValue.replaceAll("[^0-9]", "") + "%";
			    }
			}


			String empName = filters.getOrDefault("employeeName", null);
			String dayType = filters.getOrDefault("dayType", null);
			String status = filters.getOrDefault("status", null);
			String managerName = filters.getOrDefault("managerName", null);
			String departmentName = filters.getOrDefault("departmentName", null);
			String updatedBy = filters.getOrDefault("updatedBy", null);

			String dateStr = filters.getOrDefault("date", null);
			String createdOnStr = filters.getOrDefault("createdOn", null);
			String updatedOnStr = filters.getOrDefault("updatedOn", null);

			LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? parseFlexibleLocalDate(dateStr) : null;
			java.sql.Date createdOn = safeParseSqlDate(filters.get("createdOn"));
			java.sql.Date updatedOn = safeParseSqlDate(filters.get("updatedOn"));

			Employee employee = employeeRepository.findByEmpId(timesheetDTO.getCurrentUser());
			if (employee == null) {
				throw new IllegalArgumentException("Employee not found for ID: " + timesheetDTO.getCurrentUser());
			}

			JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
			if (jobRole == null) {
				throw new IllegalArgumentException("JobRole not found for employee: " + employee.getEmpId());
			}

			String role = jobRole.getEmployeeRole();
			String roleName = jobRole.getName();

			Page<TimesheetDTO> timesheetPage;

			if ("SuperAdmin".equalsIgnoreCase(role) ||
					"Director".equalsIgnoreCase(roleName) ||
					"Super Admin".equalsIgnoreCase(roleName)) {

				timesheetPage = timesheetsRepository.findAllLeaveTimesheetsWithoutLeaveApplication(
						start, end, empName, empId, date, dayType, status, managerName, departmentName,
						createdOn, updatedOn, updatedBy, dateStr, createdOnStr, updatedOnStr,empIdStr, pageable);

			} else if (departmentRepository.existsByHodId(timesheetDTO.getCurrentUser())) {
				List<Long> deptIds = departmentRepository.findDeptIdsByHodId(timesheetDTO.getCurrentUser());
				timesheetPage = timesheetsRepository.getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWise(
						start, end, empName, empId, date, dayType, status, managerName, departmentName,
						createdOn, updatedOn, updatedBy, deptIds, empIdStr,pageable);
			} else {
				Long deptId = departmentRepository.findDepartmentofCurrentuser(employee.getJobRoleId());
				timesheetPage = timesheetsRepository.getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise(
						start, end, empName, empId, date, dayType, status, managerName, departmentName,
						createdOn, updatedOn, updatedBy, deptId, dateStr, createdOnStr, updatedOnStr,empIdStr, pageable);
			}

			if (timesheetPage == null || timesheetPage.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No timesheets found for the given filters.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("No results found");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(timesheetPage);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("Fetched " + timesheetPage.getTotalElements() + " records");
			}

		} catch (IllegalArgumentException ex) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ex.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("WARN");

		} catch (DataAccessException ex) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Database access error occurred.");
			response.setServiceError(ex.getMostSpecificCause().getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

		} catch (Exception ex) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Unexpected error occurred while fetching timesheets.");
			response.setServiceError(ex.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	/* helper for date parse */
	private java.sql.Date safeParseSqlDate(String dateStr) {
		LocalDate localDate = parseFlexibleLocalDate(dateStr);
		return (localDate != null) ? java.sql.Date.valueOf(localDate) : null;
	}

	/* helper for date parse */
	private LocalDate parseFlexibleLocalDate(String dateStr) {
		if (dateStr == null || dateStr.trim().isEmpty())
			return null;

		dateStr = dateStr.trim().replace("/", "-");
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		try {
			return LocalDate.parse(dateStr, formatter1);
		} catch (Exception e1) {
			try {
				return LocalDate.parse(dateStr, formatter2);
			} catch (Exception e2) {
				System.err.println("Invalid date format: " + dateStr);
				return null;
			}
		}
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
		return timesheetQueryService.getAllOrDeptWiseEmployeeTimesheetReport(filteredTimesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetQueryService
	private ServiceResponse getAllOrDeptWiseEmployeeTimesheetReportInternal(FilteredTimesheetDTO filteredTimesheetDTO) {
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

	public ServiceResponse getProjectListForDateAndEmpId(GetProjectListForDateAndEmpIdPayload payload) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getProjectListForDateAndEmpId");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
	    	Long empId = payload.getEmpId();
	    	
	    	LocalDateTime selectedDateTime = payload.getDate();

	    	LocalDate selectedDate = selectedDateTime.toLocalDate();

	    	LocalDateTime startOfDay = selectedDate.atStartOfDay();
	    	LocalDateTime endOfDay   = selectedDate.atTime(LocalTime.MAX);

	        List<ProjectNameAndPrjoectIdDTO> activeProjectList = timesheetsRepository.getProjectListForDateAndEmpId(empId, startOfDay, endOfDay);
	        
	        if (activeProjectList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Please contact to the RMG team to provide project mapping!");
	            response.setServiceMessage("Employee has no project mapping ! For EmpId: " + empId);
	            
	            apiLogInfo.setApiResponse("Employee has no project mapping ! For EmpId: " + empId);
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
	    	Optional<String> clientSideId = employeeClientSideIdMappingRepository.findClientSideIdByProjectId(projectId);
	        
	    	if (clientSideId.isEmpty() || clientSideId.get().trim().isEmpty()) {
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

	        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        apiLogInfo.setApiResponse(e.getMessage()); 
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	public ServiceResponse fetchEmploymentIdByEmpId(Long empId) {
		return timesheetQueryService.fetchEmploymentIdByEmpId(empId);
	}
	
	// Delegate method - implementation moved to TimesheetQueryService
	private ServiceResponse fetchEmploymentIdByEmpIdInternal(Long empId) {
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
		return timesheetDocumentService.addTimesheetDocument(timesheetDocumentDetailsDTO, oprType, doc);
	}
	
	// Delegate method - implementation moved to TimesheetDocumentService
	private TimesheetDocumentDetails addTimesheetDocumentInternal(TimesheetDocumentDetailsDTO timesheetDocumentDetailsDTO, String oprType, MultipartFile doc) throws IOException {
		TimesheetDocumentDetails data = new TimesheetDocumentDetails();
		if("Create".equalsIgnoreCase(oprType)) {
			timesheetDocumentDetailsDTO.setActive(true);
			timesheetDocumentDetailsDTO.setCreatedOn(LocalDateTime.now());
		}
		else if("Update".equalsIgnoreCase(oprType) && Boolean.TRUE.equals(timesheetDocumentDetailsDTO.getFinalFlag())  ) {
			data = timesheetDocumentDetailsRepository.findByDocIdAndFinalFlag(timesheetDocumentDetailsDTO.getDocId(),true);
//			data = timesheetDocumentDetailsRepository.findByDocIdAndActive(timesheetDocumentDetailsDTO.getDocId(),true);
//			
			if(data == null) {
				data =  buildNewDoc(data, timesheetDocumentDetailsDTO, doc);
				
//				data = new TimesheetDocumentDetails();
//				timesheetDocumentDetailsDTO.setActive(true);
//				timesheetDocumentDetailsDTO.setCreatedOn(LocalDateTime.now());
//				if(doc != null) timesheetDocumentDetailsDTO.setDocFile(doc);
//				else throw new DataIntegrityViolationException("No Document found...!!");
//				data.setDocName(timesheetDocumentDetailsDTO.getDocName());
//				data.setDocData(timesheetDocumentDetailsDTO.getDocFile().getBytes());
//				data.setTimesheetId(timesheetDocumentDetailsDTO.getTimesheetId());
//				data.setEmpId(timesheetDocumentDetailsDTO.getTimesheetId());
//				if(timesheetDocumentDetailsDTO.getCreatedOn() != null)
//					data.setCreatedOn(timesheetDocumentDetailsDTO.getCreatedOn());
//				if(timesheetDocumentDetailsDTO.getCreatedBy() != null)
//					data.setCreatedBy(timesheetDocumentDetailsDTO.getCreatedBy());
//				if(timesheetDocumentDetailsDTO.getUpdatedBy() != null)
//					data.setUpdatedBy(timesheetDocumentDetailsDTO.getUpdatedBy());
//				if(timesheetDocumentDetailsDTO.getUpdatedOn() != null)
//					data.setUpdatedOn(timesheetDocumentDetailsDTO.getUpdatedOn());
//				data.setClientApprovalStatus(timesheetDocumentDetailsDTO.getClientApprovalStatus());
//			    data.setRmApprovalStatus("Pending");
//			    data.setFinalFlag(timesheetDocumentDetailsDTO.getFinalFlag());
//			    data.setDocMimeType(timesheetDocumentDetailsDTO.getDocFile().getContentType());
//			    data.setActive(true);
			}
			timesheetDocumentDetailsDTO.setUpdatedOn(LocalDateTime.now());
//			timesheetDocumentDetailsDTO.setBulkApprovedDocId(finalDocument.getDocId());
		}else if("Update".equalsIgnoreCase(oprType) && Boolean.FALSE.equals(timesheetDocumentDetailsDTO.getFinalFlag()) ) {
			data = timesheetDocumentDetailsRepository.findByDocIdAndFinalFlag(timesheetDocumentDetailsDTO.getDocId(),false);
//			data = timesheetDocumentDetailsRepository.findByDocIdAndActive(timesheetDocumentDetailsDTO.getDocId(),true);
			if(data == null) {
				data =  buildNewDoc(data, timesheetDocumentDetailsDTO, doc);
//				data = new TimesheetDocumentDetails();
//				timesheetDocumentDetailsDTO.setActive(true);
//				timesheetDocumentDetailsDTO.setCreatedOn(LocalDateTime.now());
//				if(doc != null) timesheetDocumentDetailsDTO.setDocFile(doc);
//				else throw new DataIntegrityViolationException("No Document found...!!");
//				data.setDocName(timesheetDocumentDetailsDTO.getDocName());
//				data.setDocData(timesheetDocumentDetailsDTO.getDocFile().getBytes());
//				data.setTimesheetId(timesheetDocumentDetailsDTO.getTimesheetId());
//				data.setEmpId(timesheetDocumentDetailsDTO.getTimesheetId());
//				if(timesheetDocumentDetailsDTO.getCreatedOn() != null)
//					data.setCreatedOn(timesheetDocumentDetailsDTO.getCreatedOn());
//				if(timesheetDocumentDetailsDTO.getCreatedBy() != null)
//					data.setCreatedBy(timesheetDocumentDetailsDTO.getCreatedBy());
//				if(timesheetDocumentDetailsDTO.getUpdatedBy() != null)
//					data.setUpdatedBy(timesheetDocumentDetailsDTO.getUpdatedBy());
//				if(timesheetDocumentDetailsDTO.getUpdatedOn() != null)
//					data.setUpdatedOn(timesheetDocumentDetailsDTO.getUpdatedOn());
//				data.setClientApprovalStatus(timesheetDocumentDetailsDTO.getClientApprovalStatus());
//			    data.setRmApprovalStatus("Pending");
//			    data.setFinalFlag(timesheetDocumentDetailsDTO.getFinalFlag());
//			    data.setDocMimeType(timesheetDocumentDetailsDTO.getDocFile().getContentType());
//			    data.setActive(true);
			}
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
	    data.setBulkApprovedDocId(timesheetDocumentDetailsDTO.getBulkApprovedDocId());
	    return data;
		
	}
	
	
	
	private TimesheetDocumentDetails buildNewDoc(TimesheetDocumentDetails data,TimesheetDocumentDetailsDTO timesheetDocumentDetailsDTO,MultipartFile doc) throws IOException {
		data = new TimesheetDocumentDetails();
		timesheetDocumentDetailsDTO.setActive(true);
		timesheetDocumentDetailsDTO.setCreatedOn(LocalDateTime.now());
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
		return timesheetDocumentService.fetchTimesheetDocument(docId, timesheetId);
	}
	
	@Transactional
	public ServiceResponse updateClientSideIdMapping(EmployeeClientSideIdMappingDTO empClientDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("updateClientSideIdMapping");
		apiLogInfo.setApiUrl("/api/updateClientSideIdMapping");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +empClientDTO.getEmpId()+ "projectId:" +empClientDTO.getProjectId());
		try {

			Optional<EmployeeClientSideIdMapping> existingEmpClientMap = employeeClientSideIdMappingRepository.findByProjectIdAndActiveAndEmpId(empClientDTO.getProjectId(), true, empClientDTO.getEmpId());
	        
			if (!existingEmpClientMap.isPresent()) {

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

		        apiLogInfo.setApiRequest(logBuilder.toString());
        		logService.logMyInfo(httpRequest, apiLogInfo);
        		return response;

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
	            apiLogInfo.setApiRequest(logBuilder.toString());
        		logService.logMyInfo(httpRequest, apiLogInfo);
        		return response;
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
	
	public ServiceResponse getActiveProjectsAndClientSideIdByEmpId(GetProjectListForDateAndEmpIdPayload payload) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getActiveProjectsByEmpId");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
	    	if(payload == null) {
	    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Unable to process the request!");
	            response.setServiceMessage("Payload recieved is null!");
	            
	            apiLogInfo.setApiResponse("Payload recieved is null!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	    	}
	    	
	    	Long empId = payload.getEmpId();
	    	Integer projectId = Integer.parseInt(payload.getProjectId().toString()); 	
	    	LocalDate selectedDate = payload.getDate().toLocalDate();

	    	LocalDateTime startOfDay = selectedDate.atStartOfDay();
	    	LocalDateTime startOfNextDay = selectedDate.plusDays(1).atStartOfDay();

	    	List<ProjectClientSideIdDTO> activeProjectList = timesheetsRepository.getActiveProjectsAndClientSideIdByEmpId(
	    	                empId, startOfDay, startOfNextDay, projectId);
	    	
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
	    	Optional<String> clientSideId = employeeClientSideIdMappingRepository.getClientSideIdByProjectIdAndEmpId(projectId,empId);
	        
	        if (clientSideId.isPresent()) {
	        	

	        	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(clientSideId);
	            response.setServiceMessage("Client Side Id fetched successfully!");
	    		logBuilder.append("empId : " +empId+ "Client Side Id :" +clientSideId+"\n");

	            apiLogInfo.setApiResponse("Client Side Id fetched successfully!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        	
	        } else {

	        	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No Client Side Id fetched for the selected Project!");
	            response.setServiceMessage("No Client Side Id fetched for the selected Project! For ProjectId: " + projectId);
	            
	            apiLogInfo.setApiResponse("No Client Side Id fetched for the selected Project! For ProjectId: " + projectId);
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

	        }
	        
            logService.logMyInfo(httpRequest, apiLogInfo);
            return response;
	        
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



	public ServiceResponse getTimesheetForEmployee(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			int page = timesheetDTO.getPage(); 
			int pageSize = timesheetDTO.getSize();
			int offset = (page-1) * pageSize; 

			List<Object[]> records = timesheetsRepository.findByEmpIdAndDateBetween(timesheetDTO.getEmpId(), timesheetDTO.getFromDate(), timesheetDTO.getToDate(),offset,pageSize);
			Integer totalItems = ((BigInteger) entityManager.createNativeQuery("SELECT FOUND_ROWS()").getSingleResult()).intValue();
			

			if(records == null || records.isEmpty()) {	
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Timesheet Details Found");
				response.setServiceMessage("No Timesheet Details Found");
				return response;
			}else{
				List<TimesheetDTO> employeeTimesheetsByEmployee = new ArrayList<TimesheetDTO>();
				for(Object[] record: records) {
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
					Long timesheetId = record[10] != null ? Long.valueOf(record[10].toString()) : null;
					if(timesheetId != null) {
							List<TimesheetDocumentDetailsDTO> details =timesheetDocumentDetailsRepository.findAllDocIdByTimesheetId(timesheetId);
							for (TimesheetDocumentDetailsDTO doc : details) {
								if (Boolean.TRUE.equals(doc.getFinalFlag())) {
									dto.setApprovedDocument(doc.getDocId());
								}
								if(Boolean.FALSE.equals(doc.getFinalFlag())) {
									dto.setFilledDocument(doc.getDocId());  	 
								}
							}
		
					}
					employeeTimesheetsByEmployee.add(dto);
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(employeeTimesheetsByEmployee);
					response.setServiceMessage("Timesheet details successfully fetched.");
					response.setTotalElements(totalItems);
				}	
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
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
	
	public ServiceResponse getFinalDocumentDataByDocId(Long timesheetId, Long docId) {

	    ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getFinalDocumentDataByDocId");
	    apiLogInfo.setLogLevel("INFO");
	    apiLogInfo.setApiRequest("timesheetId: " + timesheetId + ", docId: " + docId);

	    try {
	        List<Long> docIds = timesheetDocumentDetailsRepository.findDocIdsByTimesheetId(timesheetId);

	        if (docIds == null || docIds.isEmpty()) {

	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No documents mapped to this timesheet!");
	            response.setServiceMessage("No documents mapped to this timesheet for ID: " + timesheetId);

	            apiLogInfo.setLogLevel("ERROR");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("No documents mapped to this timesheet for ID: " + timesheetId);

	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        Object documentResponse;

	        // Case 1: When this timesheet had legacy entries → fetch from old table using finalFlag
	        if (docIds.size() > 1) {
	            TimesheetDocumentDetails docDetails =
	                    timesheetDocumentDetailsRepository.findByDocIdAndFinalFlag(docId, true);

	            if (docDetails == null) {

	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Final flagged document not found.");
	                response.setServiceMessage("Final flagged document not found for docId: " + docId);

	                apiLogInfo.setLogLevel("ERROR");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                apiLogInfo.setApiResponse("Final flagged document not found for docId: " + docId);

	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            }

	            documentResponse = docDetails;
	        }

	        // Case 2: New implementation → fetch from FinalDocument table
	        else {
	            FinalDocument finalDoc =
	                    finalDocumentRepository.findById(docId).orElse(null);

	            if (finalDoc == null) {

	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("FinalDocument not found.");
	                response.setServiceMessage("FinalDocument not found for docId: " + docId);

	                apiLogInfo.setLogLevel("ERROR");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                apiLogInfo.setApiResponse("FinalDocument not found for docId: " + docId);

	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            }

	            documentResponse = finalDoc;
	        }

	        // Success response
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(documentResponse);

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Document details fetched successfully");

	    } catch (Exception e) {

	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage());
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
			 Project projObj = projectRepository.findByProjectId(projectId);
			 
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
				 else {
					 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		             response.setServiceResponse("Project does not has ");
		             apiLogInfo.setApiResponse("No such project found in the system !");
		             apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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
	
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse bulkTimesheetDocumentApproval(List<TimesheetDTO> timesheetDTOList) {
		return timesheetApprovalService.bulkTimesheetDocumentApproval(timesheetDTOList);
	}
	
	// Delegate method - implementation moved to TimesheetApprovalService
	private ServiceResponse bulkTimesheetDocumentApprovalInternal(List<TimesheetDTO> timesheetDTOList) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	    	
	        for (TimesheetDTO timesheetDTO : timesheetDTOList) {
	            Optional<EmployeeDTO> empDetails =
	                    employeeRepository.findEmployeeReportingManagerIdAndHODIdDetailsByEmpId(
	                            timesheetDTO.getEmpId());
	            if (empDetails.isEmpty()) {
	                continue; 
	            }
	            EmployeeDTO empdto = empDetails.get();
	            timesheetDTO.setRmId(empdto.getReportingManagerId());
	            timesheetDTO.setHodId(empdto.getHodId());
	            timesheetDTO.setManagerId(empdto.getManagerId());
	        }
	    	
	    	
	        List<Long> timesheetIds =
	                timesheetDTOList == null ? Collections.emptyList()
	                : timesheetDTOList.stream()
	                      .map(TimesheetDTO::getTimesheetId)
	                      .filter(Objects::nonNull)
	                      .collect(Collectors.toList());
	        
	        List<TimesheetDocumentApproval> existingApprovals =
	                timesheetDocumentApprovalRepository.findAllByTimesheetIdIn(timesheetIds);
	        Map<Long, TimesheetDocumentApproval> approvalMap = existingApprovals.stream()
	                .collect(Collectors.toMap(TimesheetDocumentApproval::getTimesheetId, Function.identity()));
	        List<TimesheetDocumentApproval> approvalsToSave = new ArrayList<>();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	        for (TimesheetDTO dto : timesheetDTOList) {
	            TimesheetDocumentApproval approval = approvalMap.getOrDefault(dto.getTimesheetId(), new TimesheetDocumentApproval());
	            approval.setTimesheetId(dto.getTimesheetId());
	            approval.setApproverId(dto.getUpdatedBy());
	            approval.setApprovalStatus("Approved");
	            
//	            approval.setPreviousLevelId(approval.getLevelId());
//	            approval.setPreviousApproverId(approval.getApproverId());
//	            // Hierarchy logic
//	            setHierarchyAndLevel(approval, dto);
	            // Created/Updated fields
	            if (approval.getCreatedOn() == null) {
	                approval.setCreatedOn(LocalDateTime.parse(dto.getCreatedOn(), formatter));
	                approval.setCreatedBy(dto.getEmpId());
	            }
	            approval.setUpdatedBy(dto.getUpdatedBy());
	            approval.setUpdatedOn(LocalDateTime.now());
	            approvalsToSave.add(approval);
	        }
	        timesheetDocumentApprovalRepository.saveAll(approvalsToSave);
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Timesheet Document Approvals processed successfully.");
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong in document approval.");
	        response.setServiceError(e.getMessage());
	    }

	    return response;
	}


	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse bulkTimesheetDocumentApprovalLogs(List<TimesheetDTO> timesheetDTOList) {
		return timesheetApprovalService.bulkTimesheetDocumentApprovalLogs(timesheetDTOList);
	}
	
	// Delegate method - implementation moved to TimesheetApprovalService
	private ServiceResponse bulkTimesheetDocumentApprovalLogsInternal(List<TimesheetDTO> timesheetDTOList) {
	    ServiceResponse response = new ServiceResponse();

	    try {
	    	List<Long> timesheetIds =
	    	        timesheetDTOList == null ? Collections.emptyList()
	    	        : timesheetDTOList.stream()
	    	              .map(TimesheetDTO::getTimesheetId)
	    	              .filter(Objects::nonNull)
	    	              .collect(Collectors.toList());
	    	
	        List<TimesheetDocumentApproval> existingApprovals =
	                timesheetDocumentApprovalRepository.findAllByTimesheetIdIn(timesheetIds);
	        Map<Long, TimesheetDocumentApproval> approvalMap = existingApprovals.stream()
	                .collect(Collectors.toMap(TimesheetDocumentApproval::getTimesheetId, Function.identity()));
	        List<TimesheetApprovalAllocationLogs> logsToSave = new ArrayList<>();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	        for (TimesheetDTO dto : timesheetDTOList) {
	            TimesheetDocumentApproval approval = approvalMap.get(dto.getTimesheetId());
	            TimesheetApprovalAllocationLogs log = new TimesheetApprovalAllocationLogs();
	            log.setTimesheetId(dto.getTimesheetId());
	            if (approval != null) {
	                log.setPreviousLevelId(approval.getPreviousLevelId());
	                log.setPreviousApproverId(approval.getPreviousApproverId());
	            }
	            log.setApproverId(dto.getUpdatedBy());
	            log.setApprovalStatus(dto.getStatus());
	            log.setAllocId(dto.getAllocId());
	            log.setCreatedOn(LocalDateTime.parse(dto.getCreatedOn(), formatter));
	            log.setCreatedBy(dto.getEmpId());
	            // Hierarchy logic
//	            setHierarchyAndLevel(log, dto);
	            logsToSave.add(log);
	        }
	        timesheetApprovalAllocationLogsRepository.saveAll(logsToSave);
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Timesheet Approval Logs processed successfully.");
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong in approval logs.");
	        response.setServiceError(e.getMessage());
	    }
	    return response;
	}
	
	private void setHierarchyAndLevel(Object entity, TimesheetDTO dto) {
	    Integer levelId = 2;
	    Integer hierarchyOrder = 3;
	    Integer rejectedHierarchyOrder = null;
	    Integer rejectionLevel = null;

	    if (dto.getRejectionId() != null) {
	        rejectionLevel = 1;
	        if (dto.getHodId() != null && dto.getHodId().equals(dto.getUpdatedBy())) {
	            rejectedHierarchyOrder = 2;
	            hierarchyOrder = 2;
	            levelId = 1;
	        } else if (dto.getRmId() != null && dto.getRmId().equals(dto.getUpdatedBy())) {
	            rejectedHierarchyOrder = 1;
	            hierarchyOrder = 1;
	            levelId = 1;
	        } else if (dto.getManagerId() != null && dto.getManagerId().equals(dto.getUpdatedBy())) {
	            rejectedHierarchyOrder = 1;
	            hierarchyOrder = 1;
	            levelId = 1;
	        } else {
	            rejectedHierarchyOrder = 3;
	            rejectionLevel = 2;
	            hierarchyOrder = 3;
	            levelId = 2;
	        }
	    } else {
	        if (dto.getHodId() != null && dto.getHodId().equals(dto.getUpdatedBy())) {
	            levelId = 1;
	            hierarchyOrder = 2;
	        } else if (dto.getRmId() != null && dto.getRmId().equals(dto.getUpdatedBy())) {
	            levelId = 1;
	            hierarchyOrder = 1;
	        } else if (dto.getManagerId() != null && dto.getManagerId().equals(dto.getUpdatedBy())) {
	            levelId = 1;
	            hierarchyOrder = 1;
	        }
	    }

	    if (entity instanceof TimesheetDocumentApproval) {
	        TimesheetDocumentApproval approval = (TimesheetDocumentApproval) entity;
	        approval.setLevelId(levelId);
	        approval.setHierarchyOrder(hierarchyOrder);
	        if (rejectedHierarchyOrder != null) approval.setRejectedhierarchyOrder(rejectedHierarchyOrder);
	        if (rejectionLevel != null) approval.setRejectionLevel(rejectionLevel);
	    } else if (entity instanceof TimesheetApprovalAllocationLogs) {
	        TimesheetApprovalAllocationLogs log = (TimesheetApprovalAllocationLogs) entity;
	        log.setLevelId(levelId);
	        log.setHierarchyOrder(hierarchyOrder);
	        if (rejectedHierarchyOrder != null) log.setRejectedhierarchyOrder(rejectedHierarchyOrder);
	        if (rejectionLevel != null) log.setRejectionLevel(rejectionLevel);
	    }

	}

	
//	@Transactional(rollbackFor = Exception.class)
//	public ServiceResponse timesheetDocumentApproval(TimesheetDTO timesheetDTO) {
//         ServiceResponse response = new ServiceResponse();
//		
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("timesheetDocumentApproval");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId());
//		try {
//			
//			TimesheetDocumentApproval dbResponse = null;
//			TimesheetDocumentApproval timesheetDetailsForDocumentApproval =  timesheetDocumentApprovalRepository.findByTimesheetId(timesheetDTO.getTimesheetId());
//			if(timesheetDetailsForDocumentApproval == null) {
//				TimesheetDocumentApproval timesheetDetails = new TimesheetDocumentApproval();
//				timesheetDetails.setTimesheetId(timesheetDTO.getTimesheetId());
//				timesheetDetails.setApproverId(timesheetDTO.getTimesheetStatusUpdatedBy());	
//				timesheetDetails.setApprovalStatus(timesheetDTO.getStatus());
//				timesheetDetails.setPreviousLevelId(timesheetDTO.getPreviousLevelId());
//				timesheetDetails.setPreviousApproverId(timesheetDTO.getPreviousApproverId());
//				if (timesheetDTO.getRejectionId() != null) {
//				    timesheetDetails.setRejectionId(timesheetDTO.getRejectionId());
//
//				   
//				    if (timesheetDTO.getHodId() != null
//				            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//				            && timesheetDTO.getHodId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//				        timesheetDetails.setRejectedhierarchyOrder(2);
//				        timesheetDetails.setRejectionLevel(1);
//				        timesheetDetails.setHierarchyOrder(2);
//				        timesheetDetails.setLevelId(1);
//
//				 
//				    } else if (timesheetDTO.getRmId() != null
//				            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//				            && timesheetDTO.getRmId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//				        timesheetDetails.setRejectedhierarchyOrder(1);
//				        timesheetDetails.setRejectionLevel(1);
//				        timesheetDetails.setHierarchyOrder(1);
//				        timesheetDetails.setLevelId(1);
//
//				   
//				    }else if (timesheetDTO.getManagerId() != null
//				            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//				            && timesheetDTO.getManagerId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())
//				            && !timesheetDTO.getManagerId().equals(timesheetDTO.getHodId())
//				            && !timesheetDTO.getManagerId().equals(timesheetDTO.getRmId())) {
//
//				        // Manager case (only if not same as HOD or RM)
//				        timesheetDetails.setRejectedhierarchyOrder(1);
//				        timesheetDetails.setRejectionLevel(1);
//				        timesheetDetails.setHierarchyOrder(1);
//				        timesheetDetails.setLevelId(1);
//
//				    } else {
//				        timesheetDetails.setRejectedhierarchyOrder(3);
//				        timesheetDetails.setRejectionLevel(2);
//				        timesheetDetails.setHierarchyOrder(3);
//				        timesheetDetails.setLevelId(2);
//				    }
//				}
//				else {
//					  if (timesheetDTO.getHodId() != null
//					            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//					            && timesheetDTO.getHodId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//					        timesheetDetails.setLevelId(1);
//					        timesheetDetails.setHierarchyOrder(2);
//
//					    
//					    } else if (timesheetDTO.getRmId() != null
//					            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//					            && timesheetDTO.getRmId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//					    	 timesheetDetails.setLevelId(1);
//						        timesheetDetails.setHierarchyOrder(1);
//
//					    
//					    } else if (timesheetDTO.getManagerId() != null
//					            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//					            && timesheetDTO.getManagerId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())
//					            && !timesheetDTO.getManagerId().equals(timesheetDTO.getHodId())
//					            && !timesheetDTO.getManagerId().equals(timesheetDTO.getRmId())) {
//
//					        timesheetDetails.setHierarchyOrder(1);
//					        timesheetDetails.setLevelId(1);
//
//					    }else {
//					    	 timesheetDetails.setLevelId(2);
//						        timesheetDetails.setHierarchyOrder(3);
//					    }
//				}
//
//				timesheetDetails.setCreatedBy(timesheetDTO.getEmpId());
//				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
//				LocalDateTime createdOn = LocalDateTime.parse(timesheetDTO.getCreatedOn(), formatter);
//				timesheetDetails.setCreatedOn(createdOn);
//				
//				dbResponse = timesheetDocumentApprovalRepository.save(timesheetDetails);
//			}else {
//				
//				timesheetDetailsForDocumentApproval.setPreviousLevelId(timesheetDetailsForDocumentApproval.getLevelId());
//				timesheetDetailsForDocumentApproval.setPreviousApproverId(timesheetDetailsForDocumentApproval.getApproverId());
//				timesheetDetailsForDocumentApproval.setApproverId(timesheetDTO.getTimesheetStatusUpdatedBy());	
//				timesheetDetailsForDocumentApproval.setApprovalStatus(timesheetDTO.getStatus());
//
//				if (timesheetDTO.getRejectionId() != null) {
//					timesheetDetailsForDocumentApproval.setRejectionId(timesheetDTO.getRejectionId());
//
//				    
//				    if (timesheetDTO.getHodId() != null
//				            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//				            && timesheetDTO.getHodId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//				    	timesheetDetailsForDocumentApproval.setRejectedhierarchyOrder(2);
//				    	timesheetDetailsForDocumentApproval.setRejectionLevel(1);
//				    	timesheetDetailsForDocumentApproval.setHierarchyOrder(2);
//				    	timesheetDetailsForDocumentApproval.setLevelId(1);
//
//				    
//				    } else if (timesheetDTO.getRmId() != null
//				            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//				            && timesheetDTO.getRmId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//				    	timesheetDetailsForDocumentApproval.setRejectedhierarchyOrder(1);
//				    	timesheetDetailsForDocumentApproval.setRejectionLevel(1);
//				    	timesheetDetailsForDocumentApproval.setHierarchyOrder(1);
//				    	timesheetDetailsForDocumentApproval.setLevelId(1);
//
//				   
//				    }else if (timesheetDTO.getManagerId() != null
//				            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//				            && timesheetDTO.getManagerId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())
//				            && !timesheetDTO.getManagerId().equals(timesheetDTO.getHodId())
//				            && !timesheetDTO.getManagerId().equals(timesheetDTO.getRmId())) {
//				    	
//				    	timesheetDetailsForDocumentApproval.setRejectedhierarchyOrder(1);
//				    	timesheetDetailsForDocumentApproval.setRejectionLevel(1);
//				    	timesheetDetailsForDocumentApproval.setHierarchyOrder(1);
//				        timesheetDetailsForDocumentApproval.setLevelId(1);
//
//				    } else {
//				    	timesheetDetailsForDocumentApproval.setRejectedhierarchyOrder(3);
//				    	timesheetDetailsForDocumentApproval.setRejectionLevel(2);
//				    	timesheetDetailsForDocumentApproval.setHierarchyOrder(3);
//				    	timesheetDetailsForDocumentApproval.setLevelId(2);
//				    }
//				}
//				else {
//					  if (timesheetDTO.getHodId() != null
//					            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//					            && timesheetDTO.getHodId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//						  timesheetDetailsForDocumentApproval.setLevelId(1);
//						  timesheetDetailsForDocumentApproval.setHierarchyOrder(2);
//
//					    
//					    } else if (timesheetDTO.getRmId() != null
//					            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//					            && timesheetDTO.getRmId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//					    	timesheetDetailsForDocumentApproval.setLevelId(1);
//					    	timesheetDetailsForDocumentApproval.setHierarchyOrder(1);
//
//					    
//					    }else if (timesheetDTO.getManagerId() != null
//					            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//					            && timesheetDTO.getManagerId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())
//					            && !timesheetDTO.getManagerId().equals(timesheetDTO.getHodId())
//					            && !timesheetDTO.getManagerId().equals(timesheetDTO.getRmId())) {
//					    	
//					    	timesheetDetailsForDocumentApproval.setHierarchyOrder(1);
//					        timesheetDetailsForDocumentApproval.setLevelId(1);
//
//					    } else {
//					    	timesheetDetailsForDocumentApproval.setLevelId(2);
//					    	 timesheetDetailsForDocumentApproval.setHierarchyOrder(3);
//					    }
//				}
//				timesheetDetailsForDocumentApproval.setUpdatedBy(timesheetDTO.getTimesheetStatusUpdatedBy());
//				timesheetDetailsForDocumentApproval.setUpdatedOn(LocalDateTime.now());
//				dbResponse = timesheetDocumentApprovalRepository.save(timesheetDetailsForDocumentApproval);
//			}
//
//			 if(dbResponse == null) {
//				 
//							    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					            response.setServiceResponse("Timesheet Document Failed To Approve");
//					            response.setServiceMessage("Timesheet Document Failed To Approve");
//					            
//					            apiLogInfo.setApiResponse("Timesheet Document Failed To Approve");
//					            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//					            logService.logMyInfo(httpRequest, apiLogInfo);
//					            return response;
//						 }else {
//							    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					            response.setServiceResponse(dbResponse);
//					            response.setServiceMessage("Timesheet Document Approved!");
//					            apiLogInfo.setApiResponse("Timesheet Document Approved!");
//					            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//						 }
//			
//		}catch(Exception e) {
//			  e.printStackTrace();
//		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//		        response.setServiceResponse("Something went wrong.");
//		        response.setServiceError(e.getMessage());
//
//		        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//		        apiLogInfo.setApiResponse(e.getMessage()); 
//		        apiLogInfo.setLogLevel("ERROR");
//			
//		}
//		return response;
//	}
//	
//	@Transactional(rollbackFor = Exception.class)
//	public ServiceResponse timesheetDocumentApprovalLogs(TimesheetDTO timesheetDTO) {
//		 ServiceResponse response = new ServiceResponse();
//			
//			LogDTO apiLogInfo = new LogDTO();
//			apiLogInfo.setSubFeatureName("timesheetDocumentApprovalLogs");
//			apiLogInfo.setLogLevel("INFO");
//			StringBuilder logBuilder = new StringBuilder();
//			logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId());
//			try {
//				
//				TimesheetApprovalAllocationLogs dbResponse = null;
//				TimesheetDocumentApproval timesheetDetailsForDocumentApproval =  timesheetDocumentApprovalRepository.findByTimesheetId(timesheetDTO.getTimesheetId());
//				if(timesheetDetailsForDocumentApproval == null) {
//					TimesheetApprovalAllocationLogs timesheetDetails = new TimesheetApprovalAllocationLogs();
//					timesheetDetails.setTimesheetId(timesheetDTO.getTimesheetId());
//					timesheetDetails.setApproverId(timesheetDTO.getTimesheetStatusUpdatedBy());	
//					timesheetDetails.setApprovalStatus(timesheetDTO.getStatus());
//					if (timesheetDTO.getRejectionId() != null) {
//					    timesheetDetails.setRejectionId(timesheetDTO.getRejectionId());
//
//					    
//					    if (timesheetDTO.getHodId() != null
//					            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//					            && timesheetDTO.getHodId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//					        timesheetDetails.setRejectedhierarchyOrder(2);
//					        timesheetDetails.setRejectionLevel(1);
//					        timesheetDetails.setHierarchyOrder(2);
//					        timesheetDetails.setLevelId(1);
//
//					    
//					    } else if (timesheetDTO.getRmId() != null
//					            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//					            && timesheetDTO.getRmId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//					        timesheetDetails.setRejectedhierarchyOrder(1);
//					        timesheetDetails.setRejectionLevel(1);
//					        timesheetDetails.setHierarchyOrder(1);
//					        timesheetDetails.setLevelId(1);
//
//					    
//					    } else if (timesheetDTO.getManagerId() != null
//					            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//					            && timesheetDTO.getManagerId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())
//					            && !timesheetDTO.getManagerId().equals(timesheetDTO.getHodId())
//					            && !timesheetDTO.getManagerId().equals(timesheetDTO.getRmId())) {
//					    	
//					    	timesheetDetails.setRejectedhierarchyOrder(1);
//					    	timesheetDetails.setRejectionLevel(1);
//					    	timesheetDetails.setHierarchyOrder(1);
//					    	timesheetDetails.setLevelId(1);
//
//					    }else {
//					        timesheetDetails.setRejectedhierarchyOrder(3);
//					        timesheetDetails.setRejectionLevel(2);
//					        timesheetDetails.setHierarchyOrder(3);
//					        timesheetDetails.setLevelId(2);
//					    }
//					}
//					else {
//						  if (timesheetDTO.getHodId() != null
//						            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//						            && timesheetDTO.getHodId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//						        timesheetDetails.setLevelId(1);
//						        timesheetDetails.setHierarchyOrder(2);
//
//						    
//						    } else if (timesheetDTO.getRmId() != null
//						            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//						            && timesheetDTO.getRmId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//						    	 timesheetDetails.setLevelId(1);
//							        timesheetDetails.setHierarchyOrder(1);
//
//						   
//						    }else if (timesheetDTO.getManagerId() != null
//						            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//						            && timesheetDTO.getManagerId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())
//						            && !timesheetDTO.getManagerId().equals(timesheetDTO.getHodId())
//						            && !timesheetDTO.getManagerId().equals(timesheetDTO.getRmId())) {
//						    	
//						    	timesheetDetails.setHierarchyOrder(1);
//						    	timesheetDetails.setLevelId(1);
//
//						    } else {
//						    	 timesheetDetails.setLevelId(2);
//							        timesheetDetails.setHierarchyOrder(3);
//						    }
//					}
//
//					timesheetDetails.setCreatedBy(timesheetDTO.getEmpId());
//					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
//					LocalDateTime createdOn = LocalDateTime.parse(timesheetDTO.getCreatedOn(), formatter);
//     				timesheetDetails.setCreatedOn(createdOn);
//					timesheetDetails.setAllocId(timesheetDTO.getAllocId());					
//					dbResponse = timesheetApprovalAllocationLogsRepository.save(timesheetDetails);
//				}else {
//					TimesheetApprovalAllocationLogs timesheetDetails = new TimesheetApprovalAllocationLogs();
//					timesheetDetails.setTimesheetId(timesheetDetailsForDocumentApproval.getTimesheetId());
//					timesheetDetails.setPreviousLevelId(timesheetDetailsForDocumentApproval.getPreviousLevelId());
//					timesheetDetails.setPreviousApproverId(timesheetDetailsForDocumentApproval.getPreviousApproverId());
//					timesheetDetails.setApproverId(timesheetDetailsForDocumentApproval.getApproverId());	
//					timesheetDetails.setApprovalStatus(timesheetDetailsForDocumentApproval.getApprovalStatus());
//					if (timesheetDTO.getRejectionId() != null) {
//						timesheetDetails.setRejectionId(timesheetDTO.getRejectionId());
//
//					    
//					    if (timesheetDTO.getHodId() != null
//					            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//					            && timesheetDTO.getHodId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//					    	timesheetDetails.setRejectedhierarchyOrder(2);
//					    	timesheetDetails.setRejectionLevel(1);
//					    	timesheetDetails.setHierarchyOrder(2);
//					    	timesheetDetails.setLevelId(1);
//
//					    
//					    } else if (timesheetDTO.getRmId() != null
//					            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//					            && timesheetDTO.getRmId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//					    	timesheetDetails.setRejectedhierarchyOrder(1);
//					    	timesheetDetails.setRejectionLevel(1);
//					    	timesheetDetails.setHierarchyOrder(1);
//					    	timesheetDetails.setLevelId(1);
//
//					   
//					    }else if (timesheetDTO.getManagerId() != null
//					            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//					            && timesheetDTO.getManagerId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())
//					            && !timesheetDTO.getManagerId().equals(timesheetDTO.getHodId())
//					            && !timesheetDTO.getManagerId().equals(timesheetDTO.getRmId())) {
//					    	
//					    	timesheetDetails.setRejectedhierarchyOrder(1);
//					    	timesheetDetails.setRejectionLevel(1);
//					    	timesheetDetails.setHierarchyOrder(1);
//					    	timesheetDetails.setLevelId(1);
//
//					    } else {
//					    	timesheetDetails.setRejectedhierarchyOrder(3);
//					    	timesheetDetails.setRejectionLevel(2);
//					    	timesheetDetails.setHierarchyOrder(3);
//					    	timesheetDetails.setLevelId(2);
//					    }
//					}
//					else {
//						  if (timesheetDTO.getHodId() != null
//						            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//						            && timesheetDTO.getHodId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//							  timesheetDetails.setLevelId(1);
//							  timesheetDetails.setHierarchyOrder(2);
//
//						   
//						    } else if (timesheetDTO.getRmId() != null
//						            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//						            && timesheetDTO.getRmId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())) {
//						    	timesheetDetails.setLevelId(1);
//						    	timesheetDetails.setHierarchyOrder(1);
//
//						    
//						    }else if (timesheetDTO.getManagerId() != null
//						            && timesheetDTO.getTimesheetStatusUpdatedBy() != null
//						            && timesheetDTO.getManagerId().equals(timesheetDTO.getTimesheetStatusUpdatedBy())
//						            && !timesheetDTO.getManagerId().equals(timesheetDTO.getHodId())
//						            && !timesheetDTO.getManagerId().equals(timesheetDTO.getRmId())) {
//						    	
//						    	timesheetDetails.setHierarchyOrder(1);
//						    	timesheetDetails.setLevelId(1);
//
//						    } else {
//						    	timesheetDetails.setLevelId(2);
//						    	timesheetDetails.setHierarchyOrder(3);
//						    }
//					}
//					timesheetDetails.setCreatedBy(timesheetDTO.getEmpId());
//					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
//					LocalDateTime createdOn = LocalDateTime.parse(timesheetDTO.getCreatedOn(), formatter);
//					timesheetDetails.setCreatedOn(createdOn);
//					timesheetDetails.setAllocId(timesheetDetailsForDocumentApproval.getAllocId());					
//					dbResponse = timesheetApprovalAllocationLogsRepository.save(timesheetDetails);
//					
//				}
//				
//				 if(dbResponse == null) {
//					 
//								    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						            response.setServiceResponse("Timesheet Logs Failed To Save");
//						            response.setServiceMessage("Timesheet Logs Failed To Save");
//						            
//						            apiLogInfo.setApiResponse("Timesheet Logs Failed To Save");
//						            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//						            logService.logMyInfo(httpRequest, apiLogInfo);
//						            return response;
//							 }else {
//								    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//						            response.setServiceResponse(dbResponse);
//						            response.setServiceMessage("Timesheet logs Saved!");
//						            apiLogInfo.setApiResponse("Timesheet logs Saved!");
//						            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//							 }
//				
//			
//				}catch(Exception e) {
//				  e.printStackTrace();
//			        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			        response.setServiceResponse("Something went wrong.");
//			        response.setServiceError(e.getMessage());
//
//			        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			        apiLogInfo.setApiResponse(e.getMessage()); 
//			        apiLogInfo.setLogLevel("ERROR");
//				
//			}
//			return response;
//			}
//

	
	
	
	public ServiceResponse totalVmsFilledCount(TimesheetDTO timesheetDTO) {
		return timesheetDashboardService.totalVmsFilledCount(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetDashboardService
	private ServiceResponse totalVmsFilledCountInternal(TimesheetDTO timesheetDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
	    	
	    	Long empId = Long.valueOf(timesheetDTO.getEmpId());
	    	List<Object[]> timesheetList = timesheetsRepository.getTotalVmsFilledCount(empId);

	        
	    	if (timesheetList == null || timesheetList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No VMS Filled!");
	            response.setServiceMessage("No VMS Filled!"); 
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(timesheetList);
            response.setServiceMessage("TimesheetList List fetched successfully!");

            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
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
	
	
	
	public ServiceResponse totalIshineFilledCount(String status) {
		return timesheetDashboardService.totalIshineFilledCount(status);
	}
	
	// Delegate method - implementation moved to TimesheetDashboardService
	private ServiceResponse totalIshineFilledCountInternal(String status) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
	    	List<Object[]> ishineTimesheetList = timesheetsRepository.totalIshineFilledCount();
	        
	    	if (ishineTimesheetList == null || ishineTimesheetList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No Timesheet Filled!");
	            response.setServiceMessage("No Timesheet Filled!"); 
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(ishineTimesheetList);
            response.setServiceMessage("TimesheetList List fetched successfully!");

            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
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

	
	public ServiceResponse totalvmsNotFilled(TimesheetDTO timesheetDTO) {
		return timesheetDashboardService.totalvmsNotFilled(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetDashboardService
	private ServiceResponse totalvmsNotFilledInternal(TimesheetDTO timesheetDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
	    	List<Object[]> timesheetList = timesheetsRepository.totalvmsNotFilled(timesheetDTO.getEmpId());
	        
	    	if (timesheetList == null || timesheetList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No VMS Filled!");
	            response.setServiceMessage("No VMS Filled!"); 
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(timesheetList);
            response.setServiceMessage("TimesheetList List fetched successfully!");

            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
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
	
	public ServiceResponse getMyReporteesAndClientSideProjectsInMonthYear(TimesheetDTO timesheetDTO) {

	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getMyReporteesAndClientSideProjectsInMonthYear");
	    apiLogInfo.setLogLevel("INFO");

	    try {

	    
	        String monthYear = timesheetDTO.getMonthYear(); 
	        Integer year = null;
	        Integer month = null;

	        if (monthYear != null && monthYear.contains("-")) {
	            String[] parts = monthYear.split("-");
	            year = Integer.parseInt(parts[0]);
	            month = Integer.parseInt(parts[1]);
	        }

	       
	        List<Object[]> resultList = timesheetsRepository.getMyReporteesAndClientSideProjectsInMonthYear(year,month,timesheetDTO.getManagerId());
	                       

	        if (resultList == null || resultList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No reportees found!");
	            response.setServiceMessage("No reportees found for given month & manager.");

	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	      
	        Map<Long, EmployeeDTO> employeeMap = new LinkedHashMap<>();

	        for (Object[] row : resultList) {

	            Long empId = row[0] != null ? Long.parseLong(row[0].toString()) : null;

	            EmployeeDTO employeeDTO = employeeMap.get(empId);

	            if (employeeDTO == null) {
	                employeeDTO = new EmployeeDTO();
	                employeeDTO.setEmpId(empId);
	                employeeDTO.setName(row[1] != null ? row[1].toString() : null);
	                employeeDTO.setProjectList(new ArrayList<>());

	                employeeMap.put(empId, employeeDTO);
	            }

	           
	            ProjectDTO projectDTO = new ProjectDTO();
	            projectDTO.setProjectId(row[2] != null ? Integer.parseInt(row[2].toString()) : null);
	            projectDTO.setProjectName(row[3] != null ? row[3].toString() : null);
	            employeeDTO.getProjectList().add(projectDTO);
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(new ArrayList<>(employeeMap.values()));
	        response.setServiceMessage("Reportees & project details fetched successfully!");

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Data fetched successfully");

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
	
	public ServiceResponse getMyProjectsInMonthYear(TimesheetDTO timesheetDTO) {

	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getMyClientSideProjectsInMonthYear");
	    apiLogInfo.setLogLevel("INFO");

	    try {

	        String monthYear = timesheetDTO.getMonthYear();
	        Integer year = null;
	        Integer month = null;

	        if (monthYear != null && monthYear.contains("-")) {
	            String[] parts = monthYear.split("-");
	            year = Integer.parseInt(parts[0]);
	            month = Integer.parseInt(parts[1]);
	        }

	        
	        List<Object[]> resultList = timesheetsRepository.getMyProjectsInMonthYear(month,year, timesheetDTO.getEmpId());

	        if (resultList == null || resultList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No projects found!");
	            response.setServiceMessage("No projects found for given month & manager.");

	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        List<ProjectDTO> projectList = new ArrayList<>();

	        for (Object[] row : resultList) {

	            ProjectDTO projectDTO = new ProjectDTO();
	            projectDTO.setProjectId(
	                    row[0] != null ? Integer.parseInt(row[0].toString()) : null
	            );
	            projectDTO.setProjectName(
	                    row[1] != null ? row[1].toString() : null
	            );

	            projectList.add(projectDTO);
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(projectList);
	        response.setServiceMessage("Project details fetched successfully!");

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Data fetched successfully");

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


	
	
	
	public ServiceResponse totalIshineNotFilledCount(TimesheetDTO timesheetDTO) {
		return timesheetDashboardService.totalIshineNotFilledCount(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetDashboardService
	private ServiceResponse totalIshineNotFilledCountInternal(TimesheetDTO timesheetDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
//	    	List<Object[]> ishineTimesheetList = timesheetsRepository.totalIshineNotFilledCount(timesheetDTO.getEmpId());
	    	List<Object[]> ishineTimesheetList;
	    	if(timesheetDTO.getIsClientDashboard()){
	    		ishineTimesheetList = timesheetsRepository.totalIshineNotFilledCount(timesheetDTO.getEmpId());
	    	}else {
	    		ishineTimesheetList = timesheetsRepository.totalIshineNotFilledCountForAllEmpDash(timesheetDTO.getEmpId());
	    	}
	        
	    	if (ishineTimesheetList == null || ishineTimesheetList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No Timesheet Filled!");
	            response.setServiceMessage("No Timesheet Filled!"); 
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(ishineTimesheetList);
            response.setServiceMessage("TimesheetList List fetched successfully!");

            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
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

	public ServiceResponse getVmsDocumentApprovalStatusWiseCount() {
		return timesheetDashboardService.getVmsDocumentApprovalStatusWiseCount();
	}
	
	// Delegate method - implementation moved to TimesheetDashboardService
	private ServiceResponse getVmsDocumentApprovalStatusWiseCountInternal() {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getVmsDocumentApprovalStatusWiseCount");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getVmsDocumentApprovalStatusWiseCount");
		try {
			List<TimesheetDocumentApprovalDTO> details = timesheetDocumentApprovalRepository.getRejectionCountsByLevel();
			 if(details == null) {
				 
				    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse("VMS Rejection Count Not Present");
		            response.setServiceMessage("VMS Rejection Count Not Present");
		            
		            apiLogInfo.setApiResponse("VMS Rejection Count Not Present");
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		            logService.logMyInfo(httpRequest, apiLogInfo);
		            return response;
			 }else {
				    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(details);
		            response.setServiceMessage("VMS Rejection Count Fetched");
		            apiLogInfo.setApiResponse("VMS Rejection Count Fetched");
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
	
//	@Transactional(rollbackFor = Exception.class)
//	public ServiceResponse replaceAllTemporaryFileWithFinalFile(MultipartFile file, LocalDate fromDate, LocalDate toDate, Long empId) {
//	    ServiceResponse response = new ServiceResponse();
//
//	    LogDTO apiLogInfo = new LogDTO();
//	    apiLogInfo.setSubFeatureName("replaceAllTemporaryFileWithFinalFile");
//	    apiLogInfo.setLogLevel("INFO");
//	    StringBuilder logBuilder = new StringBuilder();
//	    logBuilder.append("replaceAllTemporaryFileWithFinalFile");
//
//	    try {
//	        if (empId == null || fromDate == null || toDate == null || file == null || file.isEmpty()) {
//	            throw new IllegalArgumentException("Required input(s) are missing or file is empty.");
//	        }
//
//	        List<TimesheetDocumentDetails> docDatas = timesheetDocumentDetailsRepository
//	                .getDocsByEmpAndDateRange(empId, fromDate, toDate);
//
//	        if (docDatas == null || docDatas.isEmpty()) {
//	            throw new IllegalStateException("No timesheet documents found for the given employee and date range.");
//	        }
//	        
//	        Map<Long, List<TimesheetDocumentDetails>> groupedByTimesheetId = docDatas == null ? 
//	                Collections.emptyMap() :
//	                docDatas.stream()
//	                    .filter(Objects::nonNull) 
//	                    .filter(doc -> {
//	                        if (doc.getTimesheetId() == null) {
//	                            return false; 
//	                        }
//	                        Timesheet t = null;
//	                        try {
//	                            t = timesheetsRepository.findById(doc.getTimesheetId()).orElse(null);
//	                        } catch (Exception e) {
//	                            e.printStackTrace();
//	                            return false;
//	                        }
//	                        if (t == null) {
//	                            return false; 
//	                        }
//	                        String dayType = t.getDayType();
//	                        if (dayType == null) {
//	                            return false; 
//	                        }
//	                        return "Working".equalsIgnoreCase(dayType) || "Non-Working".equalsIgnoreCase(dayType);
//	                    })
//	                    .collect(Collectors.groupingBy(TimesheetDocumentDetails::getTimesheetId));
//
//	        	List<TimesheetDocumentDetails> onlyOneDocWithFinalFlagFalseList = new ArrayList<>();
//	        	
//	        	List<TimesheetDocumentDetails> noDocWithList = new ArrayList<>();
//	        	
//	        	List<TimesheetDocumentDetails> filteredList = groupedByTimesheetId.entrySet().stream()
//	        	    .filter(entry -> {
//	        	        List<TimesheetDocumentDetails> group = entry.getValue();
//
//	        	       
//	        	        if (group.size() == 1 && Boolean.FALSE.equals(group.get(0).getFinalFlag())) {
//	        	            onlyOneDocWithFinalFlagFalseList.add(group.get(0));
//	        	        }
//	        	        
//	        	        if(group.size() == 0)	        	        
//	        	        boolean shouldRemove = group.size() <= 2 &&
//	        	            group.stream().anyMatch(dto ->
//	        	                Boolean.TRUE.equals(dto.getFinalFlag()) &&
//	        	                !"Rejected".equalsIgnoreCase(dto.getRmApprovalStatus()) &&
//	        	                !"Rejected".equalsIgnoreCase(dto.getHrApprovalStatus())
//	        	            );
//
//	        	        return !shouldRemove;
//	        	    })
//	        	    .flatMap(entry -> entry.getValue().stream())
//	        	    .collect(Collectors.toList());
//
//	        byte[] fileBytes = file.getBytes();
//	        String fileName = file.getOriginalFilename();
//	        String contentType = file.getContentType();
//
////	        filteredList.stream().forEach(doc -> {
////	            doc.setDocName(fileName);
////	            doc.setDocData(fileBytes);
////	            doc.setDocMimeType(contentType);
////	            doc.setClientApprovalStatus("Approved");
////	            doc.setRmApprovalStatus("Pending");
////	            doc.setHrApprovalStatus("Pending");
////	            doc.setUpdatedBy(empId);
////	            doc.setUpdatedOn(LocalDateTime.now());
////	            doc.setFinalFlag(true);
////	        });
//	        
//	        onlyOneDocWithFinalFlagFalseList.stream().forEach(newDoc ->{
//	        	TimesheetDocumentDetails timesheetDocumentDetails = new TimesheetDocumentDetails();
//	        	Timesheet timeSheet = new Timesheet();
//	        	timeSheet = timesheetsRepository.getById(newDoc.getTimesheetId());
//	        	timeSheet.setClientApprovalStatus("Approved");
//	        	timesheetsRepository.save(timeSheet);
//	        	timesheetDocumentDetails.setActive(true);
//	        	timesheetDocumentDetails.setDocName(fileName);
//	        	timesheetDocumentDetails.setDocData(fileBytes);
//	        	timesheetDocumentDetails.setDocMimeType(contentType);
//	        	timesheetDocumentDetails.setClientApprovalStatus("Approved");
//	        	timesheetDocumentDetails.setRmApprovalStatus("Pending");
//	        	timesheetDocumentDetails.setHrApprovalStatus("Pending");
//	        	timesheetDocumentDetails.setCreatedBy(empId);
//	        	timesheetDocumentDetails.setTimesheetId(newDoc.getTimesheetId());
//	        	timesheetDocumentDetails.setEmpId(empId);
//	        	timesheetDocumentDetails.setCreatedOn(LocalDateTime.now());
//	        	timesheetDocumentDetails.setFinalFlag(true);
//	        	filteredList.add(timesheetDocumentDetails);
//	        	});
//
//	        timesheetDocumentDetailsRepository.saveAll(filteredList);
//
//	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	        response.setServiceResponse("All temporary files replaced with final document successfully.");
//	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//	    } catch (IllegalArgumentException | IllegalStateException e) {
//	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	        response.setServiceResponse(e.getMessage());
//	        response.setServiceError(e.getMessage());
//	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	        apiLogInfo.setApiResponse(e.getMessage());
//	        apiLogInfo.setLogLevel("ERROR");
//	        throw e; 
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	        response.setServiceResponse("Something went wrong.");
//	        response.setServiceError(e.getMessage());
//	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	        apiLogInfo.setApiResponse(e.getMessage());
//	        apiLogInfo.setLogLevel("ERROR");
//	        throw new RuntimeException("Failed to replace documents", e); 
//	    }
//
//	    return response;
//	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse replaceAllTemporaryFileWithFinalFile(
	        MultipartFile file, LocalDate fromDate, LocalDate toDate, Long empId,Long createdBy) {

	    ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("replaceAllTemporaryFileWithFinalFile");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        if (empId == null || fromDate == null || toDate == null || file == null || file.isEmpty()) {
	            throw new IllegalArgumentException("Required input(s) are missing or file is empty.");
	        }
			/*
			 * ================================ BLOCK UPLOAD FOR CURRENT MONTH BEFORE MONTH
			 * END ================================
			 */

//			LocalDate today = LocalDate.now();
//			YearMonth currentMonth = YearMonth.now();
//			YearMonth fromMonth = YearMonth.from(fromDate);
//			YearMonth toMonth = YearMonth.from(toDate);
//
//			// If either date is in current month AND today is before month end → block
//			if ((fromMonth.equals(currentMonth) || toMonth.equals(currentMonth))
//					&& today.isBefore(currentMonth.atEndOfMonth())) {
//
//				throw new IllegalStateException("Final document upload is allowed only after the current month ends.");
//			}

			List<TimesheetDocumentDetails> docDatas = timesheetDocumentDetailsRepository.getDocsByEmpAndDateRange(empId,
					fromDate, toDate);

//			if (docDatas == null || docDatas.isEmpty()) {
//				throw new IllegalStateException("No timesheet documents found for the given employee and date range.");
//			}

	        /* ================================
	           CACHE TIMESHEETS (Reduces DB calls)
	           ================================ */

	        Set<Long> timesheetIds = docDatas.stream()
	                .map(TimesheetDocumentDetails::getTimesheetId)
	                .filter(Objects::nonNull)
	                .collect(Collectors.toSet());

	        Map<Long, Timesheet> timesheetMap = timesheetIds.stream()
	                .map(id -> timesheetsRepository.findById(id).orElse(null))
	                .filter(Objects::nonNull)
	                .collect(Collectors.toMap(Timesheet::getTimesheetId, t -> t));


	        /* ================================
	           GROUP VALID DOCUMENTS BY TIMESHEET
	           ================================ */

	        Map<Long, List<TimesheetDocumentDetails>> groupedByTimesheetId =
	                docDatas.stream()
	                        .filter(Objects::nonNull)
	                        .filter(doc -> doc.getTimesheetId() != null)
	                        .filter(doc -> {
	                            Timesheet t = timesheetMap.get(doc.getTimesheetId());
	                            if (t == null || t.getDayType() == null) return false;
	                            return "Working".equalsIgnoreCase(t.getDayType())
	                                    || "Non-Working".equalsIgnoreCase(t.getDayType());
	                        })
	                        .collect(Collectors.groupingBy(TimesheetDocumentDetails::getTimesheetId));

	        /* ================================
	        REJECTED TIMESHEETS WITH NO DOCUMENT
	        ================================ */
	    
	        List<Timesheet> rejectedTimesheetList = timesheetsRepository.getRejectedTimesheetIdByEmpAndDateRange(empId,fromDate, toDate);
	     List<Timesheet> rejectedTimesheetsWithNoDocs =
	    		 rejectedTimesheetList.stream()

	                     // Has NO document
	                     .filter(t -> !groupedByTimesheetId.containsKey(t.getTimesheetId()))

	                     // Timesheet status is Rejected
	                     .filter(t -> "Rejected".equalsIgnoreCase(t.getStatus()))

	                     // Valid day type
	                     .filter(t ->
	                             t.getDayType() != null &&
	                             ("Working".equalsIgnoreCase(t.getDayType()) ||
	                              "Non-Working".equalsIgnoreCase(t.getDayType()))
	                     )

	                     .collect(Collectors.toList());

	        /* ================================
	           PREPARE SPECIAL LISTS
	           ================================ */

	        List<TimesheetDocumentDetails> onlyOneDocWithFinalFlagFalseList = new ArrayList<>();

	        List<TimesheetDocumentDetails> filteredList =
	                groupedByTimesheetId.entrySet().stream()
	                        .filter(entry -> {
	                            List<TimesheetDocumentDetails> group = entry.getValue();

	                            // Case: only one & finalFlag is false
	                            if (group.size() == 1 && Boolean.FALSE.equals(group.get(0).getFinalFlag())) {
	                                onlyOneDocWithFinalFlagFalseList.add(group.get(0));
	                            }

	                            boolean shouldRemove =
	                                    group.size() <= 2 &&
	                                            group.stream().anyMatch(dto ->
	                                                    Boolean.TRUE.equals(dto.getFinalFlag()) &&
	                                                            !"Rejected".equalsIgnoreCase(dto.getRmApprovalStatus()) &&
	                                                            !"Rejected".equalsIgnoreCase(dto.getHrApprovalStatus())
	                                            );

	                            return !shouldRemove;
	                        })
	                        .flatMap(entry -> entry.getValue().stream())
	                        .collect(Collectors.toList());

	        /* ================================
	           FILE META
	           ================================ */

	        byte[] fileBytes = file.getBytes();
	        String fileName = file.getOriginalFilename();
	        String contentType = file.getContentType();

	        
	        /* ================================
	        UPDATE REJECTED TIMESHEETS & ADD FINAL DOC
	        ================================ */

	     for (Timesheet rejectedTimesheet : rejectedTimesheetsWithNoDocs) {

	         // 1. Update Timesheet status
	         rejectedTimesheet.setStatus("Pending");   // Use correct column name if different
	         rejectedTimesheet.setClientApprovalStatus("Approved");
			 timesheetsRepository.save(rejectedTimesheet);

	         // 2. Create new FINAL document
	         TimesheetDocumentDetails finalDoc = new TimesheetDocumentDetails();
	         
	         finalDoc.setActive(true);
	         finalDoc.setDocName(fileName);
	         finalDoc.setDocData(fileBytes);
	         finalDoc.setDocMimeType(contentType);
	         finalDoc.setClientApprovalStatus("Approved");
	         finalDoc.setRmApprovalStatus("Pending");
	         finalDoc.setHrApprovalStatus("Pending");
	         finalDoc.setCreatedBy(createdBy);
	         finalDoc.setTimesheetId(rejectedTimesheet.getTimesheetId());
	         finalDoc.setEmpId(empId);
	         finalDoc.setCreatedOn(LocalDateTime.now());
	         finalDoc.setFinalFlag(true);

	         // 3. Add to save list
	         filteredList.add(finalDoc);
	     }

	        /* ================================
	           ADD FINAL DOCUMENT FOR SINGLE TEMP DOC
	           ================================ */
	        
	        
	        for (TimesheetDocumentDetails oldDoc : onlyOneDocWithFinalFlagFalseList) {

	            Timesheet timeSheet = timesheetMap.get(oldDoc.getTimesheetId());
	            if (timeSheet != null) {
	            	timeSheet.setStatus("Pending");
	                timeSheet.setClientApprovalStatus("Approved");
	                timesheetsRepository.save(timeSheet);
	            }

	            TimesheetDocumentDetails newDoc = new TimesheetDocumentDetails();
	            newDoc.setActive(true);
	            newDoc.setDocName(fileName);
	            newDoc.setDocData(fileBytes);
	            newDoc.setDocMimeType(contentType);
	            newDoc.setClientApprovalStatus("Approved");
	            newDoc.setRmApprovalStatus("Pending");
	            newDoc.setHrApprovalStatus("Pending");
	            newDoc.setCreatedBy(createdBy);
	            newDoc.setTimesheetId(oldDoc.getTimesheetId());
	            newDoc.setEmpId(empId);
	            newDoc.setCreatedOn(LocalDateTime.now());
	            newDoc.setFinalFlag(true);

	            filteredList.add(newDoc);
	        }

	        /* ================================
	           SAVE CHANGES
	           ================================ */

	        timesheetDocumentDetailsRepository.saveAll(filteredList);

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("All temporary files replaced with final document successfully.");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (IllegalArgumentException | IllegalStateException e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse(e.getMessage());
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	        throw e;

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	        throw new RuntimeException("Failed to replace documents", e);
	    }

	    return response;
	}

	
	
	public ServiceResponse getAllDisabledDateListForBulkDocSubmit(Integer projectId, Long empId) {
		return timesheetDocumentService.getAllDisabledDateListForBulkDocSubmit(projectId, empId);
	    }
	    
	//     return response;
	// }
	
	public ServiceResponse getEmployeeViewForClientAttendanceStatus(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getEmployeeViewForClientAttendanceStatus");
	    apiLogInfo.setLogLevel("INFO");
	    
	    String employmentId = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getEmploymentId());
	    String name = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getName());
	    String billable = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getBillable());
	    String billableType = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getBillableType());
	    Long mobileNo = timesheetDTO.getColumnFilter().getMobileNo() == null ? null 
	    		: timesheetDTO.getColumnFilter().getMobileNo().toString().isBlank() ? null 
	    				: Long.parseLong(timesheetDTO.getColumnFilter().getMobileNo().toString());
	    String email = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getEmail());
	    String departmentName = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getDepartmentName());
	    Integer expectedFillCount = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getExpectedFillCount());
	    Integer clientSideAttendancePendingCount = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getClientSideAttendancePendingCount());
	    Integer clientSideAttendanceApprovedCount = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getClientSideAttendanceApprovedCount());
	    Integer clientSideAttendanceNotFilledCount = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getClientSideAttendanceNotFilledCount());
	    String projectName = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getProjectName());
	    String poNo = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getPoNo());
	    String projectType = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getProjectType());
	    String projectManagers = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getProjectManagers());
	    String clientName = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getClientName());
	    String apmosysRm = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getApmosysRm());
	    String apmosysRmEmail = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getApmosysRmEmail());
	    String clientRm = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getClientRm());
	    String team = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getTeam());
	    String teamLeadName = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getTeamLeadName());
	    
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append( "getEmployeeViewForClientAttendanceStatus: \n");
		 try {
//			 List<Object[]> resultList = employeeRepository.getEmployeeViewForClientAttendanceStatus(timesheetDTO.getStatus(), timesheetDTO.getMonth1(),timesheetDTO.getYear(),timesheetDTO.getEmpId());		 
			 List<Object[]> resultList ;
			 Integer totalItems;
			 int page = timesheetDTO.getPage(); 
			 int pageSize = timesheetDTO.getSize();
			 int offset = (page-1) * pageSize; 
			 String sortBy=timesheetDTO.getSortBy();
	         String sortDirection=timesheetDTO.getSortDirection();
	         
			 if(timesheetDTO.getDataForExcel() && timesheetDTO.getIsClientDashboard()) {
				 resultList = employeeRepository.getEmployeeViewForClientAttendanceStatus(timesheetDTO.getStatus(), timesheetDTO.getMonth1(),timesheetDTO.getYear(),
						 timesheetDTO.getEmpId(),null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,sortBy,sortDirection,offset,Integer.MAX_VALUE);
			 } 
			 else if(timesheetDTO.getDataForExcel() && ! timesheetDTO.getIsClientDashboard()) {
				 resultList = employeeRepository.getEmployeeViewForAllEmpAttendanceStatus(timesheetDTO.getStatus(), timesheetDTO.getMonth1(),timesheetDTO.getYear(),
						 timesheetDTO.getEmpId(),timesheetDTO.getBillableTypes(),null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,sortBy,sortDirection,offset,Integer.MAX_VALUE);
			 } 
			 else if(timesheetDTO.getIsClientDashboard()) {
				 resultList = employeeRepository.getEmployeeViewForClientAttendanceStatus(timesheetDTO.getStatus(), timesheetDTO.getMonth1(),timesheetDTO.getYear(),
						 timesheetDTO.getEmpId(),employmentId,name,billable,billableType,mobileNo,email,departmentName,expectedFillCount,clientSideAttendancePendingCount,
						 clientSideAttendanceApprovedCount,clientSideAttendanceNotFilledCount,projectName,poNo,projectType,projectManagers,clientName,apmosysRm,
						 apmosysRmEmail,clientRm,team,teamLeadName,sortBy,sortDirection,offset,pageSize);
				 }	
			 else {
				 resultList = employeeRepository.getEmployeeViewForAllEmpAttendanceStatus(timesheetDTO.getStatus(), timesheetDTO.getMonth1(),timesheetDTO.getYear(),
						  timesheetDTO.getEmpId(),timesheetDTO.getBillableTypes(),employmentId,name,billable,billableType,mobileNo,email,departmentName,expectedFillCount,clientSideAttendancePendingCount,
							 clientSideAttendanceApprovedCount,clientSideAttendanceNotFilledCount,projectName,poNo,projectType,projectManagers,clientName,apmosysRm,
							 apmosysRmEmail,null,team,teamLeadName,sortBy,sortDirection,offset,pageSize);
			 }
			 
			  totalItems = ((BigInteger) entityManager.createNativeQuery("SELECT FOUND_ROWS()").getSingleResult()).intValue();
			 
			 if(resultList.isEmpty()) {
			        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			        response.setServiceResponse("No data found from database");

			        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			        apiLogInfo.setApiResponse("Empty list received from "); 
			        apiLogInfo.setLogLevel("ERROR");
			        
			        logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
			 }

	        List<GetEmployeeViewForClientAttendanceStatusDTO> dtoList = new ArrayList<>();
	        
	        if(timesheetDTO.getIsClientDashboard()) {

	        for (Object[] row : resultList) {
	            GetEmployeeViewForClientAttendanceStatusDTO dto = new GetEmployeeViewForClientAttendanceStatusDTO();

	            dto.setName(row[0] != null ? row[0].toString() : null);
	            dto.setEmpId(row[1] != null ? Long.parseLong(row[1].toString()) : null);
	            dto.setProjectName(row[2] != null ? row[2].toString() : null);
	            dto.setPoNo(row[3] != null ? row[3].toString() : null);
	            dto.setClientName(row[4] != null ? row[4].toString() : null);
	            dto.setTeam(row[5] != null ? row[5].toString() : null);
	            dto.setProjectType(row[6] != null ? row[6].toString() : null);
	            dto.setTeamLeadName(row[7] != null ? row[7].toString() : null);
	            dto.setBillable(row[8] != null ? row[8].toString() : null);
	            dto.setBillableType(row[9] != null ? row[9].toString() : null);
	            dto.setMobileNo(row[10] != null ? Long.parseLong(row[10].toString()) : null);
	            dto.setEmail(row[11] != null ? row[11].toString() : null);
	            dto.setExpectedFillCount(row[12] != null ? Long.parseLong(row[12].toString()) : 0L);
//	            dto.setTimesheetFilledCount(row[13] != null ? Long.parseLong(row[13].toString()) : 0L);
	            dto.setClientSideAttendanceNotFilledCount(row[13] != null ? Long.parseLong(row[13].toString()) : 0L);
	            dto.setClientSideAttendancePendingCount(row[14] != null ? Long.parseLong(row[14].toString()) : 0L);
	            dto.setClientSideAttendanceApprovedCount(row[15] != null ? Long.parseLong(row[15].toString()) : 0L);
	            dto.setApmosysRm(row[16] != null ? row[16].toString() : null);
	            dto.setApmosysRmEmail(row[17] != null ? row[17].toString() : null);
	            dto.setClientRm(row[18] != null ? row[18].toString() : null);
	            dto.setEmploymentId(row[19] != null ? row[19].toString() : null);
	            dto.setDepartmentName(row[20] != null ? row[20].toString() : null);
	            dto.setProjectManagers(row[21] != null ? row[21].toString() : null);
	            dto.setProjectId(row[22] != null ? Long.parseLong(row[22].toString()) : 0L);

	            dtoList.add(dto);
	        }
	        
	        }else {
	        	
	            for (Object[] row : resultList) {
		            GetEmployeeViewForClientAttendanceStatusDTO dto = new GetEmployeeViewForClientAttendanceStatusDTO();

		            dto.setName(row[0] != null ? row[0].toString() : null);
		            dto.setEmpId(row[1] != null ? Long.parseLong(row[1].toString()) : null);
		            dto.setProjectName(row[2] != null ? row[2].toString() : null);
		            dto.setPoNo(row[3] != null ? row[3].toString() : null);
		            dto.setClientName(row[4] != null ? row[4].toString() : null);
		            dto.setTeam(row[5] != null ? row[5].toString() : null);
		            dto.setProjectType(row[6] != null ? row[6].toString() : null);
		            dto.setTeamLeadName(row[7] != null ? row[7].toString() : null);
		            dto.setBillable(row[8] != null ? row[8].toString() : null);
		            dto.setBillableType(row[9] != null ? row[9].toString() : null);
		            dto.setMobileNo(row[10] != null ? Long.parseLong(row[10].toString()) : null);
		            dto.setEmail(row[11] != null ? row[11].toString() : null);
		            dto.setExpectedIshineFillCount(row[12] != null ? Long.parseLong(row[12].toString()) : 0L);
//		            dto.setTimesheetFilledCount(row[13] != null ? Long.parseLong(row[13].toString()) : 0L);
		            dto.setIshineNotFilledTimesheetCount(row[13] != null ? Long.parseLong(row[13].toString()) : 0L);
		            dto.setIshinePendingTimesheetCount(row[14] != null ? Long.parseLong(row[14].toString()) : 0L);
		            dto.setIshineApprovedTimesheetCount(row[15] != null ? Long.parseLong(row[15].toString()) : 0L);
		            dto.setApmosysRm(row[16] != null ? row[16].toString() : null);
		            dto.setApmosysRmEmail(row[17] != null ? row[17].toString() : null);
		            dto.setEmploymentId(row[18] != null ? row[18].toString() : null);
		            dto.setDepartmentName(row[19] != null ? row[19].toString() : null);
		            dto.setProjectManagers(row[20] != null ? row[20].toString() : null);
		            dto.setProjectId(row[21] != null ? Long.parseLong(row[21].toString()) : 0L);

		            dtoList.add(dto);
	        }
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(dtoList);
	        response.setTotalElements(totalItems);

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Fetched " + dtoList.size() + " records successfully.");
	        
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
		        
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
	
	private String getStringColumnFilterValue(String columnValue) {
		if(columnValue == null || columnValue.isBlank()) {
			return null;
		}
		return columnValue.toLowerCase();
	}
	
	private Integer getIntegerColumnFilterValue(Integer columnValue) {
		if(columnValue == null || columnValue.toString().isBlank()) {
			return null;
		}
		return columnValue;
	}


	public ServiceResponse getEmployeeTimesheetsByProject(TimesheetDTO timesheetDTO) {
		   ServiceResponse response = new ServiceResponse();

		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setSubFeatureName("getEmployeeTimesheetsByProject");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();
		    logBuilder.append("getEmployeeTimesheetsByProject");
		    try {
		    	 int page = timesheetDTO.getPage(); 
				 int pageSize = timesheetDTO.getSize();
				 int offset = (page-1) * pageSize; 
				 String sortBy=timesheetDTO.getSortBy();
		         String sortDirection=timesheetDTO.getSortDirection();
				 
				 String employmentId = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getEmploymentId());
			     String name = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getName());
				 				 
				 List<Object[]> timesheetDetailsAccordingToProject; 
				if(timesheetDTO.getDataForExcel()) {
			    	timesheetDetailsAccordingToProject = projectRepository.getEmployeeTimesheetsByProject(timesheetDTO.getProjectId(),timesheetDTO.getFromDate(),
			    			timesheetDTO.getToDate(),null,null,sortBy,sortDirection,offset,Integer.MAX_VALUE);
				}else {
			    	timesheetDetailsAccordingToProject = projectRepository.getEmployeeTimesheetsByProject(timesheetDTO.getProjectId(),timesheetDTO.getFromDate(),
			    			timesheetDTO.getToDate(),employmentId,name,sortBy,sortDirection,offset,pageSize);
				}
		    	Integer totalItems = ((BigInteger) entityManager.createNativeQuery("SELECT FOUND_ROWS()").getSingleResult()).intValue();
		    	
		    	if(timesheetDetailsAccordingToProject == null) {
					 
				    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse("No Timesheet Details Found");
		            response.setServiceMessage("No Timesheet Details Found");
		            
		            apiLogInfo.setApiResponse("No Timesheet Details Found");
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		            logService.logMyInfo(httpRequest, apiLogInfo);
		            return response;
			 }else {
				 
				 List<TimesheetDTO> employeeTimesheetsByProjectDetails = new ArrayList<TimesheetDTO>();
				 for(Object[] object: timesheetDetailsAccordingToProject) {
					 TimesheetDTO dto = new TimesheetDTO();
					 dto.setEmployeeName(object[0] != null ? object[0].toString() : null);                 // e.name
					 dto.setEmpId(object[1] != null ? Long.parseLong(object[1].toString()) : null);        // e.emp_id
//					 dto.setEmployeementId(object[2] != null ? Long.parseLong(object[2].toString()) : null); // e.employeement_id
					 dto.setEmployeementId(
							    object[2] != null
							        ? Long.parseLong(object[2].toString().replaceAll("[^0-9]", ""))
							        : null
							);
					 dto.setClientSideId(object[3] != null ? object[3].toString() : null);                  // e.client_side_id
					 dto.setProjectName(object[4] != null ? object[4].toString() : null);                   // e.project_name
					 dto.setExpectedEODCount(object[5] != null ? Long.parseLong(object[5].toString()) : null); // wds.expected_fill_count
					 dto.setSubmittedCount(object[6] != null ? Integer.valueOf(object[6].toString()) : null);          // ts.submitted_count
					 dto.setClientPendingCount(object[7] != null ? Integer.valueOf(object[7].toString()) : null);       // ds.Client_pending_count
					 dto.setClientApprovedCount(object[8] != null ? Integer.valueOf(object[8].toString()) : null);      // ds.Client_Approved_count
//					 dto.setEmploymentId(object[9] != null ? object[9].toString() : null); 		//e.eployement_id	
					 

					 employeeTimesheetsByProjectDetails.add(dto);
				 }
				    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(employeeTimesheetsByProjectDetails);
		            response.setServiceMessage("Timesheet details successfully fetched.");
			        response.setTotalElements(totalItems);

		            apiLogInfo.setApiResponse("Timesheet details successfully fetched.");
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

	public ServiceResponse getRejectionReason() {
		return timesheetApprovalService.getRejectionReason();
	}
	
	// Delegate method - implementation moved to TimesheetApprovalService
	private ServiceResponse getRejectionReasonInternal() {
		
	   ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getRejectionReason");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("getRejectionReason");
	    try {
	    	Optional<List<TimesheetRejectionReasonsMasterDTO>> detailsOfRejection=timesheetRejectionReasonsMasterRepository.getAllRejectionReason();
	    	
	    	if(!detailsOfRejection.isPresent()) {
				 
			    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No Reject Reasons Found.");
	            response.setServiceMessage("No Reject Reasons Found.");
	            
	            apiLogInfo.setApiResponse("No Reject Reasons Found.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
			 }else {
				 
				    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(detailsOfRejection);
		            response.setServiceMessage("Reject reasons fetched successfully.");
		            apiLogInfo.setApiResponse("Reject reasons fetched successfully.");
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			 }
	    } catch(Exception e) {
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
	public ServiceResponse setTimesheetRejectReason(TimesheetRejectionReasonsMasterDTO rejectReasonObj) {
		return timesheetApprovalService.setTimesheetRejectReason(rejectReasonObj);
	}
	
	// Delegate method - implementation moved to TimesheetApprovalService
	private ServiceResponse setTimesheetRejectReasonInternal(TimesheetRejectionReasonsMasterDTO rejectReasonObj) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("setTimesheetRejectReason");
		apiLogInfo.setApiUrl("/api/setTimesheetRejectReason");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("setTimesheetRejectReason : " +rejectReasonObj.getRejectionReason());
		try {

			Optional<TimesheetRejectionReasonsMaster> existingRejectReason = timesheetRejectionReasonsMasterRepository.findByRejectionId(rejectReasonObj.getRejectionId());
	        
				if (existingRejectReason.isEmpty()) {
				
				TimesheetRejectionReasonsMaster newRejectReason = new TimesheetRejectionReasonsMaster();
				newRejectReason.setRejectionReason(rejectReasonObj.getRejectionReason());
		        newRejectReason.setActive(true);
		        newRejectReason.setCreatedBy(rejectReasonObj.getCreatedBy());
		        newRejectReason.setCreatedOn(LocalDateTime.now());

		        TimesheetRejectionReasonsMaster newMapping = timesheetRejectionReasonsMasterRepository.save(newRejectReason);
		        
		        if(newMapping == null) {
		        	
		        	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			        response.setServiceResponse("Could not store new reject reason!");
			        apiLogInfo.setApiResponse("Something went worong while storing the the new reject reason!");
			        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        		
		        } else {
		        	
		        	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			        response.setServiceResponse("New Reject Reason Successfully!");
			        apiLogInfo.setApiResponse("New Reject Reason created!");
			        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        		
		        }

		        apiLogInfo.setApiRequest(logBuilder.toString());
        		logService.logMyInfo(httpRequest, apiLogInfo);
        		return response;
		        
	        } else if(existingRejectReason.isPresent()) {

	        	TimesheetRejectionReasonsMaster exstRejectRsn = existingRejectReason.get();
	        	
	        	exstRejectRsn.setRejectionReason(rejectReasonObj.getRejectionReason());
	        	exstRejectRsn.setActive(rejectReasonObj.getActive());
	        	exstRejectRsn.setUpdatedBy(rejectReasonObj.getUpdatedBy());
	        	exstRejectRsn.setUpdatedOn(LocalDateTime.now());

	        	TimesheetRejectionReasonsMaster updatedMapping = timesheetRejectionReasonsMasterRepository.save(exstRejectRsn);

	            if (updatedMapping == null) {
	            	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Unable to update the reject reason!");
	                apiLogInfo.setApiResponse("Failed to update the reject reason! ");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                
	            } else {
	            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse("Updated Successfully!");
	                apiLogInfo.setApiResponse("Reject reason updated successfully ");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            }
	            
	            apiLogInfo.setApiRequest(logBuilder.toString());
        		logService.logMyInfo(httpRequest, apiLogInfo);
        		return response;
        		
	        } else {
	        	apiLogInfo.setApiResponse("Moved to else module");
                apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        	response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Something Went Wrong.");
				
				apiLogInfo.setApiRequest(logBuilder.toString());
        		logService.logMyInfo(httpRequest, apiLogInfo);
        		return response;
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
	
	public ServiceResponse getRejectionReasonById(Long rejectionId) {
		return timesheetApprovalService.getRejectionReasonById(rejectionId);
	}
	
	// Delegate method - implementation moved to TimesheetApprovalService
	private ServiceResponse getRejectionReasonByIdInternal(Long rejectionId) {
		
		   ServiceResponse response = new ServiceResponse();

		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setSubFeatureName("getRejectionReasonById");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();
		    logBuilder.append("getRejectionReasonById");
		    try {
		    	Optional<TimesheetRejectionReasonsMaster> detailsOfRejection=timesheetRejectionReasonsMasterRepository.findByRejectionId(rejectionId);
		    	
		    	if(detailsOfRejection.isPresent()) {
				 
				    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(detailsOfRejection);
		            response.setServiceMessage("Reject reasons fetched successfully.");
		            apiLogInfo.setApiResponse("Reject reasons fetched successfully.");
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		            
		            apiLogInfo.setApiResponse("Reject reasons fetched successfully.");
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		            
	    		}else {
	    			
	    			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse("No Reject Reasons Found.");
		            response.setServiceMessage("No Reject Reasons Found.");
		            
		            apiLogInfo.setApiResponse("No Reject Reasons Found.");
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		            
	    		}

	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
			
		    } catch(Exception e) {
		    	
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
	

    public ServiceResponse utiltyMethodToGetHodIdAndRmId(TimesheetDTO timesheetDTO) {
		return timesheetApprovalService.utiltyMethodToGetHodIdAndRmId(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetApprovalService
	private ServiceResponse utiltyMethodToGetHodIdAndRmIdInternal(TimesheetDTO timesheetDTO) {
		
		   ServiceResponse response = new ServiceResponse();

		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setSubFeatureName("utiltyMethodToGetHodIdAndRmId");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();
		    logBuilder.append("utiltyMethodToGetHodIdAndRmId");
		    try {
		    	Optional<EmployeeDTO> empDetails = employeeRepository.findEmployeeReportingManagerIdAndHODIdDetailsByEmpId(timesheetDTO.getEmpId());	
		    	if(empDetails == null) {
					 
				    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse("Employee Details Not Found.");
		            response.setServiceMessage("Employee Details Not Found.");
		            
		            apiLogInfo.setApiResponse("Employee Details Not Found.");
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		            logService.logMyInfo(httpRequest, apiLogInfo);
		            return response;
			 }else {
				    EmployeeDTO dto = empDetails.get();
				    timesheetDTO.setRmId(dto.getReportingManagerId());
				    timesheetDTO.setHodId(dto.getHodId()); 
				    timesheetDTO.setManagerId(dto.getManagerId());
				    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(timesheetDTO);
		            response.setServiceMessage("Employee Details Fetched Successfully.");
		            apiLogInfo.setApiResponse("Employee Details Fetched Successfully.");
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

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse approveOrRejectDocument(Long docId, Long approvedOrRejectedBy, String approvalStatus) {
		return timesheetDocumentService.approveOrRejectDocument(docId, approvedOrRejectedBy, approvalStatus);
	}

	
	@Transactional
	public ServiceResponse updateActiveByRejectIdId(TimesheetRejectionReasonsMasterDTO rejectionObj) {
		return timesheetApprovalService.updateActiveByRejectIdId(rejectionObj);
	}
	
	// Delegate method - implementation moved to TimesheetApprovalService
	private ServiceResponse updateActiveByRejectIdIdInternal(TimesheetRejectionReasonsMasterDTO rejectionObj) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("updateActiveByRejectIdId");
		apiLogInfo.setApiUrl("/api/updateActiveByRejectIdId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("updateActiveByRejectIdId for rejection id: " +rejectionObj.getRejectionId());
		try {

			Optional<TimesheetRejectionReasonsMaster> existingRejectReason = timesheetRejectionReasonsMasterRepository.findByRejectionId(rejectionObj.getRejectionId());
	        
				if (existingRejectReason.isEmpty()) {
				
	        	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		        response.setServiceResponse("Could not find the reject reason!");
		        apiLogInfo.setApiResponse("Something went wrong while fetching the existing reject reason!");
		        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        		
		        apiLogInfo.setApiRequest(logBuilder.toString());
        		logService.logMyInfo(httpRequest, apiLogInfo);
        		return response;
		        
	        } else if(existingRejectReason.isPresent()) {

	        	TimesheetRejectionReasonsMaster exstRejectRsn = existingRejectReason.get();
	        	
	        	exstRejectRsn.setActive(rejectionObj.getActive());
	        	exstRejectRsn.setUpdatedBy(rejectionObj.getUpdatedBy());
	        	exstRejectRsn.setUpdatedOn(LocalDateTime.now());

	        	TimesheetRejectionReasonsMaster updatedMapping = timesheetRejectionReasonsMasterRepository.save(exstRejectRsn);

	            if (updatedMapping == null) {
	            	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Unable to update the reject reason!");
	                apiLogInfo.setApiResponse("Failed to update the reject reason! ");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                
	            } else {
	            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse("Updated Successfully!");
	                apiLogInfo.setApiResponse("Reject reason updated successfully ");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            }
	            
	            apiLogInfo.setApiRequest(logBuilder.toString());
        		logService.logMyInfo(httpRequest, apiLogInfo);
        		return response;
        		
	        } else {
	        	apiLogInfo.setApiResponse("Moved to else module");
                apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        	response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Something Went Wrong.");
				
				apiLogInfo.setApiRequest(logBuilder.toString());
        		logService.logMyInfo(httpRequest, apiLogInfo);
        		return response;
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
	
	public ServiceResponse getEmployeeTimesheetAsCalender(GetEmployeeSummaryOnExportDTO object) {
		return timesheetDashboardService.getEmployeeTimesheetAsCalender(object);
	}
	
	// Delegate method - implementation moved to TimesheetDashboardService
	private ServiceResponse getEmployeeTimesheetAsCalenderInternal(GetEmployeeSummaryOnExportDTO object) {
		
		ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getEmployeeTimesheetAsCalender");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("getEmployeeTimesheetAsCalender");
	    try {
	    	List<Object[]> empTimesheet;
	    	Long empId = Long.parseLong(object.getEmpId().toString());
	    	Integer month = Integer.parseInt(object.getMonth().toString());
	    	Integer year = Integer.parseInt(object.getYear().toString());
	    	Integer projectId = Integer.parseInt(object.getProjectId().toString());
	    	Boolean flag = timesheetsRepository.checkProjectIsClientApplicable(projectId);
	    	if(flag == null || flag == false) {
	    		empTimesheet= timesheetsRepository.getEmployeeTimesheetAsCalenderForAllEmp(empId,month,year,projectId);
	    	}else{
	    		empTimesheet= timesheetsRepository.getEmployeeTimesheetAsCalender(empId,month,year,projectId);
	    	}
	    	
	    	List<GetEmployeeTimesheetAsCalenderDTO> dtoList = new ArrayList<>();
 
	    	for (Object[] obj : empTimesheet) {
	    	    GetEmployeeTimesheetAsCalenderDTO dto = new GetEmployeeTimesheetAsCalenderDTO();

	    	    dto.setEmpId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
	    	    dto.setClientSideId(obj[1] != null ? obj[1].toString() : null);
	    	    dto.setStartDate(obj[2] != null ? obj[2].toString() : null);
	    	    dto.setTeamName(obj[3] != null ? obj[3].toString() : null);
	    	    dto.setTeamId(obj[4] != null ? Long.parseLong(obj[4].toString()) : null);
	    	    dto.setEmployeeName(obj[5] != null ? obj[5].toString() : null);
	    	    dto.setSpoc(obj[6] != null ? obj[6].toString() : null);
	    	    dto.setBillableType(obj[7] != null ? obj[7].toString() : null);
	    	    dto.setEmployeeRole(obj[8] != null ? obj[8].toString() : null);
	    	    dto.setDepartment(obj[9] != null ? obj[9].toString() : null);
	    	    dto.setProjectId(obj[10] != null ? Integer.parseInt(obj[10].toString()) : null);
	    	    dto.setProjectName(obj[11] != null ? obj[11].toString() : null);
	    	    dto.setProjectManagerName(obj[12] != null ? obj[12].toString() : null);
	    	    dto.setPoNo(obj[13] != null ? obj[13].toString() : null);
	    	    dto.setClientName(obj[14] != null ? obj[14].toString() : null);
	    	    dto.setReportingManagerId(obj[15] != null ? Long.parseLong(obj[15].toString()) : null);
	    	    dto.setMonthName(obj[16] != null ? obj[16].toString() : null);
	    	    dto.setExpectedTimesheetFillCount(obj[17] != null ? Integer.parseInt(obj[17].toString()) : null);
	    	    dto.setClientSideNotFilledCount(obj[18] != null ? Integer.parseInt(obj[18].toString()) : null);
	    	    dto.setClientSidePendingCount(obj[19] != null ? Integer.parseInt(obj[19].toString()) : null);
	    	    dto.setClientSideApprovedCount(obj[20] != null ? Integer.parseInt(obj[20].toString()) : null);
	    	    
	    	    Map<String, TimesheetDataDTO> timesheetData = new HashMap<>();
	    	    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
	    	    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");

	    	    for (int i = 0; i < 31; i++) {
	    	        int baseIndex = 21 + (i * 3);
	    	        String status = obj.length > baseIndex && obj[baseIndex] != null ? obj[baseIndex].toString() : null;
	    	        
	    	        String inTimeRaw = obj.length > (baseIndex + 1) && obj[baseIndex + 1] != null ? obj[baseIndex + 1].toString() : null;
	    	        String outTimeRaw = obj.length > (baseIndex + 2) && obj[baseIndex + 2] != null ? obj[baseIndex + 2].toString() : null;

	    	        String inTime = null;
	    	        String outTime = null;

	    	        try {
	    	        	   if (inTimeRaw != null && !inTimeRaw.isEmpty() && !"NA".equalsIgnoreCase(inTimeRaw)) {
	    	        	        LocalDateTime inDateTime = LocalDateTime.parse(inTimeRaw, inputFormatter);
	    	        	        inTime = inDateTime.format(outputFormatter);
	    	        	    }
	    	        	    if (outTimeRaw != null && !outTimeRaw.isEmpty() && !"NA".equalsIgnoreCase(outTimeRaw)) {
	    	        	        LocalDateTime outDateTime = LocalDateTime.parse(outTimeRaw, inputFormatter);
	    	        	        outTime = outDateTime.format(outputFormatter);
	    	        	    }
	    	        } catch (Exception e) {
	    	            // Handle invalid format if needed
	    	            e.printStackTrace();
	    	        }

	    	        TimesheetDataDTO dayData = new TimesheetDataDTO();
	    	        dayData.setStatus(status);
	    	        dayData.setInTime(inTime);
	    	        dayData.setOutTime(outTime);

	    	        timesheetData.put("d" + (i + 1), dayData);
	    	    }

	    	    dto.setTimesheetData(timesheetData);
	    	    dto.setEmploymentId(obj[114] != null ? obj[114].toString() : null);

	    	    dtoList.add(dto);
	    	}

	    	if(dtoList.isEmpty()){
	    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Unable to fetch the timesheet Data for Employee !!!");
                apiLogInfo.setApiResponse("Failed to set the data in dto \n");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                
            } else {
            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
                apiLogInfo.setApiResponse("Timesheet Data fetched successfully ");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            }
            
            apiLogInfo.setApiRequest(logBuilder.toString());
    		logService.logMyInfo(httpRequest, apiLogInfo);
    		return response;
    		
	    } catch(Exception e) {
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
	

	public ServiceResponse getAllEmployeeDSROfRM(TimesheetDTO timesheetDTO) {
		
		ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getAllEmployeeDSROfRM");
	    apiLogInfo.setLogLevel("INFO");
	    
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append( "getAllEmployeeDSROfRM: \n");
		 try {
			 Optional<List<Object[]>> resultList = null;
			 if(timesheetDTO.getIsSearch()!= null){
//				 resultList = projectRepository.getProjectViewForClientAttendanceStatus();  
			 }else {
			    resultList = projectRepository.getAllEmployeeDSROfRM(timesheetDTO.getManagerId(),timesheetDTO.getFromDate()!= null ? timesheetDTO.getFromDate() : "",timesheetDTO.getToDate()!= null ? timesheetDTO.getToDate() : ""); 
			 }
			 
			 
			 if(!resultList.isPresent()) {
			        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			        response.setServiceResponse("No data found from database");

			        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			        apiLogInfo.setApiResponse("Empty list received from repository."); 
			        apiLogInfo.setLogLevel("ERROR");
			        
			        logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
			 }

	        List<EmployeeViewForClientAttendanceStatusDTO> dtoList = new ArrayList<>();
	        
	        for (Object[] obj : resultList.get()) {
	            EmployeeViewForClientAttendanceStatusDTO dto = new EmployeeViewForClientAttendanceStatusDTO();

	            dto.setEmpId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
	            dto.setClientSideId(obj[1] != null ? obj[1].toString() : null);
	            dto.setStartdate(obj[2] != null ? obj[2].toString() : null);
	            dto.setTeamName(obj[3] != null ? obj[3].toString() : null);
	            dto.setTeamId(obj[4] != null ? Long.parseLong(obj[4].toString()) : null);
	            dto.setName(obj[5] != null ? obj[5].toString() : null);
	            dto.setSpoc(obj[6] != null ? obj[6].toString() : null);
	            dto.setBillableType(obj[7] != null ? obj[7].toString() : null);
	            dto.setEmployeeRole(obj[8] != null ? obj[8].toString() : null);
	            dto.setDepartmentName(obj[9] != null ? obj[9].toString() : null);
	            dto.setProjectId(obj[10] != null ? Integer.parseInt(obj[10].toString()) : null);
	            dto.setProjectName(obj[11] != null ? obj[11].toString() : null);
	            dto.setProjectManagerName(obj[12] != null ? obj[12].toString() : null);
	            dto.setPoNo(obj[13] != null ? obj[13].toString() : null);
	            dto.setClientName(obj[14] != null ? obj[14].toString() : null);
	            dto.setTotalExpectedFillCount(obj[15] != null ? Integer.parseInt(obj[15].toString()) : 0);
	            dto.setTotalIshineFilledCount(obj[16] != null ? Integer.parseInt(obj[16].toString()) : 0);
	            dto.setTotalClientSideNotFilledCount(obj[17] != null ? Integer.parseInt(obj[17].toString()) : 0);
	            dto.setTotalClientSidePendingCount(obj[18] != null ? Integer.parseInt(obj[18].toString()) : 0);
	            dto.setTotalClientSideApprovedCount(obj[19] != null ? Integer.parseInt(obj[19].toString()) : 0);
	            dto.setReportingManagerId(obj[20] != null ? Long.parseLong(obj[20].toString()) : null);
                dto.setEmployeementId(obj[21] != null ? obj[21].toString() : null)	;
                dtoList.add(dto);
	        }

	        
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(dtoList);

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Fetched " + dtoList.size() + " records successfully.");
	        
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
		        
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

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse approveTimesheetRequest(TimesheetDTO timesheetDTO) {
		return timesheetApprovalService.approveTimesheetRequest(timesheetDTO);
	}
	
	// Delegate method - implementation moved to TimesheetApprovalService
	private ServiceResponse approveTimesheetRequestInternal(TimesheetDTO timesheetDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("approveTimesheetRequest");
	    apiLogInfo.setApiUrl("/api/approveTimesheetRequest");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Approving timesheets for ")
	              .append(timesheetDTO.getPendingApprovalList().size())
	              .append(" employee/team pairs.\n");

	    List<TimesheetDTO> approvedList = new ArrayList<>();

	    try {
	        for (TimesheetDTO entry : timesheetDTO.getPendingApprovalList()) {
	            Long empId = entry.getEmpId();
	            Long teamId = entry.getTeamId();

	            logBuilder.append("Processing empId: ").append(empId)
	                      .append(", teamId: ").append(teamId).append("\n");

	            List<Object[]> pendingTimesheets = timesheetsRepository.getPendingTimesheetsByEmpAndTeam(empId, teamId);

	            if (pendingTimesheets != null && !pendingTimesheets.isEmpty()) {
	                for (Object[] object : pendingTimesheets) {
	                    TimesheetDTO dto = new TimesheetDTO();

	                    Long timesheetId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
	                    dto.setTimesheetId(timesheetId);
	                    dto.setDate(object[1] != null ? object[1].toString() : null);
	                    dto.setDayType(object[2] != null ? object[2].toString() : null);
	                    dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
	                    dto.setDescription(object[4] != null ? object[4].toString() : null);
	                    dto.setStatus(entry.getStatus());
	                    dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
	                    dto.setCreatedBy(object[7] != null ? Long.parseLong(object[7].toString()) : null);
	                    if (object[8] != null) {
	                        String rawDate = object[8].toString(); // "2025-08-05 12:38:43.0"

	                        // Correct format that allows optional milliseconds
	                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.S]");
	                        LocalDateTime createdOn = LocalDateTime.parse(rawDate, inputFormatter);

	                        // Optional: reformat into your desired output format
	                        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	                        String formattedCreatedOn = createdOn.format(outputFormatter);

	                        dto.setCreatedOn(formattedCreatedOn);
	                    } else {
	                        dto.setCreatedOn(null);
	                    }


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
	                    dto.setEmploymentId(dto.getEmpId() != null ? employeeRepository.fetchEmploymentIdByEmpId(dto.getEmpId()) : null);
	                    dto.setIsShadowTimesheet(object[27] != null ? (Boolean) object[27] : null);
	                    dto.setShadowEmpId(object[28] != null ? Long.parseLong(object[28].toString()) : null);
	                    if(timesheetId != null) {
							 List<TimesheetDocumentDetailsDTO> details =timesheetDocumentDetailsRepository.findAllDocIdByTimesheetId(timesheetId);
							 for (TimesheetDocumentDetailsDTO doc : details) {
							     if (Boolean.TRUE.equals(doc.getFinalFlag())) {
							         dto.setApprovedDocument(doc.getDocId());
							     }
							     if(Boolean.FALSE.equals(doc.getFinalFlag())) {
							    	 dto.setFilledDocument(doc.getDocId());  	 
							     }
							 }
	 
						}
						
	                    if(timesheetDTO.getRejectionId()!= null) {
	                    	dto.setRejectionId(entry.getRejectionId());
	                    }

	                    // Set status updated by
	                    dto.setTimesheetStatusUpdatedBy(entry.getTimesheetStatusUpdatedBy());

	                    // Call sub-method to approve this timesheet
	                    try {
	                        updateTimesheetRequestById(dto);
	                        approvedList.add(dto);
	                    } catch (Exception e) {
	                        logBuilder.append("Failed to approve timesheetId ").append(timesheetId)
	                                  .append(": ").append(e.getMessage()).append("\n");
	                        e.printStackTrace();
	                    }
	                }
	            } else {
	                logBuilder.append("No pending timesheets found for empId: ").append(empId)
	                          .append(", teamId: ").append(teamId).append("\n");
	            }
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Approved timesheets: " + approvedList.size());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Approved timesheet count: " + approvedList.size());

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Error while approving timesheets.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	public ServiceResponse getEmployeeTimesheetAsCalenderByProjectId(GetEmployeeTimesheetAsCalenderByProjectIdDTO object) {
		
		   ServiceResponse response = new ServiceResponse();

		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setSubFeatureName("getEmployeeTimesheetAsCalenderByProjectId");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();
		    logBuilder.append("getEmployeeTimesheetAsCalenderByProjectId");
		    try {
		    	List<Object[]> empTimesheet;
		    	if(object.getAllEmp()) {
		    		empTimesheet= timesheetsRepository.getEmployeeTimesheetAsCalenderByProjectIdForAllEmp(object.getProjectId(),
			    			object.getMonth(),object.getYear(),object.getEmpId());
		    	} else {
		    		empTimesheet= timesheetsRepository.getEmployeeTimesheetAsCalenderByProjectId(object.getProjectId(),
			    			object.getMonth(),object.getYear(),object.getEmpId());
		    	}
    			
		    	List<GetEmployeeTimesheetAsCalenderDTO> dtoList = new ArrayList<>();

		    	for (Object[] obj : empTimesheet) {
		    	    GetEmployeeTimesheetAsCalenderDTO dto = new GetEmployeeTimesheetAsCalenderDTO();

		    	    dto.setEmpId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
		    	    dto.setClientSideId(obj[1] != null ? obj[1].toString() : null);
		    	    dto.setStartDate(obj[2] != null ? obj[2].toString() : null);
		    	    dto.setTeamName(obj[3] != null ? obj[3].toString() : null);
		    	    dto.setTeamId(obj[4] != null ? Long.parseLong(obj[4].toString()) : null);
		    	    dto.setEmployeeName(obj[5] != null ? obj[5].toString() : null);
		    	    dto.setSpoc(obj[6] != null ? obj[6].toString() : null);
		    	    dto.setBillableType(obj[7] != null ? obj[7].toString() : null);
		    	    dto.setEmployeeRole(obj[8] != null ? obj[8].toString() : null);
		    	    dto.setDepartment(obj[9] != null ? obj[9].toString() : null);
		    	    dto.setProjectId(obj[10] != null ? Integer.parseInt(obj[10].toString()) : null);
		    	    dto.setProjectName(obj[11] != null ? obj[11].toString() : null);
		    	    dto.setProjectManagerName(obj[12] != null ? obj[12].toString() : null);
		    	    dto.setPoNo(obj[13] != null ? obj[13].toString() : null);
		    	    dto.setClientName(obj[14] != null ? obj[14].toString() : null);
		    	    dto.setReportingManagerId(obj[15] != null ? Long.parseLong(obj[15].toString()) : null);
		    	    dto.setMonthName(obj[16] != null ? obj[16].toString() : null);
		    	    dto.setExpectedTimesheetFillCount(obj[17] != null ? Integer.parseInt(obj[17].toString()) : null);
		    	    dto.setClientSideNotFilledCount(obj[18] != null ? Integer.parseInt(obj[18].toString()) : null);
		    	    dto.setClientSidePendingCount(obj[19] != null ? Integer.parseInt(obj[19].toString()) : null);
		    	    dto.setClientSideApprovedCount(obj[20] != null ? Integer.parseInt(obj[20].toString()) : null);
		    	    
		    	    Map<String, TimesheetDataDTO> timesheetData = new HashMap<>();
		    	    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
		    	    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");

		    	    for (int i = 0; i < 31; i++) {
		    	        int baseIndex = 21 + (i * 3);
		    	        String status = obj.length > baseIndex && obj[baseIndex] != null ? obj[baseIndex].toString() : null;
		    	        
		    	        String inTimeRaw = obj.length > (baseIndex + 1) && obj[baseIndex + 1] != null ? obj[baseIndex + 1].toString() : null;
		    	        String outTimeRaw = obj.length > (baseIndex + 2) && obj[baseIndex + 2] != null ? obj[baseIndex + 2].toString() : null;

		    	        String inTime = null;
		    	        String outTime = null;

		    	        try {
		    	            if (inTimeRaw != null && !inTimeRaw.isEmpty()) {
		    	                LocalDateTime inDateTime = LocalDateTime.parse(inTimeRaw, inputFormatter);
		    	                inTime = inDateTime.format(outputFormatter);
		    	            }
		    	            if (outTimeRaw != null && !outTimeRaw.isEmpty()) {
		    	                LocalDateTime outDateTime = LocalDateTime.parse(outTimeRaw, inputFormatter);
		    	                outTime = outDateTime.format(outputFormatter);
		    	            }
		    	        } catch (Exception e) {
		    	            e.printStackTrace();
		    	        }

		    	        TimesheetDataDTO dayData = new TimesheetDataDTO();
		    	        dayData.setStatus(status);
		    	        dayData.setInTime(inTime);
		    	        dayData.setOutTime(outTime);

		    	        timesheetData.put("d" + (i + 1), dayData);
		    	    }

		    	    dto.setTimesheetData(timesheetData);
		    	    dto.setEmploymentId(obj[114] != null ? obj[114].toString() : null);
		    	    dto.setPresent(obj[115] != null ? obj[115].toString() : null);
		    	    dto.setWeekOff(obj[116] != null ? obj[116].toString() : null);
		    	    dto.setHoliday(obj[117] != null ? obj[117].toString() : null);
		    	    dto.setLeave(obj[118] != null ? obj[118].toString() : null);
		    	    dto.setCompOff(obj[119] != null ? obj[119].toString() : null);
		    	    dto.setNa(obj[120] != null ? obj[120].toString() : null);
		    	    dto.setHalfDay(obj[121] != null ? obj[121].toString() : null);
		    	    dto.setTotalNoOfDays(obj[122] != null ? obj[122].toString() : null);
		    	    dto.setApmosysTimesheetFilledCount(obj[123] != null ? Integer.parseInt(obj[123].toString()) : null);
		    	    dto.setEmploymentStatus(obj[124] != null ? obj[124].toString() : null);
		    	    dto.setEndDate(obj[125] != null ? obj[125].toString() : null);
		    	    dto.setReadyForInvoicing(obj[126] != null ? obj[126].toString() : null);
		    	    if (obj[127] != null) {
		    	        int active = Integer.parseInt(obj[127].toString());
		    	        switch (active) {
		    	            case 1:
		    	                dto.setProjectStatus("Mapped");
		    	                break;
		    	            case 0:
		    	                dto.setProjectStatus("Removed");
		    	                break;
		    	            case 2:
		    	                dto.setProjectStatus("Approval Pending");
		    	                break;
		    	            default:
		    	                dto.setProjectStatus("Undefined");
		    	                break;
		    	        }
		    	    } else {
		    	        dto.setProjectStatus("Undefined");
		    	    }

		    	    dtoList.add(dto);
		    	}

		    	if(dtoList.isEmpty()){
		    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Unable to fetch the timesheet Data !!!");
	                apiLogInfo.setApiResponse("Failed to set the data in dto \n");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                
	            } else {
	            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(dtoList);
	                apiLogInfo.setApiResponse("Timesheet Data fetched successfully ");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            }
	            
	            apiLogInfo.setApiRequest(logBuilder.toString());
	    		logService.logMyInfo(httpRequest, apiLogInfo);
	    		return response;
	    		
		    } catch(Exception e) {
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
	
	public ServiceResponse getTimesheetDashboardCountForEmployee(Integer month, Integer year,Long empId,Boolean isClientDashboard,List<String> billableTypes,String employeeActive,String clientSideFilter) {
		return timesheetDashboardService.getTimesheetDashboardCountForEmployee(
				month, year, empId, isClientDashboard, billableTypes, employeeActive, clientSideFilter);
	}
	
	// Delegate method - implementation moved to TimesheetDashboardService
	private ServiceResponse getTimesheetDashboardCountForEmployeeInternal(Integer month, Integer year,Long empId,Boolean isClientDashboard,List<String> billableTypes,String employeeActive,String clientSideFilter) {
		
		ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getTimesheetDashboardCountForEmployee");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("getTimesheetDashboardCountForEmployee");
	    try {
	    	List<Object[]> countForEmployee;
	    	if(isClientDashboard) {
	    		countForEmployee = timesheetsRepository.getTimesheetDashboardCountForEmployee(month,year,empId,clientSideFilter);
	    	}else {
		    	countForEmployee = timesheetsRepository.getTimesheetDashboardCountForAllEmployee(month,year,empId,billableTypes,employeeActive);	
	    	}
	    	if(countForEmployee.isEmpty()){
	    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Unable to fetch the dashboard count for employee!");
                apiLogInfo.setApiResponse("Failed to fetch the dashboard count for employee \n");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                
            } else {
            	if(isClientDashboard) {
            	Object[] row = countForEmployee.get(0);
                TimesheetDashboardCountDTO dto = new TimesheetDashboardCountDTO();
                dto.setTotalApplicableCount(row[0] != null ? ((Number) row[0]).intValue() : 0);
                dto.setApprovedCount(row[1] != null ? ((Number) row[1]).intValue() : 0);
                dto.setDefaulterCount(row[3] != null ? ((Number) row[3]).intValue() : 0);
                dto.setClientSidePendingCount(row[2] != null ? ((Number) row[2]).intValue() : 0);
                dto.setTotaldefaulterCount(row[4] != null ? ((Number) row[4]).intValue() : 0);
                      		
            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dto);
                apiLogInfo.setApiResponse("Dashboard count fetched successfully ");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            	}
            	else {  
            		Object[] row = countForEmployee.get(0);
                    TimesheetDashboardCountDTO dto = new TimesheetDashboardCountDTO();
                    dto.setTotalApplicableCount(row[0] != null ? ((Number) row[0]).intValue() : 0);
                    dto.setApprovedCount(row[1] != null ? ((Number) row[1]).intValue() : 0);
                    dto.setDefaulterCount(row[3] != null ? ((Number) row[3]).intValue() : 0);
                    dto.setClientSidePendingCount(row[2] != null ? ((Number) row[2]).intValue() : 0);
                    dto.setTotaldefaulterCount(row[4] != null ? ((Number) row[4]).intValue() : 0);
                          		
                	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                    response.setServiceResponse(dto);
                    apiLogInfo.setApiResponse("Dashboard count fetched successfully ");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            		
            	}
            }
            
            apiLogInfo.setApiRequest(logBuilder.toString());
    		logService.logMyInfo(httpRequest, apiLogInfo);
    		return response;
    		
	    } catch(Exception e) {
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
	
public ServiceResponse getTimesheetDashboardCountForProject(Integer month, Integer year,Long empId,Boolean isClientDashboard ,List<String> billableType,String projectActive) {
	return timesheetDashboardService.getTimesheetDashboardCountForProject(
			month, year, empId, isClientDashboard, billableType, projectActive);
}

// Delegate method - implementation moved to TimesheetDashboardService
private ServiceResponse getTimesheetDashboardCountForProjectInternal(Integer month, Integer year,Long empId,Boolean isClientDashboard ,List<String> billableType,String projectActive) {
		
		ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getTimesheetDashboardCountForProject");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("getTimesheetDashboardCountForProject");
	    try {
	    	System.err.println("test empId"+empId);
//	    	List<Object[]> countForProject = timesheetsRepository.getTimesheetDashboardCountForProject(month,year,empId);
	    	
	    	List<Object[]> countForProject;
	    	if(isClientDashboard) {
	    	countForProject = timesheetsRepository.getTimesheetDashboardCountForProject(month,year,empId);
	    	}else {
		    countForProject = timesheetsRepository.getAllEmpTimesheetDashboardCountForProject(month,year,empId,billableType,projectActive);
	    	}

	    	if(countForProject.isEmpty()){
	    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Unable to fetch the dashboard count for project!");
                apiLogInfo.setApiResponse("Failed to fetch the dashboard count for project \n");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                
            } else {
            	Object[] row = countForProject.get(0);
                TimesheetDashboardCountDTO dto = new TimesheetDashboardCountDTO();
                dto.setTotalApplicableCount(row[0] != null ? ((Number) row[0]).intValue() : 0);
                dto.setApprovedCount(row[1] != null ? ((Number) row[1]).intValue() : 0);
                dto.setDefaulterCount(row[3] != null ? ((Number) row[3]).intValue() : 0);
                dto.setClientSidePendingCount(row[2] != null ? ((Number) row[2]).intValue() : 0);
                    		
            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dto);
                apiLogInfo.setApiResponse("Dashboard count fetched successfully ");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            }
            
            apiLogInfo.setApiRequest(logBuilder.toString());
    		logService.logMyInfo(httpRequest, apiLogInfo);
    		return response;
    		
	    } catch(Exception e) {
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




public ServiceResponse getLastFilledTimesheetByEmp(Long empId) {
	return timesheetDashboardService.getLastFilledTimesheetByEmp(empId);
}

// Delegate method - implementation moved to TimesheetDashboardService
private ServiceResponse getLastFilledTimesheetByEmpInternal(Long empId) {
	   ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getLastFilledTimesheetByEmp");
	    apiLogInfo.setLogLevel("INFO");
	    
	    
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
		        
		        
		        List<Object[]> resultList = timesheetsRepository.getLastTimesheetFiledByEmpId(empId);
		        
		        if(!resultList.isEmpty()) {
		        	List<LastTimesheetFieldDto> dtoList = Optional.ofNullable(resultList)
		        		    .orElse(Collections.emptyList())
		        		    .stream()
		        		    .map(row -> {
		        		        LastTimesheetFieldDto dto = new LastTimesheetFieldDto();

		        		        dto.setEmployementId(row.length > 0 && row[0] != null ? row[0].toString() : null);
		        		        dto.setOfficeInTime(row.length > 1 && row[1] != null ? ((java.sql.Timestamp) row[1]).toLocalDateTime() : null);
		        		        dto.setOfficeOutTime(row.length > 2 && row[2] != null ? ((java.sql.Timestamp) row[2]).toLocalDateTime() : null);
		        		        dto.setProjectId(row.length > 3 && row[3] != null ? Long.parseLong(row[3].toString()) : null);
		        		        dto.setClientId(row.length > 4 && row[4] != null ? Long.parseLong(row[4].toString()) : null);
		        		        dto.setClientLocationID(row.length > 5 && row[5] != null ? Long.parseLong(row[5].toString()) : null);
		        		        dto.setTeamName(row.length > 6 && row[6] != null ? row[6].toString() : null);
		        		        dto.setActivity(row.length > 7 && row[7] != null ? row[7].toString() : null);
		        		        dto.setActivityID(row.length > 8 && row[8] != null ? Long.parseLong(row[8].toString()) : null);
		        		        dto.setDescription(row.length > 9 && row[9] != null ? row[9].toString() : null);
		        		        dto.setTeamId(row.length > 10 && row[10] != null ? Long.parseLong(row[10].toString()) : null);
		        		        dto.setClientApprovalStatus(
		        		        	    (row.length > 11) 
		        		        	        ? (row[11] != null ? row[11].toString() : null) 
		        		        	        : null
		        		        	);


		        		        dto.setCompletionTime(row.length > 12 && row[12] != null ? Float.parseFloat(row[12].toString()) : null);
		        		        dto.setTimesheetLockUpdatedOn(
		        		        	    row.length > 13 && row[13] != null 
		        		        	        ? ((java.sql.Date) row[13]).toLocalDate() 
		        		        	        : null
		        		        	);
		        		        dto.setIstimesheetLockCheckEnable(row.length > 14 && row[14] != null ? row[14].toString() : null);

		        		        return dto;
		        		    })
		        		    .collect(Collectors.toList());

		        	
		       		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(dtoList);
	            response.setServiceMessage("Last timesheet found for employee.");

	            apiLogInfo.setApiResponse("Last timesheet found for active employee");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		        }else {
		            // Case 4: Active employee but no timesheet found
		            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(Collections.emptyList());
		            response.setServiceMessage("No timesheet found for employee.");

		            apiLogInfo.setApiResponse("No timesheet found");
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		        }

	    }catch (Exception e) {
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

public ServiceResponse getEmployeeByNameAndEmpidForTimesheet(TimesheetDTO timesheetDTO) {
	return timesheetDashboardService.getEmployeeByNameAndEmpidForTimesheet(timesheetDTO);
}

// Delegate method - implementation moved to TimesheetDashboardService
private ServiceResponse getEmployeeByNameAndEmpidForTimesheetInternal(TimesheetDTO timesheetDTO) {
	ServiceResponse response = new ServiceResponse();
	LogDTO apiLogInfo = new LogDTO();
	apiLogInfo.setSubFeatureName("getEmployeeByNameAndEmpidForTimesheet");
	apiLogInfo.setApiUrl("/api/getEmployeeByNameAndEmpidForTimesheet");
	apiLogInfo.setLogLevel("INFO");

	try {

		List<Object[]> employees;
		if(timesheetDTO.getIsClientDashboard()) {
			employees= employeeRepository.getEmployeeByNameAndEmpidForTimesheetClientDashboard(timesheetDTO.getEmpId());
		}else {
			employees= employeeRepository.getEmployeeByNameAndEmpidForTimesheet(timesheetDTO.getEmpId());
		}

		List<GetEmployeeByNameAndEmpldDTO> listDto = new ArrayList<GetEmployeeByNameAndEmpldDTO>();

        if (!employees.isEmpty()) {
            
            for (Object[] object : employees) {
            	 GetEmployeeByNameAndEmpldDTO dto = new GetEmployeeByNameAndEmpldDTO();
                 dto.setEmpId(object[0] != null ? Long.valueOf(object[0].toString()) : null);
                 dto.setName(object[1] != null ? object[1].toString() : null);
                 dto.setEmploymentId(object[2] != null ? object[2].toString() : null) ;
                 listDto.add(dto);
             }
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(listDto);
        }
	} catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse("Error : " + e.getMessage());
	}

	return response;
}


/**
 * getDocumentsByEmpAndDate is used to get TimesheetDocumentDetails
 * by empId and Date for a specific employee used in calendar view
 */
public ServiceResponse getDocumentsByEmpAndDate(TimesheetDTO timesheetDTO) {
	return timesheetQueryService.getDocumentsByEmpAndDate(timesheetDTO);
}

// Delegate method - implementation moved to TimesheetQueryService
private ServiceResponse getDocumentsByEmpAndDateInternal(TimesheetDTO timesheetDTO) {
	ServiceResponse response = new ServiceResponse();
	LogDTO apiLogInfo = new LogDTO();
	apiLogInfo.setSubFeatureName("getDocumentsByEmpAndDate");
	apiLogInfo.setApiUrl("/api/getDocumentsByEmpAndDate");
	apiLogInfo.setLogLevel("INFO");

	StringBuilder logBuilder = new StringBuilder("Received request to get timesheet documents")
			.append(" | EmpId: ").append(timesheetDTO.getEmpId())
			.append(" | Date: ").append(timesheetDTO.getDate());
	apiLogInfo.setApiRequest(logBuilder.toString());

	try {
		
		if (timesheetDTO.getEmpId() == null || timesheetDTO.getEmpId() <= 0) {
			throw new IllegalArgumentException("Employee ID must be a valid positive number.");
		}
		if (timesheetDTO.getDate() == null || timesheetDTO.getDate().trim().isEmpty()) {
			throw new IllegalArgumentException("Date is required.");
		}

		LocalDate date;
		try {
			date = LocalDate.parse(timesheetDTO.getDate());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd");
		}

		List<TimesheetDocumentDetails> docs = timesheetDocumentDetailsRepository
				.findDocumentsByEmpIdAndDate(timesheetDTO.getEmpId(), date);
		if(docs.size()==1 && Boolean.TRUE.equals(docs.get(0).getFinalFlag()) && docs.get(0).getBulkApprovedDocId() != null) {
			FinalDocument finalDoc = finalDocumentRepository.findById(docs.get(0).getBulkApprovedDocId()).orElse(null);
			if (finalDoc != null) {
				TimesheetDocumentDetailsDTO dtoData = new TimesheetDocumentDetailsDTO();
				dtoData.setClientApprovalStatus(docs.get(0).getClientApprovalStatus());
				dtoData.setDocData(finalDoc.getDocData());
				dtoData.setMimeType(finalDoc.getDocMimeType());
				dtoData.setDocName(finalDoc.getDocName());	
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoData);
                response.setServiceMessage("Document fetched successfully.");

                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                apiLogInfo.setApiResponse("Returned FinalDocument DTO for employee " + timesheetDTO.getEmpId());

                logService.logMyInfo(httpRequest, apiLogInfo);
                return response; 
				}
			}
		

		if (docs == null || docs.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceMessage("No documents found for the given employee and date.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse("No documents found.");
		} else {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(docs);
			response.setServiceMessage("Documents fetched successfully.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("Fetched " + docs.size() + " document(s).");
		}

	} catch (IllegalArgumentException ex) {
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceMessage(ex.getMessage());
		response.setServiceError(ex.toString());
		apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		apiLogInfo.setApiResponse("Validation Error: " + ex.getMessage());
		apiLogInfo.setLogLevel("WARN");

	} catch (Exception ex) {
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceMessage("Unexpected error occurred while fetching documents.");
		response.setServiceError(ex.toString());
		apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		apiLogInfo.setApiResponse("Unexpected Exception: " + ex.getMessage());
		apiLogInfo.setLogLevel("ERROR");
	} finally {
		logService.logMyInfo(httpRequest, apiLogInfo);
	}

	return response;
}

	public ServiceResponse getEmployeeSummaryOnExport(GetEmployeeSummaryOnExportDTO object) {
		return timesheetDashboardService.getEmployeeSummaryOnExport(object);
	}
	
	// Delegate method - implementation moved to TimesheetDashboardService
	private ServiceResponse getEmployeeSummaryOnExportInternal(GetEmployeeSummaryOnExportDTO object) {
	
	   ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getEmployeeSummaryOnExport");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("getEmployeeTimesheetAsCalenderByProjectId");
	    try {
	    	List<Object[]> empTimesheet;
	    	if(object.getAllEmp()) {
	    		empTimesheet= timesheetsRepository.getEmployeeSummaryReportAll(object.getMonth(),
	    				object.getYear(),object.getEmpId(),object.getBillableType());
	    	} else {
	    		empTimesheet= timesheetsRepository.getEmployeeSummaryReportClientSideApplicable(object.getMonth(),
	    				object.getYear(),object.getEmpId());
	    	}
	    			List<GetEmployeeTimesheetAsCalenderDTO> dtoList = new ArrayList<>();

	    	for (Object[] obj : empTimesheet) {
	    	    GetEmployeeTimesheetAsCalenderDTO dto = new GetEmployeeTimesheetAsCalenderDTO();

	    	    dto.setEmpId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
	    	    dto.setClientSideId(obj[1] != null ? obj[1].toString() : null);
	    	    dto.setStartDate(obj[2] != null ? obj[2].toString() : null);
	    	    dto.setTeamName(obj[3] != null ? obj[3].toString() : null);
	    	    dto.setTeamId(obj[4] != null ? Long.parseLong(obj[4].toString()) : null);
	    	    dto.setEmployeeName(obj[5] != null ? obj[5].toString() : null);
	    	    dto.setSpoc(obj[6] != null ? obj[6].toString() : null);
	    	    dto.setBillableType(obj[7] != null ? obj[7].toString() : null);
	    	    dto.setEmployeeRole(obj[8] != null ? obj[8].toString() : null);
	    	    dto.setDepartment(obj[9] != null ? obj[9].toString() : null);
	    	    dto.setProjectId(obj[10] != null ? Integer.parseInt(obj[10].toString()) : null);
	    	    dto.setProjectName(obj[11] != null ? obj[11].toString() : null);
	    	    dto.setProjectManagerName(obj[12] != null ? obj[12].toString() : null);
	    	    dto.setPoNo(obj[13] != null ? obj[13].toString() : null);
	    	    dto.setClientName(obj[14] != null ? obj[14].toString() : null);
	    	    dto.setReportingManagerId(obj[15] != null ? Long.parseLong(obj[15].toString()) : null);
	    	    dto.setMonthName(obj[16] != null ? obj[16].toString() : null);
	    	    dto.setExpectedTimesheetFillCount(obj[17] != null ? Integer.parseInt(obj[17].toString()) : null);
	    	    dto.setApmosysTimesheetFilledCount(obj[123] != null ? Integer.parseInt(obj[123].toString()) : null);
	    	    dto.setClientSideNotFilledCount(obj[18] != null ? Integer.parseInt(obj[18].toString()) : null);
	    	    dto.setClientSidePendingCount(obj[19] != null ? Integer.parseInt(obj[19].toString()) : null);
	    	    dto.setClientSideApprovedCount(obj[20] != null ? Integer.parseInt(obj[20].toString()) : null);
	    	    
	    	    Map<String, TimesheetDataDTO> timesheetData = new HashMap<>();
	    	    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
	    	    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");

	    	    for (int i = 0; i < 31; i++) {
	    	        int baseIndex = 21 + (i * 3);
	    	        String status = obj.length > baseIndex && obj[baseIndex] != null ? obj[baseIndex].toString() : null;
	    	        
	    	        String inTimeRaw = obj.length > (baseIndex + 1) && obj[baseIndex + 1] != null ? obj[baseIndex + 1].toString() : null;
	    	        String outTimeRaw = obj.length > (baseIndex + 2) && obj[baseIndex + 2] != null ? obj[baseIndex + 2].toString() : null;

	    	        String inTime = null;
	    	        String outTime = null;

	    	        try {
	    	            if (inTimeRaw != null && !inTimeRaw.isEmpty()) {
	    	                LocalDateTime inDateTime = LocalDateTime.parse(inTimeRaw, inputFormatter);
	    	                inTime = inDateTime.format(outputFormatter);
	    	            }
	    	            if (outTimeRaw != null && !outTimeRaw.isEmpty()) {
	    	                LocalDateTime outDateTime = LocalDateTime.parse(outTimeRaw, inputFormatter);
	    	                outTime = outDateTime.format(outputFormatter);
	    	            }
	    	        } catch (Exception e) {
	    	            e.printStackTrace();
	    	        }

	    	        TimesheetDataDTO dayData = new TimesheetDataDTO();
	    	        dayData.setStatus(status);
	    	        dayData.setInTime(inTime);
	    	        dayData.setOutTime(outTime);

	    	        timesheetData.put("d" + (i + 1), dayData);
	    	    }

	    	    dto.setTimesheetData(timesheetData);
	    	    dto.setEmploymentId(obj[114] != null ? obj[114].toString() : null);
	    	    dto.setPresent(obj[115] != null ? obj[115].toString() : null);
	    	    dto.setWeekOff(obj[116] != null ? obj[116].toString() : null);
	    	    dto.setHoliday(obj[117] != null ? obj[117].toString() : null);
	    	    dto.setLeave(obj[118] != null ? obj[118].toString() : null);
	    	    dto.setCompOff(obj[119] != null ? obj[119].toString() : null);
	    	    dto.setNa(obj[120] != null ? obj[120].toString() : null);
	    	    dto.setHalfDay(obj[121] != null ? obj[121].toString() : null);
	    	    dto.setTotalNoOfDays(obj[122] != null ? obj[122].toString() : null);

	    	    dtoList.add(dto);
	    	}

	    	if(dtoList.isEmpty()){
	    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
             response.setServiceResponse("Unable to fetch the timesheet Data !!!");
             apiLogInfo.setApiResponse("Failed to set the data in dto \n");
             apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
             
         } else {
         	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
             response.setServiceResponse(dtoList);
             apiLogInfo.setApiResponse("Timesheet Data fetched successfully ");
             apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
         }
         
         apiLogInfo.setApiRequest(logBuilder.toString());
 		logService.logMyInfo(httpRequest, apiLogInfo);
 		return response;
 		
	    } catch(Exception e) {
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
	
//	public ServiceResponse getEmployeeViewForClientAttendanceStatus(GetEmployeeSummaryOnExportDTO object) {
//		
//		   ServiceResponse response = new ServiceResponse();
//
//		    LogDTO apiLogInfo = new LogDTO();
//		    apiLogInfo.setSubFeatureName("getEmployeeViewForClientAttendanceStatus");
//		    apiLogInfo.setLogLevel("INFO");
//		    StringBuilder logBuilder = new StringBuilder();
//		    logBuilder.append("getEmployeeViewForClientAttendanceStatus	");
//		    try {
//		    	List<Object[]> empTimesheet;
//		    	if(object.getAllEmp()) {
//		    		empTimesheet= timesheetsRepository.getEmployeeSummaryReportAllEMP(object.getMonth(),
//		    				object.getYear(),object.getEmpId(),object.getBillableType(),object.getStatus());
//		    	} else {
//		    		empTimesheet= timesheetsRepository.getEmployeeViewForClientAttendanceStatus(object.getMonth(),
//		    				object.getYear(),object.getEmpId(),object.getStatus());
//		    	}
//		    			List<GetEmployeeTimesheetAsCalenderDTO> dtoList = new ArrayList<>();
//
//		    	for (Object[] obj : empTimesheet) {
//		    	    GetEmployeeTimesheetAsCalenderDTO dto = new GetEmployeeTimesheetAsCalenderDTO();
//
//		    	    dto.setEmpId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
//		    	    dto.setClientSideId(obj[1] != null ? obj[1].toString() : null);
//		    	    dto.setStartDate(obj[2] != null ? obj[2].toString() : null);
//		    	    dto.setTeamName(obj[3] != null ? obj[3].toString() : null);
//		    	    dto.setTeamId(obj[4] != null ? Long.parseLong(obj[4].toString()) : null);
//		    	    dto.setEmployeeName(obj[5] != null ? obj[5].toString() : null);
//		    	    dto.setSpoc(obj[6] != null ? obj[6].toString() : null);
//		    	    dto.setBillableType(obj[7] != null ? obj[7].toString() : null);
//		    	    dto.setEmployeeRole(obj[8] != null ? obj[8].toString() : null);
//		    	    dto.setDepartment(obj[9] != null ? obj[9].toString() : null);
//		    	    dto.setProjectId(obj[10] != null ? Integer.parseInt(obj[10].toString()) : null);
//		    	    dto.setProjectName(obj[11] != null ? obj[11].toString() : null);
//		    	    dto.setProjectManagerName(obj[12] != null ? obj[12].toString() : null);
//		    	    dto.setPoNo(obj[13] != null ? obj[13].toString() : null);
//		    	    dto.setClientName(obj[14] != null ? obj[14].toString() : null);
//		    	    dto.setReportingManagerId(obj[15] != null ? Long.parseLong(obj[15].toString()) : null);
//		    	    dto.setMonthName(obj[16] != null ? obj[16].toString() : null);
//		    	    dto.setExpectedTimesheetFillCount(obj[17] != null ? Integer.parseInt(obj[17].toString()) : null);
//		    	    dto.setClientSideNotFilledCount(obj[18] != null ? Integer.parseInt(obj[18].toString()) : null);
//		    	    dto.setClientSidePendingCount(obj[19] != null ? Integer.parseInt(obj[19].toString()) : null);
//		    	    dto.setClientSideApprovedCount(obj[20] != null ? Integer.parseInt(obj[20].toString()) : null);
//		    	    
//		    	    Map<String, TimesheetDataDTO> timesheetData = new HashMap<>();
//		    	    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
//		    	    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");
//
//		    	    for (int i = 0; i < 31; i++) {
//		    	        int baseIndex = 21 + (i * 3);
//		    	        String status = obj.length > baseIndex && obj[baseIndex] != null ? obj[baseIndex].toString() : null;
//		    	        
//		    	        String inTimeRaw = obj.length > (baseIndex + 1) && obj[baseIndex + 1] != null ? obj[baseIndex + 1].toString() : null;
//		    	        String outTimeRaw = obj.length > (baseIndex + 2) && obj[baseIndex + 2] != null ? obj[baseIndex + 2].toString() : null;
//
//		    	        String inTime = null;
//		    	        String outTime = null;
//
//		    	        try {
//		    	            if (inTimeRaw != null && !inTimeRaw.isEmpty()) {
//		    	                LocalDateTime inDateTime = LocalDateTime.parse(inTimeRaw, inputFormatter);
//		    	                inTime = inDateTime.format(outputFormatter);
//		    	            }
//		    	            if (outTimeRaw != null && !outTimeRaw.isEmpty()) {
//		    	                LocalDateTime outDateTime = LocalDateTime.parse(outTimeRaw, inputFormatter);
//		    	                outTime = outDateTime.format(outputFormatter);
//		    	            }
//		    	        } catch (Exception e) {
//		    	            e.printStackTrace();
//		    	        }
//
//		    	        TimesheetDataDTO dayData = new TimesheetDataDTO();
//		    	        dayData.setStatus(status);
//		    	        dayData.setInTime(inTime);
//		    	        dayData.setOutTime(outTime);
//
//		    	        timesheetData.put("d" + (i + 1), dayData);
//		    	    }
//
//		    	    dto.setTimesheetData(timesheetData);
//		    	    dto.setEmploymentId(obj[114] != null ? obj[114].toString() : null);
//		    	    dto.setPresent(obj[115] != null ? obj[115].toString() : null);
//		    	    dto.setWeekOff(obj[116] != null ? obj[116].toString() : null);
//		    	    dto.setHoliday(obj[117] != null ? obj[117].toString() : null);
//		    	    dto.setLeave(obj[118] != null ? obj[118].toString() : null);
//		    	    dto.setCompOff(obj[119] != null ? obj[119].toString() : null);
//		    	    dto.setNa(obj[120] != null ? obj[120].toString() : null);
//		    	    dto.setHalfDay(obj[121] != null ? obj[121].toString() : null);
//		    	    dto.setTotalNoOfDays(obj[122] != null ? obj[122].toString() : null);
//		    	    dto.setApmosysTimesheetFilledCount(obj[123] != null ? Integer.parseInt(obj[123].toString()) : null);
//		    	    dto.setEmploymentStatus(obj[124] != null ? obj[124].toString() : null);
//		    	    dto.setEndDate(obj[125] != null ? obj[125].toString() : null);
//		    	    dto.setReadyForInvoicing(obj[126] != null ? obj[126].toString() : null);
//		    	    if (obj[127] != null) {
//		    	        int active = Integer.parseInt(obj[127].toString());
//		    	        switch (active) {
//		    	            case 1:
//		    	                dto.setProjectStatus("Mapped");
//		    	                break;
//		    	            case 0:
//		    	                dto.setProjectStatus("Removed");
//		    	                break;
//		    	            case 2:
//		    	                dto.setProjectStatus("Approval Pending");
//		    	                break;
//		    	            default:
//		    	                dto.setProjectStatus("Undefined");
//		    	                break;
//		    	        }
//		    	    } else {
//		    	        dto.setProjectStatus("Undefined");
//		    	    }
//		    	    if (obj[128] != null) {
//		    	        String active = obj[128].toString();
//		    	        switch (active) {
//		    	            case "true":
//		    	                dto.setProjectActive("Active");
//		    	                break;
//		    	            case "false":
//		    	                dto.setProjectActive("Inactive");
//		    	                break;
//		    	            default:
//		    	                dto.setProjectActive("Undefined");
//		    	                break;
//		    	        }
//		    	    } else {
//		    	        dto.setProjectActive("Undefined");
//		    	    }
//		    	    
//
//		    	    dtoList.add(dto);
//		    	}
//
//		    	if(dtoList.isEmpty()){
//		    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	             response.setServiceResponse("Unable to fetch the timesheet Data !!!");
//	             apiLogInfo.setApiResponse("Failed to set the data in dto \n");
//	             apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	             
//	         } else {
//	         	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	             response.setServiceResponse(dtoList);
//	             apiLogInfo.setApiResponse("Timesheet Data fetched successfully ");
//	             apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//	         }
//	         
//	         apiLogInfo.setApiRequest(logBuilder.toString());
//	 		logService.logMyInfo(httpRequest, apiLogInfo);
//	 		return response;
//	 		
//		    } catch(Exception e) {
//			    	    e.printStackTrace();
//				        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//				        response.setServiceResponse("Something went wrong.");
//				        response.setServiceError(e.getMessage());
//				        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				        apiLogInfo.setApiResponse(e.getMessage()); 
//				        apiLogInfo.setLogLevel("ERROR");
//			    }
//			    
//		    return response;
//		}
//	
	
	public ServiceResponse getEmployeeViewForClientAttendanceStatus(GetEmployeeSummaryOnExportDTO object) {

	    ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getEmployeeViewForClientAttendanceStatus");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("getEmployeeViewForClientAttendanceStatus	");

	    try {

	        List<Object[]> empTimesheet;
	        int page = object.getPage(); 
			int pageSize = object.getSize();
			int offset = (page-1) * pageSize; 
			String employmentId = getStringColumnFilterValue(object.getFilters().getEmploymentId());
			String clientsideId = getStringColumnFilterValue(object.getFilters().getClientSideId());
			String employeeName = getStringColumnFilterValue(object.getFilters().getEmployeeName());
			String billableType = getStringColumnFilterValue(object.getFilters().getBillableType());
			String projectName = getStringColumnFilterValue(object.getFilters().getProjectName());
			String poNo = getStringColumnFilterValue(object.getFilters().getPoNo());
			String projectManagers = getStringColumnFilterValue(object.getFilters().getProjectManagerName());
			String clientName = getStringColumnFilterValue(object.getFilters().getClientName());
			String teamName = getStringColumnFilterValue(object.getFilters().getTeamName());
			String department = getStringColumnFilterValue(object.getFilters().getDepartment());
			String employmentStatus = getStringColumnFilterValue(object.getFilters().getEmploymentStatus());
			String projectStatus = getStringColumnFilterValue(object.getFilters().getProjectStatus());
			String statusCode = null;
			if( projectStatus !=null && !projectStatus.isEmpty()) {
			if ("Mapped".equalsIgnoreCase(projectStatus) || projectStatus.contains("map") || projectStatus.startsWith("m") ) {
			    statusCode = "1";
			} else if ("Removed".equalsIgnoreCase(projectStatus)|| projectStatus.contains("re") || projectStatus.startsWith("r") ) {
			    statusCode = "0";
			} else if ("Approval Pending".equalsIgnoreCase(projectStatus) || projectStatus.contains("Ap") || projectStatus.startsWith("a")) {
			    statusCode = "2";
			}
			}
			List<Long> employeeIds = new ArrayList<Long>();
			
			Integer totalDistinctEmployees;

	        if (object.getAllEmp()) {
	        	employeeIds = timesheetsRepository.getPaginatedEmployeeIds(
						object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),   
	                    object.getBillableType(),
	                    object.getStatus(),object.getEmployeeActive(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,statusCode,offset,pageSize);
	        	
	            empTimesheet = timesheetsRepository.getEmployeeSummaryReportAllEMP(
	                    object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),   
	                    object.getBillableType(),
	                    object.getStatus(),object.getEmployeeActive(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,statusCode,
	                    object.getSortBy(),
	                    object.getSortDirection(),employeeIds);
	            
	            totalDistinctEmployees = timesheetsRepository.getTotalEmployeeCount(
						object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),   
	                    object.getBillableType(),
	                    object.getStatus(),object.getEmployeeActive(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,statusCode);
	        } else {
	        	employeeIds = timesheetsRepository.getPaginatedEmployeeIdsForClientAttendance(
	        			object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),
	                    object.getStatus(),
						object.getClientSideFilter(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,employmentStatus,statusCode,offset,pageSize);
	        	
	            empTimesheet = timesheetsRepository.getEmployeeViewForClientAttendanceStatus(
	                    object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),
	                    object.getStatus(),
						object.getClientSideFilter(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,employmentStatus,statusCode,
	                    object.getSortBy(),
	                    object.getSortDirection(),employeeIds
						);
	            
	            totalDistinctEmployees = timesheetsRepository.getTotalEmployeeCountForClientApplicable(
	            		object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),
	                    object.getStatus(),
						object.getClientSideFilter(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,employmentStatus,statusCode);
	        }
	        
	        // Map to hold employees grouped by empId
	        Map<Long, EmployeeInfoDTO> employeeMap = new HashMap<>();

	        for (Object[] obj : empTimesheet) {

	            Long empId = obj[0] != null ? Long.parseLong(obj[0].toString()) : null;

	            // ---------- Create EmployeeInfoDTO only once per Employee ----------
	            EmployeeInfoDTO employeeInfo = employeeMap.get(empId);

	            if (employeeInfo == null) {

	                employeeInfo = new EmployeeInfoDTO();

	                employeeInfo.setEmpId(empId);
	                employeeInfo.setEmployeeName(obj[5] != null ? obj[5].toString() : null);
	                employeeInfo.setDepartment(obj[9] != null ? obj[9].toString() : null);
	                employeeInfo.setEmploymentId(obj[114] != null ? obj[114].toString() : null);

	                employeeInfo.setProjectTimesheet(new ArrayList<>());

	                employeeMap.put(empId, employeeInfo);
	            }

	            // ---------- Create project + timesheet DTO ----------
	            ProjectTimesheetInfoDTO projectInfo = new ProjectTimesheetInfoDTO();

	            projectInfo.setClientSideId(obj[1] != null ? obj[1].toString() : null);
	            projectInfo.setStartDate(obj[2] != null ? obj[2].toString() : null);
	            projectInfo.setTeamName(obj[3] != null ? obj[3].toString() : null);
	            projectInfo.setTeamId(obj[4] != null ? Long.parseLong(obj[4].toString()) : null);
	            projectInfo.setSpoc(obj[6] != null ? obj[6].toString() : null);
	            projectInfo.setBillableType(obj[7] != null ? obj[7].toString() : null);
	            projectInfo.setEmployeeRole(obj[8] != null ? obj[8].toString() : null);
	            projectInfo.setProjectId(obj[10] != null ? Integer.parseInt(obj[10].toString()) : null);
	            projectInfo.setProjectName(obj[11] != null ? obj[11].toString() : null);
	            projectInfo.setProjectManagerName(obj[12] != null ? obj[12].toString() : null);
	            projectInfo.setPoNo(obj[13] != null ? obj[13].toString() : null);
	            projectInfo.setClientName(obj[14] != null ? obj[14].toString() : null);
	            projectInfo.setReportingManagerId(obj[15] != null ? Long.parseLong(obj[15].toString()) : null);
	            projectInfo.setMonthName(obj[16] != null ? obj[16].toString() : null);

	            projectInfo.setExpectedTimesheetFillCount(obj[17] != null ? Integer.parseInt(obj[17].toString()) : null);
	            projectInfo.setClientSideNotFilledCount(obj[18] != null ? Integer.parseInt(obj[18].toString()) : null);
	            projectInfo.setClientSidePendingCount(obj[19] != null ? Integer.parseInt(obj[19].toString()) : null);
	            projectInfo.setClientSideApprovedCount(obj[20] != null ? Integer.parseInt(obj[20].toString()) : null);

	            // ---------- 31 days timesheet mapping ----------
	            Map<String, TimesheetDataDTO> timesheetData = new HashMap<>();
	            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
	            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");

	            for (int i = 0; i < 31; i++) {

	                int baseIndex = 21 + (i * 3);

	                String status = obj.length > baseIndex && obj[baseIndex] != null
	                        ? obj[baseIndex].toString()
	                        : null;

	                String inTimeRaw = obj.length > (baseIndex + 1) && obj[baseIndex + 1] != null
	                        ? obj[baseIndex + 1].toString()
	                        : null;

	                String outTimeRaw = obj.length > (baseIndex + 2) && obj[baseIndex + 2] != null
	                        ? obj[baseIndex + 2].toString()
	                        : null;

	                String inTime = null;
	                String outTime = null;

	                try {
	                    if (inTimeRaw != null && !inTimeRaw.isEmpty()) {
	                        LocalDateTime inDateTime = LocalDateTime.parse(inTimeRaw, inputFormatter);
	                        inTime = inDateTime.format(outputFormatter);
	                    }
	                    if (outTimeRaw != null && !outTimeRaw.isEmpty()) {
	                        LocalDateTime outDateTime = LocalDateTime.parse(outTimeRaw, inputFormatter);
	                        outTime = outDateTime.format(outputFormatter);
	                    }
	                } catch (Exception e) {}

	                TimesheetDataDTO dayData = new TimesheetDataDTO();
	                dayData.setStatus(status);
	                dayData.setInTime(inTime);
	                dayData.setOutTime(outTime);

	                timesheetData.put("d" + (i + 1), dayData);
	            }

	            projectInfo.setTimesheetData(timesheetData);

	            // ---------- Summary fields ----------
	            projectInfo.setPresent(obj[115] != null ? obj[115].toString() : null);
	            projectInfo.setWeekOff(obj[116] != null ? obj[116].toString() : null);
	            projectInfo.setHoliday(obj[117] != null ? obj[117].toString() : null);
	            projectInfo.setLeave(obj[118] != null ? obj[118].toString() : null);
	            projectInfo.setCompOff(obj[119] != null ? obj[119].toString() : null);
	            projectInfo.setNa(obj[120] != null ? obj[120].toString() : null);
	            projectInfo.setHalfDay(obj[121] != null ? obj[121].toString() : null);
	            projectInfo.setTotalNoOfDays(obj[122] != null ? obj[122].toString() : null);
	            projectInfo.setApmosysTimesheetFilledCount(obj[123] != null ? Integer.parseInt(obj[123].toString()) : null);
	            projectInfo.setEmploymentStatus(obj[124] != null ? obj[124].toString() : null);
	            projectInfo.setEndDate(obj[125] != null ? obj[125].toString() : null);
	            projectInfo.setReadyForInvoicing(obj[126] != null ? obj[126].toString() : null);

	            // ---------- Project Status (Integer) ----------
	            if (obj[127] != null) {
	                int active = Integer.parseInt(obj[127].toString());
	                switch (active) {
	                    case 1:
	                        projectInfo.setProjectStatus("Mapped");
	                        break;
	                    case 0:
	                        projectInfo.setProjectStatus("Removed");
	                        break;
	                    case 2:
	                        projectInfo.setProjectStatus("Approval Pending");
	                        break;
	                    default:
	                        projectInfo.setProjectStatus("Undefined");
	                }
	            }

	            // ---------- Project Active (Boolean) ----------
	            if (obj[128] != null) {
	                String active = obj[128].toString();
	                if ("true".equals(active)) projectInfo.setProjectActive("Active");
	                else if ("false".equals(active)) projectInfo.setProjectActive("Inactive");
	                else projectInfo.setProjectActive("Undefined");
	            }

	            // Add project info to employee
	            employeeInfo.getProjectTimesheet().add(projectInfo);
	        }

	        // Convert map to list
	        List<EmployeeInfoDTO> finalList = new ArrayList<>(employeeMap.values());

			//code for sorting project-status mapped first then removed.
			for (EmployeeInfoDTO employee : finalList) {

				if (employee.getProjectTimesheet() != null && !employee.getProjectTimesheet().isEmpty()) {
					
					List<ProjectTimesheetInfoDTO> sortedProjects = employee.getProjectTimesheet().stream()
						.sorted((p1, p2) -> {

							boolean p1IsMapped = "Mapped".equalsIgnoreCase(p1.getProjectStatus());
							boolean p2IsMapped = "Mapped".equalsIgnoreCase(p2.getProjectStatus());
							
							if (p1IsMapped && !p2IsMapped) {
								return -1; // p1 comes first
							} else if (!p1IsMapped && p2IsMapped) {
								return 1; // p2 comes first
							} else {
								// If both are same status, you can add secondary sorting here
								// For example, by project name or start date
								return 0;
							}
						})
						.collect(Collectors.toList());
					
					employee.setProjectTimesheet(sortedProjects);
				}
			}

	        if (finalList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Unable to fetch the timesheet Data !!!");
	            apiLogInfo.setApiResponse("Failed to set the data in dto \n");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(finalList);
		        response.setTotalElements(totalDistinctEmployees);
	            apiLogInfo.setApiResponse("Timesheet Data fetched successfully ");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        }

	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;

	    } catch (Exception e) {

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



	 public ServiceResponse isEmployeeInTNMProject(Long empId) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/isEmployeeInTNMProject");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        Boolean isInTNM = timesheetsRepository.isInTNMProject(empId);

	        if (Boolean.TRUE.equals(isInTNM)) {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(true);
	            response.setServiceMessage("Employee is part of at least one active TNM project.");

	            apiLogInfo.setApiResponse("Employee is in TNM project. EmpId: " + empId);
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(false);
	            response.setServiceMessage("Employee is not part of any active TNM project.");

	            apiLogInfo.setApiResponse("Employee not in TNM project. EmpId: " + empId);
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        }

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
	 
	 
	 
//	 public ServiceResponse wasEmployeeInClientProjCurrAndPrevMon(Long empId) {
//		    ServiceResponse response = new ServiceResponse();
//		    LogDTO apiLogInfo = new LogDTO();
//		    apiLogInfo.setApiUrl("/api/wasEmployeeInClientProjCurrAndPrevMon");
//		    apiLogInfo.setLogLevel("INFO");
//
//		    try {
//		        Boolean bulkUploadApplicable = timesheetsRepository.wasEmployeeInClientProjCurrAndPrevMon(empId);
//
//		        if (Boolean.TRUE.equals(bulkUploadApplicable)) {
//		            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//		            response.setServiceResponse(true);
//		            response.setServiceMessage("Employee is/was part of at least one client project.");
//
//		            apiLogInfo.setApiResponse("Employee is/was part of at least one client project.. EmpId: " + empId);
//		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//		        } else {
//		            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//		            response.setServiceResponse(false);
//		            response.setServiceMessage("Employee is/was not part of at least one client project.");
//
//		            apiLogInfo.setApiResponse("Employee is/was not part of at least one client project.. EmpId: " + empId);
//		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//		        }
//
//		    } catch (Exception e) {
//		        e.printStackTrace();
//		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//		        response.setServiceResponse("Something went wrong.");
//		        response.setServiceError(e.getMessage());
//
//		        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//		        apiLogInfo.setApiResponse(e.getMessage());
//		        apiLogInfo.setLogLevel("ERROR");
//		    }
//
//		    logService.logMyInfo(httpRequest, apiLogInfo);
//		    return response;
//		}
	 
	 public ServiceResponse wasEmployeeInClientProjCurrAndPrevMon(Long empId) {

		    ServiceResponse response = new ServiceResponse();
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setApiUrl("/api/wasEmployeeInClientProjCurrAndPrevMon");
		    apiLogInfo.setLogLevel("INFO");

		    try {
		        Integer dbResult = timesheetsRepository.wasEmployeeInClientProjCurrAndPrevMon(empId);
		        Boolean bulkUploadApplicable = (dbResult != null && dbResult == 1);

		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse(bulkUploadApplicable);

		        if (bulkUploadApplicable) {
		        	response.setServiceResponse(true);
		            response.setServiceMessage("Employee is/was part of at least one client project.");
		            apiLogInfo.setApiResponse("Employee is/was part of at least one client project. EmpId: " + empId);
		        } else {
		        	response.setServiceResponse(false);
		            response.setServiceMessage("Employee is/was not part of any client project.");
		            apiLogInfo.setApiResponse("Employee is/was not part of any client project. EmpId: " + empId);
		        }

		        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		    } catch (Exception e) {
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        
		        response.setServiceError(e.getMessage());

		        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		        apiLogInfo.setApiResponse(e.getMessage());
		        apiLogInfo.setLogLevel("ERROR");
		    }

		    logService.logMyInfo(httpRequest, apiLogInfo);
		    return response;
		}


	public ServiceResponse getProjectViewForClientAttendanceStatus(@RequestBody TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getProjectViewForClientAttendanceStatus");
	    apiLogInfo.setLogLevel("INFO");
	    
	    String projectName = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getProjectName());
	    String poNo = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getPoNo());
	    String projectManagerName = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getProjectManagerName());
	    String projectType = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getProjectType());
	    String clientName = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getClientName());
	    String apmosysRM = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getApmosysRm());
	    String apmosysRMEmail = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getApmosysRmEmail());
	    String clientRM = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getClientRm());
	    Integer totalExpectedFillCount = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getTotalExpectedFillCount());
	    Integer totalClientSideApprovedCount = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getTotalClientSideApprovedCount());
	    Integer totalClientSidePendingCount = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getTotalClientSidePendingCount());
	    Integer totalClientSideNotFilledCou = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getTotalClientSideNotFilledCount());
	    Integer totalEmployees = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getTotalEmployees());
	    String active = null;
	    String activeValue = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getActive());
	    String startDate = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getStartDate());
	    String endDate = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getEndDate());
	    if (activeValue != null) {  
	        String lower = activeValue.trim().toLowerCase();

	        // check explicit full-word containment first for safety
	        if (lower.contains("inactive")) {
	            active = "false";
	        } else if (lower.contains("active")) {
	            // only reached if "inactive" wasn't present (avoids misclassifying "inactive")
	            active = "true";
	        } else {
	            // accept short partial prefixes commonly typed by users
	            if (lower.startsWith("in") || lower.startsWith("ina") || lower.startsWith("inac")) {
	                active = "false";
	            } else if (lower.startsWith("a") || lower.startsWith("ac") || lower.startsWith("act")) {
	                active = "true";
	            }
	        }
	    }

	    
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append( "getProjectViewForClientAttendanceStatus: \n");
		 try {
			 List<Object[]> resultList;
			 int page = timesheetDTO.getPage(); 
			 int pageSize = timesheetDTO.getSize();
			 int offset = (page-1) * pageSize;
	         String sortBy=timesheetDTO.getSortBy();
	         String sortDirection=timesheetDTO.getSortDirection();
// <<<<<<< Updated upstream

 				if (timesheetDTO.getDataForExcel()) {
 					if (timesheetDTO.getIsClientDashboard()) {
 						resultList = projectRepository.getProjectViewForClientAttendanceStatus(timesheetDTO.getMonth1(),
 								timesheetDTO.getYear(), timesheetDTO.getStatus(), timesheetDTO.getEmpId(), projectName,
 								poNo,startDate,endDate, projectManagerName, projectType, clientName, apmosysRM, apmosysRMEmail, clientRM,
 								totalExpectedFillCount, totalClientSideApprovedCount, totalClientSidePendingCount,
 								totalClientSideNotFilledCou, totalEmployees, active, sortBy, sortDirection, 0,
 								Integer.MAX_VALUE);
 					} else {
 						resultList = projectRepository.getProjectViewForAllEmpAttendanceStatus(timesheetDTO.getMonth1(),
 								timesheetDTO.getYear(), timesheetDTO.getStatus(), timesheetDTO.getEmpId(),
 								timesheetDTO.getBillableTypes(), timesheetDTO.getProjectActive(), projectName, poNo,startDate,endDate,
 								projectManagerName, projectType, clientName, apmosysRM, apmosysRMEmail, clientRM,
 								totalExpectedFillCount, totalClientSideApprovedCount, totalClientSidePendingCount,
 								totalClientSideNotFilledCou, totalEmployees, sortBy, sortDirection, 0, Integer.MAX_VALUE);
 					}
 				} else {
 					if (timesheetDTO.getIsClientDashboard()) {
 						resultList = projectRepository.getProjectViewForClientAttendanceStatus(timesheetDTO.getMonth1(),
 								timesheetDTO.getYear(), timesheetDTO.getStatus(), timesheetDTO.getEmpId(), projectName,
 								poNo,startDate,endDate, projectManagerName, projectType, clientName, apmosysRM, apmosysRMEmail, clientRM,
 								totalExpectedFillCount, totalClientSideApprovedCount, totalClientSidePendingCount,
 								totalClientSideNotFilledCou, totalEmployees, active, sortBy, sortDirection, offset,
 								pageSize);
 					} else {
 						resultList = projectRepository.getProjectViewForAllEmpAttendanceStatus(timesheetDTO.getMonth1(),
 								timesheetDTO.getYear(), timesheetDTO.getStatus(), timesheetDTO.getEmpId(),
 								timesheetDTO.getBillableTypes(), timesheetDTO.getProjectActive(), projectName, poNo,startDate,endDate,
 								projectManagerName, projectType, clientName, apmosysRM, apmosysRMEmail, clientRM,
 								totalExpectedFillCount, totalClientSideApprovedCount, totalClientSidePendingCount,
 								totalClientSideNotFilledCou, totalEmployees, sortBy, sortDirection, offset, pageSize);
 					}
 				}

			 
			 //We want the filterd data to be exported,so I am removing the upper part.
//			 if(timesheetDTO.getDataForExcel() && timesheetDTO.getIsClientDashboard()) {
//				//    resultList = projectRepository.getProjectViewForClientAttendanceStatus(timesheetDTO.getMonth1(),timesheetDTO.getYear(),
//				// 		   timesheetDTO.getStatus(),timesheetDTO.getEmpId(),null,null,null,null,null,null,null,null,null,null,null,null,null,null,sortBy,sortDirection,offset,Integer.MAX_VALUE);
//				resultList = projectRepository.getProjectViewForClientAttendanceStatus(timesheetDTO.getMonth1(),timesheetDTO.getYear(),
//				timesheetDTO.getStatus(),timesheetDTO.getEmpId(),projectName,poNo,projectManagerName,projectType,clientName,apmosysRM,apmosysRMEmail,
//				clientRM,totalExpectedFillCount,totalClientSideApprovedCount,totalClientSidePendingCount,totalClientSideNotFilledCou,totalEmployees,active,sortBy,sortDirection,0,Integer.MAX_VALUE);
//
//			}else if (timesheetDTO.getDataForExcel() && !timesheetDTO.getIsClientDashboard()){
//				//    resultList = projectRepository.getProjectViewForAllEmpAttendanceStatus(timesheetDTO.getMonth1(),timesheetDTO.getYear(),timesheetDTO.getStatus(),
//				// 			 timesheetDTO.getEmpId(),timesheetDTO.getBillableType(),timesheetDTO.getProjectActive(), null,null,null,null,null,null,null,null,null,null,null,null,null,sortBy,sortDirection,offset,Integer.MAX_VALUE);
//
//				resultList = projectRepository.getProjectViewForAllEmpAttendanceStatus(timesheetDTO.getMonth1(),timesheetDTO.getYear(),timesheetDTO.getStatus(),
//						 timesheetDTO.getEmpId(),timesheetDTO.getBillableTypes(),timesheetDTO.getProjectActive(),projectName,poNo,projectManagerName,projectType,clientName,apmosysRM,apmosysRMEmail,
//						   clientRM,totalExpectedFillCount,totalClientSideApprovedCount,totalClientSidePendingCount,totalClientSideNotFilledCou,totalEmployees,sortBy,sortDirection,0,Integer.MAX_VALUE);
//		
//			} else if(timesheetDTO.getIsClientDashboard()) {
//			   resultList = projectRepository.getProjectViewForClientAttendanceStatus(timesheetDTO.getMonth1(),timesheetDTO.getYear(),
//					   timesheetDTO.getStatus(),timesheetDTO.getEmpId(),projectName,poNo,projectManagerName,projectType,clientName,apmosysRM,apmosysRMEmail,
//					   clientRM,totalExpectedFillCount,totalClientSideApprovedCount,totalClientSidePendingCount,totalClientSideNotFilledCou,totalEmployees,active,sortBy,sortDirection,offset,pageSize);
//			 }else {
//			   resultList = projectRepository.getProjectViewForAllEmpAttendanceStatus(timesheetDTO.getMonth1(),timesheetDTO.getYear(),timesheetDTO.getStatus(),
//						 timesheetDTO.getEmpId(),timesheetDTO.getBillableTypes(),timesheetDTO.getProjectActive(),projectName,poNo,projectManagerName,projectType,clientName,apmosysRM,apmosysRMEmail,
//						   clientRM,totalExpectedFillCount,totalClientSideApprovedCount,totalClientSidePendingCount,totalClientSideNotFilledCou,totalEmployees,sortBy,sortDirection,offset,pageSize);
//			 }

			
			 Integer totalItems = ((BigInteger) entityManager.createNativeQuery("SELECT FOUND_ROWS()").getSingleResult()).intValue();
			 
			 if(resultList.isEmpty()) {
			        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			        response.setServiceResponse("No data found from database");

			        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			        apiLogInfo.setApiResponse("Empty list received from repository."); 
			        apiLogInfo.setLogLevel("ERROR");
			        
			        logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
			 }

	        List<GetProjectViewForClientAttendanceStatusDTO> dtoList = new ArrayList<>();
	        
	        for (Object[] obj : resultList){
	            GetProjectViewForClientAttendanceStatusDTO dto = new GetProjectViewForClientAttendanceStatusDTO();
	            dto.setProjectId(obj[0] != null ? Integer.parseInt(obj[0].toString()) : null);
	            dto.setProjectName(obj[1] != null ? obj[1].toString() : null);
	            dto.setProjectManagerName(obj[2] != null ? obj[2].toString() : null);
	            dto.setPoNo(obj[3] != null ? obj[3].toString() : null);
	            dto.setProjectType(obj[4] != null ? obj[4].toString() : null);
	            dto.setClientName(obj[5] != null ? obj[5].toString() : null);
	            dto.setApmosysRM(obj[6] != null ? obj[6].toString() : null);
	            dto.setApmosysRMEmail(obj[7] != null ? obj[7].toString() : null);
	            dto.setClientRM(obj[8] != null ? obj[8].toString() : null);
	            dto.setTotalExpectedFillCount(obj[9] != null ? Integer.parseInt(obj[9].toString()) : 0);
//	            dto.setTotalIshineFilledCount(obj[10] != null ? Integer.parseInt(obj[10].toString()) : 0);
	            dto.setTotalClientSideNotFilledCount(obj[10] != null ? Integer.parseInt(obj[10].toString()) : 0);
	            dto.setTotalClientSidePendingCount(obj[11] != null ? Integer.parseInt(obj[11].toString()) : 0);
	            dto.setTotalClientSideApprovedCount(obj[12] != null ? Integer.parseInt(obj[12].toString()) : 0);
	            dto.setClientSideApprovedPercent(obj[14] != null ? Double.parseDouble(obj[14].toString()) : 0.0);
	            dto.setClientSidePendingPercent(obj[15] != null ? Double.parseDouble(obj[15].toString()) : 0.0);
	            dto.setClientSideNotFilledPercent(obj[16] != null ? Double.parseDouble(obj[16].toString()) : 0.0);
	            dto.setTotalEmployees(obj[17] != null ? Integer.parseInt(obj[17].toString()) : 0);
	            dto.setActive(obj[18] != null ? obj[18].toString() : null);
	            dto.setStartDate(obj[19] != null ? obj[19].toString() : null);
	            dto.setEndDate(obj[20] != null ? obj[20].toString() : null);
	            dtoList.add(dto);
	        }
	        
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(dtoList);
	        response.setTotalElements(totalItems);

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Fetched " + dtoList.size() + " records successfully.");
	        
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
		        
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

	public ServiceResponse getEmployeeSummaryOnExportAccordingToStatus(GetEmployeeSummaryOnExportDTO object) {
	
		ServiceResponse response = new ServiceResponse();
 
		 LogDTO apiLogInfo = new LogDTO();
		 apiLogInfo.setSubFeatureName("getEmployeeSummaryOnExport");
		 apiLogInfo.setLogLevel("INFO");
		 StringBuilder logBuilder = new StringBuilder();
		 logBuilder.append("getEmployeeTimesheetAsCalenderByProjectId");
		 try {
			 List<Object[]> empTimesheet;
//		        int page = object.getPage(); 

			//  if(object.getAllEmp()) {
			// 	//  empTimesheet= timesheetsRepository.getEmployeeSummaryReportAll(object.getMonth(),
			// 	// 		 object.getYear(),object.getEmpId(),object.getBillableType());
			// 	String status = object.getStatus();
			// 	empTimesheet = timesheetsRepository.getEmployeeSummaryReportAllEMP(
			// 		object.getMonth(),object.getYear(),object.getEmpId(),object.getBillableType(),status,object.getEmployeeActive(),
			// 		null,null,null,null,null,null,null,null,null,null,0,Integer.MAX_VALUE,
            //         object.getSortBy(),
            //         object.getSortDirection());
			//  } else {
			// 	//  empTimesheet= timesheetsRepository.getEmployeeSummaryReportClientSideApplicable(object.getMonth(),
			// 	// 		 object.getYear(),object.getEmpId());
			// 	empTimesheet = timesheetsRepository.getEmployeeViewForClientAttendanceStatus(object.getMonth(),object.getYear(),object.getEmpId(),object.getStatus(),object.getClientSideFilter(),
			// 			null,null,null,null,null,null,null,null,null,null,null,null,0,Integer.MAX_VALUE,
	        //             object.getSortBy(),
	        //             object.getSortDirection());
			//  }
			// List<Object[]> empTimesheet;
	        int page = object.getPage(); 
			int pageSize = object.getSize();
			int offset = (page-1) * pageSize; 
			String employmentId = getStringColumnFilterValue(object.getFilters().getEmploymentId());
			String clientsideId = getStringColumnFilterValue(object.getFilters().getClientSideId());
			String employeeName = getStringColumnFilterValue(object.getFilters().getEmployeeName());
			String billableType = getStringColumnFilterValue(object.getFilters().getBillableType());
			String projectName = getStringColumnFilterValue(object.getFilters().getProjectName());
			String poNo = getStringColumnFilterValue(object.getFilters().getPoNo());
			String projectManagers = getStringColumnFilterValue(object.getFilters().getProjectManagers());
			String clientName = getStringColumnFilterValue(object.getFilters().getClientName());
			String teamName = getStringColumnFilterValue(object.getFilters().getTeamName());
			String department = getStringColumnFilterValue(object.getFilters().getDepartment());
			String employmentStatus = getStringColumnFilterValue(object.getFilters().getEmploymentStatus());
			String projectStatus = getStringColumnFilterValue(object.getFilters().getProjectStatus());
			String statusCode = null;
			if ("Mapped".equalsIgnoreCase(projectStatus)) {
			    statusCode = "1";
			} else if ("Removed".equalsIgnoreCase(projectStatus)) {
			    statusCode = "0";
			} else if ("Approval Pending".equalsIgnoreCase(projectStatus)) {
			    statusCode = "2";
			}
			
			List<Long> employeeIds = new ArrayList<Long>();
			
			if (object.getAllEmp()) {
	        	employeeIds = timesheetsRepository.getPaginatedEmployeeIds(
						object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),   
	                    object.getBillableType(),
	                    object.getStatus(),object.getEmployeeActive(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,statusCode,0,Integer.MAX_VALUE);
	        	
	            empTimesheet = timesheetsRepository.getEmployeeSummaryReportAllEMP(
	                    object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),   
	                    object.getBillableType(),
	                    object.getStatus(),object.getEmployeeActive(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,statusCode,
	                    object.getSortBy(),
	                    object.getSortDirection(),employeeIds);
	        } else {
	        	employeeIds = timesheetsRepository.getPaginatedEmployeeIdsForClientAttendance(
	        			object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),
	                    object.getStatus(),
						object.getClientSideFilter(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,employmentStatus,statusCode,0,Integer.MAX_VALUE);
	        	
	            empTimesheet = timesheetsRepository.getEmployeeViewForClientAttendanceStatus(
	                    object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),
	                    object.getStatus(),
						object.getClientSideFilter(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,employmentStatus,statusCode,
	                    object.getSortBy(),
	                    object.getSortDirection(),employeeIds
						);
	        }


					 List<GetEmployeeTimesheetAsCalenderDTO> dtoList = new ArrayList<>();
 
			 for (Object[] obj : empTimesheet) {
				 GetEmployeeTimesheetAsCalenderDTO dto = new GetEmployeeTimesheetAsCalenderDTO();
 
				 dto.setEmpId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
				 dto.setClientSideId(obj[1] != null ? obj[1].toString() : null);
				 dto.setStartDate(obj[2] != null ? obj[2].toString() : null);
				 dto.setTeamName(obj[3] != null ? obj[3].toString() : null);
				 dto.setTeamId(obj[4] != null ? Long.parseLong(obj[4].toString()) : null);
				 dto.setEmployeeName(obj[5] != null ? obj[5].toString() : null);
				 dto.setSpoc(obj[6] != null ? obj[6].toString() : null);
				 dto.setBillableType(obj[7] != null ? obj[7].toString() : null);
				 dto.setEmployeeRole(obj[8] != null ? obj[8].toString() : null);
				 dto.setDepartment(obj[9] != null ? obj[9].toString() : null);
				 dto.setProjectId(obj[10] != null ? Integer.parseInt(obj[10].toString()) : null);
				 dto.setProjectName(obj[11] != null ? obj[11].toString() : null);
				 dto.setProjectManagerName(obj[12] != null ? obj[12].toString() : null);
				 dto.setPoNo(obj[13] != null ? obj[13].toString() : null);
				 dto.setClientName(obj[14] != null ? obj[14].toString() : null);
				 dto.setReportingManagerId(obj[15] != null ? Long.parseLong(obj[15].toString()) : null);
				 dto.setMonthName(obj[16] != null ? obj[16].toString() : null);
				 dto.setExpectedTimesheetFillCount(obj[17] != null ? Integer.parseInt(obj[17].toString()) : null);
				 dto.setClientSideNotFilledCount(obj[18] != null ? Integer.parseInt(obj[18].toString()) : null);
				 dto.setClientSidePendingCount(obj[19] != null ? Integer.parseInt(obj[19].toString()) : null);
				 dto.setClientSideApprovedCount(obj[20] != null ? Integer.parseInt(obj[20].toString()) : null);
				 
				 Map<String, TimesheetDataDTO> timesheetData = new HashMap<>();
				 DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
				 DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");
 
				 for (int i = 0; i < 31; i++) {
					 int baseIndex = 21 + (i * 3);
					 String status = obj.length > baseIndex && obj[baseIndex] != null ? obj[baseIndex].toString() : null;
					 
					 String inTimeRaw = obj.length > (baseIndex + 1) && obj[baseIndex + 1] != null ? obj[baseIndex + 1].toString() : null;
					 String outTimeRaw = obj.length > (baseIndex + 2) && obj[baseIndex + 2] != null ? obj[baseIndex + 2].toString() : null;
 
					 String inTime = null;
					 String outTime = null;
 
					 try {
						 if (inTimeRaw != null && !inTimeRaw.isEmpty()) {
							 LocalDateTime inDateTime = LocalDateTime.parse(inTimeRaw, inputFormatter);
							 inTime = inDateTime.format(outputFormatter);
						 }
						 if (outTimeRaw != null && !outTimeRaw.isEmpty()) {
							 LocalDateTime outDateTime = LocalDateTime.parse(outTimeRaw, inputFormatter);
							 outTime = outDateTime.format(outputFormatter);
						 }
					 } catch (Exception e) {
						 e.printStackTrace();
					 }
 
					 TimesheetDataDTO dayData = new TimesheetDataDTO();
					 dayData.setStatus(status);
					 dayData.setInTime(inTime);
					 dayData.setOutTime(outTime);
 
					 timesheetData.put("d" + (i + 1), dayData);
				 }
 
				 dto.setTimesheetData(timesheetData);
				 dto.setEmploymentId(obj[114] != null ? obj[114].toString() : null);
				 dto.setPresent(obj[115] != null ? obj[115].toString() : null);
				 dto.setWeekOff(obj[116] != null ? obj[116].toString() : null);
				 dto.setHoliday(obj[117] != null ? obj[117].toString() : null);
				 dto.setLeave(obj[118] != null ? obj[118].toString() : null);
				 dto.setCompOff(obj[119] != null ? obj[119].toString() : null);
				 dto.setNa(obj[120] != null ? obj[120].toString() : null);
				 dto.setHalfDay(obj[121] != null ? obj[121].toString() : null);
				 dto.setTotalNoOfDays(obj[122] != null ? obj[122].toString() : null);
				 dto.setApmosysTimesheetFilledCount(obj[123] != null ? Integer.parseInt(obj[123].toString()) : null);
				 dto.setEmploymentStatus(obj[124] != null ? obj[124].toString() : null);
				 dto.setEndDate(obj[125] != null ? obj[125].toString() : null);
				 dto.setReadyForInvoicing(obj[126] != null ? obj[126].toString() : null);
				 if (obj[127] != null) {
					 int active = Integer.parseInt(obj[127].toString());
					 switch (active) {
						 case 1:
							 dto.setProjectStatus("Mapped");
							 break;
						 case 0:
							 dto.setProjectStatus("Removed");
							 break;
						 case 2:
							 dto.setProjectStatus("Approval Pending");
							 break;
						 default:
							 dto.setProjectStatus("Undefined");
							 break;
					 }
				 } else {
					 dto.setProjectStatus("Undefined");
				 }
				 if (obj[128] != null) {
					 String active = obj[128].toString();
					 switch (active) {
						 case "true":
							 dto.setProjectActive("Active");
							 break;
						 case "false":
							 dto.setProjectActive("Inactive");
							 break;
						 default:
							 dto.setProjectActive("Undefined");
							 break;
					 }
				 } else {
					 dto.setProjectActive("Undefined");
				 }
				 
 
				 dtoList.add(dto);
			 }
 
			 if(dtoList.isEmpty()){
				 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			  response.setServiceResponse("Unable to fetch the timesheet Data !!!");
			  apiLogInfo.setApiResponse("Failed to set the data in dto \n");
			  apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			  
		  } else {
			  response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			  response.setServiceResponse(dtoList);
			  apiLogInfo.setApiResponse("Timesheet Data fetched successfully ");
			  apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		  }
		  
		  apiLogInfo.setApiRequest(logBuilder.toString());
		  logService.logMyInfo(httpRequest, apiLogInfo);
		  return response;
		  
		 } catch(Exception e) {
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

	public ServiceResponse isClientMandetory(int projectId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("isClientIdMandetory");
		 apiLogInfo.setLogLevel("INFO");
		 StringBuilder logBuilder = new StringBuilder();
		 logBuilder.append("isClientIdMandetory");
		try {
			
			Boolean result = projectRepository.isClientIdMandetory(projectId);
			Boolean isMandetory = Boolean.TRUE.equals(result);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(isMandetory);
			apiLogInfo.setApiResponse("isClientMandetory fetched successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			return response;
		}
		catch(Exception e) {
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

	public ServiceResponse getProjectByMonthRangeAndEmpId(GetEmployeeSummaryOnExportDTO object) {
		return timesheetDashboardService.getProjectByMonthRangeAndEmpId(object);
	}
	
	// Delegate method - implementation moved to TimesheetDashboardService
	private ServiceResponse getProjectByMonthRangeAndEmpIdInternal(GetEmployeeSummaryOnExportDTO object) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Timesheet/Calander View/Project Drop-Down");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Timesheet/Calander View/Project Drop-Down/getProjectByMonthRangeAndEmpId");
		try {
	        if (object == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid request: request body is missing.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }

	        if (object.getMonth() == null || object.getYear() == null || object.getEmpId() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Month, Year and Employee ID are mandatory.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }

	        if (object.getMonth() < 1 || object.getMonth() > 12) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid month value. It must be between 1 and 12.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }
	        
			List<Object[]> projList = timesheetsRepository.getProjectByMonthRangeAndEmpId(
					object.getMonth(), object.getYear(), object.getEmpId());
			
			if (projList == null || projList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No projects found for the selected month.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }
			
			List<GetProjectByMonthRangeAndEmpIdDTO> projectList = new ArrayList<>();

			for (Object[] row : projList) {

			    GetProjectByMonthRangeAndEmpIdDTO dto = new GetProjectByMonthRangeAndEmpIdDTO();

			    dto.setEmpId(row[0] != null ? ((Number) row[0]).longValue() : null);
			    dto.setName(row[1] != null ? row[1].toString() : null);
			    dto.setProjectId(row[2] != null ? ((Number) row[2]).intValue() : null);
			    dto.setProjectName(row[3] != null ? row[3].toString() : null);
			    dto.setPoNo(row[4] != null ? row[4].toString() : null);
			    dto.setEmployementId(row[5] != null ? row[5].toString() : null);

			    projectList.add(dto); 
			}

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(projectList);
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		}
		catch(Exception e) {
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
		
	public ServiceResponse getClientDetailsByProjectIdAndEmpId(GetEmployeeSummaryOnExportDTO timesheetDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("add_timesheet");
	    apiLogInfo.setApiUrl("/api/getClientDetailsByProjectIdAndEmpId");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("empId : " +timesheetDTO.getEmpId());
	    logBuilder.append("projectId : " +timesheetDTO.getProjectId());

	    try {
	    	
	    	if(timesheetDTO == null) {
	    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Unable to fetch the client details !!");
	            apiLogInfo.setApiResponse("Payload received is empty !!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

	            apiLogInfo.setApiRequest(logBuilder.toString());
	    	    logService.logMyInfo(httpRequest, apiLogInfo);
	    	    return response;
	    	}
	    	
	    	Long empId = timesheetDTO.getEmpId();
	    	Integer projectId = timesheetDTO.getProjectId();

	        List<GetClientDetailsByProjectIdAndEmpIdDTO> flatList = employeeTeamMapRepository.getClientDetailsByProjectIdAndEmpId(projectId,empId); 

	        if (flatList.isEmpty()) {
	        	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No valid client details found.");
                apiLogInfo.setApiResponse("Query returned empty list");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                
                apiLogInfo.setApiRequest(logBuilder.toString());
	    	    logService.logMyInfo(httpRequest, apiLogInfo);
	    	    return response;
	        }
	        
	        ClientDetailsForActivityDTO responseDTO = new ClientDetailsForActivityDTO();

	        ProjectDetailsForActivityDTO projectDTO = new ProjectDetailsForActivityDTO();

	        Set<Integer> locationSet = new HashSet<>();
	        Set<Long> teamSet = new HashSet<>();

	        for (GetClientDetailsByProjectIdAndEmpIdDTO row : flatList) {

	            // Client 
	            responseDTO.setClientId(row.getClientId());
	            responseDTO.setClientName(row.getClientName());

	            // Client Locations
	            if (locationSet.add(row.getClientLocationId())) {
	                responseDTO.getClientLocations().add(
	                        new ClientLocationDTO(
	                                row.getClientLocationId(),
	                                row.getClientLocation()
	                        )
	                );
	            }

	            // Project 
	            projectDTO.setProjectId(row.getProjectId());
	            projectDTO.setProjectName(row.getProjectName());

	            // Teams
	            if (teamSet.add(row.getTeamId())) {
	                projectDTO.getTeams().add(
	                        new TeamIdTeamNameDTO(row.getTeamId(), row.getTeamName())
	                );
	            }
	        }
	        
	        responseDTO.setProject(projectDTO);

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(responseDTO);
            apiLogInfo.setApiResponse("listDto : " + responseDTO);
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//            System.out.println("Client Details: " + responseDTO);

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

	public ServiceResponse getOtherTeamMembersByDateAndProjectId(GetProjectListForDateAndEmpIdPayload payload) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getOtherTeamMembersByDateAndProjectId");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    
	    try {
	    	if(payload == null) {
	    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Unable to fetch the Employee list !!");
	            apiLogInfo.setApiResponse("Payload received is empty !!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

	            apiLogInfo.setApiRequest(logBuilder.toString());
	    	    logService.logMyInfo(httpRequest, apiLogInfo);
	    	    return response;
	    	}
	    	Long empId = payload.getEmpId();
	    	LocalDateTime date = payload.getDate();
	    	Integer projectId = Integer.parseInt(payload.getProjectId().toString());	    	
	    	
	    	List<EmpIdAndNameDTO> empList = timesheetsRepository.getOtherTeamMembersByDateAndProjectId(empId,date,projectId);
	        
	        if (empList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("You are the only resource mapped to this Project!");
	            response.setServiceMessage("No other emps mapped to the project except empId: " + empId);
	            
	            apiLogInfo.setApiResponse("You are the only resource mapped to this Project ! "
	            		+ "No other emps mapped to the project except empId:  " + empId);
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(empList);
            response.setServiceMessage("Employee List fetched successfully!");

            apiLogInfo.setApiResponse("Employee list where employee has active != 2 for the given in Employee Team Mapping table fetched successfully!");
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
	
	
	
	private TimesheetDocumentDetailsDTO getSingleNonFinalDocument(TimesheetDTO timesheetDTO) {

	    List<TimesheetDocumentDetailsDTO> nonFinalDocs =
	            timesheetDTO.getDocumentData()
	                    .stream()
	                    .filter(doc -> Boolean.FALSE.equals(doc.getFinalFlag()))
	                    .collect(Collectors.toList());

	    if (nonFinalDocs.size() != 1) {
	        throw new IllegalArgumentException("Expected exactly one non-final document");
	    }

	    return nonFinalDocs.get(0);
	}
	
	private TimesheetDocumentDetails getSingleExistingDocument(Long docId) {

	    List<TimesheetDocumentDetails> records =
	            timesheetDocumentDetailsRepository.findByDocId(docId);

	    if (records == null || records.size() != 1) {
	        throw new IllegalStateException("Invalid document state in DB for docId: " + docId);
	    }

	    return records.get(0);
	}
	
	
	private void savePendingDocument(
	        TimesheetDocumentDetails existing,
	        TimesheetDocumentDetailsDTO document,
	        MultipartFile doc1,
	        TimesheetDTO timesheetDTO,
	        Timesheet updatedTimesheet,
	        ServiceResponse response,
	        LogDTO apiLogInfo
	) {

	    try {
	        TimesheetDocumentDetails docData =
	                addTimesheetDocument(document, "Update", doc1);

	        docData.setTimesheetId(updatedTimesheet.getTimesheetId());
	        docData.setEmpId(timesheetDTO.getEmpId());
	        docData.setCreatedBy(timesheetDTO.getEmpId());

	        TimesheetDocumentDetails saved =
	                timesheetDocumentDetailsRepository.save(docData);

	        handleSaveResponse(saved, response, apiLogInfo);

	    } catch (Exception e) {
	        handleException(e, response, apiLogInfo);
	    }
	}
	
	private void rollbackApprovedToPending(
	        TimesheetDocumentDetails existing,
	        TimesheetDocumentDetailsDTO document,
	        TimesheetDTO timesheetDTO,
	        ServiceResponse response,
	        LogDTO apiLogInfo,
	        MultipartFile doc1
	) {

	    try {
	        Long bulkApprovedDocId = existing.getBulkApprovedDocId();

	        existing.setBulkApprovedDocId(null);
	        existing.setClientApprovalStatus(timesheetDTO.getClientApprovalStatus().toLowerCase());
	        existing.setFinalFlag(false);
	        existing.setUpdatedBy(timesheetDTO.getEmpId());
	        existing.setDocName(doc1.getOriginalFilename());
	        existing.setDocData(doc1.getBytes());
	        existing.setDocMimeType(doc1.getContentType());
	        existing.setUpdatedOn(LocalDateTime.now());

	        TimesheetDocumentDetails saved =
	                timesheetDocumentDetailsRepository.save(existing);

	        finalDocumentRepository.deleteById(bulkApprovedDocId);

	        handleSaveResponse(saved, response, apiLogInfo);

	    } catch (Exception e) {
//	        handleException(e, response, apiLogInfo);
	        throw new IllegalStateException("Unxpected error occured" + e.getMessage());
	    }
	}

	
	private void handleSaveResponse(
	        TimesheetDocumentDetails saved,
	        ServiceResponse response,
	        LogDTO apiLogInfo
	) {

	    if (saved == null) {
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
	}

	private void handleException(
	        Exception e,
	        ServiceResponse response,
	        LogDTO apiLogInfo
	) {

	    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	    response.setServiceResponse("Timesheet added, but document save failed due to an error.");

	    apiLogInfo.setApiResponse("Exception while saving document: " + e.getMessage());
	    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

	    e.printStackTrace();
	}

	private TimesheetDocumentDetailsDTO getSingleFinalDocument(TimesheetDTO timesheetDTO) {

	    List<TimesheetDocumentDetailsDTO> finalDocs =
	            timesheetDTO.getDocumentData()
	                    .stream()
	                    .filter(doc -> Boolean.TRUE.equals(doc.getFinalFlag()))
	                    .collect(Collectors.toList());	                    ;

	    if (finalDocs.size() != 1) {
	        throw new IllegalArgumentException("Expected exactly one final document");
	    }

	    return finalDocs.get(0);
	}

	private void replaceApprovedDocumentNewCode(
	        MultipartFile doc2,
	        TimesheetDocumentDetailsDTO document,
	        TimesheetDocumentDetails existing,
	        TimesheetDTO timesheetDTO,
	        ServiceResponse response,
	        LogDTO apiLogInfo
	) {

	    try {
	        finalDocumentRepository.deleteById(existing.getBulkApprovedDocId());

	        FinalDocument finalDocument =
	                finalDocumentRepository.save(
	                        buildFinalDocument(doc2, document.getEmpId(),true)
	                );

	        existing.setBulkApprovedDocId(finalDocument.getDocId());
	        existing.setUpdatedBy(timesheetDTO.getEmpId());
	        existing.setUpdatedOn(LocalDateTime.now());

	        TimesheetDocumentDetails saved =
	                timesheetDocumentDetailsRepository.save(existing);

	        handleSaveResponse(saved, response, apiLogInfo);

	    } catch (Exception e) {
	        handleException(e, response, apiLogInfo);
	    }
	}
	
	private FinalDocument buildFinalDocument(MultipartFile file, Long empId,Boolean isUpdate) throws IOException {

	    FinalDocument doc = new FinalDocument();
	    doc.setDocName(file.getOriginalFilename());
	    doc.setDocData(file.getBytes());
	    doc.setDocMimeType(file.getContentType());
	    doc.setCreatedBy(empId);
	    doc.setCreatedOn(LocalDateTime.now());
	    if(isUpdate) {	    	
	    doc.setUpdatedBy(empId);
	    doc.setUpdatedOn(LocalDateTime.now());
	    }
	    return doc;
	}

	private void saveApprovedDocumentOldCode(
	        MultipartFile doc2,
	        TimesheetDocumentDetailsDTO document,
	        TimesheetDTO timesheetDTO,
	        Timesheet updatedTimesheet,
	        ServiceResponse response,
	        LogDTO apiLogInfo
	) {

	    try {
	        TimesheetDocumentDetails docData =
	                addTimesheetDocument(document, "Update", doc2);

	        docData.setTimesheetId(updatedTimesheet.getTimesheetId());
	        docData.setEmpId(timesheetDTO.getEmpId());
	        docData.setCreatedBy(timesheetDTO.getEmpId());

	        TimesheetDocumentDetails saved =
	                timesheetDocumentDetailsRepository.save(docData);

	        handleSaveResponse(saved, response, apiLogInfo);

	    } catch (Exception e) {
	        handleException(e, response, apiLogInfo);
	    }
	}

	private void handleFirstTimeApprovedDocument(
	        MultipartFile doc2,
	        TimesheetDocumentDetailsDTO document,
	        TimesheetDTO timesheetDTO,
	        ServiceResponse response,
	        LogDTO apiLogInfo
	) {

	    try {
	        Long docId =
	                timesheetDocumentDetailsRepository
	                        .findDocIdByTimesheetId(timesheetDTO.getTimesheetId());

	        if (docId == null) {
	            throw new IllegalStateException("No base document found for approval");
	        }

	        TimesheetDocumentDetails existing =
	                getSingleExistingDocument(docId);

	        FinalDocument finalDocument =
	                finalDocumentRepository.save(
	                        buildFinalDocument(doc2, document.getEmpId(),false)
	                );

	        existing.setBulkApprovedDocId(finalDocument.getDocId());
	        existing.setFinalFlag(true);
	        existing.setClientApprovalStatus("Approved");
	        existing.setUpdatedBy(timesheetDTO.getEmpId());
	        existing.setUpdatedOn(LocalDateTime.now());

	        TimesheetDocumentDetails saved =
	                timesheetDocumentDetailsRepository.save(existing);

	        handleSaveResponse(saved, response, apiLogInfo);

	    } catch (Exception e) {
	        handleException(e, response, apiLogInfo);
	    }
	}
	

	public ServiceResponse getAllDayTypes() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		 apiLogInfo.setLogLevel("INFO");
		 StringBuilder logBuilder = new StringBuilder();
		 logBuilder.append("getAllDayTypes");
		 try {
			 List<DayTypeMasterNew> data = new ArrayList<>();
				data = dayTypeMasterNewRepository.findAll();
				 if(data != null && data.size() != 0) { 
				        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				        response.setServiceResponse(data);
				        apiLogInfo.setApiResponse(data.toString());
				        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				    }
				 else {
					 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				        response.setServiceResponse("No data found");
				        apiLogInfo.setApiResponse("No data found");
				        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				 }
		 }catch(Exception e) {
			 e.printStackTrace();
			 response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			 response.setServiceResponse("Something went wrong.Please contact support");
			 response.setServiceError(e.getMessage());
			 apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			 apiLogInfo.setApiResponse(e.getMessage()); 
			 apiLogInfo.setLogLevel("ERROR");
			 
		 }
		
		 apiLogInfo.setApiRequest(logBuilder.toString());
		  logService.logMyInfo(httpRequest, apiLogInfo);
		  return response;
	}
	
}

package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.AssetDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeExitDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.dto.SurveyQuestionDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.model.Asset;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeAssetMap;
import com.apmosys.employeeportal.model.EmployeeResignation;
import com.apmosys.employeeportal.model.ExitInterviewResponse;
import com.apmosys.employeeportal.model.SurveyEmployeeResponse;
import com.apmosys.employeeportal.repository.EmployeeOnBoardingMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeResignationRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ExitInterviewResponseRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class EmployeeExitService {
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	EmployeeOnBoardingMapRepository employeeOnboardingMapRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	ValidationService validationService;
	
	@Autowired
	ExitInterviewResponseRepository exitInterviewResponseRepository;
	
	@Autowired
	EmployeeResignationRepository employeeResignationRepository;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Value("${hr.mail}")
	private String hrMailAddress;

	public ServiceResponse updateEmployeeResignationDetails(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/updateEmployeeResignationDetails");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeeDTO.getEmpId());
		try {
			Employee employeeObj = employeeRepository.findByEmpId(employeeDTO.getEmpId());
			
			if(employeeObj != null) {
				LocalDate relievingDate = stringToDateTimeParser.getDate(employeeDTO.getDateOfResign(), "yyyy-MM-dd").plusDays(employeeObj.getNoticePeriod());
				
				employeeObj.setDateOfResign(stringToDateTimeParser.getDate(employeeDTO.getDateOfResign(), "yyyy-MM-dd"));
				employeeObj.setDateOfRelieving(relievingDate.toString());
				employeeObj.setEmploymentstatus("Resigned");
				Employee dbResponse = employeeRepository.save(employeeObj);
				
				if(dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Employee Resignation Details Updated Successfully.");
					
					apiLogInfo.setApiResponse("Employee Resignation Details Updated Successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Resignation Details Updation Failed.");
					
					apiLogInfo.setApiResponse("Employee Resignation Details Updation Failed.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee not found.");
				
				apiLogInfo.setApiResponse("Employee not found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getEmployeeResignationDetails(EmployeeExitDTO employeeExitDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/getEmployeeResignationDetails");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeeExitDTO.getEmpId());
		try {

			List<Object[]> employeeResignation = employeeResignationRepository.getEmployeeResignationDetail(employeeExitDTO.getEmpId());
			List<EmployeeExitDTO> dtoList = new ArrayList<EmployeeExitDTO>();
			
			if (!employeeResignation.isEmpty()) {
				employeeResignation.forEach((object) -> {
					EmployeeExitDTO empDTO = new EmployeeExitDTO();
					
					empDTO.setCreatedOn(object[0] != null ? object[0].toString() : null);
					empDTO.setEmpId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					empDTO.setRejectReason(object[2] != null ? object[2].toString() : null);
					empDTO.setResignationMail(object[3] != null ? object[3].toString() : null);
					empDTO.setResignationStatus(object[4] != null ? object[4].toString() : null);
					empDTO.setStatusUpdatedBy(object[5] != null ? Long.parseLong(object[5].toString()) : null);
					empDTO.setDateOfResign(object[6] != null ? object[6].toString() : null);
					empDTO.setDateOfRelieving(object[7] != null ? object[7].toString() : null);
					empDTO.setStatusUpdatedByName(object[8] != null ? object[8].toString() : null);
					
					dtoList.add(empDTO);
				});
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse("Employee Resignation Info found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Resignation Info not found.");
				
				apiLogInfo.setApiResponse("Employee Resignation Info not found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getEmployeeExitAssetDetails(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/getEmployeeExitAssetDetails");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeeDTO.getEmpId());
		try {
			
			List<Object[]> exitAssetList = employeeOnboardingMapRepository.getExitAssetDetailsByEmployeementId(employeeDTO.getEmployeementId());
			List<AssetDTO> dtoList = new ArrayList<>();
			
			if(!exitAssetList.isEmpty()) {
				
				for(Object[] object: exitAssetList) {
					AssetDTO dto = new AssetDTO();
					dto.setAssetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setAssestName(object[1] != null ? object[1].toString() : null);
					dto.setDeptId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
					dto.setDepartmentName(object[3] != null ? object[3].toString() : null);
					dto.setEmployeeName(object[4] != null ? object[4].toString() : null);
					dto.setEmployeementId(object[5] != null ? Long.parseLong(object[5].toString()) : null);
					dto.setEmpId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
					dto.setIsAssigned(object[7] != null ? object[7].toString() : null);
					dto.setDeptConsent(object[8] != null ? object[8].toString() : null);
					dto.setEmployeeAssetMapId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
					dto.setUpdatedByName(object[10] != null ? object[10].toString() : null);
					
					dtoList.add(dto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse(dtoList + "Employee Exit Asset Details found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Exit Asset Details is empty.");
				
				apiLogInfo.setApiResponse("Employee Exit Asset Details is empty.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getEmployeeInfo(EmployeeDTO employeeDTO) {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/getEmployeeInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeeDTO.getEmpId());
		ServiceResponse response = new ServiceResponse();
		try {
			
			Employee employee = employeeRepository.findByEmployeementId(employeeDTO.getEmployeementId());
			
			if(employee != null) {
				List<Object[]> employeeData = employeeRepository.getEmployeeData(employee.getEmpId());
				List<EmployeeDTO> employeeDataList = new ArrayList<EmployeeDTO>();
				
				if(employeeData != null) {
					employeeData.forEach((object) -> {
						EmployeeDTO empDto = new EmployeeDTO();
						empDto.setManagerName(object[0] != null ? object[0].toString() : null);
						empDto.setJobRoleName(object[1] != null ? object[1].toString() : null);
						empDto.setDepartmentName(object[2] != null ? object[2].toString() : null);
						empDto.setEmploymentstatus(object[3] != null ? object[3].toString() : null);
						empDto.setEmployeementId(employee.getEmployeementId());
						empDto.setDateOfJoining(employee.getDateOfJoining() != null ? employee.getDateOfJoining().toString() : null);
						empDto.setEmail(employee.getEmail());
						empDto.setName(employee.getName());
						empDto.setDateOfResign(employee.getDateOfResign() != null ? employee.getDateOfResign().toString() : null);
						
						employeeDataList.add(empDto);
					});
					
					apiLogInfo.setApiResponse("Employee Information found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(employeeDataList);
				}else {
					apiLogInfo.setApiResponse("Employee Information not found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Information not found.");
				}
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid Emp Id.");
				
				apiLogInfo.setApiResponse("Invalid Emp Id.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse setDeptHeadConcent(EmployeeDTO employeeDTO) {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/setDeptHeadConcent");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("concent Given By : "+employeeDTO.getUpdatedBy());
		ServiceResponse response = new ServiceResponse(); 
		try {
			
			employeeDTO.getDeptHeadConsentList().forEach((Object) -> {
				EmployeeAssetMap assetObj = employeeOnboardingMapRepository.getById(Object.getEmployeeAssetMapId());
				
				if(assetObj != null) {
					assetObj.setDeptConsent(Object.getDeptConsent());
					assetObj.getCommonProperty().setUpdatedBy(employeeDTO.getUpdatedBy());
					EmployeeAssetMap dbResponse = employeeOnboardingMapRepository.save(assetObj);
					
					if(dbResponse != null) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Consent submitted successfully.");
						
						apiLogInfo.setApiResponse("Consent submitted successfully.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Unable to submit consent.");
						
						apiLogInfo.setApiResponse("Unable to submit consent.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Asset Information not found.");
					
					apiLogInfo.setApiResponse("Asset Information not found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			});
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public ServiceResponse setExitInterviewResponseByEmpId(SurveyDTO surveyDTO) {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/setExitInterviewResponseByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+surveyDTO.getEmpId());
		ServiceResponse response = new ServiceResponse();
		try {
			
			if (!validationService.validateEmpId(surveyDTO.getEmpId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id does not exists.");
				return response;
			}

			List<ExitInterviewResponse> dtoList = new ArrayList<ExitInterviewResponse>();

			surveyDTO.getSurveyQuestionList().forEach((question) -> {

				ExitInterviewResponse exitInterviewResponse = new ExitInterviewResponse();

				exitInterviewResponse.setEmpId(surveyDTO.getEmpId());
				exitInterviewResponse.setQuestion(question.getQuestion());
				exitInterviewResponse.setResponse(question.getResponse());
				exitInterviewResponse.setSurveyId(question.getSurveyId());
				dtoList.add(exitInterviewResponse);
				
			});

			List<ExitInterviewResponse> responseList = exitInterviewResponseRepository.saveAll(dtoList);

			if (responseList.size() > 0) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Responses stored successfully.");
				
				apiLogInfo.setApiResponse("Responses stored successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to save response.");
				
				apiLogInfo.setApiResponse("Unable to save response.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getAnsweredInterviewByEmpId(SurveyDTO surveyDTO) {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/getAnsweredInterviewByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+surveyDTO.getEmpId());
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> objectList = exitInterviewResponseRepository.getAnsweredInterviewByEmpId(surveyDTO.getEmpId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No survey found. List is empty.");
					
					apiLogInfo.setApiResponse("No survey found. List is empty.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<ExitInterviewResponse> dtoList = new ArrayList<ExitInterviewResponse>();

					list.forEach((object) -> {

						ExitInterviewResponse dto = new ExitInterviewResponse();
						dto.setSurveyId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("Employee Exit Interview Response found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No survey found. List is null.");
				
				apiLogInfo.setApiResponse("No survey found. List is null.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getExitInterviewResponseBySurveyIdAndEmp(SurveyDTO surveyDTO) {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/getExitInterviewResponseBySurveyIdAndEmp");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+surveyDTO.getEmpId());
		ServiceResponse response = new ServiceResponse();
		try {
			
			Employee employee = employeeRepository.findByEmployeementId(surveyDTO.getEmployeementId());
			
			if(employee != null) {
				
				List<ExitInterviewResponse> responseList = exitInterviewResponseRepository
						.findBySurveyIdAndEmpId(surveyDTO.getSurveyId(), employee.getEmpId());
				
				if (!responseList.isEmpty()) {

					List<SurveyQuestionDTO> dtoList = new ArrayList<SurveyQuestionDTO>();

					responseList.forEach((object) -> {

						SurveyQuestionDTO dto = new SurveyQuestionDTO();
						dto.setResponse(object.getResponse());
						dto.setQuestion(object.getQuestion());
						dtoList.add(dto);

					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("Survey Found by EmpId & Survey Id.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					apiLogInfo.setApiResponse("No responses found for exit Interview.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No responses found for exit Interview.");
				}
			}else {
				apiLogInfo.setApiResponse("Employee not found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee not found.");
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
                       /* Employee Resignation Application Service*/

	public ServiceResponse createResignationApplication(EmployeeExitDTO employeeExitDTO) {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Resignation");
		apiLogInfo.setApiUrl("/api/createResignationApplication");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeeExitDTO.getEmpId());
		ServiceResponse response = new ServiceResponse();
		try {
			
			EmployeeResignation resignationObj = new EmployeeResignation();
			
			resignationObj.setEmpId(employeeExitDTO.getEmpId());
			resignationObj.setResignationStatus("Pending");
			resignationObj.setResignationMail(employeeExitDTO.getResignationMail());
			
			EmployeeResignation dbResponse = employeeResignationRepository.save(resignationObj);
			
			if(dbResponse != null) {
				
				//Send Resignation Mail
				List<Object[]> employeeData = employeeRepository.getEmployeeData(employeeExitDTO.getEmpId());
				Employee empObj = employeeRepository.findByEmpId(dbResponse.getEmpId());
				
				if(employeeData != null) {
					String managerEmail = null;
					String HodMail = null;
					
					for(Object[] object: employeeData) {
						managerEmail = object[5] != null ? object[5].toString() : null;
						HodMail = object[6] != null ? object[6].toString() : null;
					}
					
					mailService.sendMailWithCC(hrMailAddress,managerEmail+","+HodMail,
							"Resignation Letter - "+empObj.getName(),
							employeeExitDTO.getResignationMail());
				}
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Resignation Application submitted successfully.");
				
				apiLogInfo.setApiResponse("Resignation Application submitted successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to submit Resignation Application.");
				
				apiLogInfo.setApiResponse("Unable to submit Resignation Application.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getAllResignationApplication() {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/getAllResignationApplication");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("All Resignation Application");
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Object[]> allResignationList = employeeResignationRepository.getAllResignationApplication();
			List<EmployeeExitDTO> dtoList = new ArrayList<EmployeeExitDTO>();
			
			if(!allResignationList.isEmpty()) {
				allResignationList.forEach((object) -> {
					
					EmployeeExitDTO dto = new EmployeeExitDTO();
					
					dto.setEmploymentId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setName(object[1] != null ? object[1].toString() : null);
					dto.setCreatedOn(object[2] != null ? object[2].toString() : null);
					dto.setResignationStatus(object[3] != null ? object[3].toString() : null);
					dto.setStatusUpdatedByName(object[4] != null ? object[4].toString() : null);
					dto.setStatusUpdatedBy(object[5] != null ? Long.parseLong(object[5].toString()) : null);
					dto.setResignationMail(object[6] != null ? object[6].toString() : null);
					dto.setDeptName(object[7] != null ? object[7].toString() : null);
					dto.setEmployeeResignationId(object[8] != null ? Long.parseLong(object[8].toString()) : null);
					dto.setStatusUpdatedOn(object[9] != null ? object[9].toString() : null);
					dto.setEmpId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
					
					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse("All Resignation Application Found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Resignation Application Found.");
				
				apiLogInfo.setApiResponse("No Resignation Application Found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse approveResignationApplication(EmployeeExitDTO employeeExitDTO) {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/approveResignationApplication");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeeExitDTO.getEmpId() + " Application Approved By : "+employeeExitDTO.getStatusUpdatedBy());
		ServiceResponse response = new ServiceResponse();
		try {
			
			EmployeeResignation applicationObj = employeeResignationRepository.findByEmployeeResignationId(employeeExitDTO.getEmployeeResignationId());
			
			if(applicationObj != null) {
				applicationObj.setResignationStatus("Approved");
				applicationObj.setStatusUpdatedBy(employeeExitDTO.getStatusUpdatedBy());
				applicationObj.setStatusUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				
				EmployeeResignation dbResponse = employeeResignationRepository.save(applicationObj);
				
				if(dbResponse != null) {
					//Changes employee EmploymentStatus
					
					Employee employeeObj = employeeRepository.findByEmpId(dbResponse.getEmpId());
					Employee approvedByEmployeeObj = employeeRepository.findByEmpId(employeeExitDTO.getStatusUpdatedBy());
					
					if(employeeObj != null) {
						LocalDate relievingDate = dbResponse.getCreatedOn().toLocalDateTime().toLocalDate().plusDays(employeeObj.getNoticePeriod());
						LocalDate dateOfResign = dbResponse.getCreatedOn().toLocalDateTime().toLocalDate();
						
						employeeObj.setDateOfResign(dateOfResign);
						employeeObj.setDateOfRelieving(relievingDate.toString());
						employeeObj.setEmploymentstatus("Resigned");
						employeeObj.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
						employeeObj.setUpdatedBy(Integer.parseInt(employeeExitDTO.getStatusUpdatedBy().toString()));
						
						Employee employeeResponse = employeeRepository.save(employeeObj);
						
						if(employeeResponse != null) {
							
							List<Object[]> employeeData = employeeRepository.getEmployeeData(employeeExitDTO.getEmpId());
							
							String managerEmail = null;
							String HodMail = null;
							if(employeeData != null) {
								for(Object[] object: employeeData) {
									managerEmail = object[5] != null ? object[5].toString() : null;
									HodMail = object[6] != null ? object[6].toString() : null;
								}
							}
							
							//Send Resignation Application Approved mail
							mailService.sendMailWithCC(employeeResponse.getEmail(), hrMailAddress +","+ managerEmail+","+HodMail,
									"Regarding Resignation Application",
									"Dear "+ employeeResponse.getName() + ","+"<br>"
									+"<br>"+" &nbsp"+" &nbsp"+" "+"Resignation Applied on "+dateOfResign+ " has been approved by "+approvedByEmployeeObj.getName()+
									"<br>"+"<br>"+"<b>"+"Resignation Details"+"<b>"+
									"<br>"+
									"EmpID :"+" "+ "A-"+employeeResponse.getEmployeementId()+
									"<br>"+
									"Name :"+" "+ employeeResponse.getName()+
									"<br>"+
									"Date Of Resignation :"+" "+ employeeResponse.getDateOfResign()+
									"<br>"+
									"Date Of Relieving :"+" "+ employeeResponse.getDateOfRelieving());
							
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Resignation Application Approved, and Employment Status changed Successfully.");
							
							apiLogInfo.setApiResponse("Resignation Application Approved, and Employment Status changed Successfully.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Resignation Application Approved, but unable to Change Employment Status.");
							
							apiLogInfo.setApiResponse("Resignation Application Approved, but unable to Change Employment Status.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Unable to find Employee.");
						
						apiLogInfo.setApiResponse("Unable to find Employee.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to approve Resignation Application.");
					
					apiLogInfo.setApiResponse("Unable to approve Resignation Application.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Resignation Application found.");
				
				apiLogInfo.setApiResponse("No Resignation Application found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse rejectResignationApplication(EmployeeExitDTO employeeExitDTO) {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Resignation");
		apiLogInfo.setApiUrl("/api/rejectResignationApplication");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeeExitDTO.getEmpId() + "Rejected By : "+employeeExitDTO.getStatusUpdatedBy());
		ServiceResponse response = new ServiceResponse();
		try {
			
			EmployeeResignation applicationObj = employeeResignationRepository
					.findByEmployeeResignationId(employeeExitDTO.getEmployeeResignationId());

			if (applicationObj != null) {
				applicationObj.setResignationStatus("Rejected");
				applicationObj.setStatusUpdatedBy(employeeExitDTO.getStatusUpdatedBy());
				applicationObj.setRejectReason(employeeExitDTO.getRejectReason());
				applicationObj.setStatusUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				
				EmployeeResignation dbResponse = employeeResignationRepository.save(applicationObj);

				if (dbResponse != null) {
					LocalDate dateOfResign = dbResponse.getCreatedOn().toLocalDateTime().toLocalDate();
					
					Employee employeeObj = employeeRepository.findByEmpId(dbResponse.getEmpId());
					Employee rejctedByEmployeeObj = employeeRepository.findByEmpId(employeeExitDTO.getStatusUpdatedBy());
					List<Object[]> employeeData = employeeRepository.getEmployeeData(employeeExitDTO.getEmpId());
					
					String managerEmail = null;
					String HodMail = null;
					if(employeeData != null) {
						for(Object[] object: employeeData) {
							managerEmail = object[5] != null ? object[5].toString() : null;
							HodMail = object[6] != null ? object[6].toString() : null;
						}
					}
					
					//Send Resignation Application reject mail
					mailService.sendMailWithCC(employeeObj.getEmail(), hrMailAddress +","+ managerEmail+","+HodMail,
							"Regarding Resignation Application",
							"Dear "+ employeeObj.getName() + ","+"<br>"
							+"<br>"+" &nbsp"+" &nbsp"+" "+"Resignation Applied on "+dateOfResign+ " has been rejected by "+rejctedByEmployeeObj.getName()+
							"<br>"+"<br>"+"<b>"+"Resignation Details"+"<b>"+
							"<br>"+
							"EmpID :"+" "+ "A-"+employeeObj.getEmployeementId()+
							"<br>"+
							"Name :"+" "+ employeeObj.getName()+
							"<br>"+
							"Date Of Resignation :"+" "+ dateOfResign+
							"<br>"+
							"Reject Reason :"+" "+ dbResponse.getRejectReason());
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Resignation Application Rejected Successfully.");
					
					apiLogInfo.setApiResponse("Resignation Application Rejected Successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to rejected Resignation Application.");
					
					apiLogInfo.setApiResponse("Unable to rejected Resignation Application.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Resignation Application found.");
				
				apiLogInfo.setApiResponse("No Resignation Application found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getAllProjectByEmpId(EmployeeExitDTO employeeExitDTO) {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("My Resignation");
		apiLogInfo.setApiUrl("/api/getAllProjectByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeeExitDTO.getEmpId());
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Object[]> allProjects = employeeTeamMapRepository.getAllProjectByEmpId(employeeExitDTO.getEmpId());
			List<TeamDTO> dtoList = new ArrayList<TeamDTO>();
			
			if(!allProjects.isEmpty()) {
				allProjects.forEach((object) -> {
					
					TeamDTO dto = new TeamDTO();
					
					dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setProjectName(object[1] != null ? object[1].toString() : null);
					dto.setTeamName(object[2] != null ? object[2].toString() : null);
					dto.setActive(object[3] != null ? object[3].toString() : null);
					dto.setStartDate(object[4] != null ? object[4].toString() : null);
					dto.setEndDate(object[5] != null ? object[5].toString() : null);
					
					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse("Project By Empid Found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Project Found.");
				
				apiLogInfo.setApiResponse("No Project Found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse revokeResignationApplication(EmployeeExitDTO employeeExitDTO) {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("REsignation");
		apiLogInfo.setApiUrl("/api/revokeResignationApplication");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmployeeResignation Id : "+employeeExitDTO.getEmployeeResignationId() + " Resigantion application revoked by : "+ employeeExitDTO.getStatusUpdatedBy());
		ServiceResponse response = new ServiceResponse();
		try {
			
			EmployeeResignation applicationObj = employeeResignationRepository
					.findByEmployeeResignationId(employeeExitDTO.getEmployeeResignationId());

			if (applicationObj != null) {
				applicationObj.setResignationStatus("Retained");
				applicationObj.setStatusUpdatedBy(employeeExitDTO.getStatusUpdatedBy());
				applicationObj.setStatusUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				
				EmployeeResignation dbResponse = employeeResignationRepository.save(applicationObj);

				if (dbResponse != null) {
					
					//Change Emploment Info
					Employee employeeObj = employeeRepository.findByEmpId(dbResponse.getEmpId());
					Employee retainByEmployeeObj = employeeRepository.findByEmpId(employeeExitDTO.getStatusUpdatedBy());
					
					if(employeeObj != null) {
						
						employeeObj.setDateOfResign(null);
						employeeObj.setDateOfRelieving(null);
						employeeObj.setEmploymentstatus("Confirmed");
						employeeObj.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
						employeeObj.setUpdatedBy(Integer.parseInt(employeeExitDTO.getStatusUpdatedBy().toString()));
						
						Employee employeeResponse = employeeRepository.save(employeeObj);
						
						if(employeeResponse != null) {
							LocalDate dateOfResign = dbResponse.getCreatedOn().toLocalDateTime().toLocalDate();
							List<Object[]> employeeData = employeeRepository.getEmployeeData(employeeExitDTO.getEmpId());
							
							String managerEmail = null;
							String HodMail = null;
							if(employeeData != null) {
								for(Object[] object: employeeData) {
									managerEmail = object[5] != null ? object[5].toString() : null;
									HodMail = object[6] != null ? object[6].toString() : null;
								}
							}
							
							//Send Resignation Application reject mail
							mailService.sendMailWithCC(employeeObj.getEmail(), hrMailAddress +","+ managerEmail+","+HodMail,
									"Regarding Retention",
									"Dear "+ employeeObj.getName() + ","+"<br>"
									+"<br>"+" &nbsp"+" &nbsp"+" "+"Your resignation application has been retained by "+retainByEmployeeObj.getName()+
									"<br>"+"<br>"+"<b>"+"Resignation Details"+"<b>"+
									"<br>"+
									"EmpID :"+" "+ "A-"+employeeObj.getEmployeementId()+
									"<br>"+
									"Name :"+" "+ employeeObj.getName()+
									"<br>"+
									"Date Of Resignation :"+" "+ dateOfResign);
							
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Employee retained successfully and Employee info changed.");
							
							apiLogInfo.setApiResponse("Employee retained successfully and Employee info changed.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Employee retained, but unable to change Employee info.");
							
							apiLogInfo.setApiResponse("Employee retained, but unable to change Employee info.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Employee retained, but unable to find Employee.");
						
						apiLogInfo.setApiResponse("Employee retained, but unable to find Employee.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to retain Employee.");
					
					apiLogInfo.setApiResponse("Unable to retain Employee.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Resignation Application found.");
				
				apiLogInfo.setApiResponse("No Resignation Application found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} 
						
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse revokeMyResignationApplication(EmployeeExitDTO employeeExitDTO) {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("My Resignation");
		apiLogInfo.setApiUrl("/api/getEmployeeInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeeExitDTO.getEmpId());
		ServiceResponse response = new ServiceResponse();
		try {
			
			EmployeeResignation resignationObj = employeeResignationRepository
					.findByEmpIdAndResignationStatus(employeeExitDTO.getEmpId(), "Pending");
			
			if(resignationObj != null) {
				resignationObj.setResignationStatus("Revoked");
				resignationObj.setStatusUpdatedBy(employeeExitDTO.getEmpId());
				resignationObj.setStatusUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				
				EmployeeResignation dbResponse = employeeResignationRepository.save(resignationObj);
				
				if(dbResponse != null) {
					
					//send application revoke mail
					Employee employeeObj = employeeRepository.findByEmpId(dbResponse.getEmpId());
					List<Object[]> employeeData = employeeRepository.getEmployeeData(employeeExitDTO.getEmpId());
					LocalDate dateOfResign = dbResponse.getCreatedOn().toLocalDateTime().toLocalDate();
					
					String managerEmail = null;
					String HodMail = null;
					if(employeeData != null) {
						for(Object[] object: employeeData) {
							managerEmail = object[5] != null ? object[5].toString() : null;
							HodMail = object[6] != null ? object[6].toString() : null;
						}
					}
					
					//Send Resignation Application reject mail
					mailService.sendMailWithCC(employeeObj.getEmail(), hrMailAddress +","+ managerEmail+","+HodMail,
							"Resignation Application Revoked",
							"Dear All,"
							+"<br>"+" &nbsp"+" &nbsp"+" "+employeeObj.getName()+" has revoked it's resignation Application."+
							"<br>"+"<br>"+"<b>"+"Resignation Details"+"<b>"+
							"<br>"+
							"EmpID :"+" "+ "A-"+employeeObj.getEmployeementId()+
							"<br>"+
							"Name :"+" "+ employeeObj.getName()+
							"<br>"+
							"Date Of Resignation :"+" "+ dateOfResign+
							"<br>"+
							"Revoke Reason :"+" "+ employeeExitDTO.getRevokeReason());
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Resignation Application revoked successfully.");
					
					apiLogInfo.setApiResponse("Resignation Application revoked successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to revoke Resignation Application.");
					
					apiLogInfo.setApiResponse("Unable to revoke Resignation Application.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find Resignation Application.");
				
				apiLogInfo.setApiResponse("Unable to find Resignation Application.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

}

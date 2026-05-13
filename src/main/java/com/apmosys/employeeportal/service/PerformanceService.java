package com.apmosys.employeeportal.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import com.apmosys.employeeportal.dto.EmployeeteamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.ExportExcelPerformance;
import com.apmosys.employeeportal.dto.HrHodHrViewPerformance;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PerformanceDTO;
import com.apmosys.employeeportal.dto.PerformanceRatingDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDTO;
import com.apmosys.employeeportal.dto.QuarterCycleDTO;
import com.apmosys.employeeportal.dto.ReviewTypeDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeePerformance;
import com.apmosys.employeeportal.model.EmployeeRatingPerformance;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.ProjectInsightResponse;
import com.apmosys.employeeportal.model.QuaterCycle;
import com.apmosys.employeeportal.model.QuaterCycleExcludedEmployeesMap;
import com.apmosys.employeeportal.model.ReviewType;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeePerformanceRepository;
import com.apmosys.employeeportal.repository.EmployeeRatingPerformanceRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectInsightResponseRepository;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
import com.apmosys.employeeportal.repository.QuaterCycleExcludedEmployeesMapRepo;
import com.apmosys.employeeportal.repository.ReviewTypeRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class PerformanceService {

	@Autowired
	HttpServletRequest httpRequest;

	@Autowired
	private MailService mailService;

	@Autowired
	private EmployeePerformanceRepository employeePerformanceRepository;

	@Autowired
	private EmployeeRatingPerformanceRepository employeeRatingPerformanceRepository;

	@Autowired
	private QuarterCycleRepository quarterCycleRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	LogService logService;

	@Autowired
	private ReviewTypeRepository reviewTypeRepository;

	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;

	@Autowired
	TeamRepository teamRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	ProjectInsightResponseRepository projectInsightResponseRepository;
	
	@Autowired
	QuaterCycleExcludedEmployeesMapRepo quaterCycleExcludedEmployeesMapRepo;
	
	public ServiceResponse addReviewType(ReviewTypeDTO reviewTypeDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("create_review");
		apiLogInfo.setApiUrl("/api/addReviewType");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("reviewTypeId : " + reviewTypeDTO.getReviewTypeId() + ", reviewLabel : "
				+ reviewTypeDTO.getReviewLabel());
		try {
			ReviewType dbResponse = null;
			for (Long deptId : reviewTypeDTO.getDeptId()) {
				for (ReviewType reviewType : reviewTypeDTO.getAllSpecializationList()) {
					ReviewType reviewObj = new ReviewType();
					reviewObj.setReviewLabel(reviewType.getReviewLabel());
					reviewObj.setReviewFieldType(reviewType.getReviewFieldType());
					reviewObj.setQuarterId(reviewTypeDTO.getQuarterId());
					reviewObj.setDeptId(deptId);
					reviewObj.setFlag(true);
					reviewObj.setCreatedBy(reviewTypeDTO.getCreatedBy());
					reviewObj.setCondition(reviewType.getCondition());

					dbResponse = reviewTypeRepository.save(reviewObj);
				}
			}

			if (dbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Review Type  Added successfully.");
				apiLogInfo.setApiResponse("Review Type  Added successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to add Review Type.");
				apiLogInfo.setApiResponse("Unable to add Review Type.");
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
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;

	}

	@Transactional(rollbackOn = Exception.class)
	public ServiceResponse createQuarterCycle(QuarterCycleDTO quarterCycleDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			QuaterCycle quar = new QuaterCycle();
			
			quar.setFinancialYear(quarterCycleDTO.getFinancialYear());
			quar.setQuarterCycle(quarterCycleDTO.getQuarterCycle());
			quar.setCreatedBy(quarterCycleDTO.getCreatedBy());
			quar.setIsActive(quarterCycleDTO.getIsActive());
			quar.setIsEnable(quarterCycleDTO.getIsEnable());
			quar.setCycleType(quarterCycleDTO.getCycleType()); // added this to store the cycleType , i.e(monthly , quarterly , halfyearly).

			QuaterCycle quarterCycle = quarterCycleRepository.save(quar);
			if(!quarterCycleDTO.getExcludedEmployees().isEmpty() && quarterCycle != null) {
				List<QuaterCycleExcludedEmployeesMap> excludedEmpdata = new ArrayList<QuaterCycleExcludedEmployeesMap>();
				quarterCycleDTO.getExcludedEmployees().forEach(empId ->{
					QuaterCycleExcludedEmployeesMap data = new QuaterCycleExcludedEmployeesMap();
					data.setEmpId(empId);
					data.setQuarterId(quarterCycle.getQuarterId());
					data.setCreatedBy(quarterCycle.getCreatedBy());
					excludedEmpdata.add(data);
					});
				quaterCycleExcludedEmployeesMapRepo.saveAll(excludedEmpdata);
			}
			if (quarterCycle != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New Quarter Cycle Created.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("New Quarter Cycle creation Failed.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			throw e;
//
//		QuaterCycle quar = new QuaterCycle();
//		
//		quar.setFinancialYear(quarterCycleDTO.getFinancialYear());
//		quar.setQuarterCycle(quarterCycleDTO.getQuarterCycle());
//		quar.setCreatedBy(quarterCycleDTO.getCreatedBy());
//		quar.setIsActive(quarterCycleDTO.getIsActive());
//		quar.setIsEnable(quarterCycleDTO.getIsEnable());
//		quar.setCycleType(quarterCycleDTO.getCycleType()); // added this to store the cycleType , i.e(monthly , quarterly , halfyearly).
//		
//		
//		QuaterCycle quarterCycle = quarterCycleRepository.save(quar);
//		if (quarterCycle != null) {
//			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			response.setServiceResponse("New Cycle Created.");
//		} else {
//			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			response.setServiceResponse("New Quarter Cycle creation Failed.");			
//		}

		}
		return response;
	
	}
	public ServiceResponse getQuartersByYear(String financialYear) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<QuaterCycle> quarters = quarterCycleRepository.findByFinancialYear(financialYear);
			if (!quarters.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(quarters);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No quarters found for the given financial year.");
			}
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong while fetching quarters.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllQuarterCycles() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> quarterCycles = quarterCycleRepository.findAllQuartercycles();
			List<QuarterCycleDTO> dtoList = new ArrayList<QuarterCycleDTO>();
			if (quarterCycles != null) {
				quarterCycles.forEach((object) -> {
					QuarterCycleDTO quarDTO = new QuarterCycleDTO();

					quarDTO.setQuarterId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					quarDTO.setFinancialYear(object[1] != null ? object[1].toString() : null);
					quarDTO.setQuarterCycle(object[2] != null ? object[2].toString() : null);
					quarDTO.setCreatedOn(object[3] != null ? object[3].toString() : null);
					quarDTO.setCreatedBy(object[4] != null ? Long.parseLong(object[4].toString()) : null);
					quarDTO.setCreatedByName(object[5] != null ? object[5].toString() : null);
					quarDTO.setUpdatedOn(object[6] != null ? object[6].toString() : null);
					quarDTO.setUpdatedBy(object[7] != null ? Long.parseLong(object[7].toString()) : null);
					quarDTO.setUpdatedByName(object[8] != null ? object[8].toString() : null);
					quarDTO.setIsActive(object[9] != null ? Boolean.parseBoolean(object[9].toString()) : false);
					quarDTO.setIsEnable(object[10] != null ? Boolean.parseBoolean(object[10].toString()) : false);

					dtoList.add(quarDTO);

				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("QuarterCycles List is null.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getQuarterCycleById(Long quarterId) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> getQuarterCycleDetails = quarterCycleRepository.findQuarterCycleById(quarterId);

			if (!getQuarterCycleDetails.isEmpty()) {
				Object[] object = getQuarterCycleDetails.get(0);

				QuarterCycleDTO quarDTO = new QuarterCycleDTO();
				quarDTO.setQuarterId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
				quarDTO.setFinancialYear(object[1] != null ? object[1].toString() : null);
				quarDTO.setQuarterCycle(object[2] != null ? object[2].toString() : null);
				quarDTO.setCreatedOn(object[3] != null ? object[3].toString() : null);
				quarDTO.setCreatedBy(object[4] != null ? Long.parseLong(object[4].toString()) : null);
				quarDTO.setCreatedByName(object[5] != null ? object[5].toString() : null);
				quarDTO.setUpdatedOn(object[6] != null ? object[6].toString() : null);
				quarDTO.setUpdatedBy(object[7] != null ? Long.parseLong(object[7].toString()) : null);
				quarDTO.setUpdatedByName(object[8] != null ? object[8].toString() : null);
				quarDTO.setIsActive(object[9] != null ? Boolean.parseBoolean(object[9].toString()) : false);
				quarDTO.setIsEnable(object[10] != null ? Boolean.parseBoolean(object[10].toString()) : false);
				quarDTO.setCycleType(object[11] != null ? object[11].toString():null);
				List<QuaterCycleExcludedEmployeesMap> excludedEmpList = quaterCycleExcludedEmployeesMapRepo.findByQuarterId(quarterId);
				if(!excludedEmpList.isEmpty()) {
				quarDTO.setExcludedEmployees(excludedEmpList.stream()
				        .map(QuaterCycleExcludedEmployeesMap::getEmpId)
				        .filter(Objects::nonNull)
				        .collect(Collectors.toList()));
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(quarDTO);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("QuarterCycle not found for ID: " + quarterId);
			}
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse isEnable(QuarterCycleDTO quarterCycleDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();

		try {
			Optional<QuaterCycle> optionalQuarterCyles = quarterCycleRepository
					.findById(quarterCycleDTO.getQuarterId());

			if (optionalQuarterCyles.isPresent()) {
				QuaterCycle quaterCycle = optionalQuarterCyles.get();

				quaterCycle.setIsEnable(quarterCycleDTO.getIsEnable());

				quarterCycleRepository.save(quaterCycle);

				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceError("Quarter Cycle not found with ID: " + quarterCycleDTO.getQuarterId());
			}

		} catch (Exception e) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceError(e.getMessage());
		}

		return serviceResponse;
	}
	
	public ServiceResponse isDelete(QuarterCycleDTO quarterCycleDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();

		try {
			Optional<QuaterCycle> optionalQuarterCyles = quarterCycleRepository
					.findById(quarterCycleDTO.getQuarterId());

			if (optionalQuarterCyles.isPresent()) {
				QuaterCycle quaterCycle = optionalQuarterCyles.get();

				quaterCycle.setIsActive(quarterCycleDTO.getIsActive());

				quarterCycleRepository.save(quaterCycle);

				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceError("Quarter Cycle not found with ID: " + quarterCycleDTO.getQuarterId());
			}

		} catch (Exception e) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceError(e.getMessage());
		}

		return serviceResponse;
	}

	public ServiceResponse getReviewType() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_ReviewType");
		apiLogInfo.setApiUrl("/api/getAllReview");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			List<ReviewType> validReviewDetails = new ArrayList<>();
			List<ReviewType> reviewDetails=reviewTypeRepository.findAll();
			logBuilder.append("getAllReview size : "+reviewDetails.size());
			if (reviewDetails.isEmpty()) {

	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No ReviewType found.");

	            return response;
	        }
			 // Filter only active records
	        validReviewDetails = reviewDetails.stream()
	                .filter(ReviewType::getFlag)
	                .collect(Collectors.toList());

	        // Collect unique IDs
	        Set<Long> deptIds = validReviewDetails.stream()
	                .map(ReviewType::getDeptId)
	                .filter(Objects::nonNull)
	                .collect(Collectors.toSet());

	        Set<Long> empIds = new HashSet<>();

	        validReviewDetails.forEach(r -> {

	            if (r.getCreatedBy() != null) {
	                empIds.add(r.getCreatedBy());
	            }

	            if (r.getUpdatedBy() != null) {
	                empIds.add(r.getUpdatedBy());
	            }
	        });

	        Set<Long> quarterIds = validReviewDetails.stream()
	                .map(ReviewType::getQuarterId)
	                .filter(Objects::nonNull)
	                .collect(Collectors.toSet());
	        
	        // Bulk fetch
	        Map<Long, Department> departmentMap =
	                departmentRepository.findAllById(deptIds)
	                        .stream()
	                        .collect(Collectors.toMap(
	                                Department::getDeptId,
	                                Function.identity()
	                        ));

	        Map<Long, Employee> employeeMap =
	                employeeRepository.findAllById(empIds)
	                        .stream()
	                        .collect(Collectors.toMap(
	                                Employee::getEmpId,
	                                Function.identity()
	                        ));

	        Map<Long, QuaterCycle> quarterMap =
	                quarterCycleRepository.findAllById(quarterIds)
	                        .stream()
	                        .collect(Collectors.toMap(
	                                QuaterCycle::getQuarterId,
	                                Function.identity()
	                        ));
	        
	     // Populate response data
	        for (ReviewType reviewDetail : validReviewDetails) {

	            Department department = departmentMap.get(reviewDetail.getDeptId());

	            if (department != null) {
	                reviewDetail.setDepartmentName(department.getName());
	            }

	            Employee createdByEmployee =
	                    employeeMap.get(reviewDetail.getCreatedBy());

	            if (createdByEmployee != null) {
	                reviewDetail.setEmployeeName(createdByEmployee.getName());
	            }

	            Employee updatedByEmployee =
	                    employeeMap.get(reviewDetail.getUpdatedBy());

	            if (updatedByEmployee != null) {
	                reviewDetail.setUpdatedByName(updatedByEmployee.getName());
	            }

	            QuaterCycle quaterCycle =
	                    quarterMap.get(reviewDetail.getQuarterId());

	            if (quaterCycle != null) {
	                reviewDetail.setActive(quaterCycle.getIsActive());
	                reviewDetail.setQuarterCycle(quaterCycle.getQuarterCycle());
	            }
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(validReviewDetails);
//			if(reviewDetails !=null) {
//				reviewDetails.forEach((reviewDetail)->{
//					
//					if (reviewDetail.getFlag()) {
//
//						Optional.ofNullable(reviewDetail.getDeptId()).ifPresent(deptId -> {
//							Optional<Department> department = Optional
//									.ofNullable(departmentRepository.findByDeptId(deptId));
//							department.ifPresent(dept -> reviewDetail.setDepartmentName(dept.getName()));
//						});
//
//						Optional.ofNullable(reviewDetail.getCreatedBy()).ifPresent(empId -> {
//							Optional<Employee> employee = Optional.ofNullable(employeeRepository.findByEmpId(empId));
//							employee.ifPresent(emp -> reviewDetail.setEmployeeName(emp.getName()));
//						});
//
//						Optional.ofNullable(reviewDetail.getUpdatedBy()).ifPresent(empId -> {
//							Optional<Employee> employee = Optional.ofNullable(employeeRepository.findByEmpId(empId));
//							employee.ifPresent(emp -> reviewDetail.setUpdatedByName(emp.getName()));
//						});
//
//						Optional.ofNullable(reviewDetail.getQuarterId()).ifPresent(quarterId -> {
//							Optional<QuaterCycle> optionalQuarterCycle = quarterCycleRepository.findById(quarterId);
//							optionalQuarterCycle.ifPresent(quaterCycle -> {
//								reviewDetail.setActive(quaterCycle.getIsActive());
//								reviewDetail.setQuarterCycle(quaterCycle.getQuarterCycle());
//							});
//						});
//
//						validReviewDetails.add(reviewDetail);
//					}
//
//				});
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(validReviewDetails);
//				apiLogInfo.setApiResponse("ReviewType  Fetched successfully.");
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Unable to Fetche ReviewType.");
//				apiLogInfo.setApiResponse("Unable to Fetche ReviewType.");
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}

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

	@Transactional(rollbackOn = Exception.class)
	public ServiceResponse deleteReviewType(Long reviewTypeId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Delete_ReviewType");
		apiLogInfo.setApiUrl("/api/deleteReviewType");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getAllReview size : " + reviewTypeRepository.findAll().size());
		try {

			ReviewType reviewDetails = reviewTypeRepository.findByReviewTypeId(reviewTypeId);
			if (reviewDetails != null) {
				reviewDetails.setFlag(false);
				reviewTypeRepository.save(reviewDetails);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Review Type Deleted successfully.");
				apiLogInfo.setApiResponse("Review Type Deleted successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to Delete Review Type.");
				apiLogInfo.setApiResponse("Unable to Delete Review Type.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
			throw e;
		}

		return response;
	}

	@Transactional(rollbackOn = Exception.class)
	public ServiceResponse updateQuarterCycle(QuarterCycleDTO quarterCycleDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			QuaterCycle quar = quarterCycleRepository.getById(quarterCycleDTO.getQuarterId());
			quar.setFinancialYear(quarterCycleDTO.getFinancialYear());
			quar.setQuarterCycle(quarterCycleDTO.getQuarterCycle());
			quar.setUpdatedBy(quarterCycleDTO.getUpdatedBy());
			quar.setUpdatedOn(LocalDateTime.now());

			QuaterCycle quarterCycle = quarterCycleRepository.save(quar);
			List<QuaterCycleExcludedEmployeesMap> excludedEmployees = quaterCycleExcludedEmployeesMapRepo.findByQuarterId(quarterCycle.getQuarterId());  
			if(!excludedEmployees.isEmpty()) {
			quaterCycleExcludedEmployeesMapRepo.deleteAll(excludedEmployees);
			if(!quarterCycleDTO.getExcludedEmployees().isEmpty() && quarterCycle != null) {
				List<QuaterCycleExcludedEmployeesMap> newExcludedEmpdata = new ArrayList<QuaterCycleExcludedEmployeesMap>();
				quarterCycleDTO.getExcludedEmployees().forEach(empId ->{
					QuaterCycleExcludedEmployeesMap data = new QuaterCycleExcludedEmployeesMap();
					data.setEmpId(empId);
					data.setQuarterId(quarterCycle.getQuarterId());
					data.setCreatedBy(quarterCycle.getCreatedBy());
					newExcludedEmpdata.add(data);
					});
				quaterCycleExcludedEmployeesMapRepo.saveAll(newExcludedEmpdata);
			}
			}else {
				if(!quarterCycleDTO.getExcludedEmployees().isEmpty() && quarterCycle != null) {
					List<QuaterCycleExcludedEmployeesMap> newExcludedEmpdata = new ArrayList<QuaterCycleExcludedEmployeesMap>();
					quarterCycleDTO.getExcludedEmployees().forEach(empId ->{
						QuaterCycleExcludedEmployeesMap data = new QuaterCycleExcludedEmployeesMap();
						data.setEmpId(empId);
						data.setQuarterId(quarterCycle.getQuarterId());
						data.setCreatedBy(quarterCycle.getCreatedBy());
						newExcludedEmpdata.add(data);
						});
					quaterCycleExcludedEmployeesMapRepo.saveAll(newExcludedEmpdata);
				}
			}
			if (quarterCycle != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(" Quarter Cycle Updated.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(" Quarter Cycle updation Failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			throw e;
		}
		return response;
	}
	@Transactional(rollbackOn = Exception.class)
	public ServiceResponse updateReviewType(ReviewTypeDTO reviewTypeDTO) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_review");
		apiLogInfo.setApiUrl("/api/updateReviewType");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("reviewTypeId : " + reviewTypeDTO.getReviewTypeId() + ", reviewLabel : "
				+ reviewTypeDTO.getReviewLabel());
		try {
			ReviewType dbResponse = null;
			ReviewType reviewDetails = reviewTypeRepository.findByReviewTypeId(reviewTypeDTO.getReviewTypeId());

			if (reviewDetails != null) {
				for (Long deptId : reviewTypeDTO.getDeptId()) {
					for (ReviewType reviewType : reviewTypeDTO.getAllSpecializationList()) {
						reviewDetails.setReviewLabel(reviewType.getReviewLabel());
						reviewDetails.setReviewFieldType(reviewType.getReviewFieldType());
						reviewDetails.setDeptId(deptId);
						reviewDetails.setFlag(true);
						reviewDetails.setQuarterId(reviewTypeDTO.getQuarterId());
						reviewDetails.setUpdatedBy(reviewTypeDTO.getUpdatedBy());
						reviewDetails.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
						reviewDetails.setCondition(reviewType.getCondition());

						dbResponse = reviewTypeRepository.save(reviewDetails);
					}
				}
				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Review Type  updated successfully.");
					apiLogInfo.setApiResponse("Review Type  updated successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to update Review Type.");
					apiLogInfo.setApiResponse("Unable to update Review Type.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(" Review Type Type Does Not Exit");
				apiLogInfo.setApiResponse(" Review Type Type Does Not Exit");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
			throw e;
		}

		return response;
	}

	public ServiceResponse getReviewTypeById(ReviewTypeDTO reviewTypeDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getReviewTypeById");
		apiLogInfo.setApiUrl("/api/getReviewTypeById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getReviewTypeId : " + reviewTypeDTO.getReviewTypeId());
		try {

			ReviewType reviewDetails = reviewTypeRepository.findByReviewTypeId(reviewTypeDTO.getReviewTypeId());
			System.out.println("kjnbsjvh" + reviewDetails);
			if (reviewDetails != null) {

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(reviewDetails);
				apiLogInfo.setApiResponse("ReviewTypeById Fetched successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to Fetched ReviewTypeById.");
				apiLogInfo.setApiResponse("Unable to Fetched ReviewTypeById.");
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

	public ServiceResponse submitEmployeePerformanceHOD(PerformanceDTO employeePerformanceDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Long empId = employeePerformanceDTO.getEmpId();
			Long quarterId = employeePerformanceDTO.getQuarterId();
			String actionBy = employeePerformanceDTO.getActionBy();

			EmployeePerformance employeePerformance = employeePerformanceRepository
					.findLatestByEmpIdAndQuarterId(empId, quarterId)
					.stream().findFirst().orElse(null);

			if (employeePerformance == null) {
				employeePerformance = new EmployeePerformance();
				employeePerformance.setEmp_id(empId);
				employeePerformance.setQuarterId(quarterId);
			}

			employeePerformance.setFinal_rating(employeePerformanceDTO.getFinalRating());

			if ("RM".equals(actionBy)) {
				// Manager / Reporting Manager submit: set manager fields
				Long submitterId = employeePerformanceDTO.getHodId(); // current user (submitter)
				employeePerformance.setManager_id(submitterId);
				employeePerformance.setManager_remarks(employeePerformanceDTO.getHodRemarks());
				employeePerformance.setManager_review_date(LocalDateTime.now());
				employeePerformance.setCompletion_status("Ongoing");
				// When Manager and HOD are the same person: auto-complete HOD step so one submit clears both
				Long employeeDeptHodId = departmentRepository.findHodIdByEmpId(empId);
				if (employeeDeptHodId != null && employeeDeptHodId.equals(submitterId)) {
					employeePerformance.setHod_id(submitterId);
					employeePerformance.setHod_approval_date(LocalDateTime.now());
					employeePerformance.setHod_remarks(employeePerformanceDTO.getHodRemarks() != null
							? employeePerformanceDTO.getHodRemarks() : employeePerformance.getManager_remarks());
				}
			} else {
				// HOD submit: set HOD fields. Completed only after HR accepts (see submitEmployeePerformanceHR).
				employeePerformance.setHod_id(employeePerformanceDTO.getHodId());
				employeePerformance.setHod_remarks(employeePerformanceDTO.getHodRemarks());
				employeePerformance.setHod_approval_date(LocalDateTime.now());
				employeePerformance.setCompletion_status("Pending");
			}

			EmployeePerformance savedEmployeePerformance = employeePerformanceRepository.save(employeePerformance);
			if (savedEmployeePerformance != null) {

				for (PerformanceRatingDTO ratingDTO : employeePerformanceDTO.getPerformanceRatings()) {
					EmployeeRatingPerformance ratingPerformance = new EmployeeRatingPerformance();
					ratingPerformance.setQuarterId(employeePerformanceDTO.getQuarterId());
					ratingPerformance.setReviewTypeId(ratingDTO.getReviewTypeId());
					ratingPerformance.setRatingValue(ratingDTO.getRating());
					ratingPerformance.setEmpId(employeePerformanceDTO.getEmpId());
					ratingPerformance.setCriteriaRemark(normalizeCriteriaRemark(ratingDTO.getCriteriaRemark()));

					employeeRatingPerformanceRepository.save(ratingPerformance);
				}

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Employee Performance Submitted Successfully.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Performance Submission Failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());

		}
		return response;
	}

	public ServiceResponse submitEmployeePerformanceHR(PerformanceDTO employeePerformanceDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			EmployeePerformance savedEmployeePerformanceHR = null;
			EmployeePerformance optionalEmployeePerformance = employeePerformanceRepository
					.findByPerformanceId(employeePerformanceDTO.getEmployeePerformanceId());

			if (optionalEmployeePerformance != null) {

				optionalEmployeePerformance.setHr_id(employeePerformanceDTO.getHrId());
				optionalEmployeePerformance.setHr_remarks(employeePerformanceDTO.getHrRemark());
				optionalEmployeePerformance.setHr_review_status(employeePerformanceDTO.getHrReviewStatus());

				if (employeePerformanceDTO.getHrReviewStatus().equalsIgnoreCase("Accepted")) {
					optionalEmployeePerformance.setCompletion_status("Completed");
					optionalEmployeePerformance.setFinal_rating(employeePerformanceDTO.getFinalRating());
				} else if (employeePerformanceDTO.getHrReviewStatus().equalsIgnoreCase("Rejected")) {
					optionalEmployeePerformance.setCompletion_status("Rejected");
					optionalEmployeePerformance.setRejectStatus(true);
					// Persist edited rating even when HR rejects
					optionalEmployeePerformance.setFinal_rating(employeePerformanceDTO.getFinalRating());
				}

				savedEmployeePerformanceHR = employeePerformanceRepository.save(optionalEmployeePerformance);

				if (savedEmployeePerformanceHR != null && employeePerformanceDTO.getPerformanceRatings() != null
						&& !employeePerformanceDTO.getPerformanceRatings().isEmpty()) {
					for (PerformanceRatingDTO ratingDTO : employeePerformanceDTO.getPerformanceRatings()) {
						EmployeeRatingPerformance ratingPerformance = employeeRatingPerformanceRepository
								.findById(ratingDTO.getPerformanceRatingId()).orElse(null);
						if (ratingPerformance != null) {
							ratingPerformance.setQuarterId(employeePerformanceDTO.getQuarterId());
							ratingPerformance.setReviewTypeId(ratingDTO.getReviewTypeId());
							ratingPerformance.setRatingValue(ratingDTO.getRating());
							ratingPerformance.setEmpId(employeePerformanceDTO.getEmpId());
							applyCriteriaRemarkUpdate(ratingDTO, ratingPerformance);
							employeeRatingPerformanceRepository.save(ratingPerformance);
						}
					}
				}

			if (employeePerformanceDTO.getHrReviewStatus().equalsIgnoreCase("Accepted")) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Performance review accepted successfully.");
				} else if (employeePerformanceDTO.getHrReviewStatus().equalsIgnoreCase("Rejected")) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Performance review rejected successfully.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("could not change review review status.");
				}
			} else {

				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Performance record not found.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong while processing the request.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	/** HOD accept/reject: store hod_id, hod_remarks, hod_approval_date (accept) or hod_rejected_date (reject). On Accept, also persist final_rating and criteria ratings. */
	public ServiceResponse submitRemarksByHOD(PerformanceDTO dto) {
		ServiceResponse response = new ServiceResponse();
		try {
			EmployeePerformance ep = employeePerformanceRepository.findByPerformanceId(dto.getEmployeePerformanceId());
			if (ep == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Performance record not found.");
				return response;
			}
			ep.setHod_id(dto.getHodId());
			ep.setHod_remarks(dto.getHodRemarks());
			String status = dto.getHodReviewStatus() != null ? dto.getHodReviewStatus().trim() : "";
			if ("Accepted".equalsIgnoreCase(status)) {
				ep.setHod_approval_date(LocalDateTime.now());
				ep.setHod_rejected_date(null);
				ep.setCompletion_status("Ongoing");
				// Persist HOD-updated final rating and criteria ratings on accept/reject
				if (dto.getFinalRating() != null) {
					ep.setFinal_rating(dto.getFinalRating());
				}
				if (dto.getPerformanceRatings() != null && !dto.getPerformanceRatings().isEmpty()) {
					for (PerformanceRatingDTO ratingDTO : dto.getPerformanceRatings()) {
						EmployeeRatingPerformance ratingPerformance = employeeRatingPerformanceRepository
								.findById(ratingDTO.getPerformanceRatingId()).orElse(null);
						if (ratingPerformance != null) {
							ratingPerformance.setQuarterId(dto.getQuarterId());
							ratingPerformance.setReviewTypeId(ratingDTO.getReviewTypeId());
							ratingPerformance.setRatingValue(ratingDTO.getRating());
							ratingPerformance.setEmpId(dto.getEmpId());
							applyCriteriaRemarkUpdate(ratingDTO, ratingPerformance);
							employeeRatingPerformanceRepository.save(ratingPerformance);
						}
					}
				}
			} else if ("Rejected".equalsIgnoreCase(status)) {
				ep.setHod_rejected_date(LocalDateTime.now());
				ep.setCompletion_status("Rejected");
				ep.setRejectStatus(true);
				// Persist HOD-edited final rating/criteria even on rejection
				if (dto.getFinalRating() != null) {
					ep.setFinal_rating(dto.getFinalRating());
				}
				if (dto.getPerformanceRatings() != null && !dto.getPerformanceRatings().isEmpty()) {
					for (PerformanceRatingDTO ratingDTO : dto.getPerformanceRatings()) {
						EmployeeRatingPerformance ratingPerformance = employeeRatingPerformanceRepository
								.findById(ratingDTO.getPerformanceRatingId()).orElse(null);
						if (ratingPerformance != null) {
							ratingPerformance.setQuarterId(dto.getQuarterId());
							ratingPerformance.setReviewTypeId(ratingDTO.getReviewTypeId());
							ratingPerformance.setRatingValue(ratingDTO.getRating());
							ratingPerformance.setEmpId(dto.getEmpId());
							applyCriteriaRemarkUpdate(ratingDTO, ratingPerformance);
							employeeRatingPerformanceRepository.save(ratingPerformance);
						}
					}
				}
			}
			employeePerformanceRepository.save(ep);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Accepted".equalsIgnoreCase(status)
					? "Performance review accepted successfully."
					: "Performance review rejected successfully.");
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getReviewDataForQuarter() {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getReviewDataForQuarter");
		apiLogInfo.setApiUrl("/api/getReviewDataForQuarter");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getReviewDataForQuarter");
		try {

			List<ReviewTypeDTO> reviewDataforQuater = new ArrayList<>();
			List<Object[]> getReviewDataForQuarter = reviewTypeRepository.getReviewDataForQuarter();
			if (getReviewDataForQuarter != null) {
				for (Object[] object : getReviewDataForQuarter) {
					ReviewTypeDTO reviewobj = new ReviewTypeDTO();
					reviewobj.setReviewLabel(object[0] != null ? object[0].toString() : null);
					reviewDataforQuater.add(reviewobj);
				}

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(reviewDataforQuater);
				apiLogInfo.setApiResponse("ReviewTypeData Fetched successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to Fetched ReviewTypeData.");
				apiLogInfo.setApiResponse("Unable to Fetched ReviewTypeData.");
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

	public ServiceResponse getEmployeePerformanceHOD() {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getEmployeePerformanceHOD");
		apiLogInfo.setApiUrl("/api/getEmployeePerformanceHOD");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getEmployeePerformanceHOD : ");
		try {

			List<PerformanceDTO> performance = new ArrayList<>();
			List<Object[]> getReviewDataForQuarter = employeePerformanceRepository.getEmployeePerformanceHOD();

			if (!getReviewDataForQuarter.isEmpty()) {
				// Grouping data by empId
				Map<Long, PerformanceDTO> empIdToPerformanceDTOMap = new HashMap<>();

				for (Object[] object : getReviewDataForQuarter) {
					Long empId = object[4] != null ? Long.parseLong(object[4].toString()) : null;

					// If this empId is not already in the map, create a new PerformanceDTO for it
					PerformanceDTO performanceDTO = empIdToPerformanceDTOMap.get(empId);
					if (performanceDTO == null) {
						performanceDTO = new PerformanceDTO();
						performanceDTO.setEmpId(empId);
						performanceDTO.setEmployeePerformanceId(
								object[0] != null ? Long.parseLong(object[0].toString()) : null);
						performanceDTO.setFinancialYear(object[2] != null ? object[2].toString() : null);
						performanceDTO.setQuarterCycle(object[3] != null ? object[3].toString() : null);
						performanceDTO.setFinalRating(object[7] != null ? object[7].toString() : null);
						performanceDTO.setHodApprovalDate(object[8] != null ? object[8].toString() : null);
						performanceDTO.setHodId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
						performanceDTO.setHodRemarks(object[10] != null ? object[10].toString() : null);

						// Initialize the performanceRatings list
						performanceDTO.setPerformanceRatings(new ArrayList<>());
						empIdToPerformanceDTOMap.put(empId, performanceDTO);
					}

					// Now handle the review data and group by quarterId
					PerformanceRatingDTO performanceRatingDTO = new PerformanceRatingDTO();
					performanceRatingDTO.setReviewLabel(object[5] != null ? object[5].toString() : null);
					performanceRatingDTO.setRating(object[6] != null ? new BigDecimal(object[6].toString()) : null);
					performanceRatingDTO
							.setReviewTypeId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
					performanceRatingDTO.setQuarterId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					// Now find the quarter group or create it
					Optional<PerformanceRatingDTO> existingRating = performanceDTO.getPerformanceRatings().stream()
							.filter(rating -> rating.getReviewTypeId().equals(performanceRatingDTO.getReviewTypeId())
									&& rating.getReviewLabel().equals(performanceRatingDTO.getReviewLabel()))
							.findFirst();

					if (!existingRating.isPresent()) {
						performanceDTO.getPerformanceRatings().add(performanceRatingDTO);
					}
				}

				// Add all grouped performanceDTOs to the list
				performance.addAll(empIdToPerformanceDTOMap.values());

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(performance);
				apiLogInfo.setApiResponse("Employee Performance fetched successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to fetch Employee Performance.");
				apiLogInfo.setApiResponse("Unable to fetch Employee Performance.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());

		}
		return response;
	}

	public ServiceResponse hrAndHODEmployeePerformanceView(PerformanceDTO employeePerformanceDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("hrAndHODEmployeePerformanceView");
		apiLogInfo.setApiUrl("/api/hrAndHODEmployeePerformanceView");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("hrAndHODEmployeePerformanceView : ");

		try {

			List<PerformanceDTO> performance = new ArrayList<>();
			List<Object[]> hrAndHODEmployeePerformanceViewDetails = employeePerformanceRepository
					.hrAndHODEmployeePerformanceView(employeePerformanceDTO.getEmpId(),
							employeePerformanceDTO.getQuarterId());
//			System.out.println("resultset is : "+hrAndHODEmployeePerformanceViewDetails);
			if (hrAndHODEmployeePerformanceViewDetails != null && !hrAndHODEmployeePerformanceViewDetails.isEmpty()) {
				System.out.println("resultset is : "+hrAndHODEmployeePerformanceViewDetails);

				for (Object[] object : hrAndHODEmployeePerformanceViewDetails) {
					PerformanceDTO performanceDetails = new PerformanceDTO();
					performanceDetails.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					performanceDetails.setCompletionStatus(object[1] != null ? object[1].toString() : null);
					performanceDetails
							.setEmployeePerformanceId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
					performanceDetails.setQuarterId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
					performanceDetails.setReviewLabel(object[4] != null ? object[4].toString() : null);
					performanceDetails.setReviewFieldType(object[5] != null ? object[5].toString() : null);
					performanceDetails.setCondition(object[6] != null ? object[6].toString() : null);
					performanceDetails.setRatingValue(object[7] != null ? new BigDecimal(object[7].toString()) : null);
					performanceDetails.setReviewTypeId(object[8] != null ? Long.parseLong(object[8].toString()) : null);
					performanceDetails.setHodRemarks(object[9] != null ? object[9].toString() : null);
					performanceDetails.setFinalRating(object[10] != null ? object[10].toString() : null);
					performanceDetails.setDeptId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
					performanceDetails
							.setPerformanceRatingId(object[12] != null ? Long.parseLong(object[12].toString()) : null);
					performanceDetails.setHrRemark(object[13] != null ? object[13].toString() : null);
					performanceDetails.setHrReviewStatus(object[14] != null ? object[14].toString() : null);
					performanceDetails
							.setRejectStatus(object[15] != null ? Boolean.parseBoolean(object[15].toString()) : false);
					performanceDetails.setManagerRemarks(object[16] != null ? object[16].toString() : null);
					performanceDetails.setCriteriaRemark(object[17] != null ? object[17].toString() : null);

					performance.add(performanceDetails);

				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(performance);
				apiLogInfo.setApiResponse("Employee Performance fetched successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to fetch Employee Performance.");
				apiLogInfo.setApiResponse("Unable to fetch Employee Performance.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {

			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());

		}
		return response;
	}

	/**
	 * Returns approval details for popup: manager, HOD, HR each with name, id, employmentId, rating, feedback.
	 * Uses latest employee_performance row for given empId and quarterId.
	 */
	public ServiceResponse getApprovalDetails(Long empId, Long quarterId) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> rows = employeePerformanceRepository.findApprovalDetailsByEmpIdAndQuarterId(empId, quarterId);
			if (rows == null || rows.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No performance record found.");
				return response;
			}
			Object[] row = rows.get(0);
			// Index: 0 manager_id, 1 manager_remarks, 2 final_rating, 3 manager_name, 4 manager_employment_id,
			// 5 hod_id, 6 hod_remarks, 7 hod_name, 8 hod_employment_id, 9 hr_id, 10 hr_remarks, 11 hr_name, 12 hr_employment_id
			Map<String, Object> result = new HashMap<>();
			Map<String, Object> manager = new HashMap<>();
			manager.put("name", row[3] != null ? row[3].toString() : null);
			manager.put("id", row[0] != null ? Long.parseLong(row[0].toString()) : null);
			manager.put("employmentId", row[4] != null ? row[4].toString() : null);
			manager.put("rating", row[2] != null ? row[2].toString() : null);
			manager.put("feedback", row[1] != null ? row[1].toString() : null);
			manager.put("history", new ArrayList<Map<String, Object>>());
			result.put("manager", manager);
			Map<String, Object> hod = new HashMap<>();
			hod.put("name", row[7] != null ? row[7].toString() : null);
			hod.put("id", row[5] != null ? Long.parseLong(row[5].toString()) : null);
			hod.put("employmentId", row[8] != null ? row[8].toString() : null);
			hod.put("rating", row[2] != null ? row[2].toString() : null);
			hod.put("feedback", row[6] != null ? row[6].toString() : null);
			hod.put("history", new ArrayList<Map<String, Object>>());
			result.put("hod", hod);
			Map<String, Object> hr = new HashMap<>();
			hr.put("name", row[11] != null ? row[11].toString() : null);
			hr.put("id", row[9] != null ? Long.parseLong(row[9].toString()) : null);
			hr.put("employmentId", row[12] != null ? row[12].toString() : null);
			hr.put("rating", row[2] != null ? row[2].toString() : null);
			hr.put("feedback", row[10] != null ? row[10].toString() : null);
			hr.put("history", new ArrayList<Map<String, Object>>());
			result.put("hr", hr);

			// Build audit history (all years for this employee) for each role + year-wise rating summary
			List<Object[]> historyRows = employeePerformanceRepository.findApprovalAuditHistoryByEmpId(empId);
			if (historyRows != null) {
				List<Map<String, Object>> managerHistory = new ArrayList<>();
				List<Map<String, Object>> hodHistory = new ArrayList<>();
				List<Map<String, Object>> hrHistory = new ArrayList<>();
				Map<String, List<Double>> fyToRatings = new HashMap<>();
				Map<String, List<String>> fyToManagers = new HashMap<>();
				for (Object[] h : historyRows) {
					String finalRating = h[1] != null ? h[1].toString() : null;
					String fy = h[3] != null ? h[3].toString() : "N/A";
					String quarterCycle = h[4] != null ? h[4].toString() : "N/A";
					if (finalRating != null && !finalRating.isEmpty()) {
						try {
							fyToRatings.computeIfAbsent(fy, k -> new ArrayList<>()).add(Double.parseDouble(finalRating));
						} catch (Exception ignored) {
						}
					}
					if (h[8] != null) {
						fyToManagers.computeIfAbsent(fy, k -> new ArrayList<>()).add(h[8].toString());
					}
					// manager: 5 id,6 remarks,7 date,8 name,9 employmentId
					if (h[5] != null && (h[6] != null || h[7] != null)) {
						Map<String, Object> entry = new HashMap<>();
						entry.put("name", h[8] != null ? h[8].toString() : null);
						entry.put("id", Long.parseLong(h[5].toString()));
						entry.put("employmentId", h[9] != null ? h[9].toString() : null);
						entry.put("feedback", h[6] != null ? h[6].toString() : null);
						entry.put("rating", finalRating);
						entry.put("status", h[7] != null ? "Submitted" : "Pending");
						entry.put("actionDate", h[7] != null ? h[7].toString() : null);
						entry.put("financialYear", fy);
						entry.put("quarterCycle", quarterCycle);
						entry.put("managerName", h[8] != null ? h[8].toString() : null);
						managerHistory.add(entry);
					}
					// hod: 10 id,11 remarks,12 approval date,13 rejected date,14 name,15 employmentId
					if (h[10] != null && (h[11] != null || h[12] != null || h[13] != null)) {
						Map<String, Object> entry = new HashMap<>();
						entry.put("name", h[14] != null ? h[14].toString() : null);
						entry.put("id", Long.parseLong(h[10].toString()));
						entry.put("employmentId", h[15] != null ? h[15].toString() : null);
						entry.put("feedback", h[11] != null ? h[11].toString() : null);
						entry.put("rating", finalRating);
						String hodStatus = h[12] != null ? "Accepted" : (h[13] != null ? "Rejected" : "Pending");
						entry.put("status", hodStatus);
						entry.put("actionDate", h[12] != null ? h[12].toString() : (h[13] != null ? h[13].toString() : null));
						entry.put("financialYear", fy);
						entry.put("quarterCycle", quarterCycle);
						entry.put("managerName", h[8] != null ? h[8].toString() : null);
						hodHistory.add(entry);
					}
					// hr: 16 id,17 remarks,18 date,19 status,20 name,21 employmentId
					if (h[16] != null && (h[17] != null || h[18] != null || h[19] != null)) {
						Map<String, Object> entry = new HashMap<>();
						entry.put("name", h[20] != null ? h[20].toString() : null);
						entry.put("id", Long.parseLong(h[16].toString()));
						entry.put("employmentId", h[21] != null ? h[21].toString() : null);
						entry.put("feedback", h[17] != null ? h[17].toString() : null);
						entry.put("rating", finalRating);
						entry.put("status", h[19] != null ? h[19].toString() : "Pending");
						entry.put("actionDate", h[18] != null ? h[18].toString() : null);
						entry.put("financialYear", fy);
						entry.put("quarterCycle", quarterCycle);
						entry.put("managerName", h[8] != null ? h[8].toString() : null);
						hrHistory.add(entry);
					}
				}
				manager.put("history", managerHistory);
				hod.put("history", hodHistory);
				hr.put("history", hrHistory);

				List<Map<String, Object>> yearWiseRatings = new ArrayList<>();
				for (String yearKey : fyToRatings.keySet()) {
					List<Double> vals = fyToRatings.get(yearKey);
					double avg = vals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
					Map<String, Object> y = new HashMap<>();
					y.put("financialYear", yearKey);
					y.put("averageRating", String.format("%.2f", avg));
					y.put("records", vals.size());
					List<String> managers = fyToManagers.getOrDefault(yearKey, new ArrayList<>()).stream().filter(s -> s != null && !s.trim().isEmpty()).distinct().collect(Collectors.toList());
					y.put("managerNames", managers);
					yearWiseRatings.add(y);
				}
				result.put("yearWiseRatings", yearWiseRatings);
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(result);
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse DepartmentbyEmployeecont(HrHodHrViewPerformance hrHodHrViewPerformance) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getAllDepartmentbyEmployeecont");
		apiLogInfo.setApiUrl("/api/getAllDepartmentbyEmployeecont");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getAllDepartmentbyEmployeecont : ");

		try {
			List<Object[]> employeeDepertmemtCount = null;
			List<HashMap<String, Object>> department = new ArrayList<HashMap<String, Object>>();
			HashMap<String, Object> map = new HashMap<String, Object>();
			if (hrHodHrViewPerformance.getDeptId() == null) {
				employeeDepertmemtCount = employeePerformanceRepository.DepartmentbyEmployeecontquery();
			} else {
				employeeDepertmemtCount = employeePerformanceRepository
						.DepartmentbyEmployeecontqueryForEachDepartment(hrHodHrViewPerformance.getDeptId());
			}

			if (employeeDepertmemtCount != null && !employeeDepertmemtCount.isEmpty()) {
				for (Object[] object : employeeDepertmemtCount) {

					map = new HashMap<String, Object>();

					map.put("department", object[0] != null ? object[0].toString() : null);
					map.put("TotalNumberofemp", object[1] != null ? Long.parseLong(object[1].toString()) : null);
					map.put("ratinggivenbymanager", object[2] != null ? Long.parseLong(object[2].toString()) : null);
					map.put("pendingratinggivenbymanager",
							object[3] != null ? Long.parseLong(object[3].toString()) : null);
					map.put("managerName", object[4] != null ? object[4].toString() : null);

					department.add(map);

				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(department);
				apiLogInfo.setApiResponse("Department  fetched successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to fetch Department.");
				apiLogInfo.setApiResponse("Unable to fetch Department.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {

			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());

		}
		return response;

	}

	public ServiceResponse updateEmployeePerformanceHOD(PerformanceDTO employeePerformanceDTO) {

		ServiceResponse response = new ServiceResponse();
		try {
			EmployeePerformance savedEmployeePerformance = null;
			EmployeePerformance employeePerformanceDetails = employeePerformanceRepository
					.findByPerformanceId(employeePerformanceDTO.getEmployeePerformanceId());
			if (employeePerformanceDetails != null) {

				employeePerformanceDetails.setFinal_rating(employeePerformanceDTO.getFinalRating());
				employeePerformanceDetails.setHod_remarks(employeePerformanceDTO.getHodRemarks());

				// Manager resubmit after HOD reject: do not set hod_id/hod_approval_date; status = Pending HOD until HOD accepts
				boolean isManagerResubmit = Boolean.TRUE.equals(employeePerformanceDetails.getRejectStatus())
						|| "RM".equalsIgnoreCase(employeePerformanceDTO.getActionBy());
				if (isManagerResubmit) {
					employeePerformanceDetails.setCompletion_status("Pending HOD");
					// do not set hod_id or hod_approval_date – HOD has not accepted yet
				} else {
					employeePerformanceDetails.setHod_id(employeePerformanceDTO.getHodId());
					employeePerformanceDetails.setHod_approval_date(LocalDateTime.now());
					employeePerformanceDetails.setCompletion_status("Ongoing");
				}
				savedEmployeePerformance = employeePerformanceRepository.save(employeePerformanceDetails);
			}

			if (savedEmployeePerformance != null) {

				for (PerformanceRatingDTO ratingDTO : employeePerformanceDTO.getPerformanceRatings()) {
					EmployeeRatingPerformance ratingPerformance = employeeRatingPerformanceRepository
							.findById(ratingDTO.getPerformanceRatingId()).orElse(null);
					if (ratingPerformance != null) {
						ratingPerformance.setQuarterId(employeePerformanceDTO.getQuarterId());
						ratingPerformance.setReviewTypeId(ratingDTO.getReviewTypeId());
						ratingPerformance.setRatingValue(ratingDTO.getRating());
						ratingPerformance.setEmpId(employeePerformanceDTO.getEmpId());
						applyCriteriaRemarkUpdate(ratingDTO, ratingPerformance);
						employeeRatingPerformanceRepository.save(ratingPerformance);
					}

				}

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Employee Performance updated Successfully.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Performance updatation Failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());

		}
		return response;
	}

	public ServiceResponse getReviewLabelForEveryDepartment() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getReviewLabelForEveryDepartment");
		apiLogInfo.setApiUrl("/api/getReviewLabelForEveryDepartment");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getReviewLabelForEveryDepartment  : ");

		try {
			List<ReviewTypeDTO> reviewLabelList = new ArrayList<>();
			List<Object[]> reviewLabelDetails = reviewTypeRepository.getReviewLabelForEveryDepartment();

			if (reviewLabelDetails != null && !reviewLabelDetails.isEmpty()) {
				for (Object[] object : reviewLabelDetails) {
					ReviewTypeDTO reviewTypeDTO = new ReviewTypeDTO();
					reviewTypeDTO.setReviewLabel(object[0] != null ? object[0].toString() : null);
					reviewTypeDTO.setQuarterId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					reviewTypeDTO.setDepartmentId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
					reviewLabelList.add(reviewTypeDTO);

				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(reviewLabelList);
				apiLogInfo.setApiResponse("getReviewLabelForEveryDepartment fetched successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to fetch getReviewLabelForEveryDepartment.");
				apiLogInfo.setApiResponse("Unable to fetch getReviewLabelForEveryDepartment.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}

		return response;
	}

	// mails to hods
//	@Scheduled(cron = "0 14 14 * * ?")
	public void sendPerformanceReviewEmailsToHOD() {
		List<Object[]> activehods = employeePerformanceRepository.findAllActiveHODs();

		for (Object[] hod : activehods) {
			Long hodId = ((Number) hod[0]).longValue();
			String hodEmail = (String) hod[1];
			String hodName = (String) hod[2];
			List<Object[]> reviewedEmployees = employeePerformanceRepository
					.findAllOngoingReviewedEmployeesUnderHOD(hodId);
			if (!reviewedEmployees.isEmpty()) {
				try {
					String mailBody = generateHtmlEmail(hodName, reviewedEmployees);
					String subject = "Performance Review Pending - Action Required";
					mailService.sendMail(hodEmail, subject, mailBody);
				} catch (MessagingException e) {
					System.err.println("Failed to send email to HOD: " + hodEmail);
					e.printStackTrace();
				}
			}
		}
	}

	private String generateHtmlEmail(String hodName,List<Object[]> reviewedEmployees) {
	    StringBuilder html = new StringBuilder();

	    html.append("<html><body>");
	    html.append("<p>Dear ").append(hodName).append(",</p>");
	    html.append("<p>The following employees have ongoing performance reviews that require your attention:</p>");

	    // Creating Table for Employee Details
	    html.append("<table border='1' style='border-collapse: collapse; width: 100%; text-align: left;'>");
	    html.append("<tr style='background-color: #f2f2f2;'>");
	    html.append("<th style='padding: 8px;'>Employee ID</th>");
	    html.append("<th style='padding: 8px;'>Employee Name</th>");
	    html.append("<th style='padding: 8px;'>Department</th>");
	    html.append("<th style='padding: 8px;'>Financial Year</th>");
	    html.append("<th style='padding: 8px;'>Quarter Cycle</th>");
	    html.append("</tr>");

	    for (Object[] emp : reviewedEmployees) {
	        html.append("<tr>");
	        html.append("<td style='padding: 8px;'>A-" + emp[6] + "</td>"); 
	        html.append("<td style='padding: 8px;'>" + emp[4] + "</td>"); 
	        html.append("<td style='padding: 8px;'>" + emp[5] + "</td>"); 
	        html.append("<td style='padding: 8px;'>" + emp[2] + "</td>"); 
	        html.append("<td style='padding: 8px;'>" + emp[3] + "</td>"); 
	        html.append("</tr>");
	    }

	    html.append("</table>");
	    html.append("<p>Please take further necessary actions.</p>");
	    html.append("<p>Regards,</p>");
	    html.append("<p>HR Team</p>");
	    html.append("</body></html>");

	    return html.toString();
	}
	
	public ResponseEntity<PerformanceDTO> checkUserHaveTeam(PerformanceDTO performanceDTO) {
		PerformanceDTO responseObj = new PerformanceDTO();
		try {
			List<EmployeeTeamMap> emplTeamList = employeeTeamMapRepository
					.findByEmpIdAndActive(performanceDTO.getEmpId(), 1l);

			if (!emplTeamList.isEmpty()) {
				emplTeamList.forEach((object) -> {
					if (object.getEmployeeRole().contains("TeamLead") || object.getEmployeeRole().contains("HOD")
							|| object.getEmployeeRole().contains("Manager") || object.getEmployeeRole().contains("HR")
							|| object.getEmployeeRole().contains("RMG")
							|| object.getEmployeeRole().contains("SuperAdmin")) {
						responseObj.setIsUserHaveTeam("true");
						return;
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(responseObj);
	}

	public ResponseEntity<List<EmployeeteamDto>> getTeamEmployeeListInTeamDashboard(PerformanceDTO performanceDTO) {
		List<EmployeeteamDto> response = new ArrayList<EmployeeteamDto>();
		try {
			List<Object[]> employeeDbResponse = new ArrayList<>();
			List<Object[]> employeeTeamDbResponse = new ArrayList<>();
			
			if(performanceDTO.getTabType().equals("reviewTeam")) {
				employeeDbResponse = employeePerformanceRepository.getEmployeeUnderReviewByEmpId(performanceDTO.getEmpId());
				employeeTeamDbResponse = new ArrayList<>();
			}else {
				// if employeeRole = SuperAdmin,HR,RMG show all users
				if(performanceDTO.getEmployeeRole().equalsIgnoreCase("SuperAdmin")
						|| performanceDTO.getEmployeeRole().equalsIgnoreCase("HR")
						|| performanceDTO.getEmployeeRole().equalsIgnoreCase("RMG")) {
					employeeDbResponse = employeePerformanceRepository.getAllEmployeeForTeamMember();
				}else if(performanceDTO.getEmployeeRole().equalsIgnoreCase("HOD") ) {
					// if employeeRole = HoD show all users by department
					List<Long> deptIds = departmentRepository.findByHodId(performanceDTO.getEmpId())
						    .stream()
						    .map(Department::getDeptId)
						    .collect(Collectors.toList());
					
					employeeDbResponse = employeePerformanceRepository
							.getAllEmployeeForTeamMemberByDepartment(deptIds);
				}else {
					/* if employeeRole = Manager, TL, Employee show user by reportsTo 
					   && user persona in a Team i.e if user is Employee but persona in Team is of TL
					*/
					List<EmployeeTeamMap> emplTeamList = employeeTeamMapRepository.findByEmpIdAndActive(performanceDTO.getEmpId(), 1l);
					List<Long> teamIds = new ArrayList<>();
					AtomicBoolean isTeamAssign = new AtomicBoolean(false);
					if(!emplTeamList.isEmpty()) {
						teamIds = emplTeamList.stream().map(EmployeeTeamMap::getTeamId).collect(Collectors.toList());
						emplTeamList.forEach((object) -> {
							if(object.getEmployeeRole().contains("TeamLead") ||
									object.getEmployeeRole().contains("HOD") || 
									object.getEmployeeRole().contains("Manager") || 
									object.getEmployeeRole().contains("HR") || 
									object.getEmployeeRole().contains("RMG") || object.getEmployeeRole().contains("SuperAdmin")) {
								isTeamAssign.set(true);
								return;
							}
						});
					}
					
					if(isTeamAssign.get()) {
						// get Team members
						employeeDbResponse = employeePerformanceRepository.getAllTeamMembers(teamIds);
						employeeTeamDbResponse = employeePerformanceRepository.getAllEmployeeReportByEmpId(performanceDTO.getEmpId());
					}else {
						// get employee who are reporting to me
						employeeDbResponse = employeePerformanceRepository.getAllEmployeeReportByEmpId(performanceDTO.getEmpId());
					}
				}
			}
			
			if(!employeeDbResponse.isEmpty()) {
				employeeDbResponse.forEach((object) -> {
					EmployeeteamDto employee = new EmployeeteamDto();
					
					employee.setEmpId(object[0]!= null ? Long.parseLong(object[0].toString()) : null);
					employee.setName(object[1] != null ? object[1].toString(): null);
					employee.setEmployeementId(object[2]!= null ? Long.parseLong(object[2].toString()) : null);
					employee.setGoalsCompleted(null);
					employee.setTotalGoals(null);
				
					response.add(employee);
				});
				
				if(!employeeTeamDbResponse.isEmpty()) {
					employeeTeamDbResponse.forEach((object) -> {
						Long empIdFromDb = Long.parseLong(object[0].toString());

				        boolean exists = response.stream()
				                .anyMatch(dto -> empIdFromDb == dto.getEmpId());
				        
				        if (!exists) {
				            EmployeeteamDto dto = new EmployeeteamDto();

				            dto.setEmpId(empIdFromDb);
				            dto.setName(object[1] != null ? object[1].toString(): null);
				            dto.setEmployeementId(object[2]!= null ? Long.parseLong(object[2].toString()) : null);
				            dto.setGoalsCompleted(null);
				            dto.setTotalGoals(null);

				            response.add(dto);
				        }
					});
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(response);
	}

	public ServiceResponse currentStatusForPerformanceTableView() {
		
		ServiceResponse response = new ServiceResponse();
		try {
		List<Object[]> currentStatus = employeePerformanceRepository.currentStatusForPerformanceTableView();
		List<Object[]> finalRatingList = employeePerformanceRepository.findFinalRatingByEmpId();
		java.util.Map<Long, String> empIdToFinalRating = new java.util.HashMap<>();
		if (finalRatingList != null) {
			for (Object[] row : finalRatingList) {
				if (row[0] != null && row[1] != null) {
					Long empId = Long.parseLong(row[0].toString());
					empIdToFinalRating.put(empId, row[1].toString());
				}
			}
		}
		List<Object[]> approvalStatusList = employeePerformanceRepository.findApprovalStatusByEmpId();
		java.util.Map<Long, String> empIdToManagerStatus = new java.util.HashMap<>();
		java.util.Map<Long, String> empIdToHodStatus = new java.util.HashMap<>();
		java.util.Map<Long, String> empIdToHrStatus = new java.util.HashMap<>();
		if (approvalStatusList != null) {
			for (Object[] row : approvalStatusList) {
				if (row[0] != null) {
					Long empId = Long.parseLong(row[0].toString());
					if (row[1] != null) empIdToManagerStatus.put(empId, row[1].toString());
					if (row[2] != null) empIdToHodStatus.put(empId, row[2].toString());
					if (row[3] != null) empIdToHrStatus.put(empId, row[3].toString());
				}
			}
		}
		List<PerformanceDTO> dtoList = new ArrayList<PerformanceDTO>();
			if (currentStatus != null) {
				currentStatus.forEach((object) -> {
					PerformanceDTO performanceDTO = new PerformanceDTO();
					
					performanceDTO.setCompletionStatus(object[0] != null ? object[0].toString() : null);
					Long empId = object[1] != null ? Long.parseLong(object[1].toString()) : null;
					performanceDTO.setEmpId(empId);
					if (empId != null && empIdToFinalRating.containsKey(empId)) {
						performanceDTO.setFinalRating(empIdToFinalRating.get(empId));
					}
					if (empId != null) {
						performanceDTO.setManagerReviewStatus(empIdToManagerStatus.get(empId));
						performanceDTO.setHodReviewStatus(empIdToHodStatus.get(empId));
						performanceDTO.setHrReviewStatus(empIdToHrStatus.get(empId));
					}
					
					dtoList.add(performanceDTO);
					
					});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("currentStatus List is null.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
//	public ServiceResponse getAllEmployeePerformanceForQuarter(String financialYear)
//	{
//		ServiceResponse response = new ServiceResponse();
//		try {
//            
//            if (!isValidFYFormat(financialYear)) {
//                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                response.setServiceResponse("Invalid Financial Year format. Expected format: 2024-2025");
//                return response;
//            }
//            
//            Integer fyStartYear = extractYear(financialYear);
//            
//            List<EmployeeDTO> eligibleEmployeeList = 
//            employeeRepository.findEligibleEmployeesByFY(fyStartYear);
//            
//            if (eligibleEmployeeList != null && !eligibleEmployeeList.isEmpty()) {
//            	enrichEmployeeData(eligibleEmployeeList, financialYear);
//            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//                response.setServiceResponse(eligibleEmployeeList);
//            }
//            else {
//                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                response.setServiceResponse("No eligible employees found for increment in FY " + financialYear);
//            }
//		}
//            catch (Exception e) {
//                e.printStackTrace();
//                response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//                response.setServiceResponse("Error fetching eligible employees ");
//                response.setServiceError(e.getMessage());
//            }
//		return response;
//		
//		
//		
//	}
//	
//	 private void enrichEmployeeData(List<EmployeeDTO> employeeList, String financialYear) {
//	        employeeList.forEach(emp -> {
//	            // Generate Employment ID with prefix based on product type
//	            if (emp.getEmployeementId() != null) {
//	                String prefix = "true".equalsIgnoreCase(emp.getIsApmosysProduct()) ? "AP-" : "A-";
//	                emp.setEmploymentIdAcToET(prefix + emp.getEmployeementId());
//	            }
//	            
//	            // DateOfJoining is already a String from database - no formatting needed
//	            // It comes directly from the database in correct format via JPQL projection
//	            
//	            // Set Financial Year for reference
////	            emp.setFinancialYear(financialYear);
//	        });
//	    }
//	 private boolean isValidFYFormat(String financialYear) {
//	        if (financialYear == null || !financialYear.matches("\\d{4}-\\d{4}")) {
//	            return false;
//	        }
//	        
//	        String[] parts = financialYear.split("-");
//	        try {
//	            int startYear = Integer.parseInt(parts[0]);
//	            int endYear = Integer.parseInt(parts[1]);
//	            
//	            return endYear == startYear + 1;
//	        } catch (NumberFormatException e) {
//	            return false;
//	        }
//	    }
//	 private Integer extractYear(String financialYear) {
//	        String[] parts = financialYear.split("-");
//	        return Integer.parseInt(parts[0]);
//	    }
//	 
//	 private LocalDate calculateCutoffDate(String financialYear) {
//	        Integer fyStartYear = extractYear(financialYear);
//	        
//	        // Cutoff is always 31-Dec of FY start year
//	        return LocalDate.of(fyStartYear, 12, 31);
//	    }
//	 public LocalDate getCutoffDateForFY(String financialYear) {
//	        if (!isValidFYFormat(financialYear)) {
//	            throw new IllegalArgumentException(
//	                "Invalid Financial Year format. Expected format: YYYY-YYYY (e.g., 2024-2025)"
//	            );
//	        }
//	        return calculateCutoffDate(financialYear);
//	    }
//	 
	 



	public ServiceResponse exportExcelForHodAndManger(HrHodHrViewPerformance hrHodHrViewPerformance) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("exportExcelForHodAndManger");
		apiLogInfo.setApiUrl("/api/exportExcelForHodAndManger");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("exportExcelForHodAndManger : ");

		try {
			List<ExportExcelPerformance> performance = new ArrayList<>();
			List<Object[]> hrAndHODEmployeePerformanceViewDetails = null;
			if (hrHodHrViewPerformance.getHrvalidate()) {
				hrAndHODEmployeePerformanceViewDetails = employeePerformanceRepository
						.ExcelExportQueryForPerformnaceHr();
			} else {
				hrAndHODEmployeePerformanceViewDetails = employeePerformanceRepository
						.ExcelExportQueryForPerformnaceHODManager(hrHodHrViewPerformance.getEmpId());

			}

			if (hrAndHODEmployeePerformanceViewDetails != null && !hrAndHODEmployeePerformanceViewDetails.isEmpty()) {
				for (Object[] object : hrAndHODEmployeePerformanceViewDetails) {
					ExportExcelPerformance performanceDetails = new ExportExcelPerformance();
					performanceDetails
							.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					performanceDetails.setEmail(object[2] != null ? object[2].toString() : null);
					performanceDetails.setEmploymentstatus(object[3] != null ? object[3].toString() : null);
					performanceDetails.setName(object[4] != null ? object[4].toString() : null);
					performanceDetails.setDepartmentName(object[5] != null ? object[5].toString() : null);
					performanceDetails.setManagerName(object[6] != null ? object[6].toString() : null);
					performanceDetails.setBillable(object[7] != null ? (object[7].toString()) : null);
					performanceDetails
							.setTotalExperience(object[8] != null ? Float.parseFloat(object[8].toString()) : null);
					performanceDetails.setBillableType(object[9] != null ? object[9].toString() : null);
					performanceDetails.setReportingManagerName(object[10] != null ? object[10].toString() : null);
					performanceDetails.setHodName(object[11] != null ? object[11].toString() : null);
					performanceDetails.setCompletionStatus(object[12] != null ? object[12].toString()
							: "Review Not Given By Manager/ReportingManager");
					performanceDetails.setQuarterycle(object[13] != null ? object[13].toString() : null);
					performanceDetails.setFinancialYear(object[14] != null ? object[14].toString() : null);
					performanceDetails.setHodRemarks(object[15] != null ? object[15].toString() : null);
					performanceDetails.setFinalRating(object[16] != null ? object[16].toString() : null);
					performanceDetails.setHrRemarks(object[17] != null ? object[17].toString() : null);
					performanceDetails.setHrReviewStatus(object[18] != null ? object[18].toString() : null);

					performance.add(performanceDetails);

				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(performance);
				apiLogInfo.setApiResponse("Employee Performance fetched successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to fetch Employee Performance.");
				apiLogInfo.setApiResponse("Unable to fetch Employee Performance.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {

			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());

		}
		return response;
	}

	public ResponseEntity<ProjectInsightDTO> addRemarkAsPerQuestion(ProjectInsightDTO projectInsightDTO) {
		ProjectInsightDTO response = new ProjectInsightDTO();
//		try {
//			if(!projectInsightDTO.getEmpMarkList().isEmpty()) {
//				List<ProjectInsightResponse> addResponseList = new ArrayList<>();
//				projectInsightDTO.getEmpMarkList().forEach((object) -> {
//					ProjectInsightResponse projectResponse = projectInsightResponseRepository.
//							findByEmpIdAndQuestionMasterId(object.getEmpId(),object.getQuestionId());
//					
//					if(object.getMarkType().equals("reject")) {
//						projectResponse.setResponse(null);
//						projectResponse.setIsDraft("Y");
//						projectResponse.setMarks(null);
//						projectResponse.setProcessTo(null);
//					}else {
//						projectResponse.setMarks(object.getMarks());
//					}
//					
//					addResponseList.add(projectResponse);
//				});
//				
//				List<ProjectInsightResponse> dbResponse = projectInsightResponseRepository.saveAll(addResponseList);
//			}
//			
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
		return ResponseEntity.ok(response);
	}

	// mail to managers/reporting managers pending employees under them
	public void sendPerformanceReviewEmailsToManagerReportingManager() {

		List<Object[]> quarterCycles = employeePerformanceRepository.getAllActiveEnabledQuarterCycles();
		if (quarterCycles == null || quarterCycles.isEmpty()) {
			return;
		}

		List<Long> quarterIds = quarterCycles.stream().map(q -> ((Number) q[0]).longValue())
				.collect(Collectors.toList());
		String quarterCycleNames = quarterCycles.stream().map(q -> q[1] + " - " + q[2])
				.collect(Collectors.joining(", "));

		LocalDate lastDate = LocalDate.of(LocalDate.now().getYear() - 1, 12, 31);
		List<Object[]> reportees = employeePerformanceRepository.getEmployeesWithoutPerformance(lastDate, quarterIds);

		Map<Long, List<Object[]>> managerWiseReportees = new HashMap<>();
		for (Object[] row : reportees) {
			Long reviewerId = ((Number) row[5]).longValue(); // reviewer_id
			managerWiseReportees.computeIfAbsent(reviewerId, k -> new ArrayList<>()).add(row);
		}

		for (Map.Entry<Long, List<Object[]>> entry : managerWiseReportees.entrySet()) {
			Long reviewerId = entry.getKey();
			List<Object[]> employees = entry.getValue();

			Object[] reviewer = employeePerformanceRepository.findEmailAndNameByEmpId(reviewerId);
			if (reviewer == null)
				continue;

			String reviewerEmail = (String) reviewer[0];
			String reviewerName = (String) reviewer[1];

			String emailBody = generateEmailHtml(reviewerName, employees, quarterCycleNames);
			String subject = "Pending Performance Review - Action Required";

			try {
				mailService.sendMail(reviewerEmail, subject, emailBody);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	private String generateEmailHtml(String reviewerName, List<Object[]> employees, String quarterCycleNames) {
		StringBuilder html = new StringBuilder();

		html.append("<html><body>");
		html.append("<p>Dear ").append(reviewerName).append(",</p>");
		html.append("<p>The following employees under your supervision have not been reviewed for the quarter(s): <b>")
				.append(quarterCycleNames).append("</b></p>");

		html.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
		html.append("<tr><th>Employment ID</th><th>Name</th><th>Department</th></tr>");
		for (Object[] emp : employees) {
			html.append("<tr>");
			html.append("<td>").append("A-").append(emp[1]).append("</td>").append("<td>").append(emp[2])
					.append("</td>").append("<td>").append(emp[3]).append("</td>").append("</tr>");
		}
		html.append("</table>");
		html.append("<p>Please initiate the performance review as soon as possible.</p>");
		html.append("<p>Regards,<br/>HR Team</p>");
		html.append("</body></html>");

		return html.toString();
	}

	// rejected mails to manager/reporting manager

	public void rejectedReviewsToManagerOrReportingManager() {
		List<Object[]> rejectedData = employeePerformanceRepository.findAllRejectedReviewsAndTheirManagers();

		if (rejectedData.isEmpty())
			return;
		Map<Long, List<Object[]>> groupedByReviewer = new HashMap<>();

		for (Object[] row : rejectedData) {
			Long reviewerId = ((Number) row[9]).longValue();
			groupedByReviewer.computeIfAbsent(reviewerId, k -> new ArrayList<>()).add(row);
		}
		for (Map.Entry<Long, List<Object[]>> entry : groupedByReviewer.entrySet()) {
			List<Object[]> rows = entry.getValue();
			String reviewerEmail = (String) rows.get(0)[11];
			String reviewerName = (String) rows.get(0)[10];

			String html = buildRejectedReviewEmailHtml(reviewerName, rows);
			try {
				mailService.sendMail(reviewerEmail, "Performance Review Rejected - Re-Reviewing Required", html);
			} catch (Exception e) {
				System.err.println("Failed to send rejected mail to " + reviewerEmail);
				e.printStackTrace();
			}
		}
	}

	private String buildRejectedReviewEmailHtml(String reviewerName, List<Object[]> employees) {
		StringBuilder html = new StringBuilder();

		html.append("<html><body>");
		html.append("<p>Dear ").append(reviewerName).append(",</p>");
		html.append(
				"<p>The following performance reviews were <strong>rejected by the HOD</strong>. Please re-submit the reviews with necessary corrections:</p>");

		html.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
		html.append("<tr style='background-color: #f2f2f2;'>").append("<th style='padding: 8px;'>Employment ID</th>")
				.append("<th style='padding: 8px;'>Name</th>").append("<th style='padding: 8px;'>Department</th>")
				.append("<th style='padding: 8px;'>Financial Year</th>")
				.append("<th style='padding: 8px;'>Quarter</th>")
				.append("<th style='padding: 8px;'>Rejection Reason</th>").append("</tr>");

		for (Object[] emp : employees) {
			html.append("<tr>").append("<td style='padding: 8px;'>").append("A-").append(emp[1]).append("</td>")
					.append("<td style='padding: 8px;'>").append(emp[2]).append("</td>")
					.append("<td style='padding: 8px;'>").append(emp[3]).append("</td>")
					.append("<td style='padding: 8px;'>").append(emp[6]).append("</td>")
					.append("<td style='padding: 8px;'>").append(emp[7]).append("</td>")
					.append("<td style='padding: 8px;'>").append(emp[4]).append("</td>").append("</tr>");
		}

		html.append("</table>");
		html.append("<p>Kindly take action as soon as possible.</p>");
		html.append("<p>Regards,<br>HR Team</p>");
		html.append("</body></html>");

		return html.toString();
	}	
	public ServiceResponse getAllEmployeePerformanceForQuarter(String financialYear,Long quarterId ,Long empId , int page , int size) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        String[] fromToYear = financialYear.split("-");
	        Integer fyStartYear = Integer.parseInt(fromToYear[0]);
	        
	        Pageable pageable = PageRequest.of(page, size);
	        Long empid = empId;
	        Long quartId = quarterId;
	        Page<Object[]> pageResult =
                    employeeRepository.findEligibleEmployeesByFY(
                            fyStartYear,quartId,empid, pageable
                    );

	        
	        List<ExportExcelPerformance> emplist = new ArrayList<ExportExcelPerformance>();
	        
	        for (Object[] row : pageResult.getContent()) {
	        	ExportExcelPerformance dto = new ExportExcelPerformance();

	            dto.setEmployeementId(row[0] != null ? Long.valueOf(row[0].toString()) : null);
	            dto.setDateOfJoining(row[1]!=null ? row[1].toString():null);
	            dto.setEmail(row[2] != null ? row[2].toString() : null);
	            dto.setEmploymentstatus(row[3] != null ? row[3].toString() : null);
	            dto.setName(row[4] != null ? row[4].toString() : null);
	            dto.setDepartmentName(row[5] != null ? row[5].toString() : null);
	            dto.setManagerName(row[6] != null ? row[6].toString() : null);
	            dto.setReportingManagerName(row[7] != null ? row[7].toString() : null);
	            dto.setHodName(row[8] != null ? row[8].toString() : null);
	            dto.setFinancialYear(row[9]!=null ? row[9].toString() : null);
	            dto.setFinalRating(row[10]!=null ? row[10].toString() : null);
	            
	            emplist.add(dto);
	            
	        }
	        
	            
	            Map<String, Object> result = new HashMap<>();
	            result.put("data", emplist);
	            result.put("totalCount", pageResult.getTotalElements());
	            result.put("page", page);
	            result.put("size", size);

	            
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(result);
	        

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong");
	        response.setServiceError(e.getMessage());
	        return response;
	    }

	    return response;
	}
	
	
	public ServiceResponse exportExcelForEligiblePreview(String financialYear , Long empId)
	{
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setSubFeatureName("exportExcelForEligiblePreview");
		apiLogInfo.setApiUrl("/api/exportExcelForEligiblePreview");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("exportExcelForEligiblePreview : ");
		try {
	        String[] fromToYear = financialYear.split("-");
	        Integer fyStartYear = Integer.parseInt(fromToYear[0]);
	        
	        Long empid = empId;
	        List<Object[]> exportList = employeeRepository.exportEligibleEmployeesByFY(fyStartYear, empid);
	    

	        
	        List<ExportExcelPerformance> emplist = new ArrayList<ExportExcelPerformance>();
	        if(exportList!=null) {
	        exportList.forEach((object)->{
	        	ExportExcelPerformance dto = new ExportExcelPerformance();

	        	dto.setEmployeementId(object[0] != null ? Long.valueOf(object[0].toString()) : null);
	            dto.setDateOfJoining(object[1]!=null ? object[1].toString():null);
	            dto.setEmail(object[2] != null ? object[2].toString() : null);
	            dto.setEmploymentstatus(object[3] != null ? object[3].toString() : null);
	            dto.setName(object[4] != null ? object[4].toString() : null);
	            dto.setDepartmentName(object[5] != null ? object[5].toString() : null);
	            dto.setManagerName(object[6] != null ? object[6].toString() : null);
	            dto.setReportingManagerName(object[7] != null ? object[7].toString() : null);
	            dto.setHodName(object[8] != null ? object[8].toString() : null);
	            dto.setFinancialYear(object[9]!=null ? object[9].toString() : null);
	            dto.setFinalRating(object[10]!=null ? object[10].toString() : null);
	            
	            emplist.add(dto);
	            
	        });
	        
	            
	          
	            
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(emplist);
	        }
	        else 
	        {
	        	
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("currentStatus List is null.");
				
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong");
	        response.setServiceError(e.getMessage());
	        return response;
	    }

	    return response;

	}
	
	public ServiceResponse getCurrentUserDepartment(Long empId)
	{
		ServiceResponse response = new ServiceResponse();
		try {
		String currentUserDepartment = employeePerformanceRepository.getCurrentUserDepartment(empId);
		if(currentUserDepartment !=null && !currentUserDepartment.trim().isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(currentUserDepartment);
			System.out.println("currentUserDepartment" + currentUserDepartment);
			
		} else 
        {
        	
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("current employee Department not found.");
			
        }
		
		}
		catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse updateEmployeePerformanceHr(PerformanceDTO employeePerformanceDTO) {

		ServiceResponse response = new ServiceResponse();
		try {
			EmployeePerformance savedEmployeePerformance = null;
			EmployeePerformance employeePerformanceDetails = employeePerformanceRepository
					.findByPerformanceId(employeePerformanceDTO.getEmployeePerformanceId());
			if (employeePerformanceDetails != null) {

				employeePerformanceDetails.setFinal_rating(employeePerformanceDTO.getFinalRating());
				employeePerformanceDetails.setHr_id(employeePerformanceDTO.getHrId());
				employeePerformanceDetails.setHr_remarks(employeePerformanceDTO.getHrRemark());
				employeePerformanceDetails.setHr_review_date(LocalDateTime.now());
				employeePerformanceDetails.setHod_remarks(employeePerformanceDTO.getHodRemarks());
				// Draft HR update: do not set Completed — only submitEmployeePerformanceHR / bulk HR accept does.
				String hrSt = employeePerformanceDTO.getHrReviewStatus();
				if (hrSt != null && "Accepted".equalsIgnoreCase(hrSt.trim())) {
					employeePerformanceDetails.setCompletion_status("Completed");
				} else if (hrSt != null && "Rejected".equalsIgnoreCase(hrSt.trim())) {
					employeePerformanceDetails.setCompletion_status("Rejected");
				}
				savedEmployeePerformance = employeePerformanceRepository.save(employeePerformanceDetails);
			}

			if (savedEmployeePerformance != null) {

				for (PerformanceRatingDTO ratingDTO : employeePerformanceDTO.getPerformanceRatings()) {
					EmployeeRatingPerformance ratingPerformance = employeeRatingPerformanceRepository
							.findById(ratingDTO.getPerformanceRatingId()).orElse(null);
					if (ratingPerformance != null) {
						ratingPerformance.setQuarterId(employeePerformanceDTO.getQuarterId());
						ratingPerformance.setReviewTypeId(ratingDTO.getReviewTypeId());
						ratingPerformance.setRatingValue(ratingDTO.getRating());
						ratingPerformance.setEmpId(employeePerformanceDTO.getEmpId());
						applyCriteriaRemarkUpdate(ratingDTO, ratingPerformance);
						employeeRatingPerformanceRepository.save(ratingPerformance);
					}

				}

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Employee Performance updated Successfully.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Performance updatation Failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());

		}
		return response;
	}

	public ServiceResponse bulkSubmitEmployeePerformanceHR(PerformanceDTO employeePerformanceDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (employeePerformanceDTO.getEmpIds() == null || employeePerformanceDTO.getEmpIds().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No employees selected for bulk HR action.");
				return response;
			}
			if (employeePerformanceDTO.getQuarterId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Quarter is required for bulk HR action.");
				return response;
			}
			String status = employeePerformanceDTO.getHrReviewStatus() != null
					? employeePerformanceDTO.getHrReviewStatus().trim()
					: "";
			if (!"Accepted".equalsIgnoreCase(status) && !"Rejected".equalsIgnoreCase(status)) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid bulk HR action. Use Accepted or Rejected.");
				return response;
			}

			int updated = 0;
			int skipped = 0;
			List<Long> uniqueEmpIds = employeePerformanceDTO.getEmpIds().stream().filter(id -> id != null).distinct()
					.collect(Collectors.toList());
			for (Long empId : uniqueEmpIds) {
				EmployeePerformance ep = employeePerformanceRepository
						.findLatestByEmpIdAndQuarterId(empId, employeePerformanceDTO.getQuarterId()).stream().findFirst()
						.orElse(null);
				if (ep == null) {
					skipped++;
					continue;
				}
				ep.setHr_id(employeePerformanceDTO.getHrId());
				ep.setHr_remarks(employeePerformanceDTO.getHrRemark());
				ep.setHr_review_status(status);
				ep.setHr_review_date(LocalDateTime.now());
				if ("Accepted".equalsIgnoreCase(status)) {
					ep.setCompletion_status("Completed");
					ep.setRejectStatus(false);
				} else {
					ep.setCompletion_status("Rejected");
					ep.setRejectStatus(true);
				}
				employeePerformanceRepository.save(ep);
				updated++;
			}

			Map<String, Object> result = new HashMap<>();
			result.put("updatedCount", updated);
			result.put("skippedCount", skipped);
			result.put("status", status);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(result);
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	/** Trim per-criterion comment; null/blank stored as null. */
	private static String normalizeCriteriaRemark(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	/** On update: change stored comment only when the client sends criteriaRemark (non-null JSON field). */
	private static void applyCriteriaRemarkUpdate(PerformanceRatingDTO dto, EmployeeRatingPerformance entity) {
		if (dto == null || entity == null) {
			return;
		}
		if (dto.getCriteriaRemark() != null) {
			entity.setCriteriaRemark(normalizeCriteriaRemark(dto.getCriteriaRemark()));
		}
	}

}

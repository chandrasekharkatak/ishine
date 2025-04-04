package com.apmosys.employeeportal.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.apmosys.employeeportal.dto.EmployeeteamDto;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PerformanceDTO;
import com.apmosys.employeeportal.dto.PerformanceRatingDTO;
import com.apmosys.employeeportal.dto.QuarterCycleDTO;
import com.apmosys.employeeportal.dto.ReviewTypeDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeePerformance;
import com.apmosys.employeeportal.model.EmployeeRatingPerformance;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.QuaterCycle;
import com.apmosys.employeeportal.model.ReviewType;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeePerformanceRepository;
import com.apmosys.employeeportal.repository.EmployeeRatingPerformanceRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
import com.apmosys.employeeportal.repository.ReviewTypeRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class PerformanceService {

	@Autowired
	HttpServletRequest httpRequest;
	
	@Autowired
	private EmployeePerformanceRepository employeePerformanceRepository;
	
	@Autowired
	private EmployeeRatingPerformanceRepository employeeRatingPerformanceRepository;
	
	@Autowired
	private QuarterCycleRepository quarterCycleRepository;
	
	@Autowired
	private DepartmentRepository departmentRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	LogService logService;
	
	@Autowired
	private ReviewTypeRepository reviewTypeRepository;
	
	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Autowired
	TeamRepository teamRepository;
	
	public ServiceResponse addReviewType(ReviewTypeDTO reviewTypeDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setSubFeatureName("create_review");
		apiLogInfo.setApiUrl("/api/addReviewType");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("reviewTypeId : "+reviewTypeDTO.getReviewTypeId()+", reviewLabel : "+reviewTypeDTO.getReviewLabel());
		try {   
             ReviewType dbResponse = null;  
             for (Long deptId :  reviewTypeDTO.getDeptId()) {
            	 for (ReviewType reviewType : reviewTypeDTO.getAllSpecializationList()) {
                     ReviewType reviewObj = new ReviewType();
                     reviewObj.setReviewLabel(reviewType.getReviewLabel());
                     reviewObj.setReviewFieldType(reviewType.getReviewFieldType());
                     reviewObj.setQuarterId(reviewTypeDTO.getQuarterId());
                     reviewObj.setDeptId(deptId); 
                     reviewObj.setFlag(true);
                     reviewObj.setCreatedBy(reviewTypeDTO.getCreatedBy());
                     reviewObj.setCondition(reviewType.getCondition()); 
                   
                     dbResponse= reviewTypeRepository.save(reviewObj);
                 }         	
            }
             			
				if(dbResponse !=null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Review Type  Added successfully.");
					apiLogInfo.setApiResponse("Review Type  Added successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to add Review Type.");
					apiLogInfo.setApiResponse("Unable to add Review Type.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
					
		}
		catch(Exception e){
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
	
	
	
	
	public ServiceResponse createQuarterCycle(QuarterCycleDTO quarterCycleDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
		QuaterCycle quar = new QuaterCycle();
		
		quar.setFinancialYear(quarterCycleDTO.getFinancialYear());
		quar.setQuarterCycle(quarterCycleDTO.getQuarterCycle());
		quar.setCreatedBy(quarterCycleDTO.getCreatedBy());
		quar.setIsActive(quarterCycleDTO.getIsActive());
		quar.setIsEnable(quarterCycleDTO.getIsEnable());
		
		QuaterCycle quarterCycle = quarterCycleRepository.save(quar);
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
            Optional<QuaterCycle> optionalQuarterCyles = quarterCycleRepository.findById(quarterCycleDTO.getQuarterId());

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
            Optional<QuaterCycle> optionalQuarterCyles = quarterCycleRepository.findById(quarterCycleDTO.getQuarterId());

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
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setSubFeatureName("get_ReviewType");
		apiLogInfo.setApiUrl("/api/getAllReview");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getAllReview size : "+reviewTypeRepository.findAll().size());
		try {
			List<ReviewType> validReviewDetails = new ArrayList<>();
			List<ReviewType> reviewDetails=reviewTypeRepository.findAll();
			if(reviewDetails !=null) {
				reviewDetails.forEach((reviewDetail)->{
					
					if (reviewDetail.getFlag()) {

					    Optional.ofNullable(reviewDetail.getDeptId())
					            .ifPresent(deptId -> {
					                Optional<Department> department = Optional.ofNullable(departmentRepository.findByDeptId(deptId));
					                department.ifPresent(dept -> reviewDetail.setDepartmentName(dept.getName()));
					            });

					   
					    Optional.ofNullable(reviewDetail.getCreatedBy())
					            .ifPresent(empId -> {
					                Optional<Employee> employee = Optional.ofNullable(employeeRepository.findByEmpId(empId));
					                employee.ifPresent(emp -> reviewDetail.setEmployeeName(emp.getName()));
					            });

					   
					    Optional.ofNullable(reviewDetail.getUpdatedBy())
					            .ifPresent(empId -> {
					                Optional<Employee> employee = Optional.ofNullable(employeeRepository.findByEmpId(empId));
					                employee.ifPresent(emp -> reviewDetail.setUpdatedByName(emp.getName()));
					            });

					  
					    Optional.ofNullable(reviewDetail.getQuarterId())
					            .ifPresent(quarterId -> {
					                Optional<QuaterCycle> optionalQuarterCycle = quarterCycleRepository.findById(quarterId);
					                optionalQuarterCycle.ifPresent(quaterCycle -> {
					                    reviewDetail.setActive(quaterCycle.getIsActive());
					                    reviewDetail.setQuarterCycle(quaterCycle.getQuarterCycle());
					                });
					            });

					   
					    validReviewDetails.add(reviewDetail);
					}

				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(validReviewDetails);
				apiLogInfo.setApiResponse("ReviewType  Fetched successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to Fetche ReviewType.");
				apiLogInfo.setApiResponse("Unable to Fetche ReviewType.");
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
		return response;
	}


	public ServiceResponse deleteReviewType(Long reviewTypeId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setSubFeatureName("Delete_ReviewType");
		apiLogInfo.setApiUrl("/api/deleteReviewType");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getAllReview size : "+reviewTypeRepository.findAll().size());
		try {

			ReviewType reviewDetails=reviewTypeRepository.findByReviewTypeId(reviewTypeId);
			 if(reviewDetails!=null) {
				 reviewDetails.setFlag(false);
				  reviewTypeRepository.save(reviewDetails);
				    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Review Type Deleted successfully.");
					apiLogInfo.setApiResponse("Review Type Deleted successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				 
			 }else {
				    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to Delete Review Type.");
					apiLogInfo.setApiResponse("Unable to Delete Review Type.");
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
		
		return response;
	}
	
	public ServiceResponse updateQuarterCycle(QuarterCycleDTO quarterCycleDTO)
	{
		ServiceResponse response = new ServiceResponse();
		try {
			QuaterCycle quar = quarterCycleRepository.getById(quarterCycleDTO.getQuarterId());
			quar.setFinancialYear(quarterCycleDTO.getFinancialYear());
			quar.setQuarterCycle(quarterCycleDTO.getQuarterCycle());
			quar.setUpdatedBy(quarterCycleDTO.getUpdatedBy());	
			quar.setUpdatedOn(LocalDateTime.now());
			
			QuaterCycle quarterCycle = quarterCycleRepository.save(quar);
			if (quarterCycle != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(" Quarter Cycle Updated.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(" Quarter Cycle updation Failed.");			
			}
		
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
		}
		return response;
	}




	public ServiceResponse updateReviewType(ReviewTypeDTO reviewTypeDTO) {
		
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setSubFeatureName("update_review");
		apiLogInfo.setApiUrl("/api/updateReviewType");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("reviewTypeId : "+reviewTypeDTO.getReviewTypeId()+", reviewLabel : "+reviewTypeDTO.getReviewLabel());
		try {
			ReviewType dbResponse = null; 
			ReviewType reviewDetails=reviewTypeRepository.findByReviewTypeId(reviewTypeDTO.getReviewTypeId());
			
			if(reviewDetails !=null) {
				 for (Long deptId :  reviewTypeDTO.getDeptId()) {
	            	 for (ReviewType reviewType : reviewTypeDTO.getAllSpecializationList()) {
	                     reviewDetails.setReviewLabel(reviewType.getReviewLabel());
	                     reviewDetails.setReviewFieldType(reviewType.getReviewFieldType());
	                     reviewDetails.setDeptId(deptId); 
	                     reviewDetails.setFlag(true);
	                     reviewDetails.setQuarterId(reviewTypeDTO.getQuarterId());
	                     reviewDetails.setUpdatedBy(reviewTypeDTO.getUpdatedBy());
	                     reviewDetails.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
	                     reviewDetails.setCondition(reviewType.getCondition()); 
	                   
	                     dbResponse= reviewTypeRepository.save(reviewDetails);
	                 }         	
	            }
				 if(dbResponse !=null) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Review Type  updated successfully.");
						apiLogInfo.setApiResponse("Review Type  updated successfully.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Unable to update Review Type.");
						apiLogInfo.setApiResponse("Unable to update Review Type.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(" Review Type Type Does Not Exit");
				apiLogInfo.setApiResponse(" Review Type Type Does Not Exit");
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
		
		return response;
	}




	public ServiceResponse getReviewTypeById(ReviewTypeDTO reviewTypeDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setSubFeatureName("getReviewTypeById");
		apiLogInfo.setApiUrl("/api/getReviewTypeById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getReviewTypeId : "+reviewTypeDTO.getReviewTypeId());
		try {

			ReviewType reviewDetails=reviewTypeRepository.findByReviewTypeId(reviewTypeDTO.getReviewTypeId());
			System.out.println("kjnbsjvh"+reviewDetails);
			 if(reviewDetails != null) {
				 
				    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(reviewDetails);
					apiLogInfo.setApiResponse("ReviewTypeById Fetched successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				 
			 }else {
				    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to Fetched ReviewTypeById.");
					apiLogInfo.setApiResponse("Unable to Fetched ReviewTypeById.");
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
		
		return response;
	}
	
	
	public ServiceResponse submitEmployeePerformanceHOD(PerformanceDTO employeePerformanceDTO) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	    	 EmployeePerformance employeePerformance = new EmployeePerformance();
	    	 
	    	 employeePerformance.setEmp_id(employeePerformanceDTO.getEmpId());
	         employeePerformance.setFinal_rating(employeePerformanceDTO.getFinalRating());         
	         employeePerformance.setHod_id(employeePerformanceDTO.getHodId());
	         
	         employeePerformance.setHod_remarks(employeePerformanceDTO.getHodRemarks());
	         employeePerformance.setQuarterId(employeePerformanceDTO.getQuarterId());
	         employeePerformance.setHod_approval_date(LocalDateTime.now());	  
	         employeePerformance.setCompletion_status("Ongoing");	         
	         EmployeePerformance savedEmployeePerformance = employeePerformanceRepository.save(employeePerformance);
	         if (savedEmployeePerformance != null) {
	             
	             for (PerformanceRatingDTO ratingDTO : employeePerformanceDTO.getPerformanceRatings()) {
	                 EmployeeRatingPerformance ratingPerformance = new EmployeeRatingPerformance();
	                 ratingPerformance.setQuarterId(employeePerformanceDTO.getQuarterId());
	                 ratingPerformance.setReviewTypeId(ratingDTO.getReviewTypeId());
	                 ratingPerformance.setRatingValue(ratingDTO.getRating());
	                 ratingPerformance.setEmpId(employeePerformanceDTO.getEmpId());
	                 
	                 
	                 employeeRatingPerformanceRepository.save(ratingPerformance);
	             }
	         
	             response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	             response.setServiceResponse("Employee Performance Submitted Successfully.");
	         } else {
	             response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	             response.setServiceResponse("Employee Performance Submission Failed.");
	         }
	    
	    }catch(Exception e) {
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
	        Optional<EmployeePerformance> optionalEmployeePerformance =  employeePerformanceRepository.findById(employeePerformanceDTO.getEmployeePerformanceId());

	        if (optionalEmployeePerformance.isPresent()) { 
	            EmployeePerformance employeePerformance = optionalEmployeePerformance.get();
	            
	            employeePerformance.setHr_id(employeePerformanceDTO.getHrId());
	            employeePerformance.setHr_remarks(employeePerformanceDTO.getHrRemark());
	            employeePerformance.setHr_review_status(employeePerformanceDTO.getHrReviewStatus());
	            employeePerformance.setCompletion_status(
	                employeePerformanceDTO.getHrReviewStatus().equals("Accepted") ? "Completed" : "Rejected"
	            );

	            EmployeePerformance savedEmployeePerformanceHR = 
	                    employeePerformanceRepository.save(employeePerformance);

	            if (savedEmployeePerformanceHR != null) {
	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse("Reviewed HOD Remarks Successfully");
	            } else {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Couldn't submit Remarks");
	            }
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee Performance record not found.");
	        }

	    } catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
		}
	    return response;
	} 


	public ServiceResponse getReviewDataForQuarter() {
		
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setSubFeatureName("getReviewDataForQuarter");
		apiLogInfo.setApiUrl("/api/getReviewDataForQuarter");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getReviewDataForQuarter");
		try {

            List<ReviewTypeDTO> reviewDataforQuater= new ArrayList<>();
			List<Object[]> getReviewDataForQuarter = reviewTypeRepository.getReviewDataForQuarter();
			 if(getReviewDataForQuarter != null) {
				 for(Object[] object:getReviewDataForQuarter) {
					 ReviewTypeDTO reviewobj = new ReviewTypeDTO();
					 reviewobj.setReviewLabel(object[0] != null ? object[0].toString() : null);
					 reviewDataforQuater.add(reviewobj);
				 }
				 
				    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(reviewDataforQuater);
					apiLogInfo.setApiResponse("ReviewTypeData Fetched successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				 
			 }else {
				    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to Fetched ReviewTypeData.");
					apiLogInfo.setApiResponse("Unable to Fetched ReviewTypeData.");
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
		
		return response;
	}
	
	public ServiceResponse getEmployeePerformanceHOD() {
		
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
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
			            performanceDTO.setEmployeePerformanceId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
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
			        performanceRatingDTO.setReviewTypeId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
			        performanceRatingDTO.setQuarterId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
			        // Now find the quarter group or create it
			        Optional<PerformanceRatingDTO> existingRating = performanceDTO.getPerformanceRatings().stream()
			            .filter(rating -> rating.getReviewTypeId().equals(performanceRatingDTO.getReviewTypeId()) && 
			                    rating.getReviewLabel().equals(performanceRatingDTO.getReviewLabel()))
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
				 
			 }else {
				    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to fetch Employee Performance.");
					apiLogInfo.setApiResponse("Unable to fetch Employee Performance.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			 }
	    }catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
		}
	    return response;
	}




	public ServiceResponse hrAndHODEmployeePerformanceView(PerformanceDTO employeePerformanceDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setSubFeatureName("hrAndHODEmployeePerformanceView");
		apiLogInfo.setApiUrl("/api/hrAndHODEmployeePerformanceView");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("hrAndHODEmployeePerformanceView : ");
		
		try {
			List<PerformanceDTO> performance = new ArrayList<>();
			List<Object[]> hrAndHODEmployeePerformanceViewDetails = employeePerformanceRepository.hrAndHODEmployeePerformanceView(employeePerformanceDTO.getEmpId(),employeePerformanceDTO.getQuarterId());
			
			if(hrAndHODEmployeePerformanceViewDetails != null && !hrAndHODEmployeePerformanceViewDetails.isEmpty()) {
				for(Object[] object:hrAndHODEmployeePerformanceViewDetails) {
					PerformanceDTO performanceDetails= new PerformanceDTO();
					performanceDetails.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					performanceDetails.setCompletionStatus(object[1] != null ? object[1].toString() : null);
					performanceDetails.setEmployeePerformanceId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
					performanceDetails.setQuarterId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
					performanceDetails.setReviewLabel(object[4] != null ? object[4].toString() : null);
					performanceDetails.setReviewFieldType(object[5] != null ? object[5].toString() : null);
					performanceDetails.setCondition(object[6] != null ? object[6].toString() : null);
					performanceDetails.setRatingValue(object[7] != null ? new BigDecimal(object[7].toString()) : null);
					performanceDetails.setReviewTypeId(object[8] != null ? Long.parseLong(object[8].toString()) : null);
					performanceDetails.setHodRemarks(object[9] != null ? object[9].toString() : null);
					performanceDetails.setFinalRating(object[10] != null ? object[10].toString() : null);
					performanceDetails.setDeptId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
					performanceDetails.setPerformanceRatingId(object[12] != null ? Long.parseLong(object[12].toString()) : null);
									
					performance.add(performanceDetails);
			 
			    }
				    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(performance);
					apiLogInfo.setApiResponse("Employee Performance fetched successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			}else {
			    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to fetch Employee Performance.");
				apiLogInfo.setApiResponse("Unable to fetch Employee Performance.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		 }
		}catch(Exception e) {
			
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
		}		
		return response;
	}
	
	
	public ServiceResponse	DepartmentbyEmployeecont () {
		
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setSubFeatureName("getAllDepartmentbyEmployeecont");
		apiLogInfo.setApiUrl("/api/getAllDepartmentbyEmployeecont");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getAllDepartmentbyEmployeecont : ");
		
		
		try {
		
		List<HashMap<String, Object>> department = new  ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = new HashMap< String, Object> ();
		
		List<Object[]> employeeDepertmemtCount = employeePerformanceRepository.DepartmentbyEmployeecontquery();
		
		if(employeeDepertmemtCount != null && !employeeDepertmemtCount.isEmpty()) {
			for(Object[] object:employeeDepertmemtCount) {
				
				map = new HashMap< String, Object> ();
				
				map.put( "department",object[0] != null ? object[0].toString() : null);
				map.put("TotalNumberofemp" ,object[1] != null ? Long.parseLong(object[1].toString()) : null);
				map.put("ratinggivenbymanager",object[2] != null ? Long.parseLong(object[2].toString()) : null);
				map.put("pendingratinggivenbymanager",object[3] != null ? Long.parseLong(object[3].toString()) : null);
				map.put("managerName",object[4] != null ? object[4].toString() : null);		
				
				
				department.add(map);
		 
		    }
			    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(department);
				apiLogInfo.setApiResponse("Department  fetched successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		}else {
		    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Unable to fetch Department.");
			apiLogInfo.setApiResponse("Unable to fetch Department.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	 }
		
		
	}catch(Exception e) {
		
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
		    	EmployeePerformance savedEmployeePerformance=null;
		    	EmployeePerformance employeePerformanceDetails = employeePerformanceRepository.findByPerformanceId(employeePerformanceDTO.getEmployeePerformanceId());
		    	 if(employeePerformanceDetails != null) {
		    		 
		    		 employeePerformanceDetails.setFinal_rating(employeePerformanceDTO.getFinalRating());         
		    		 employeePerformanceDetails.setHod_remarks(employeePerformanceDTO.getHodRemarks());
		    		 employeePerformanceDetails.setHod_approval_date(LocalDateTime.now());	  
			         employeePerformanceDetails.setCompletion_status("Ongoing");	         
			         savedEmployeePerformance = employeePerformanceRepository.save(employeePerformanceDetails);
		    	 }
		    	 
		         if (savedEmployeePerformance != null) {
		                    
		             for (PerformanceRatingDTO ratingDTO : employeePerformanceDTO.getPerformanceRatings()) {
		            	 EmployeeRatingPerformance ratingPerformance = employeeRatingPerformanceRepository.getById(ratingDTO.getPerformanceRatingId());
		            	 if(ratingPerformance != null) {
		            		 ratingPerformance.setQuarterId(employeePerformanceDTO.getQuarterId());
			                 ratingPerformance.setReviewTypeId(ratingDTO.getReviewTypeId());
			                 ratingPerformance.setRatingValue(ratingDTO.getRating());
			                 ratingPerformance.setEmpId(employeePerformanceDTO.getEmpId());
			                 employeeRatingPerformanceRepository.save(ratingPerformance);
		            	 }
		                 
		                 
		                 
		                 
		             }
		         
		             response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		             response.setServiceResponse("Employee Performance updated Successfully.");
		         } else {
		             response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		             response.setServiceResponse("Employee Performance updatation Failed.");
		         }
		    
		    }catch(Exception e) {
				e.printStackTrace();
				response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Something Went Wrong.");
				response.setServiceError(e.getMessage());
				
			}
		    return response;
	}




	public ServiceResponse getReviewLabelForEveryDepartment() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo=new LogDTO();
		apiLogInfo.setSubFeatureName("getReviewLabelForEveryDepartment");
		apiLogInfo.setApiUrl("/api/getReviewLabelForEveryDepartment");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getReviewLabelForEveryDepartment  : ");
		
		try {
			List<ReviewTypeDTO> reviewLabelList = new ArrayList<>();
			List<Object[]> reviewLabelDetails= reviewTypeRepository.getReviewLabelForEveryDepartment();
			
			if(reviewLabelDetails !=null && !reviewLabelDetails.isEmpty() ) {
				for(Object[] object:reviewLabelDetails) {
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
				
			}
			else {
			    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to fetch getReviewLabelForEveryDepartment.");
				apiLogInfo.setApiResponse("Unable to fetch getReviewLabelForEveryDepartment.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		 }
		}catch(Exception e){
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		
		
		return response;
	}

	public ResponseEntity<PerformanceDTO> checkUserHaveTeam(PerformanceDTO performanceDTO) {
		PerformanceDTO responseObj = new PerformanceDTO();
		try {
			List<EmployeeTeamMap> emplTeamList = employeeTeamMapRepository.findByEmpIdAndActive(performanceDTO.getEmpId(), 1l);
			
			if(!emplTeamList.isEmpty()) {
				emplTeamList.forEach((object) -> {
					if(object.getEmployeeRole().contains("TeamLead") ||
							object.getEmployeeRole().contains("HOD") || 
							object.getEmployeeRole().contains("Manager") || 
							object.getEmployeeRole().contains("HR") || 
							object.getEmployeeRole().contains("RMG") || object.getEmployeeRole().contains("SuperAdmin")) {
						responseObj.setIsUserHaveTeam("true");
						return;
					}
				});
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(responseObj);
	}




	public ResponseEntity<List<EmployeeteamDto>> getTeamEmployeeListInTeamDashboard(PerformanceDTO performanceDTO) {
		List<EmployeeteamDto> response = new ArrayList<EmployeeteamDto>();
		try {
			List<Object[]> employeeDbResponse = null;
			List<Object[]> employeeTeamDbResponse = null;
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
			
			if(!employeeDbResponse.isEmpty()) {
				employeeDbResponse.forEach((object) -> {
					
					if(performanceDTO.getTabType().equals("reviewTeam")) {
						
					}
					
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


}



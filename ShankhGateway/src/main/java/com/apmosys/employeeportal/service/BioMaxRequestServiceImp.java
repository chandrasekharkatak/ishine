package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.BioMaxRequestDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.BioMaxRequestIssue;
import com.apmosys.employeeportal.model.BiomaxDefaulter;
import com.apmosys.employeeportal.model.BiomaxRequest;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.repository.BioMaxRequestIssueRepository;
import com.apmosys.employeeportal.repository.BiomaxDefaulterRepository;
import com.apmosys.employeeportal.repository.BiomaxRequestRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.serviceInterface.BioMaxRequestService;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class BioMaxRequestServiceImp implements BioMaxRequestService {

	@Autowired
	private BiomaxRequestRepository bioMaxRequestRepository;
	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private LeaveBalanceLogRepository leaveBalanceLogRepository;

	@Autowired
	private BioMaxRequestIssueRepository bioMaxRequestIssueRepository;
	@Autowired
	private CronJobService cronJobService;

	@Autowired
	private EmployeeLeavesMapRepository employeeLeavesMapRepository;

	@Autowired
	private BiomaxDefaulterRepository biomaxDefaulterRepository;
	
	@Value("${leavetypeId}")
	private int leaveTypeId;

	@Override
	public ServiceResponse createBioMaxRequest(BioMaxRequestDTO biomaxRequest) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		Optional<BioMaxRequestIssue> requestType = bioMaxRequestIssueRepository
				.findById(biomaxRequest.getBiomaxTitle());
		apiLogInfo.setSubFeatureName("Add createBioMaxRequest");
		apiLogInfo.setApiUrl("/api/createBioMaxRequest");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(
				"BioMax Request Type :" + requestType + " ,is created and pending to be Approved By :" + biomaxRequest.getStatusBy());
		try {
			BiomaxRequest biomax = new BiomaxRequest();
			biomax.setBiomaxrequestDate(biomaxRequest.getBiomaxrequestDate());
			biomax.setEmpId(biomaxRequest.getEmpId());
			biomax.setBiomaxStatus("Pending");
			biomax.setCreatedOn(LocalDateTime.now());
			biomax.setTobiomaxrequestDate(biomaxRequest.getTobiomaxrequestDate());
			biomax.setBiomaxIssueId(biomaxRequest.getBiomaxTitle());
			biomax.setRequestRemark(biomaxRequest.getRequestRemark());
			biomax.setReportingManagerId(biomaxRequest.getReportingManagerId());
			bioMaxRequestRepository.save(biomax);
			response.setServiceStatus("success");
			response.setServiceMessage("Biomax Request Has been Raised assign to your Reporting Manager");
			response.setServiceResponse(biomax);

		} catch (Exception e) {
			logBuilder.append("Error" + e.getMessage());
			response.setServiceStatus("fails");
			response.setServiceMessage("Some Thing Wrong" + e.getMessage());

		}
		return response;
	}

	@Override
	@Transactional
	public ServiceResponse updateBioMaxRequest(Long id, BioMaxRequestDTO biomaxRequestDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("updateBioMaxRequest");
		apiLogInfo.setApiUrl("/api/updateBioMaxRequest");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type :" + biomaxRequestDTO.getBiomaxTitleValue() + " ,is Approved By :"
				+ biomaxRequestDTO.getStatusBy());
		try {
			Optional<BiomaxRequest> exitingBiomaxRequestForUpdate = bioMaxRequestRepository.findById(id);
			if (exitingBiomaxRequestForUpdate.isPresent()) {
				BiomaxRequest biomax = exitingBiomaxRequestForUpdate.get();
				biomax.setBiomaxStatus(biomaxRequestDTO.getBiomaxStatus());
				biomax.setEnabled(true);
				biomax.setStatusBy(biomaxRequestDTO.getStatusBy());
				biomax.setBiomaxStatus(biomaxRequestDTO.getBiomaxStatus());
				if (biomax.getBiomaxrequestDate().isBefore(LocalDateTime.now())) {
					afterLeaveApprovedLeaveAdded(biomaxRequestDTO.getEmpId(),
							exitingBiomaxRequestForUpdate.get().getBiomaxrequestDate(),
							exitingBiomaxRequestForUpdate.get().getTobiomaxrequestDate());
				}
				bioMaxRequestRepository.save(biomax);

				response.setServiceStatus("success");
				response.setServiceMessage(
						"Biomax Request Staus Is Updated To " + biomaxRequestDTO.getBiomaxStatus());
				response.setServiceResponse(biomax);
			} else {
				response.setServiceStatus("fails");
				response.setServiceMessage("Biomax Request Not found");

			}

		} catch (Exception e) {
			logBuilder.append("Error" + e.getMessage());
			response.setServiceStatus("fails");
			response.setServiceMessage("Some Thing Wrong" + e.getMessage());

		}
		return response;
	}
	
	private List<List<BiomaxDefaulter>> groupConsecutiveDays(List<BiomaxDefaulter> defaulterList) {

		defaulterList.sort(Comparator.comparing(defaulter -> LocalDateTime.parse(defaulter.getDefaultedDate(),
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))));

		List<List<BiomaxDefaulter>> groupedDefaulters = new ArrayList<>();
		List<BiomaxDefaulter> currentGroup = new ArrayList<>();

		for (int i = 0; i < defaulterList.size(); i++) {
			BiomaxDefaulter currentDefaulter = defaulterList.get(i);
			LocalDateTime currentDate = LocalDateTime.parse(currentDefaulter.getDefaultedDate(),
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));

			if (currentGroup.isEmpty()) {
				currentGroup.add(currentDefaulter);
			} else {
				BiomaxDefaulter lastDefaulterInGroup = currentGroup.get(currentGroup.size() - 1);
				LocalDateTime lastDateInGroup = LocalDateTime.parse(lastDefaulterInGroup.getDefaultedDate(),
						DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
				if (lastDateInGroup.plusDays(1).equals(currentDate)) {
					currentGroup.add(currentDefaulter);
				} 
				else if(lastDateInGroup.plusDays(2).equals(currentDate)) {
					currentGroup.add(currentDefaulter);
				}
				else if(lastDateInGroup.plusDays(3).equals(currentDate)) {
					currentGroup.add(currentDefaulter);
				}
					else {
					if (currentGroup.size() == 3) {
						groupedDefaulters.add(new ArrayList<>(currentGroup));
					}
					currentGroup.clear();
					currentGroup.add(currentDefaulter);
				}
			}

			if (i == defaulterList.size() - 1 && currentGroup.size() == 3) {
				groupedDefaulters.add(new ArrayList<>(currentGroup));
			}
		}

		return groupedDefaulters;
	}

	public void afterLeaveApprovedLeaveAdded(Long empid, LocalDateTime fromDate, LocalDateTime toDate) {
		try {
			List<LeaveBalanceLog> log1 = leaveBalanceLogRepository.findLogValidation(empid);

			Employee employee = employeeRepository.findByEmpId(empid);
			System.out.println("log1" + log1.size());
			if (log1.size() == 0) {

				if (employee.getEmploymentstatus().equals("Confirmed")) {
					EmployeeLeavesMap employeeLeaveMapObject = employeeLeavesMapRepository
							.findByEmpIdAndLeaveTypeMasterId(empid, (short) leaveTypeId);
					List<BiomaxDefaulter> defaulterListLatest = biomaxDefaulterRepository.findByEmpIdAndIsDeducted(empid,true);
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

					List<BiomaxDefaulter> filteredList = defaulterListLatest.stream().filter(defaulter -> {
						String defaultedDateStr = defaulter.getDefaultedDate();
						try {
							if (defaultedDateStr.contains(".")) {
						        defaultedDateStr = defaultedDateStr.substring(0, defaultedDateStr.indexOf(".")); 
						    }
							LocalDateTime defaultedDate = LocalDateTime.parse(defaultedDateStr, formatter);
							return !defaultedDate.isBefore(fromDate) && !defaultedDate.isAfter(toDate);
						} catch (Exception e) {
							e.printStackTrace();
							return false;
						}
					}).collect(Collectors.toList());
					List<List<BiomaxDefaulter>> groupedDefaulters = groupConsecutiveDays(filteredList);

					String mostFrequentLeaveToDeduct = "";
				    float totalDeductionToBeAdded = 0;

				    for (List<BiomaxDefaulter> group : groupedDefaulters) {
				        
				    	String leaveToDeduct = group.stream()
				    	        .collect(Collectors.groupingBy(BiomaxDefaulter::getLeaveToDeduct, Collectors.counting())) 
				    	        .entrySet().stream()
				    	        .max((entry1, entry2) -> Long.compare(entry1.getValue(), entry2.getValue())) 
				    	        .map(Map.Entry::getKey)
				    	        .orElse("No data found");

				        // If we have found a valid leave to deduct, accumulate the deduction
				        if (!leaveToDeduct.equals("No data found")) {
				            mostFrequentLeaveToDeduct = leaveToDeduct;
				            totalDeductionToBeAdded += Float.parseFloat(mostFrequentLeaveToDeduct);
				        }
				    }

				    // Update the leave balance based on the most frequent leave to deduct
				    if (employeeLeaveMapObject != null && !mostFrequentLeaveToDeduct.equals("No data found")) {
				        Float newBalance = employeeLeaveMapObject.getBalance() + totalDeductionToBeAdded;
				        
					employeeLeaveMapObject.setBalance(newBalance);
					EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(employeeLeaveMapObject);
					filteredList.forEach(data->{
						data.setIsApprovedByManager(true);
					});
					biomaxDefaulterRepository.saveAll(filteredList);
					LeaveBalanceLog log = new LeaveBalanceLog();

					log.setBalance(newBalance);
					log.setEmpId(empid);
					log.setLeaveTypeMasterId((short) leaveTypeId);
					log.setMessage(LeaveLogMessage.autoDeductedLeaveOnTimesheetDefaulterAddedBack.replace("0.0", ""+totalDeductionToBeAdded));
					log.setUpdateBalanceBy("+" + totalDeductionToBeAdded);

					LeaveBalanceLog leaveLogDbResponse = leaveBalanceLogRepository.save(log);
				}
			}
		} 
		}catch (Exception e) {
			e.printStackTrace();

		}
	}

	@Override
	public ServiceResponse getByEmployeeId(Long empid) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getByEmployeeId");
		apiLogInfo.setApiUrl("/api/getByEmployeeId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type :" + empid);
		List<BioMaxRequestDTO> listbiomax = new ArrayList<>();
		try {
			List<BiomaxRequest> list = bioMaxRequestRepository.findByEmpId(empid);
			if (list.size() > 0) {
				list.forEach((e) -> {
					BioMaxRequestDTO ob = new BioMaxRequestDTO();
					ob.setBiomaxreequestId(e.getBiomaxreequestId());
					ob.setBiomaxStatus(ob.getBiomaxStatus());
					ob.setBiomaxTitle(e.getBiomaxIssueId());
					ob.setTobiomaxrequestDate(e.getTobiomaxrequestDate());
					Optional<BioMaxRequestIssue> requestType = bioMaxRequestIssueRepository
							.findById(e.getBiomaxIssueId());
					if (requestType.isPresent()) {
						ob.setBiomaxTitleValue(requestType.get().getBioMaxRequestIssueName());
					}
					ob.setReportingManagerId(e.getReportingManagerId());

					Optional<Employee> emp = employeeRepository.findById(e.getReportingManagerId());
					if (emp.isPresent()) {
						ob.setReportingManagerName(emp.get().getName());
					}
					ob.setCreatedOn(e.getCreatedOn());
					ob.setEmpId(e.getEmpId());
					ob.setBiomaxStatus(e.getBiomaxStatus());
					ob.setRequestRemark(e.getRequestRemark());
					ob.setBiomaxrequestDate(e.getBiomaxrequestDate());
					List<BiomaxDefaulter> defaulterList = biomaxDefaulterRepository.findByEmpIdAndIsDeductedAndIsApprovedByManager(empid,
							true,false);
					List<String> dateList = defaulterList.stream().map(BiomaxDefaulter::getDefaultedDate)
							.collect(Collectors.toList());

					ob.setApplicableDates(dateList);
					listbiomax.add(ob);
				});
				response.setServiceStatus("true");
				response.setServiceMessage("Record Found");
				response.setServiceResponse(listbiomax);
			} else {
				response.setServiceStatus("true");
				response.setServiceMessage("Not Record Found");
			}
		} catch (Exception e) {
			logBuilder.append("Exception found " + e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getByRepostingManager(Long reportingManagerId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getByEmployeeId");
		apiLogInfo.setApiUrl("/api/getByEmployeeId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type :" + reportingManagerId);

		try {
			List<BiomaxRequest> list = bioMaxRequestRepository.findByReportingManagerId(reportingManagerId);
			List<BioMaxRequestDTO> listbiomax = new ArrayList<>();

			if (list.size() > 0) {
				list.forEach((e) -> {
					BioMaxRequestDTO ob = new BioMaxRequestDTO();
					ob.setBiomaxreequestId(e.getBiomaxreequestId());
					ob.setBiomaxStatus(ob.getBiomaxStatus());
					ob.setBiomaxTitle(e.getBiomaxIssueId());
					ob.setTobiomaxrequestDate(e.getTobiomaxrequestDate());

					ob.setReportingManagerId(e.getReportingManagerId());
					Optional<BioMaxRequestIssue> requestType = bioMaxRequestIssueRepository
							.findById(e.getBiomaxIssueId());
					if (requestType.isPresent()) {
						ob.setBiomaxTitleValue(requestType.get().getBioMaxRequestIssueName());
					}
					Optional<Employee> emp = employeeRepository.findById(e.getEmpId());
					if (emp.isPresent()) {
						ob.setEmpName(emp.get().getName());
					}
					Optional<Employee> rep = employeeRepository.findById(e.getReportingManagerId());
					if (rep.isPresent()) {
						ob.setReportingManagerName(rep.get().getName());
					}
					ob.setCreatedOn(e.getCreatedOn());
					ob.setEmpId(e.getEmpId());
					ob.setBiomaxStatus(e.getBiomaxStatus());
					ob.setRequestRemark(e.getRequestRemark());
					ob.setBiomaxrequestDate(e.getBiomaxrequestDate());
					listbiomax.add(ob);
				});
				response.setServiceStatus("true");
				response.setServiceMessage("Record Found");
				response.setServiceResponse(listbiomax);
			} else {
				response.setServiceStatus("true");
				response.setServiceMessage("Not Record Found");
			}

			return response;
		} catch (Exception e) {
			logBuilder.append("Exception found " + e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse deletedRequest(Long id) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("deletedRequest");
		apiLogInfo.setApiUrl("/api/deletedRequest");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type Id :" + id);
		try {
			Optional<BiomaxRequest> list = bioMaxRequestRepository.findById(id);
			if (list.isPresent()) {
				bioMaxRequestRepository.delete(list.get());
				response.setServiceStatus("true");
				response.setServiceMessage("Record Found has been deleted");
			} else {
				response.setServiceStatus("true");
				response.setServiceMessage("Not Record Found");
			}
		} catch (Exception e) {
			logBuilder.append("Exception found " + e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getById(Long id) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getById");
		apiLogInfo.setApiUrl("/api/getById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type :" + id);
		try {
			Optional<BiomaxRequest> list = bioMaxRequestRepository.findById(id);
			if (list.isPresent()) {
				response.setServiceStatus("true");
				response.setServiceMessage("Record Found");
				response.setServiceResponse(list.get());
			} else {
				response.setServiceStatus("true");
				response.setServiceMessage("Not Record Found");
			}
		} catch (Exception e) {
			logBuilder.append("Exception found " + e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getBioMaxRequestType() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getBioMaxRequestType");
		apiLogInfo.setApiUrl("/api/getBioMaxRequestType");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type Id :");
		try {
			List<BioMaxRequestIssue> list = bioMaxRequestIssueRepository.findAll();
			if (list.size() > 0) {

				response.setServiceStatus("true");
				response.setServiceMessage("Record Found has been deleted");
				response.setServiceResponse(list);
			} else {
				response.setServiceStatus("true");
				response.setServiceMessage("Not Record Found");
			}
		} catch (Exception e) {
			logBuilder.append("Exception found " + e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getBioMaxRequestTypeCronJon() {
		// TODO Auto-generated method stub
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getById");
		apiLogInfo.setApiUrl("/api/getById");
		apiLogInfo.setLogLevel("INFO");
		cronJobService.leaveDeduct();
		System.out.println("Sdfdsg");

		return response;
	}

	@Override
	public ServiceResponse leaveDeductRoleBackForEmloyee(BioMaxRequestDTO bioMaxRequestDTO) {
		// TODO Auto-generated method stub
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getById");
		apiLogInfo.setApiUrl("/api/getById");
		apiLogInfo.setLogLevel("INFO");
		response.setServiceResponse(bioMaxRequestDTO);

		return response;
	}

}

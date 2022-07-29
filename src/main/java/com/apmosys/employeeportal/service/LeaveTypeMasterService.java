package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class LeaveTypeMasterService {

	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;

	@Value("${financialYear.startDate}")
	String financialYearStartDate;

	@Value("${financialYear.startMonth}")
	String financialYearStartMonth;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	EmployeeLeavesMapRepository employeeLeavesMapRepository;

	public ServiceResponse getAllLeaveTypes() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			List<LeaveTypeMaster> list = leaveTypeMasterRepository.findAll();

			if (list != null) {
				for (LeaveTypeMaster leaveMaster : list) {
					LeaveDTO leaveDTO = new LeaveDTO();

					leaveDTO.setLeaveTypeMasterId(leaveMaster.getLeaveTypeMasterId());
					leaveDTO.setLeaveType(leaveMaster.getLeaveType());
					leaveDTO.setNoOfDays(leaveMaster.getNoOfDays());
					leaveDTO.setDescription(leaveMaster.getDescription());
					leaveDTO.setPaidLeave(leaveMaster.getPaidLeave());
					leaveDTO.setRules(leaveMaster.getRules());
					leaveDTO.setLeaveTypeCode(leaveMaster.getLeaveTypeCode());
					leaveDTO.setGender(leaveMaster.getGender());
					dtoList.add(leaveDTO);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave List is null.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateLeaveType(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Optional<LeaveTypeMaster> leaveTypeMaster = leaveTypeMasterRepository
					.findById(leaveDTO.getLeaveTypeMasterId());

			if (leaveTypeMaster.isPresent()) {
				LeaveTypeMaster leaveType = leaveTypeMaster.get();

				leaveType.setDescription(leaveDTO.getDescription());
				leaveType.setRules(leaveDTO.getRules());
				leaveType.setLeaveType(leaveDTO.getLeaveType());
				leaveType.setLeaveTypeCode(leaveDTO.getLeaveTypeCode());
				leaveType.setGender(leaveDTO.getGender());
				leaveType.setNoOfDays(leaveDTO.getNoOfDays());
				leaveType.setPaidLeave(leaveDTO.getPaidLeave());

				LeaveTypeMaster updatedLeaveType = leaveTypeMasterRepository.save(leaveType);

				if (updatedLeaveType != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave type updated.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Leave type updation failed.");
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Type Not Found.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse createLeaveType(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			LeaveTypeMaster leaveType = new LeaveTypeMaster();

			leaveType.setLeaveType(leaveDTO.getLeaveType());
			leaveType.setLeaveTypeCode(leaveDTO.getLeaveTypeCode());
			leaveType.setGender(leaveDTO.getGender());
			leaveType.setNoOfDays(leaveDTO.getNoOfDays());
			leaveType.setPaidLeave(leaveDTO.getPaidLeave());
			leaveType.setRules(leaveDTO.getRules());
			leaveType.setDescription(leaveDTO.getDescription());

			LeaveTypeMaster newLeaveType = leaveTypeMasterRepository.save(leaveType);

			if (newLeaveType != null) {

				List<Employee> employeeList = employeeRepository.findAll();

				List<EmployeeLeavesMap> mapList = new ArrayList<EmployeeLeavesMap>();

				employeeList.forEach((employee) -> {

					EmployeeLeavesMap map = new EmployeeLeavesMap();

					map.setBalance(newLeaveType.getNoOfDays());
					map.setEmpId(employee.getEmpId());
					map.setLeaveTypeMasterId(newLeaveType.getLeaveTypeMasterId());
					map.setPendingForApproval((float) 0);
					mapList.add(map);
				});

				employeeLeavesMapRepository.saveAll(mapList);

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Leave type created.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave type creation failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllLeaveTypesByLeavePolicies(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> list = leaveTypeMasterRepository
					.getAllLeaveTypesByLeavePolicies(leaveDTO.getEmploymentStatus(), leaveDTO.getGender());

			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Type list is empty.");
			} else {
				for (Object[] object : list) {

					LeaveDTO dto = new LeaveDTO();

					dto.setLeaveTypeMasterId(object[0] != null ? Short.parseShort(object[0].toString()) : null);
					dto.setLeaveType(object[1] != null ? object[1].toString() : null);
					dto.setLeaveTypeCode(object[2] != null ? object[2].toString() : null);
					dto.setLeavePolicyMasterId(object[3] != null ? Short.parseShort(object[3].toString()) : null);
					dto.setLeavePolicyName(object[4] != null ? object[4].toString() : null);
					dto.setEmploymentStatus(object[5] != null ? object[5].toString() : null);
					dto.setDescription(object[6] != null ? object[6].toString() : null);
					dto.setLeaveApplication(object[7] != null ? object[7].toString() : null);
					dto.setIncrement(object[8] != null ? object[8].toString() : null);
					dto.setIncrementValue(object[9] != null ? Float.parseFloat(object[9].toString()) : null);
					dto.setOneTimeLeave(object[10] != null ? object[10].toString() : null);
					dto.setOneTimeLeaveMinCount(object[11] != null ? Float.parseFloat(object[11].toString()) : null);
					dto.setOneTimeLeaveCount(object[12] != null ? Float.parseFloat(object[12].toString()) : null);
					dto.setCarryForward(object[13] != null ? object[13].toString() : null);
					dto.setCarryForwardValue(object[14] != null ? Integer.parseInt(object[14].toString()) : null);
					dto.setExpirationPeriod(object[15] != null ? object[15].toString() : null);
					dto.setExpirationPeriodValue(object[16] != null ? Integer.parseInt(object[16].toString()) : null);
					dto.setLockingPeriod(object[17] != null ? object[17].toString() : null);
					dto.setLockingPeriodValue(object[18] != null ? Integer.parseInt(object[18].toString()) : null);
					dto.setLockingValue(object[19] != null ? Integer.parseInt(object[19].toString()) : null);
					dto.setProbation(object[20] != null ? object[20].toString() : null);
					dto.setProbationPeriod(object[21] != null ? Integer.parseInt(object[21].toString()) : null);
					
					// set locking references 
					if(dto.getLockingPeriod().equalsIgnoreCase("Yes")) {
						dto.setFinancialYearStartDate(financialYearStartDate);
						dto.setFinancialYearStartMonth(financialYearStartMonth);
					}

					dtoList.add(dto);
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

}

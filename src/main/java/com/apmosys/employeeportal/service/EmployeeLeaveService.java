package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.CompOffMaster;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.repository.CompOffMasterRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class EmployeeLeaveService {

	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	@Autowired
	EmployeeLeavesMapRepository employeeLeavesMapRepository;
	
	@Autowired
	CompOffMasterRepository compOffMasterRepository;
	
	@Autowired
	LeaveBalanceLogRepository leaveBalanceLogRepository;

	@Transactional
	public ServiceResponse applyLeave(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
					.findByEmpIdAndLeaveTypeMasterId(leaveDTO.getEmpId(), leaveDTO.getLeaveTypeMasterId());

			EmployeeLeave leaveApplication = new EmployeeLeave();

			leaveApplication.setEmpId(leaveDTO.getEmpId());
			leaveApplication.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
			leaveApplication.setLeaveStatusId((short) 1);
			leaveApplication.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate()));
			leaveApplication.setToDate(stringToDateTimeParser.getDate(leaveDTO.getToDate()));
			leaveApplication.setNoOfDays((Float) leaveDTO.getNoOfDays());
			leaveApplication.setReason(leaveDTO.getReason());
			leaveApplication.setManagerId(leaveDTO.getManagerId());
			leaveApplication.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());

			// Leave deduction from balance leaves
			Float balance = employeeLeavesMap.getBalance();
			balance =  balance - leaveDTO.getNoOfDays();
			Float pendingForApproval = employeeLeavesMap.getPendingForApproval();
			pendingForApproval = pendingForApproval + leaveDTO.getNoOfDays();

			employeeLeavesMap.setBalance(balance);
			employeeLeavesMap.setPendingForApproval(pendingForApproval);

			EmployeeLeavesMap dbResponse1 = employeeLeavesMapRepository.save(employeeLeavesMap);

			EmployeeLeave dbResponse2 = employeeLeaveRepository.save(leaveApplication);

			if (dbResponse1 != null && dbResponse2 != null) {
				LeaveBalanceLog log = new LeaveBalanceLog();
				
				log.setBalance(balance);
				log.setEmpId(leaveDTO.getEmpId());
				log.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
				log.setMessage(LeaveLogMessage.requestDeductLeave.replace("0.0", leaveDTO.getNoOfDays().toString()));
				log.setUpdateBalanceBy("-"+leaveDTO.getNoOfDays());
				
				leaveBalanceLogRepository.save(log);
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Leave application submitted.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Creation Failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllMyLeaveApplicationsByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> list = employeeLeaveRepository.getAllMyLeaveApplicationsByEmpId(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found");

			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setLeaveType(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
					dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
					dto.setReason(object[8] != null ? object[8].toString() : null);
					dtoList.add(dto);
				});

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

	public ServiceResponse getAllMyTeamsPendingLeaveApplicationsByManagerId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> list = employeeLeaveRepository
					.getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveDTO.getManagerId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found");

			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setLeaveType(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
					dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
					dto.setReason(object[8] != null ? object[8].toString() : null);
					dto.setEmpId(object[9] != null ? Long.parseLong(object[9].toString()): null);
					dto.setLeaveTypeMasterId(object[10] != null ? Short.parseShort(object[10].toString()): null);
					dtoList.add(dto);
				});

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

	@Transactional
	public ServiceResponse updateLeaveStatus(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Optional<EmployeeLeave> leaveApplication = employeeLeaveRepository.findById(leaveDTO.getLeaveId());
			EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
					.findByEmpIdAndLeaveTypeMasterId(leaveDTO.getEmpId(), leaveDTO.getLeaveTypeMasterId());

			if (leaveApplication.isPresent()) {
				EmployeeLeave pendingLeaveApplication = leaveApplication.get();

				employeeLeavesMap.setPendingForApproval(
						employeeLeavesMap.getPendingForApproval() - pendingLeaveApplication.getNoOfDays());

				// 1 = pending , 2 = Approved , 3= Rejected
				if (leaveDTO.getLeaveStatusId() == 2) {
					pendingLeaveApplication.setLeaveStatusId((short) 2);
					response.setServiceResponse("Leave application approved.");
				} else if (leaveDTO.getLeaveStatusId() == 3) {
					pendingLeaveApplication.setLeaveStatusId((short) 3);
					employeeLeavesMap
							.setBalance(employeeLeavesMap.getBalance() + pendingLeaveApplication.getNoOfDays());
					
					
					LeaveBalanceLog log = new LeaveBalanceLog();
					log.setBalance(employeeLeavesMap.getBalance());
					log.setEmpId(leaveDTO.getEmpId());
					log.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
					log.setMessage(LeaveLogMessage.requestAddLeave.replace("0.0", pendingLeaveApplication.getNoOfDays().toString()));
					log.setUpdateBalanceBy("+" + pendingLeaveApplication.getNoOfDays());
					leaveBalanceLogRepository.save(log);
					response.setServiceResponse("Leave application rejected.");

				}
				EmployeeLeave updatedLeaveApplication = employeeLeaveRepository.save(pendingLeaveApplication);
				EmployeeLeavesMap updatedEmployeeLeavesMap = employeeLeavesMapRepository.save(employeeLeavesMap);

				if (updatedLeaveApplication != null && updatedEmployeeLeavesMap != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Leave Updation Failed.");
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getMyLeaveBalancesByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		Float totalbalance = 0.0f;
		Float totalPendingForApproval = 0.0f;

		try {

			List<Object[]> employeeLeavesList = employeeLeavesMapRepository
					.getMyLeaveBalancesByEmpId(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (employeeLeavesList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leaves balance found.");

			} else {

				employeeLeavesList.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveType(object[0] != null ? object[0].toString() : null);
					dto.setBalance(object[1] != null ? Float.parseFloat(object[1].toString()) : null);
					dto.setPendingForApproval(object[2] != null ? Float.parseFloat(object[2].toString()) : null);
					dto.setLeaveTypeMasterId(object[3] != null ? Short.parseShort(object[3].toString()): null);
					
					dtoList.add(dto);
				});

				for (Object[] leave : employeeLeavesList) {
					totalbalance =  totalbalance
							+ (leave[1] != null ? Float.parseFloat(leave[1].toString()) : null);
					totalPendingForApproval =  totalPendingForApproval
							+ (leave[2] != null ? Float.parseFloat(leave[2].toString()) : null);
				}
				
				LeaveDTO dto = new LeaveDTO();
				dto.setLeaveType("Total");
				dto.setBalance(totalbalance);
				dto.setPendingForApproval(totalPendingForApproval);
				dtoList.add(dto);
				
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

	@Transactional
	public ServiceResponse updateLeavesByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			List<EmployeeLeavesMap> employeeLeavesList = employeeLeavesMapRepository
					.findAllByEmpId(leaveDTO.getEmpId());

			if (employeeLeavesList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leaves found.");
			} else {
				employeeLeavesList.forEach((leave) -> {

					leaveDTO.getEmployeeLeaveList().forEach((dto) -> {

						if (leave.getLeaveTypeMasterId() == dto.getLeaveTypeMasterId()) {

							if (leave.getBalance() == 0) {
								//set leaves to newly created employee when his/her bucket is 0.0 for all leave types
								LeaveBalanceLog log = new LeaveBalanceLog();
								log.setBalance(dto.getBalance());
								log.setEmpId(leaveDTO.getEmpId());
								log.setLeaveTypeMasterId(dto.getLeaveTypeMasterId());
								log.setMessage(LeaveLogMessage.adminAddLeave.replace("0.0", dto.getBalance().toString()));
								log.setUpdateBalanceBy("+" + dto.getBalance());
								leaveBalanceLogRepository.save(log);
								
								leave.setBalance(dto.getBalance());
							} else {

								if (dto.getBalance().equals(leave.getBalance())) {
									// No change in balance leave
									leave.setBalance(dto.getBalance());
								} else {

									LeaveBalanceLog log = new LeaveBalanceLog();
									log.setBalance(dto.getBalance());
									log.setEmpId(leaveDTO.getEmpId());
									log.setLeaveTypeMasterId(dto.getLeaveTypeMasterId());

									if (dto.getBalance() > leave.getBalance()) {
										// leave added to bucket balance
										Float change = dto.getBalance() - leave.getBalance();
										log.setMessage(LeaveLogMessage.adminAddLeave.replace("0.0", change.toString()));
										log.setUpdateBalanceBy("+" + change);

									} else if (dto.getBalance() < leave.getBalance()) {
										// leave deducted from bucket balance
										Float change = dto.getBalance() - leave.getBalance();
										log.setMessage(LeaveLogMessage.adminDeductLeave.replace("0.0", (change*-1)+""));
										log.setUpdateBalanceBy(change.toString());
									}
									leave.setBalance(dto.getBalance());
									leaveBalanceLogRepository.save(log);
								}
							}

						}

					});

				});
			}

			List<EmployeeLeavesMap> updatedEmployeeLeavesList = employeeLeavesMapRepository.saveAll(employeeLeavesList);

			if (updatedEmployeeLeavesList.isEmpty() || updatedEmployeeLeavesList == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee leaves updation failed.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Employee leaves updated.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	

	public ServiceResponse getLeaveLogsByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> objectList = leaveBalanceLogRepository.getLeaveLogsByEmpId(leaveDTO.getEmpId());

			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (objectList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No leave logs found for employee.");

			} else {

				objectList.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();

					dto.setLeaveType(object[0] != null ? object[0].toString() : null);
					dto.setUpdateBalanceBy(object[1] != null ? object[1].toString() : null);
					dto.setBalance(object[2] != null ? Float.parseFloat(object[2].toString()) : null);
					dto.setMessage(object[3] != null ? object[3].toString() : null);
					dto.setCreatedOn(object[4] != null ? object[4].toString() : null);
					dtoList.add(dto);
				});

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

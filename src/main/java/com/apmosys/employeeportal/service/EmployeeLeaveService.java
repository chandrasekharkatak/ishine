package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.repository.CompOffMasterRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
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

	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;
	
	@Autowired
	private MailService mailService;
	
	@Value("${hr.mail}")
	private String hrMailAddress;

	@Transactional
	public ServiceResponse applyLeave(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();

		try {
			//LeaveTypeMaster leaveTypeMasterObj = leaveTypeMasterRepository.findByLeaveTypeCode(leaveDTO.getLeaveTypeCode());		
			EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
					.findByEmpIdAndLeaveTypeMasterId(leaveDTO.getEmpId(), leaveDTO.getLeaveTypeMasterId());
			
			System.out.println("Leave DTO check :"+leaveDTO);
			
			List<Object[]> empObj = employeeRepository.getManagerEmail(leaveDTO.getEmpId());
			
			EmployeeDTO empDto = new EmployeeDTO();
			
				empObj.forEach((object) -> {
					
					empDto.setEmail(object[0] != null ? object[0].toString() : null);
					empDto.setManagerEmail(object[1] != null ? object[1].toString() : null);
					empDto.setName(object[2] != null ? object[2].toString() : null);
					empDto.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()): null);
					});
				
			
			// HERE : Effective Leave Balance = employeeLeavesMap.getBalance()
			if (!leaveDTO.getLeaveTypeCode().equalsIgnoreCase("LWP") && (employeeLeavesMap.getBalance() == 0
					|| employeeLeavesMap.getBalance() < leaveDTO.getNoOfDays())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Your available balance of " + employeeLeavesMap.getBalance()
						+ " day(s) is not sufficient for this Leave Application.");

				return response;
			}

			EmployeeLeave leaveApplication = new EmployeeLeave();

			leaveApplication.setEmpId(leaveDTO.getEmpId());
			leaveApplication.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
			leaveApplication.setLeaveStatusId((short) 1);
			leaveApplication.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));
			leaveApplication.setToDate(stringToDateTimeParser.getDate(leaveDTO.getToDate(), "yyyy-MM-dd"));
			leaveApplication.setNoOfDays((Float) leaveDTO.getNoOfDays());
			leaveApplication.setReason(leaveDTO.getReason());
			leaveApplication.setManagerId(leaveDTO.getManagerId());
			leaveApplication.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());

			// Leave deduction from balance leaves
			Float balance = employeeLeavesMap.getBalance();
			balance = balance - leaveDTO.getNoOfDays();
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
				log.setUpdateBalanceBy("-" + leaveDTO.getNoOfDays());

				leaveBalanceLogRepository.save(log);

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Leave application submitted.");
				
				
			mailService.sendMailWithCC(hrMailAddress,empDto.getManagerEmail() + " , " + empDto.getEmail(),"Regarding Leave Application",
					"Employee Id : A-"+empDto.getEmployeementId()+"<br>"+
					"Employee Name :-  "+ empDto.getName()+"  has applied for leave ");
			
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

	@Transactional
	public ServiceResponse deletePendingLeave(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();

		try {

			Optional<EmployeeLeave> leaveObject = employeeLeaveRepository.findById(leaveDTO.getLeaveId());

			if (leaveObject.isPresent()) {

				EmployeeLeave leaveToBeDeleted = leaveObject.get();

				employeeLeaveRepository.deleteById(leaveDTO.getLeaveId());

				EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(
						leaveToBeDeleted.getEmpId(), leaveToBeDeleted.getLeaveTypeMasterId());

				Float balance = employeeLeavesMap.getBalance();
				balance = balance + leaveDTO.getNoOfDays();
				Float pendingForApproval = employeeLeavesMap.getPendingForApproval();
				pendingForApproval = pendingForApproval - leaveDTO.getNoOfDays();

				employeeLeavesMap.setBalance(balance);
				employeeLeavesMap.setPendingForApproval(pendingForApproval);

				System.out.println(employeeLeavesMap + " employee leave");
				EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(employeeLeavesMap);

				if (dbResponse != null) { 

					LeaveBalanceLog log = new LeaveBalanceLog();

					log.setBalance(balance);
					log.setEmpId(leaveToBeDeleted.getEmpId());
					log.setLeaveTypeMasterId(leaveToBeDeleted.getLeaveTypeMasterId());
					log.setMessage(LeaveLogMessage.deleteLeave.replace("0.0", leaveDTO.getNoOfDays().toString()));
					log.setUpdateBalanceBy("+" + leaveDTO.getNoOfDays());

					leaveBalanceLogRepository.save(log);

				}

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Leave Application Deleted.");

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Application Not Found.");
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
	public ServiceResponse updatePendingLeave(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();

		try {

			Optional<EmployeeLeave> leaveObject = employeeLeaveRepository.findById(leaveDTO.getLeaveId());

			if (leaveObject.isPresent()) {

				EmployeeLeave leaveToBeUpdated = leaveObject.get();

				leaveToBeUpdated.setReason(leaveDTO.getReason());

				EmployeeLeave dbResponse = employeeLeaveRepository.save(leaveToBeUpdated);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave Application Updated.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave Application Updation Failed.");
				}

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
					dto.setLeaveTypeMasterId(object[9] != null ? Short.parseShort(object[9].toString()) : null);
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
					dto.setEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
					dto.setLeaveTypeMasterId(object[10] != null ? Short.parseShort(object[10].toString()) : null);
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
			System.out.println("leaveDTO.getEmpId() : -- " +leaveDTO.getEmpId());
			System.out.println("leaveDTO.getLeaveTypeMasterId() : -- " +leaveDTO.getLeaveTypeMasterId());
			if (leaveApplication.isPresent()) {
				EmployeeLeave pendingLeaveApplication = leaveApplication.get();

				pendingLeaveApplication.setLeaveStatusUpdatedBy(leaveDTO.getLeaveStatusUpdatedBy());
				pendingLeaveApplication.getCommonProperty().setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());

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
					log.setMessage(LeaveLogMessage.requestAddLeave.replace("0.0",
							pendingLeaveApplication.getNoOfDays().toString()));
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

			Employee employee = employeeRepository.findByEmployeementId(leaveDTO.getEmployeementId());

			if (employee != null) {
				List<Object[]> employeeLeavesList = employeeLeavesMapRepository
						.getMyLeaveBalancesByEmpId(employee.getEmpId());
				List<Object[]> employeeData = employeeRepository
						.getEmployeeData(employee.getEmpId());
				List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
				List<LeaveDTO> employeeDataList = new ArrayList<LeaveDTO>();

				if (employeeLeavesList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Leaves balance found.");

				} else {

					employeeLeavesList.forEach((object) -> {
						LeaveDTO dto = new LeaveDTO();
						dto.setLeaveType(object[0] != null ? object[0].toString() : null);
						dto.setBalance(object[1] != null ? Float.parseFloat(object[1].toString()) : null);
						dto.setPendingForApproval(object[2] != null ? Float.parseFloat(object[2].toString()) : null);
						dto.setLeaveTypeMasterId(object[3] != null ? Short.parseShort(object[3].toString()) : null);
						dto.setLeaveTypeCode(object[4] != null ? object[4].toString() : null);

						dtoList.add(dto);
					});

					for (Object[] leave : employeeLeavesList) {
						totalbalance = totalbalance + (leave[1] != null ? Float.parseFloat(leave[1].toString()) : null);
						totalPendingForApproval = totalPendingForApproval
								+ (leave[2] != null ? Float.parseFloat(leave[2].toString()) : null);
					}

					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveType("Total");
					dto.setBalance(totalbalance);
					dto.setPendingForApproval(totalPendingForApproval);
					dtoList.add(dto);
					
					employeeData.forEach((object) -> {
						LeaveDTO empDto = new LeaveDTO();
						empDto.setManagerName(object[0] != null ? object[0].toString() : null);
						empDto.setJobRoleName(object[1] != null ? object[1].toString() : null);
						empDto.setDepartmentName(object[2] != null ? object[2].toString() : null);
						empDto.setEmploymentStatus(object[3] != null ? object[3].toString() : null);
						empDto.setEmployeeName(employee.getName());
						employeeDataList.add(empDto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					response.setServiceResponse1(employee.getEmpId());
					response.setServiceResponse2(employeeDataList);
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee not found. Kindly check Employee ID.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse revokeApprovedLeaveApplication(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Optional<EmployeeLeave> leaveObj = employeeLeaveRepository.findById(leaveDTO.getLeaveId());
			
			if(leaveObj.isPresent()) {
				EmployeeLeave leaveToBeRevoked = leaveObj.get();
				
				Float noOfDays = leaveToBeRevoked.getNoOfDays();
				employeeLeaveRepository.deleteById(leaveDTO.getLeaveId());
				
				// updating leave balance after leave revoked
				
				EmployeeLeavesMap employeeLeaveMapObj = employeeLeavesMapRepository
						.findByEmpIdAndLeaveTypeMasterId(leaveToBeRevoked.getEmpId(), leaveToBeRevoked.getLeaveTypeMasterId());
				
				Float newBalance = employeeLeaveMapObj.getBalance() + noOfDays;
				employeeLeaveMapObj.setBalance(newBalance);
				
				EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(employeeLeaveMapObj);
				
				if(dbResponse != null) {
					
					LeaveBalanceLog log = new LeaveBalanceLog();

					log.setBalance(newBalance);
					log.setEmpId(leaveToBeRevoked.getEmpId());
					log.setLeaveTypeMasterId(leaveToBeRevoked.getLeaveTypeMasterId());
					log.setMessage(LeaveLogMessage.leaveRevoked.replace("0.0", noOfDays.toString()));
					log.setUpdateBalanceBy("+" + noOfDays);

					leaveBalanceLogRepository.save(log);
					
					//send mail to hr & manager
					Optional<Employee> employee = employeeRepository.findById(leaveToBeRevoked.getEmpId());
					if(!employee.isEmpty()) {
						
						Employee empObj = employee.get();
						Optional<Employee> empManager = employeeRepository.findById(empObj.getManagerId());
						
						if(!empManager.isEmpty()) {
							Employee empManagerObj = empManager.get();
							mailService.sendMailWithCC(empManagerObj.getEmail(), hrMailAddress, "Leave revoked", "Employee have revoked its approved leave <br> EmpId : A-" + leaveToBeRevoked.getEmpId() + "<br> Reason : " + leaveDTO.getRevokeReason());
						}
					}
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave Application revoked.");
				}
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Application not found");
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
								// set leaves to newly created employee when his/her bucket is 0.0 for all leave
								// types
								LeaveBalanceLog log = new LeaveBalanceLog();
								log.setBalance(dto.getBalance());
								log.setEmpId(leaveDTO.getEmpId());
								log.setLeaveTypeMasterId(dto.getLeaveTypeMasterId());
								log.setMessage(
										LeaveLogMessage.adminAddLeave.replace("0.0", dto.getBalance().toString()));
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
										log.setMessage(
												LeaveLogMessage.adminDeductLeave.replace("0.0", (change * -1) + ""));
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

	public ServiceResponse updateLeaveBalanceByEmployeementId(LeaveDTO leaveDTO) {			
		ServiceResponse response = new ServiceResponse();			
		try {			
								
			System.out.println(leaveDTO.getEmployeementId() + " employeement id");			
			Optional<Employee> EmpId = Optional.ofNullable(employeeRepository.findByEmployeementId(leaveDTO.getEmployeementId()));			
			if(EmpId.isPresent()) {			
							
				Employee employee = EmpId.get();			
				Long primaryEmpid = employee.getEmpId();			
				System.out.println(primaryEmpid + " primary empid");			
							
							
					List<EmployeeLeavesMap> employeeLeavesList = employeeLeavesMapRepository.findAllByEmpId(primaryEmpid);			
								
					if (employeeLeavesList.isEmpty()) {			
						System.out.println("in if block");			
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);			
						response.setServiceResponse("No Leaves found.");			
					} else {			
									
						for(EmployeeLeavesMap e: employeeLeavesList) {			
							System.out.println(e.getLeaveTypeMasterId() + " leave type id");			
										
							if(e.getLeaveTypeMasterId() == 4) {			
								e.setBalance(leaveDTO.getBalance());			
								System.out.println(leaveDTO.getBalance() + " set balance");			
    						}			
//							else if(e.getLeaveTypeMasterId() == 3) {			
//								e.setBalance(leaveDTO.getBalance());			
//								System.out.println(leaveDTO.getBalance() + " set balance");			
//							}			
//							else if(e.getLeaveTypeMasterId() == 5) {			
//								e.setBalance(leaveDTO.getBalance());			
//								System.out.println(leaveDTO.getBalance() + " set balance");			
//							}			
						
											
							EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(e);			
							System.out.println(dbResponse + " dp response");			
										
							if(dbResponse != null) {			
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);					
								response.setServiceResponse("leave balance Updated.");			
							}else {			
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);					
								response.setServiceResponse("leave balance Updation Failed.");			
							}			
						}			
									
									
					}			
							
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);					
				response.setServiceResponse("Employee not found");
			}
						
		} catch (Exception e) {			
			e.printStackTrace();			
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);			
			response.setServiceResponse("Something Went Wrong.");			
			response.setServiceError(e.getMessage());			
		}			
		return response;			
	}	
		
	public ServiceResponse addOldLeaveApplicationByList(LeaveDTO leaveDTO) {			
		ServiceResponse response = new ServiceResponse();			
		try {			
						
			Optional<Employee> EmpId = Optional.ofNullable(employeeRepository.findByEmployeementId(leaveDTO.getEmployeementId()));			
			if(EmpId.isPresent()) {			
							
				Employee employee = EmpId.get();			
				Long primaryEmpid = employee.getEmpId();			
				Long managerId = employee.getManagerId();			
				Long approverId;			
							
				Employee approverName = employeeRepository.findByName(leaveDTO.getLeaveStatusUpdatedByName());			
				if(approverName == null) {			
					approverId = (long) 2;			
				}else {			
					approverId = approverName.getEmpId();			
				}			
							
			EmployeeLeave leaveApplication = new EmployeeLeave();			
			leaveApplication.setEmpId(primaryEmpid);			
						
			if(leaveDTO.getLeaveType().equals("PL")) {			
				leaveApplication.setLeaveTypeMasterId((short) 1);			
			}else if(leaveDTO.getLeaveType().equals("LWP")) {			
				leaveApplication.setLeaveTypeMasterId((short) 3);			
			}else if(leaveDTO.getLeaveType().equals("CO")) {			
				leaveApplication.setLeaveTypeMasterId((short) 4);			
			}			
						
			if(leaveDTO.getStatus().equals("Approved")) {			
				leaveApplication.setLeaveStatusId((short) 2);			
			}else if(leaveDTO.getStatus().equals("Rejected")) {			
				leaveApplication.setLeaveStatusId((short) 3);			
			}else if(leaveDTO.getStatus().equals("Pending")) {			
				leaveApplication.setLeaveStatusId((short) 1);			
			}			
						
			leaveApplication.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));			
			leaveApplication.setToDate(stringToDateTimeParser.getDate(leaveDTO.getToDate(), "yyyy-MM-dd"));			
			leaveApplication.setNoOfDays((Float) leaveDTO.getNoOfDays());			
			leaveApplication.setReason(leaveDTO.getReason());			
						
			if(managerId == null) {			
				leaveApplication.setManagerId(2);			
			}else {			
				leaveApplication.setManagerId(managerId.intValue());			
			}			
						
			leaveApplication.setHodId(approverId);			
			leaveApplication.setLeaveStatusUpdatedBy(approverId);			
						
			leaveApplication.getCommonProperty().setCreatedBy(primaryEmpid);			
						
						
			final String OLD_FORMAT = "yyyy-MM-dd";			
			final String NEW_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";			
			String oldDateString = leaveDTO.getCreatedOn();			
			String newDateString;			
			DateFormat formatter = new SimpleDateFormat(OLD_FORMAT);			
			Date d = formatter.parse(oldDateString);			
			((SimpleDateFormat) formatter).applyPattern(NEW_FORMAT);			
			newDateString = formatter.format(d);		
			Timestamp ts = Timestamp.valueOf(newDateString);		
					
					
			leaveApplication.getCommonProperty().setCreatedOn(ts);			
			EmployeeLeave dbResponse = employeeLeaveRepository.save(leaveApplication);			
			System.out.println("db response=============================================");			
			if(dbResponse != null) {			
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);					
				response.setServiceResponse("Application created");			
			}else {			
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);					
				response.setServiceResponse("Application creation Failed.");			
			}			
							
			}			
						
		}catch (Exception e) {			
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
				int i = 0;
				LeaveDTO dto;
				for (Object[] object : objectList) {
					dto = new LeaveDTO();
					i++;
					dto.setLeaveType(object[0] != null ? object[0].toString() : null);
					dto.setUpdateBalanceBy(object[1] != null ? object[1].toString() : null);
					dto.setBalance(object[2] != null ? Float.parseFloat(object[2].toString()) : null);
					dto.setMessage(object[3] != null ? object[3].toString() : null);
					dto.setCreatedOn(object[4] != null ? object[4].toString() : null);
					dto.setRowNumber(i);
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

	public ServiceResponse getAppliedLeaveApplicationsByEmpIdAndDateRange(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			// HERE : We are fetching All Past Leave Applications with Pending & Approved
			// Status
			List<Object[]> list = employeeLeaveRepository.getAppliedLeaveApplicationsByEmpIdAndDateRange(
					leaveDTO.getEmpId(), leaveDTO.getFromDate(), leaveDTO.getToDate(), leaveDTO.getLeaveTypeMasterId());
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

	public ServiceResponse countAllMyTeamsPendingLeaveApplicationsByManagerId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Long applicationCount = employeeLeaveRepository
					.countAllMyTeamsPendingLeaveApplicationsByManagerId(leaveDTO.getManagerId());
			if (applicationCount == 0) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No leave applications found.");

			} else {
				leaveDTO = new LeaveDTO();
				leaveDTO.setApplicationCount(applicationCount);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(leaveDTO);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;

	}

	public ServiceResponse countMyPendingLeaveApplicationsByLeaveType(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			List<Object[]> list = employeeLeaveRepository
					.countMyPendingLeaveApplicationsByLeaveType(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			Optional.ofNullable(list).ifPresentOrElse((employeeLeavesList) -> {

				if (employeeLeavesList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Application list is empty.Count is zero.");
				} else {
					employeeLeavesList.forEach((object) -> {
						LeaveDTO dto = new LeaveDTO();
						dto.setApplicationCount(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setLeaveType(object[1] != null ? object[1].toString() : null);
						dto.setLeaveTypeCode(object[2] != null ? object[2].toString() : null);
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Application list is null");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse countMyApprovedLeaveApplicationsByLeaveType(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			List<Object[]> list = employeeLeaveRepository
					.countMyApprovedLeaveApplicationsByLeaveType(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			Optional.ofNullable(list).ifPresentOrElse((employeeLeavesList) -> {

				if (employeeLeavesList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Application list is empty.Count is zero");
				} else {
					employeeLeavesList.forEach((object) -> {
						LeaveDTO dto = new LeaveDTO();
						dto.setApplicationCount(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setLeaveType(object[1] != null ? object[1].toString() : null);
						dto.setLeaveTypeCode(object[2] != null ? object[2].toString() : null);
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Application list is null");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}

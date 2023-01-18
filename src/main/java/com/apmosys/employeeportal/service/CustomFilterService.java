package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.CustomFilterDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class CustomFilterService {
	
	@PersistenceContext
    private EntityManager entityManager;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	EmployeeService employeeService;
	
	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;
	
	@Autowired
	TeamRepository teamRepository;
	
	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	ClientsRepository clientsRepository;
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	JobRoleRepository jobRoleRepository;

	public StringBuilder createQueryForLeaveReport(List<CustomFilterDTO> queryList) {
			StringBuilder query = new StringBuilder("");
			
				for(CustomFilterDTO dto: queryList) {
					if(dto.getOperator()!=null && dto.getOperator().equals("like")) {
						dto.setValue("%"+dto.getValue()+"%");
					}
					
					switch (dto.getColumn()) {
					case "Employee Id": {
						query = query.append(" e.employeement_id ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Full Name": {
						query = query.append(" e.name ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Leave Type": {
						query = query.append(" ltm.leave_type ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}

					case "From Date": {
						query = query.append(" el.from_date ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "To Date": {
						query = query.append(" el.to_date ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "No. of Days": {
						query = query.append(" el.no_of_days ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Reason": {
						query = query.append(" el.reason ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Status": {
						query = query.append(" ls.status ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Manager Name": {
						query = query.append(" e2.name ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Created On": {
						query = query.append(" el.created_on ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Updated On": {
						query = query.append(" el.updated_on ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Updated By": {
						query = query.append(" e3.name ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Team Name": {
						query = query.append(" t.team_name ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Project Name": {
						query = query.append(" p.project_name ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Client Name": {
						query = query.append(" p.client ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Department": {
						query = query.append(" d.name ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "Employment Status": {	
						query = query.append(" e.employmentstatus ").append(dto.getOperator() + " '")	
								.append(dto.getValue() + "' ").append(dto.getConjunction());	
						break;	
					}
					default:
						break;
					}
				}
				return query;
	    }
	
	public List<Object[]> getCustomLeaveReport(String customQuery) {
		try {
			
			Session session = entityManager.unwrap(Session.class);
			
			try {				
//				String q="select e.employeement_id, e.name as employee, ltm.leave_type, el.from_date, el.to_date, "
//						+ "el.no_of_days,el.reason, ls.status, e2.name as manager, el.created_on, "
//						+ "el.updated_on, e3.name as statusUpdateBy from employee_leave el "
//						+ "inner join employee e on el.emp_id = e.emp_id "
//						+ "inner join leave_type_master ltm on el.leave_type_master_id = ltm.leave_type_master_id "
//						+ "inner join leave_status ls on el.leave_status_id = ls.leave_status_id "
//						+ "inner join employee e2 on el.manager_id = e2.emp_id "
//						+ "inner join employee e3 on el.leave_status_updated_by = e3.emp_id where "+customQuery;
				
				String q="select e.employeement_id, e.name as employee, ltm.leave_type, el.from_date, el.to_date,el.no_of_days,el.reason, ls.status, e2.name as manager, el.created_on, "
						+ "el.updated_on, e3.name as statusUpdateBy,d.name department,t.team_name,p.project_name,p.client_name from employee_leave el "
						+ "INNER JOIN employee e on el.emp_id = e.emp_id "
						+ "INNER JOIN leave_type_master ltm on el.leave_type_master_id = ltm.leave_type_master_id "
						+ "INNER JOIN leave_status ls on el.leave_status_id = ls.leave_status_id "
						+ "INNER JOIN employee e2 on el.manager_id = e2.emp_id "
						+ "LEFT JOIN employee e3 on el.leave_status_updated_by = e3.emp_id "
						+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id "
						+ "INNER JOIN department d ON d.dept_id = jr.dept_id "
						+ "LEFT JOIN employee_team_mapping etm on etm.emp_id = el.emp_id "
						+ "LEFT JOIN teams t on t.team_id = etm.team_id "
						+ "LEFT JOIN projects p on p.project_id = t.project_id where "+customQuery;
				
				System.out.println(q);
				Query query = session.createSQLQuery(q);
				System.out.println(query);
				System.out.println(query.getResultList() + " ====");
				return query.getResultList();
				
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				if(session!=null && session.isOpen()) {
					session.close();
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	public ServiceResponse customQueryForLeaveReport(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			StringBuilder subQuery = createQueryForLeaveReport(leaveDTO.getQueryList());
			List<Object[]> list = getCustomLeaveReport(subQuery.toString());
			
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (list != null) {
				list.forEach((object) -> {
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
					dtoList.add(leavedto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("leave Application list is empty.");
			}
		}catch(Exception e){
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse customQueryForLeaveTrendAnalysisReport(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			ServiceResponse customQueryForLeaveReportResponse = customQueryForLeaveReport(leaveDTO);	
			
			if(customQueryForLeaveReportResponse.getServiceStatus().equals("Success")) {	
				List<LeaveDTO> leavedata = (List<LeaveDTO>)customQueryForLeaveReportResponse.getServiceResponse();	
					
				List<LeaveDTO> newLeaveData = new ArrayList<>();
			
				if(leavedata != null) {	
					leavedata.forEach((obj) -> {	
						LocalDate tempdate = LocalDate.parse(obj.getFromDate());	
						LocalDate toDate = LocalDate.parse(obj.getToDate());	
						long i = 0L;	
							
						while(i <= ChronoUnit.DAYS.between(tempdate, toDate)) {	
							LeaveDTO dto = new LeaveDTO();	
								
							dto.setEmployeementId(obj.getEmployeementId());	
							dto.setDepartmentName(obj.getDepartmentName());	
							dto.setEmployeeName(obj.getEmployeeName());	
							dto.setFromDate(tempdate.toString());	
							dto.setToDate(obj.getToDate());	
							dto.setStatus(obj.getStatus());	
							dto.setLeaveType(obj.getLeaveType());	
							newLeaveData.add(dto);	
								
							tempdate = tempdate.plusDays(1);	
						}	
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);	
					response.setServiceResponse(newLeaveData);	
				}	
			}		
		}catch(Exception e){
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public StringBuilder createQueryForEmployeeReport(List<CustomFilterDTO> queryList) {
		StringBuilder query = new StringBuilder("");
		
			for(CustomFilterDTO dto: queryList) {
				if(dto.getOperator()!=null && dto.getOperator().equals("like")) {
					dto.setValue("%"+dto.getValue()+"%");
				}
				
				switch (dto.getColumn()) {
				case "Employee Id": {
					query = query.append(" e.employeement_id ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Full Name": {
					query = query.append(" e.name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Employment Status": {
					query = query.append(" e.employmentstatus ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Date Of Joining": {
					query = query.append(" e.date_of_joining ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "City": {
					query = query.append(" e.city ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Blood Group": {
					query = query.append(" e.blood_group ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Gender": {
					query = query.append(" e.gender ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Work Location": {
					query = query.append(" e.work_location ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Probation Period": {
					query = query.append(" e.probation_period ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Notice Period": {
					query = query.append(" e.notice_period ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Marital Status": {
					query = query.append(" e.marital_status ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "State": {
					query = query.append(" e.state ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Bank Name": {
					query = query.append(" e.bank_name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Created By": {
					query = query.append(" e.created_by ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Created On": {
					query = query.append(" e.created_on ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Department": {
					query = query.append(" d.name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Job Role": {
					query = query.append(" jr.name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Manager": {
					query = query.append(" e2.name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Experience": {
					query = query.append(" e.total_experience ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Team Name": {
					query = query.append(" t.team_name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Project Name": {
					query = query.append(" p.project_name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Client Name": {
					query = query.append(" p.client_name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				default:
					break;
				}
			}
			return query;
     }
	
	 List<Object[]> getCustomEmployeeReport(String customQuery) {
		try {
			Session session = entityManager.unwrap(Session.class);
			
			try {
//				String q="SELECT e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode,"
//						+ " e.bank_name, e.blood_group, e.city, e.country, e.created_by, e.created_on, e.date_of_birth,"
//						+ " e.date_of_joining, e.email, e.emergency_contact_mobile, e.emergency_contact_person,"
//						+ " e.employmentstatus, e.esic_number, e.father_name, e.gender, e.graduation_type, e.pursuing,"
//						+ " e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, e.mother_tongue, e.name,"
//						+ " e.notice_period, e.alternate_mobile_no,  e.pan_number, e.passport_number,"
//						+ " e.permanent_address, e.pf_account_number, e.pincode, e.place_of_birth, e.passing_grade,"
//						+ " e.previous_pf_account_number, e.relation, e.state, e.uan,"
//						+ " e.views_on_organisation, e.year_of_passing,"
//						+ "  jr.dept_id, jr.name as jobrolename,"
//						+ " d.name as departmentname, e.work_location, e.probation_period, e.emp_id, e2.name as manager, e.experience, "
//						+ "e.billable,e.child1,e.child2,e.child3,e.mothers_name,e.spouse,e.total_experience "
//						+ "FROM employee e "
//						+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id "
//						+ "INNER JOIN department d ON d.dept_id = jr.dept_id "
//						+ "INNER JOIN employee e2 ON e.manager_id = e2.emp_id where "+customQuery;
				
				String q="SELECT e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode,e.bank_name, e.blood_group, e.city, e.country, e.created_by, e.created_on, e.date_of_birth,\n"
						+ "e.date_of_joining, e.email, e.emergency_contact_mobile, e.emergency_contact_person,\n"
						+ "e.employmentstatus, e.esic_number, e.father_name, e.gender, e.graduation_type, e.pursuing,\n"
						+ "e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, e.mother_tongue, e.name,\n"
						+ "e.notice_period, e.alternate_mobile_no,  e.pan_number, e.passport_number,\n"
						+ "e.permanent_address, e.pf_account_number, e.pincode, e.place_of_birth, e.passing_grade,\n"
						+ "e.previous_pf_account_number, e.relation, e.state, e.uan,\n"
						+ "e.views_on_organisation, e.year_of_passing,\n"
						+ "jr.dept_id, jr.name as jobrolename,\n"
						+ "d.name as departmentname, e.work_location, e.probation_period, e.emp_id, e2.name as manager, e.experience, \n"
						+ "e.billable,e.child1,e.child2,e.child3,e.mothers_name,e.spouse,e.total_experience,t.team_name,p.project_name,p.client_name, e.updated_on,e4.name as createdByName, e3.name as updatedByName \n"
						+ "FROM employee e \n"
						+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
						+ "INNER JOIN department d ON d.dept_id = jr.dept_id \n"
						+ "INNER JOIN employee e2 ON e.manager_id = e2.emp_id \n"
						+ "LEFT JOIN employee e3 ON e.updated_by = e3.emp_id \n"
						+ "LEFT JOIN employee_team_mapping etm on etm.emp_id = e.emp_id \n"
						+ "LEFT JOIN teams t on t.team_id = etm.team_id \n"
						+ "LEFT JOIN employee e4 on e.created_by = e4.emp_id \n"
						+ "LEFT JOIN projects p on p.project_id = t.project_id where "+customQuery;
				
				System.out.println(q);
				Query query = session.createSQLQuery(q);
				System.out.println(query);
				System.out.println(query.getResultList() + " ====");
				return query.getResultList();
				
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				if(session!=null && session.isOpen()) {
					session.close();
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public ServiceResponse customQueryForEmployeeReport(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			StringBuilder subQuery = createQueryForEmployeeReport(employeeDTO.getQueryList());
			List<Object[]> list = getCustomEmployeeReport(subQuery.toString());
			
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();

			if (list != null) {
				list.forEach((object) -> {
					EmployeeDTO empDTO = new EmployeeDTO();
					
					empDTO.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					empDTO.setAadhar(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					empDTO.setAboutMe(object[2] != null ? object[2].toString() : null);
					empDTO.setAddress(object[3] != null ? object[3].toString() : null);
					empDTO.setBankAccountNo(object[4] != null ? object[4].toString() : null);
					empDTO.setBankIFSCCode(object[5] != null ? object[5].toString() : null);
					empDTO.setBankName(object[6] != null ? object[6].toString() : null);
					empDTO.setBloodGroup(object[7] != null ? object[7].toString() : null);
					empDTO.setCity(object[8] != null ? object[8].toString() : null);
					empDTO.setCountry(object[9] != null ? object[9].toString() : null);
					empDTO.setCreatedBy(object[10] != null ? Integer.parseInt(object[10].toString()) : null);
					empDTO.setCreatedOn(object[11] != null ? (object[11].toString()) : null);
					empDTO.setDateOfBirth(
							object[12] != null ? stringToDateTimeParser.formatDateToString(object[12].toString())
									: null);
					empDTO.setDateOfJoining(
							object[13] != null ? stringToDateTimeParser.formatDateToString(object[13].toString())
									: null);
					empDTO.setEmail(object[14] != null ? object[14].toString() : null);
					empDTO.setEmergencyContactMobile(object[15] != null ? Long.parseLong(object[15].toString()) : null);
					empDTO.setEmergencyContactPerson(object[16] != null ? object[16].toString() : null);
					empDTO.setEmploymentstatus(object[17] != null ? object[17].toString() : null);
					empDTO.setEsicNumber(object[18] != null ? object[18].toString() : null);
					empDTO.setFatherName(object[19] != null ? object[19].toString() : null);
					empDTO.setGender(object[20] != null ? object[20].toString() : null);
					empDTO.setGraduationType(object[21] != null ? object[21].toString() : null);
					empDTO.setPursuing(object[22] != null ? object[22].toString() : null);
					empDTO.setJobRoleId(object[23] != null ? Long.parseLong(object[23].toString()) : null);
					empDTO.setLandline(object[24] != null ? Long.parseLong(object[24].toString()) : null);
					empDTO.setManagerId(object[25] != null ? Long.parseLong(object[25].toString()) : null);
					empDTO.setMaritalStatus(object[26] != null ? object[26].toString() : null);
					empDTO.setMobileNo(object[27] != null ? Long.parseLong(object[27].toString()) : null);
					empDTO.setMotherTongue(object[28] != null ? object[28].toString() : null);
					empDTO.setName(object[29] != null ? object[29].toString() : null);
					empDTO.setNoticePeriod(object[30] != null ? Short.parseShort(object[30].toString()) : null);
					empDTO.setAlternateMobileNo(object[31] != null ? Long.parseLong(object[31].toString()) : null);
					empDTO.setPanNumber(object[32] != null ? object[32].toString() : null);
					empDTO.setPassportNumber(object[33] != null ? object[33].toString() : null);
					empDTO.setPermanentAddress(object[34] != null ? object[34].toString() : null);
					empDTO.setPfAccountNumber(object[35] != null ? object[35].toString() : null);
					empDTO.setPincode(object[36] != null ? Integer.parseInt(object[36].toString()) : null);
					empDTO.setPlaceOfBirth(object[37] != null ? object[37].toString() : null);
					empDTO.setPassingGrade(object[38] != null ? object[38].toString() : null);
					empDTO.setPreviousPfAccountNumber(object[39] != null ? object[39].toString() : null);
					empDTO.setRelation(object[40] != null ? object[40].toString() : null);
					empDTO.setState(object[41] != null ? object[41].toString() : null);
					empDTO.setUan(object[42] != null ? object[42].toString() : null);
					empDTO.setViewsOnOrganisation(object[43] != null ? object[43].toString() : null);
					empDTO.setYearOfPassing(object[44] != null ? Short.parseShort(object[44].toString()) : null);
					empDTO.setDepartmentId(object[45] != null ? Long.parseLong(object[45].toString()) : null);
					empDTO.setJobRoleName(object[46] != null ? object[46].toString() : null);
					empDTO.setDepartmentName(object[47] != null ? object[47].toString() : null);
					empDTO.setWorkLocation(object[48] != null ? object[48].toString() : null);
					empDTO.setProbationPeriod(object[49] != null ? Short.parseShort(object[49].toString()) : null);
					empDTO.setEmpId(object[50] != null ? Long.parseLong(object[50].toString()) : null);
					empDTO.setManagerName(object[51] != null ? object[51].toString() : null);
					empDTO.setExperience(object[52] != null ? object[52].toString() : null);
					empDTO.setBillable(object[53] != null ? object[53].toString() : null);
					empDTO.setChild1(object[54] != null ? (object[54].toString()) : null);
					empDTO.setChild2(object[55] != null ? (object[55].toString()) : null);
					empDTO.setChild3(object[56] != null ? (object[56].toString()) : null);
					empDTO.setMothersName(object[57] != null ? (object[57].toString()) : null);
					empDTO.setSpouse(object[58] != null ? (object[58].toString()) : null);
					empDTO.setTotalExperience(object[59] != null ? Float.parseFloat(object[59].toString()) : null);
					empDTO.setUpdatedOn(object[63] != null ? (object[63].toString()) : null);	
					empDTO.setCreatedByName(object[64] != null ? (object[64].toString()) : null);
					empDTO.setUpdatedByName(object[65] != null ? (object[65].toString()) : null);
					ServiceResponse completionResponse = employeeService.getEmployeeProfileCompletion(empDTO);
					EmployeeDTO emp = (EmployeeDTO) completionResponse.getServiceResponse();
					
					empDTO.setProfileCompletedPercent(emp != null ? emp.getProfileCompletedPercent() : 0.00);
					
					dtoList.add(empDTO);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee list is empty.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public StringBuilder createQueryForTimesheetReport(List<CustomFilterDTO> queryList) {
		StringBuilder query = new StringBuilder("");
		
			for(CustomFilterDTO dto: queryList) {
				if(dto.getOperator()!=null && dto.getOperator().equals("like")) {
					dto.setValue("%"+dto.getValue()+"%");
				}
				
				switch (dto.getColumn()) {
				case "Employee Id": {
					query = query.append(" e1.employeement_id ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Full Name": {
					query = query.append(" e1.name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Date": {
					query = query.append(" et.date ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Day Type": {
					query = query.append(" et.day_type ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Status": {
					query = query.append(" et.status ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Total Working Hour": {
					query = query.append(" et.total_time ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Team Name": {
					query = query.append(" t.team_name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Project Name": {
					query = query.append(" p.project_name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Client Name": {
					query = query.append(" p.client_name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "From Date": {
					query = query.append(" et.date ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "To Date": {
					query = query.append(" et.date ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Created On": {
					query = query.append(" et.created_on ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Updated On": {
					query = query.append(" et.updated_on ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Updated By": {
					query = query.append(" e2.name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Department": {	
					query = query.append(" d.name ").append(dto.getOperator() + " '")	
							.append(dto.getValue() + "' ").append(dto.getConjunction());	
					break;	
				}	
				case "Employment Status": {	
					query = query.append(" e1.employmentstatus ").append(dto.getOperator() + " '")	
							.append(dto.getValue() + "' ").append(dto.getConjunction());	
					break;	
				}
				default:
					break;
				}
			}
			return query;
     }
	
	 List<Object[]> getCustomTimesheetReport(String customQuery) {
		try {
			Session session = entityManager.unwrap(Session.class);
			
			try {
				String q="SELECT e1.employeement_id,e1.name employee, et.date, et.day_type, et.description, et.status, \n"
						+ "et.total_time, et.created_on, et.updated_on, e2.name statusUpdatedBy, t.team_name,p.project_name,p.client_name, et.office_in_time, et.office_out_time, et.total_working_hours \n"
						+ "FROM employee_timesheets et \n"
						+ "INNER JOIN employee e1 on et.emp_id = e1.emp_id \n"
						+ "LEFT JOIN employee e2 on et.timesheet_status_updated_by = e2.emp_id \n"
						+ "LEFT JOIN job_role jr on e1.job_role_id=jr.job_role_id \n"	
						+ "LEFT JOIN department d on jr.dept_id = d.dept_id \n"
						+ "LEFT JOIN employee_team_mapping etm on etm.emp_id = et.emp_id \n"
						+ "LEFT JOIN teams t on t.team_id = etm.team_id \n"
						+ "LEFT JOIN projects p on p.project_id = t.project_id where "+customQuery;
				
				System.out.println(q);
				Query query = session.createSQLQuery(q);
				System.out.println(query);
				System.out.println(query.getResultList() + " ====");
				return query.getResultList();
				
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				if(session!=null && session.isOpen()) {
					session.close();
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public ServiceResponse customTimesheetApplicationReport(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			StringBuilder subQuery = createQueryForTimesheetReport(timesheetDTO.getQueryList());
			List<Object[]> list = getCustomTimesheetReport(subQuery.toString());
			
			List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

			if (list != null) {
				list.forEach((object) -> {
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
					timesheetDto.setOfficeInTime(object[13] != null ? object[13].toString() : null);
					timesheetDto.setOfficeOutTime(object[14] != null ? object[14].toString() : null);
					timesheetDto.setTotalWorkingOfficeHours(object[15] != null ? object[15].toString() : null);
					dtoList.add(timesheetDto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet list is empty.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public StringBuilder createQueryForTimesheetSummaryChart(List<CustomFilterDTO> queryList) {
		StringBuilder query = new StringBuilder("");
		
			for(CustomFilterDTO dto: queryList) {
				if(dto.getOperator()!=null && dto.getOperator().equals("like")) {
					dto.setValue("%"+dto.getValue()+"%");
				}
				
				switch (dto.getColumn()) {
				case "Employee Id": {
					query = query.append(" e.employeement_id ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Full Name": {
					query = query.append(" e.name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Date": {
					query = query.append(" et.date ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Day Type": {
					query = query.append(" et.day_type ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Status": {
					query = query.append(" et.status ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Total Working Hour": {
					query = query.append(" et.total_time ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Team Name": {
					query = query.append(" t.team_name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Project Name": {
					query = query.append(" p.project_name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Client Name": {
					query = query.append(" p.client_name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "From Date": {
					query = query.append(" et.date ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "To Date": {
					query = query.append(" et.date ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Created On": {
					query = query.append(" et.created_on ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Updated On": {
					query = query.append(" et.updated_on ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Updated By": {
					query = query.append(" e2.name ").append(dto.getOperator() + " '")
							.append(dto.getValue() + "' ").append(dto.getConjunction());
					break;
				}
				case "Department": {	
					query = query.append(" d.name ").append(dto.getOperator() + " '")	
							.append(dto.getValue() + "' ").append(dto.getConjunction());	
					break;	
				}
				default:
					break;
				}
			}
			return query;
     }
	public StringBuilder createQueryForTimesheetSummaryChart1(List<CustomFilterDTO> queryList) {	
		StringBuilder query = new StringBuilder("");	
			
			for(CustomFilterDTO dto: queryList) {	
				if(dto.getOperator()!=null && dto.getOperator().equals("like")) {	
					dto.setValue("%"+dto.getValue()+"%");	
				}	
					
				switch (dto.getColumn()) {	
				case "Employee Id": {	
					query = query.append(" e.employeement_id ").append(dto.getOperator() + " '")	
							.append(dto.getValue() + "' ").append(dto.getConjunction());	
					break;	
				}	
				case "Full Name": {	
					query = query.append(" e.name ").append(dto.getOperator() + " '")	
							.append(dto.getValue() + "' ").append(dto.getConjunction());	
					break;	
				}	

				case "Team Name": {	
					query = query.append(" t.team_name ").append(dto.getOperator() + " '")	
							.append(dto.getValue() + "' ").append(dto.getConjunction());	
					break;	
				}	
				case "Project Name": {	
					query = query.append(" p.project_name ").append(dto.getOperator() + " '")	
							.append(dto.getValue() + "' ").append(dto.getConjunction());	
					break;	
				}	
				case "Client Name": {	
					query = query.append(" p.client_name ").append(dto.getOperator() + " '")	
							.append(dto.getValue() + "' ").append(dto.getConjunction());	
					break;	
				}	
	
				case "Updated By": {	
					query = query.append(" e2.name ").append(dto.getOperator() + " '")	
							.append(dto.getValue() + "' ").append(dto.getConjunction());	
					break;	
				}	
				case "Department": {	
					query = query.append(" d.name ").append(dto.getOperator() + " '")	
							.append(dto.getValue() + "' ").append(dto.getConjunction());	
					break;	
				}	
				default:	
					break;	
				}	
			}	
			return query;	
     }	
	
	public ServiceResponse getCustomTimesheetSummaryChart(String customQuery,String customQuery1, List<CustomFilterDTO> queryList) {
		ServiceResponse response = new ServiceResponse();
		
			Session session = entityManager.unwrap(Session.class);
			String fromDate = null;
			String toDate = null;
			
			for(CustomFilterDTO dto: queryList) {
				if(dto.getColumn().equals("From Date")) {
					fromDate = dto.getValue();
				}
				if(dto.getColumn().equals("To Date")) {
					toDate = dto.getValue();
				}
			}
			
			try {
									
				String q1="SELECT e.emp_id,count(*) filled_eod FROM employee_timesheets et "
						+ "INNER JOIN employee e ON e.emp_id = et.emp_id "
						+ "WHERE date between '"+fromDate+"' and '"+toDate+"' "
						+ " group by e.emp_id";
				Query query1 = session.createSQLQuery(q1);
				List<Object[]> timesheetList = query1.getResultList();
				
				

				String q2="SELECT et.status,e.employeement_id,e.name,d.name dept,e.email,e.mobile_no,date,total_time working_hours, "
						+ "et.day_type, e2.name manager, t.team_name,p.project_name,p.client_name "
						+ "FROM employee_timesheets et "
						+ "INNER JOIN employee e ON e.emp_id = et.emp_id "
						+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id "
						+ "INNER JOIN department d ON d.dept_id = jr.dept_id "
						+ "INNER JOIN employee e2 on e2.emp_id = e.manager_id "
						+ "LEFT JOIN employee_team_mapping etm on etm.emp_id = et.emp_id "
						+ "LEFT JOIN teams t on t.team_id = etm.team_id "
						+ "LEFT JOIN projects p on p.project_id = t.project_id where "+customQuery;
				
				System.out.println(q2);
				Query query2 = session.createSQLQuery(q2);
				List<Object[]> filledTimesheetList = query2.getResultList();
				
				
				LocalDate startDate = LocalDate.parse(fromDate);
				LocalDate endDate = LocalDate.parse(toDate);
				Long pendingEOdNumber = ChronoUnit.DAYS.between(startDate, endDate);

				List<TimesheetDTO> dtoList = new ArrayList<>();
				
				if(timesheetList != null) {
				if(!customQuery1.equalsIgnoreCase("")) {
				String empId = " SELECT distinct(e.emp_id),e.employeement_id,e.name,d.name as dept,e.email,e.mobile_no, e2.name as managerName,e.employmentstatus " 
						+ " FROM employee_timesheets et "
						+ " INNER JOIN employee e ON e.emp_id = et.emp_id "
						+ " INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id "
						+ " INNER JOIN department d ON d.dept_id = jr.dept_id " 
						+ " INNER JOIN employee e2 on e2.emp_id = e.manager_id "
						+ " LEFT JOIN employee_team_mapping etm on etm.emp_id = et.emp_id "
						+ " LEFT JOIN teams t on t.team_id = etm.team_id "
						+ " LEFT JOIN projects p on p.project_id = t.project_id "
						+ "where e.employmentstatus not like 'InActive' and "  +customQuery1;
				
				Query EmployeeId = session.createSQLQuery(empId);
				List<Object[]> employeeList= EmployeeId.getResultList();
				
				employeeList.forEach((employee) -> {
					TimesheetDTO dto = new TimesheetDTO();

					dto.setEmployeementId(employee[1] != null ? Long.parseLong(employee[1].toString()) : null);
					dto.setEmployeeName(employee[2] != null ? employee[2].toString() : null);
					dto.setDepartmentName(employee[3] != null ? employee[3].toString() : null);
					dto.setEmail(employee[4] != null ? employee[4].toString() : null);
					dto.setMobileNo(employee[5] != null ? Long.parseLong(employee[5].toString()) : null);
					dto.setManagerName(employee[6] != null ? employee[6].toString() : null);
					dto.setEmpId(employee[0] != null ? Long.parseLong(employee[0].toString()) : null);
					dto.setPendingEodCount(pendingEOdNumber);
					dto.setLegend("Pending By User");
					dto.setEmploymentstatus(employee[7] != null ? employee[7].toString() : null);

					timesheetList.forEach((timesheet) -> {

						Long timesheetEmpId = timesheet[0] != null ? Long.parseLong(timesheet[0].toString()) : null;
						Long employeeEmpId = employee[0] != null ? Long.parseLong(employee[0].toString()) : null;

						if (timesheetEmpId.equals(employeeEmpId)) {

							Long filledEodCount = timesheet[1] != null ? Long.parseLong(timesheet[1].toString()) : 0L;

							Long pendingEodCount = pendingEOdNumber - filledEodCount;

							dto.setPendingEodCount(pendingEodCount);

						}

					});
					dtoList.add(dto);
				});

				}else {
					List<Object[]> employeeList = employeeRepository.getAllEmployees();
					
					employeeList.forEach((employee) -> {
						TimesheetDTO dto = new TimesheetDTO();

						dto.setEmployeementId(employee[0] != null ? Long.parseLong(employee[0].toString()) : null);
						dto.setEmployeeName(employee[29] != null ? employee[29].toString() : null);
						dto.setDepartmentName(employee[47] != null ? employee[47].toString() : null);
						dto.setEmail(employee[14] != null ? employee[14].toString() : null);
						dto.setMobileNo(employee[27] != null ? Long.parseLong(employee[27].toString()) : null);
						dto.setManagerName(employee[51] != null ? employee[51].toString() : null);
						dto.setEmpId(employee[50] != null ? Long.parseLong(employee[50].toString()) : null);
						dto.setPendingEodCount(pendingEOdNumber);
						dto.setLegend("Pending By User");
						dto.setEmploymentstatus(employee[17] != null ? employee[17].toString() : null);

						timesheetList.forEach((timesheet) -> {

							Long timesheetEmpId = timesheet[0] != null ? Long.parseLong(timesheet[0].toString()) : null;
							Long employeeEmpId = employee[50] != null ? Long.parseLong(employee[50].toString()) : null;

							if (timesheetEmpId.equals(employeeEmpId)) {

								Long filledEodCount = timesheet[1] != null ? Long.parseLong(timesheet[1].toString()) : 0L;

								Long pendingEodCount = pendingEOdNumber - filledEodCount;

								dto.setPendingEodCount(pendingEodCount);

							}

						});
						dtoList.add(dto);
					});

				}
				
					if (filledTimesheetList != null) {
						filledTimesheetList.forEach((filledTimesheet) -> {
							TimesheetDTO dto = new TimesheetDTO();
							
							dto.setLegend(filledTimesheet[0] != null ? filledTimesheet[0].toString() : null);
							dto.setEmployeementId(filledTimesheet[1] != null ? Long.parseLong(filledTimesheet[1].toString()) : null);
							dto.setEmployeeName(filledTimesheet[2] != null ? filledTimesheet[2].toString() : null);
							dto.setDepartmentName(filledTimesheet[3] != null ? filledTimesheet[3].toString() : null);
							dto.setEmail(filledTimesheet[4] != null ? filledTimesheet[4].toString() : null);
							dto.setMobileNo(filledTimesheet[5] != null ? Long.parseLong(filledTimesheet[5].toString()) : null);
							dto.setDate(filledTimesheet[6] != null ? filledTimesheet[6].toString() : null);
							dto.setTotalWorkingHours(filledTimesheet[7] != null ? Float.parseFloat(filledTimesheet[7].toString()) : null);
							dto.setDayType(filledTimesheet[8] != null ? filledTimesheet[8].toString() : null);
							dto.setManagerName(filledTimesheet[9] != null ? filledTimesheet[9].toString() : null);
							dtoList.add(dto);
						});
					}

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet not found. Kindly check date range.");
				}
				
				}catch(Exception e) {
				e.printStackTrace();
				response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Something Went Wrong.");
				response.setServiceError(e.getMessage());
			}finally {
				if(session!=null && session.isOpen()) {
					session.close();
				}
			}
		return response;
	}


	public ServiceResponse customQueryForTimesheetSummaryChart(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			StringBuilder subQuery = createQueryForTimesheetSummaryChart(timesheetDTO.getQueryList());
			
			StringBuilder subQuery1 = createQueryForTimesheetSummaryChart1(timesheetDTO.getQueryList1());

			ServiceResponse timesheetSummaryLeaveResposne = getCustomTimesheetSummaryChart(subQuery.toString(),subQuery1.toString(), timesheetDTO.getQueryList());
			
			if(timesheetSummaryLeaveResposne.getServiceStatus().equals("Success")) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(timesheetSummaryLeaveResposne.getServiceResponse());
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(timesheetSummaryLeaveResposne.getServiceResponse());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	private StringBuilder createQueryForViewTimesheet(List<CustomFilterDTO> queryList) {
		StringBuilder query = new StringBuilder("");
		
		for(CustomFilterDTO dto: queryList) {
			if(dto.getOperator()!=null && dto.getOperator().equals("like")) {
				dto.setValue("%"+dto.getValue()+"%");
			}
			
			switch (dto.getColumn()) {
			case "Project Name": {
				query = query.append(" p.project_name ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			case "Client Name": {
				query = query.append(" p.client_name ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			case "From Date": {
				query = query.append(" et.date ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			case "To Date": {
				query = query.append(" et.date ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			default:
				break;
			}
		}
		return query;
	}
	
	List<Object[]> getCustomViewTimesheet(String customQuery, Long empId) {
		try {
			Session session = entityManager.unwrap(Session.class);

			try {
//				String q = "SELECT et.timesheet_id,et.date,et.day_type,e.name employeeName,et.description, et.total_time, et.status, "
//						+ "em.name created_by,et.created_on,et.emp_id, et.remarks "
//						+ "FROM employee_timesheets et "
//						+ "INNER JOIN employee e ON e.emp_id = et.emp_id "
//						+ "INNER JOIN employee em ON  et.created_by = em.emp_id "
//						+ "WHERE et.emp_id ="+ empId+" AND "+ customQuery;
				String q = "SELECT et.timesheet_id, et.date, et.day_type, map.completion_time, et.status, e.name, et.created_on, et.remarks "
						+ ", ac.activity, p.project_name,c.client_name "
						+ "FROM employee_timesheet_activities_mapping map "
						+ "LEFT JOIN activities ac ON ac.activity_id = map.activity_id "
						+ "LEFT JOIN teams t ON t.team_id = ac.team_id "
						+ "LEFT JOIN projects p ON p.project_id = t.project_id "
						+ "LEFT JOIN clients c ON c.client_id = p.client_id "
						+ "LEFT JOIN employee_timesheets et ON et.timesheet_id = map.timesheet_id "
						+ "LEFT JOIN employee e ON e.emp_id = et.emp_id "
						+ "LEFT JOIN employee e2 ON e2.emp_id = e.manager_id "
						+ "LEFT JOIN client_locations cl ON cl.client_location_id = map.client_location_id "
						+ "WHERE et.emp_id ="+ empId+" AND"+ customQuery;

				System.out.println(q);
				Query query = session.createSQLQuery(q);
				System.out.println(query);
				System.out.println(query.getResultList() + " ====");
				return query.getResultList();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (session != null && session.isOpen()) {
					session.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public ServiceResponse getCustomFilteredTimesheet(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			StringBuilder subQuery = createQueryForViewTimesheet(timesheetDTO.getQueryList());
			List<Object[]> list = getCustomViewTimesheet(subQuery.toString(), timesheetDTO.getEmpId());
			
			List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

			if (list != null) {
				list.forEach((object) -> {
					String activity = object[8] != null ? object[8].toString() : null;
					String project = object[9] != null ? object[9].toString() : null;
					String client = object[10] != null ? object[10].toString() : null;

					TimesheetDTO dto = new TimesheetDTO();
					dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setDate(object[1] != null ? object[1].toString() : null);
					dto.setDayType(object[2] != null ? object[2].toString() : null);
					dto.setTotalTime(object[3] != null ? Float.parseFloat(object[3].toString() ) : null);
					dto.setStatus(object[4] != null ? object[4].toString() : null);
					dto.setEmployeeName(object[5] != null ? object[5].toString() : null);
					dto.setCreatedOn(object[6] != null ? object[6].toString() : null);
					dto.setRemarks(object[7] != null ? object[7].toString() : null);
					dto.setDescription(object[8] != null ? object[8].toString() : null);
					
					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet list is empty.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getValueOptionData(CustomFilterDTO customFilterDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Object[]> allEmployeeList = employeeRepository.getAllEmployees();
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
			
			switch (customFilterDTO.getColumn()) {
			case "Employee Id": {
				if(!allEmployeeList.isEmpty()){
					allEmployeeList.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object[0] != null ? object[0].toString() : null);
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}
				break;
			}
			case "Full Name": {
				if(!allEmployeeList.isEmpty()){
					allEmployeeList.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object[29] != null ? object[29].toString() : null);
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}
				break;
			}
			case "Leave Type": {
				List<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findAll();
				if(!leaveType.isEmpty()){
					leaveType.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getLeaveType());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}
				break;
			}
			case "Team Name": {
				List<Team> teamObj = teamRepository.findAll();
				if(!teamObj.isEmpty()){
					teamObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getTeamName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}
				break;
			}
            case "Project Name": {
            	List<Project> projectObj = projectRepository.findAll();
            	if(!projectObj.isEmpty()){
            		projectObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getProjectName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}
				break;
			}
            case "Client Name": {
				List<Client> clientObj = clientsRepository.findAll();
				if(!clientObj.isEmpty()){
					clientObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getClientName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}
				break;
			}
            case "Department": {
				List<Department> departmentObj = departmentRepository.findAll();
				if(!departmentObj.isEmpty()){
					departmentObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}
				break;
			}
            // timesheet / leave status
            case "Status": {
            	String[] status = new String[]{"Pending", "Approved", "Rejected"};
            	for(String object: status) {
            		EmployeeDTO dto = new EmployeeDTO();
					dto.setName(object);
					dtoList.add(dto);
            	}
            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				break;
			}
            case "Manager Name": {
            	 List<Object[]> empObj = employeeRepository.getAllManagers();
            	 if(!empObj.isEmpty()){
            		 empObj.forEach((object) -> {
 						EmployeeDTO dto = new EmployeeDTO();
 						dto.setName(object[1] != null ? object[1].toString() : null);
 						dtoList.add(dto);
 					});
 					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
 					response.setServiceResponse(dtoList);
 				}
				break;
			}
            case "Job Role": {
				List<JobRole> jobRoleObj = jobRoleRepository.findAll();
				if(!jobRoleObj.isEmpty()){
					jobRoleObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}
				break;
			}
            case "Manager": {
				List<Object[]> empObj = employeeRepository.getAllManagers();
				if (!empObj.isEmpty()) {
					empObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object[1] != null ? object[1].toString() : null);
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}
				break;
			}
            case "Employment Status": {
            	String[] status = new String[]{"Probation", "Confirmed", "Resigned", "InActive"};
            	for(String object: status) {
            		EmployeeDTO dto = new EmployeeDTO();
					dto.setName(object);
					dtoList.add(dto);
            	}
            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				break;
			}
            case "Gender": {
            	String[] status = new String[]{"male", "female", "other"};
            	for(String object: status) {
            		EmployeeDTO dto = new EmployeeDTO();
					dto.setName(object);
					dtoList.add(dto);
            	}
            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				break;
			}
            case "Day Type": {
            	String[] status = new String[]{"Working", "Holiday", "Non-working"};
            	for(String object: status) {
            		EmployeeDTO dto = new EmployeeDTO();
					dto.setName(object);
					dtoList.add(dto);
            	}
            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				break;
			}
            case "Work Location": {
            	String[] state = new String[]{"Andaman & Nicobar Islands","Andhra Pradesh","Arunachal Pradesh","Assam","Bihar","Chandigarh","Chhattisgarh","Dadra and Nagar Haveli and  Daman & Diu","Delhi","Goa","Gujarat",
            	                                "Haryana","Himachal Pradesh","Jammu & Kashmir","Jharkhand","Karnataka","Kerala","Ladakh","Lakshadweep","Madhya Pradesh","Maharashtra","Manipur","Meghalaya","Mizoram",
            	                                "Nagaland","Odisha","Puducherry","Punjab","Rajasthan","Sikkim","Tamil Nadu","Telangana","Tripura","Uttar Pradesh","Uttarakhand","West Bengal"};
            	for(String object: state) {
            		EmployeeDTO dto = new EmployeeDTO();
					dto.setName(object);
					dtoList.add(dto);
            	}
            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
            }
            case "State": {
            	String[] state = new String[]{"Andaman & Nicobar Islands","Andhra Pradesh","Arunachal Pradesh","Assam","Bihar","Chandigarh","Chhattisgarh","Dadra and Nagar Haveli and  Daman & Diu","Delhi","Goa","Gujarat",
            	                                "Haryana","Himachal Pradesh","Jammu & Kashmir","Jharkhand","Karnataka","Kerala","Ladakh","Lakshadweep","Madhya Pradesh","Maharashtra","Manipur","Meghalaya","Mizoram",
            	                                "Nagaland","Odisha","Puducherry","Punjab","Rajasthan","Sikkim","Tamil Nadu","Telangana","Tripura","Uttar Pradesh","Uttarakhand","West Bengal"};
            	for(String object: state) {
            		EmployeeDTO dto = new EmployeeDTO();
					dto.setName(object);
					dtoList.add(dto);
            	}
            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
            }
			default:
				break;
			}
		}catch(Exception e) {
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
}

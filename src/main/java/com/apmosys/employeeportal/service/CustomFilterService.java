package com.apmosys.employeeportal.service;
import com.apmosys.employeeportal.util.EmployeeEmploymentIdUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import com.apmosys.employeeportal.dto.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Designation;
import com.apmosys.employeeportal.model.Domain;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.Specialization;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.DesignationRepository;
import com.apmosys.employeeportal.repository.DomainRepository;
import com.apmosys.employeeportal.repository.EmployeeAccessOverrideRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.ReportDashboardRepository;
import com.apmosys.employeeportal.repository.SpecializationRepository;
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
	private EmployeeAccessOverrideRepository employeeAccessOverrideRepository;

	@Autowired
	JobRoleRepository jobRoleRepository;

	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;

	@Autowired
	SpecializationRepository specializationRepository;

	@Autowired
	DomainRepository domainRepository;

	@Autowired
	DesignationRepository designationRepository;

	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;

	@Value("${spring.datasource.url}")
	private String dbURL;

	@Value("${spring.datasource.username}")
	private String dbUsername;

	@Value("${spring.datasource.password}")
	private String dbPassword;
	
	@Autowired // Keep your existing injections...
	 private ReportDashboardRepository reportDashboardRepository; 

public StringBuilder createQueryForLeaveReport(List<CustomFilterDTO> queryList) {
		StringBuilder query = new StringBuilder("");
		boolean hasFromDate = false;
	    boolean hasToDate = false;
	    boolean dateConditionAppended = false; 
		for (CustomFilterDTO dto : queryList) {
			if (dto.getOperator() != null && dto.getOperator().equals("like")) {
				dto.setValue("%" + dto.getValue() + "%");
			}

			switch (dto.getColumn()) {
//			case "Employee Id": {
//				query = query.append(" e.employeement_id ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction());
//				break;
//			}
			case "Employee Id":
			case "employmentID": {
				appendPrefixedEmployeeIdFilter(query, dto.getValue(), dto.getOperator(), dto.getConjunction(), false);
				break;
			}
			case "employeeType":
			case "Employee Type": {
				query.append(" ").append(buildEmployeeTypeFilterSql(dto.getValue())).append(" ")
						.append(dto.getConjunction());
				break;
			}
			
			case "Full Name": {
				query = query.append(" e.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Leave Type": {
				query = query.append(" ltm.leave_type ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}

			case "From Date": {
				query = query.append(" el.from_date ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				
				hasFromDate = true;
				if (dateConditionAppended) {
                    int startRemove = query.lastIndexOf("AND el.from_date >= CURRENT_DATE - 1");
                    int endRemove = query.lastIndexOf("AND el.to_date <= CURRENT_DATE");
                    if (startRemove != -1 && endRemove != -1) {
                        query.delete(startRemove, endRemove + "AND el.to_date <= CURRENT_DATE".length());
                    }
                    dateConditionAppended = false;
                }
				break;
			}
			case "To Date": {
				query = query.append(" el.to_date ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				
				hasToDate = true;
				if (dateConditionAppended) {
                    int startRemove = query.lastIndexOf("AND el.from_date >= CURRENT_DATE - 1");
                    int endRemove = query.lastIndexOf("AND el.to_date <= CURRENT_DATE");
                    if (startRemove != -1 && endRemove != -1) {
                        query.delete(startRemove, endRemove + "AND el.to_date <= CURRENT_DATE".length());
                    }
                    dateConditionAppended = false;
                }
				break;
			}
			case "No. of Days": {
				query = query.append(" el.no_of_days ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Reason": {
				query = query.append(" el.reason ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Status": {
				query = query.append(" ls.status ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Manager Name": {
				query = query.append(" e2.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Created On": {
				query = query.append(" el.created_on ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Updated On": {
				query = query.append(" el.updated_on ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Updated By": {
				query = query.append(" e3.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Team Name": {
				query = query.append(" t.team_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Project Name": {
				query = query.append(" p.project_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Client Name": {
				query = query.append(" p.client ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Department": {
				query = query.append(" d.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
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
			if (!hasFromDate && !hasToDate && !dateConditionAppended) {
		        query.append(" AND el.from_date >= CURRENT_DATE - 1 ");
		        query.append(" AND el.to_date <= CURRENT_DATE ");
		        dateConditionAppended = true;
		    }
		}
		return query;
	}

	public List<Object[]> getCustomLeaveReport(String customQuery,Long empId) {
		try {

			Session session = entityManager.unwrap(Session.class);
			 String deptList = departmentRepository.findAccessibleDeptIdsForEmp(empId);

			try {
				

				String q = "select e.employeement_id, e.name as employee, ltm.leave_type, el.from_date, el.to_date,el.no_of_days,el.reason, ls.status, e2.name as manager, el.created_on, "
						+ "el.updated_on, e3.name as statusUpdateBy,d.name department,t.team_name,p.project_name,el.from_date_day_type, el.to_date_day_type,e.is_consultant,e.is_apprenticeship, el.leave_status_updated_by, el.manager_id, el.emp_id ,e.is_apmosys_product from employee_leave el "
						+ "INNER JOIN employee e on el.emp_id = e.emp_id "
						+ "INNER JOIN leave_type_master ltm on el.leave_type_master_id = ltm.leave_type_master_id "
						+ "INNER JOIN leave_status ls on el.leave_status_id = ls.leave_status_id "
						+ "INNER JOIN employee e2 on el.manager_id = e2.emp_id "
						+ "LEFT JOIN employee e3 on el.leave_status_updated_by = e3.emp_id "
						+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id "
						+ "INNER JOIN department d ON d.dept_id = jr.dept_id "
						+ "LEFT JOIN employee_team_mapping etm on etm.emp_id = el.emp_id "
						+ "LEFT JOIN teams t on t.team_id = etm.team_id "
						+ "LEFT JOIN projects p on p.project_id = t.project_id where " + customQuery +"AND jr.dept_id IN (" + deptList + ")"
						+ " GROUP BY e.employeement_id, el.from_date";

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

	public ServiceResponse customQueryForLeaveReport(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();

		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("customQueryForLeaveReport");
		apiLogInfo.setApiUrl("/api/customQueryForLeaveReport");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("QueryList : " + leaveDTO.getQueryList().size());

		try {
			StringBuilder subQuery = createQueryForLeaveReport(leaveDTO.getQueryList());
			List<Object[]> list = getCustomLeaveReport(subQuery.toString(),leaveDTO.getEmpId());			
			System.out.println("SubQuery : "+subQuery);

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
					leavedto.setTeamName(object[13] != null ? object[13].toString() : null);
					leavedto.setProjectName(object[14] != null ? object[14].toString() : null);
					leavedto.setFromDateDayType(object[15] != null ? Float.parseFloat(object[15].toString()) : null);
					leavedto.setToDateDayType(object[16] != null ? Float.parseFloat(object[16].toString()) : null);
					leavedto.setIsConsultant(object[17] != null ? object[17].toString() : null);
					leavedto.setIsApprenticeship(object[18] != null ? object[18].toString() : null);
					leavedto.setLeaveStatusUpdatedBy(object[19] != null ? Long.parseLong(object[19].toString()) : null);
					leavedto.setManagerId(object[20] != null? Integer.parseInt(object[20].toString()) : null);
					leavedto.setEmpId(object[21] != null? Long.parseLong(object[21].toString()):null);
					leavedto.setIsApmosysProduct(object[22] != null ? object[22].toString() : null);
					applyLeaveDtoEmployeeDisplay(leavedto);

					dtoList.add(leavedto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse(" dtoList :" + dtoList);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("leave Application list is empty.");
				apiLogInfo.setApiResponse("leave Application list is empty");
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

	public ServiceResponse customQueryForLeaveTrendAnalysisReport(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("CustomQueryForLeaveTrendAnalysisReport");
		apiLogInfo.setApiUrl("/api/customQueryForLeaveTrendAnalysisReport");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("FromDate : " + leaveDTO.getFromDate() + " ,ToDate : " + leaveDTO.getToDate());
		try {
			ServiceResponse customQueryForLeaveReportResponse = customQueryForLeaveReport(leaveDTO);

			if (customQueryForLeaveReportResponse.getServiceStatus().equals("Success")) {
				List<LeaveDTO> leavedata = (List<LeaveDTO>) customQueryForLeaveReportResponse.getServiceResponse();

				List<LeaveDTO> newLeaveData = new ArrayList<>();

				if (leavedata != null) {
					leavedata.forEach((obj) -> {
						LocalDate tempdate = LocalDate.parse(obj.getFromDate());
						LocalDate toDate = LocalDate.parse(obj.getToDate());
						long i = 0L;

						while (i <= ChronoUnit.DAYS.between(tempdate, toDate)) {
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
					apiLogInfo.setApiResponse("NewLeaveData" + newLeaveData);
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

//	public StringBuilder createQueryForEmployeeReport(List<CustomFilterDTO> queryList) {
//		StringBuilder query = new StringBuilder("");
//		System.err.println(" queryList ::   " + queryList);
//		for (CustomFilterDTO dto : queryList) {
//			if (dto.getOperator() != null && dto.getOperator().equals("like")) {
//				dto.setValue("%" + dto.getValue() + "%");
//			}
//
//			switch (dto.getColumn()) {
//			case "Employee Id": {
//				query = query.append(" e.employeement_id ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Full Name": {
//				query = query.append("where e.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction()).append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Employment Status": {
//				query = query.append("where e.employmentstatus ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Date Of Joining": {
//				query = query.append("where e.date_of_joining ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "City": {
//				query = query.append("where e.city ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction()).append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Blood Group": {
//				query = query.append("where e.blood_group ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Gender": {
//				query = query.append("where e.gender ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction()).append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Work Location": {
//				query = query.append("where e.work_location ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Probation Period": {
//				query = query.append("where e.probation_period ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Notice Period": {
//				query = query.append("where e.notice_period ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Marital Status": {
//				query = query.append("where e.marital_status ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "State": {
//				query = query.append("where e.state ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction()).append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Bank Name": {
//				query = query.append("where e.bank_name ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Created By": {
//				Employee findEmployee = employeeRepository.findByName(dto.getValue());
//				dto.setValue(findEmployee.getEmpId().toString());
//				query = query.append("where e.created_by ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Created On": {
//				query = query.append("where e.created_on ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Department": {
//				query = query.append("where d.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction()).append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Job Role": {
//				query = query.append("where jr.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction()).append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Designation": {
//				query = query.append("where de.designation_name ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Manager": {
//				query = query.append("where e2.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction()).append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Experience": {
//				query = query.append("where e.total_experience ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Team Name": {
//				query = query.append("where t.team_name ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Project Name": {
//				query = query.append("where p.project_name ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Client Name": {
//				query = query.append("where p.client_name ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Domain": {
//				query = query.append("where dm.domain_name ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Specialization": {
//				query = query.append("where s.specialization_name ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Updated By": {
//				Employee findEmployee = employeeRepository.findByName(dto.getValue());
//				dto.setValue(findEmployee.getEmpId().toString());
//				query = query.append("where e.updated_by ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Updated On": {
//				query = query.append("where e.updated_on ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction())
//						.append("GROUP BY e.employeement_id");
//				;
//				break;
//			}
//			case "Profile Completion": {
//				query = query.append("GROUP BY e.employeement_id HAVING profile_completion_percentage ")
//						.append(dto.getOperator() + " '").append(dto.getValue() + "' ").append(dto.getConjunction());
//				break;
//			}
//			default:
//				break;
//			}
//		}
//		return query;
//	}
	
	public StringBuilder createQueryForEmployeeReport(List<CustomFilterDTO> queryList) {
		StringBuilder query = new StringBuilder("");
		System.err.println(" queryList ::   " + queryList);
		for (CustomFilterDTO dto : queryList) {
			if (dto.getOperator() != null && dto.getOperator().equals("like")) {
				dto.setValue("%" + dto.getValue() + "%");
			}

			switch (dto.getColumn()) {
//			case "Employee Id": {
//				query = query.append(" e.employeement_id ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction());
//				;
//				break;
//			}
			case "Employee Id":
			case "employmentID": {
				if (dto.getOperator() != null && dto.getOperator().equalsIgnoreCase("LIKE")) {
					appendFormattedEmploymentIdLikeFilter(query, dto.getValue(), "e");
				} else {
					appendPrefixedEmployeeIdFilter(query, dto.getValue(), dto.getOperator(), dto.getConjunction(), false);
				}
				break;
			}
			case "employeeType":
			case "Employee Type": {
				if (dto.getOperator() != null && dto.getOperator().equalsIgnoreCase("LIKE")) {
					query.append(" (").append(buildEmployeeTypeLikeFilterSql(dto.getValue(), "e")).append(") ");
				} else {
					query.append(" (").append(buildEmployeeTypeFilterSql(dto.getValue(), "e")).append(") ");
				}
				query.append(dto.getConjunction());
				break;
			}

			case "Full Name": {
				query = query.append(" e.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				;
				break;
			}
			case "Employment Status": {
				query = query.append(" e.employmentstatus ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Date Of Joining": {
				query = query.append(" e.date_of_joining ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "City": {
				query = query.append(" e.city ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				;
				break;
			}
			case "Blood Group": {
				query = query.append(" e.blood_group ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Gender": {
				query = query.append(" e.gender ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				;
				break;
			}
			case "Work Location": {
				query = query.append(" e.work_location ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Probation Period": {
				query = query.append(" e.probation_period ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Notice Period": {
				query = query.append(" e.notice_period ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Marital Status": {
				query = query.append(" where e.marital_status ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "State": {
				query = query.append(" e.state ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				;
				break;
			}
			case "Bank Name": {
				query = query.append(" e.bank_name ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Created By": {
				Employee findEmployee = employeeRepository.findByName(dto.getValue());
				dto.setValue(findEmployee.getEmpId().toString());
				query = query.append(" where e.created_by ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Created On": {
				query = query.append(" e.created_on ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Department": {
				query = query.append(" d.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				;
				break;
			}
			case "Job Role": {
				query = query.append(" jr.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				;
				break;
			}
			case "Designation": {
				query = query.append(" de.designation_name ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Manager": {
				query = query.append(" e2.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Experience": {
				query = query.append(" e.total_experience ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			case "Team Name": {
				query = query.append(" emp_proj_client.team_name ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			case "Project Name": {
				query = query.append("  emp_proj_client.project_name ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			
			case "Po No": {
				query = query.append("  emp_proj_client.po_no ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			case "Po Start Date": {
				query = query.append("  emp_proj_client.start_date ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			case "Po End Date": {
				query = query.append("  emp_proj_client.end_date ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			case "Po Project Type": {
				query = query.append("  emp_proj_client.po_project_type ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			
			case "Client Name": {
				query = query.append(" emp_proj_client.client_name ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
//			case "Domain": {
//				query = query.append(" dm.domain_name ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction());
//				;
//				break;
//			}
//			case "Specialization": {
//				query = query.append(" s.specialization_name ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction());
//				;
//				break;
//			}
			case "Updated By": {
				Employee findEmployee = employeeRepository.findByName(dto.getValue());
				dto.setValue(findEmployee.getEmpId().toString());
				query = query.append(" e.updated_by ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Updated On": {
				query = query.append(" e.updated_on ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				;
				break;
			}
			case "Profile Completion": {
				query = query.append(" GROUP BY e.employeement_id HAVING profile_completion_percentage ")
						.append(dto.getOperator() + " '").append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			default:
				break;
			}
		}
		return query;
	}

	List<Object[]> getCustomEmployeeReport(String customQuery,Long empId, String subFeatureName) {
		try {
			Session session = entityManager.unwrap(Session.class);
			String q;
          
            try {
//				String q="SELECT e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode,e.bank_name, e.blood_group, e.city, e.country, e.created_by, e.created_on, e.date_of_birth,\n"
//						+ "e.date_of_joining, e.email, e.emergency_contact_mobile, e.emergency_contact_person,\n"
//						+ "e.employmentstatus, e.esic_number, e.father_name, e.gender, e.graduation_type, e.pursuing,\n"
//						+ "e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, e.mother_tongue, e.name,\n"
//						+ "e.notice_period, e.alternate_mobile_no,  e.pan_number, e.passport_number,\n"
//						+ "e.permanent_address, e.pf_account_number, e.pincode, e.place_of_birth, e.passing_grade,\n"
//						+ "e.previous_pf_account_number, e.relation, e.state, e.uan,\n"
//						+ "e.views_on_organisation, e.year_of_passing,\n"
//						+ "jr.dept_id, jr.name as jobrolename,\n"
//						+ "d.name as departmentname, e.work_location, e.probation_period, e.emp_id, e2.name as manager, e.experience, \n"
//						+ "e.billable,e.child1,e.child2,e.child3,e.mothers_name,e.spouse,e.total_experience,t.team_name,p.project_name,p.client_name, e.updated_on,e4.name as createdByName, e3.name as updatedByName \n"
//						+ "FROM employee e \n"
//						+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
//						+ "INNER JOIN department d ON d.dept_id = jr.dept_id \n"
//						+ "INNER JOIN employee e2 ON e.manager_id = e2.emp_id \n"
//						+ "LEFT JOIN employee e3 ON e.updated_by = e3.emp_id \n"
//						+ "LEFT JOIN employee_team_mapping etm on etm.emp_id = e.emp_id \n"
//						+ "LEFT JOIN teams t on t.team_id = etm.team_id \n"
//						+ "LEFT JOIN employee e4 on e.created_by = e4.emp_id \n"
//						+ "LEFT JOIN projects p on p.project_id = t.project_id where "+customQuery;

//				String q = "SELECT e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode,e.bank_name, e.blood_group, e.city, e.country, e.created_by, e.created_on, e.date_of_birth,\n"
//						+ "e.date_of_joining, e.email, e.emergency_contact_mobile, e.emergency_contact_person,\n"
//						+ "e.employmentstatus, e.esic_number, e.father_name, e.gender, e.graduation_type, e.pursuing,\n"
//						+ "e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, e.mother_tongue, e.name,\n"
//						+ "e.notice_period, e.alternate_mobile_no,  e.pan_number, e.passport_number,\n"
//						+ "e.permanent_address, e.pf_account_number, e.pincode, e.place_of_birth, e.passing_grade,\n"
//						+ "e.previous_pf_account_number, e.relation, e.state, e.uan,\n"
//						+ "e.views_on_organisation, e.year_of_passing,\n" + "jr.dept_id, jr.name as jobrolename,\n"
//						+ "d.name as departmentname, e.work_location, e.probation_period, e.emp_id, e2.name as manager, e.experience,\n"
//						+ "e.billable,e.child1,e.child2,e.child3,e.mothers_name,e.spouse,e.total_experience,t.team_name,p.project_name,p.client_name, e.updated_on,e4.name as createdByName, e3.name as updatedByName,\n"
//						+ "s.specialization_name,dm.domain_name,e.designation_id,de.designation_name,e.updated_by,e.billable_type,\n"
//						+ "(SELECT\n" + "    COUNT(*) * 100.0 / NULLIF(COUNT(*), 0)\n" + "   FROM\n"
//						+ "    employee e_profile\n" + "   WHERE\n"
//						+ "    e_profile.emp_id = e.emp_id) AS profile_completion_percentage \n" + "FROM employee e\n"
//						+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id\n"
//						+ "INNER JOIN department d ON d.dept_id = jr.dept_id\n"
//						+ "INNER JOIN employee e2 ON e.manager_id = e2.emp_id\n"
//						+ "LEFT JOIN employee e3 ON e.updated_by = e3.emp_id\n"
//						+ "LEFT JOIN employee_team_mapping etm on etm.emp_id = e.emp_id\n"
//						+ "LEFT JOIN teams t on t.team_id = etm.team_id\n"
//						+ "LEFT JOIN employee e4 on e.created_by = e4.emp_id\n"
//						+ "LEFT JOIN projects p on p.project_id = t.project_id\n"
//						+ "LEFT JOIN employee_specialization_map esm ON esm.emp_id = e.emp_id\n"
//						+ "LEFT JOIN specialization s ON s.specialization_id = esm.specialization_id\n"
//						+ "LEFT JOIN domain dm ON dm.domain_id = s.domain_id\n"
//						+ "LEFT JOIN designation de ON de.designation_id = e.designation_id  " + customQuery;
				
				// String q = "SELECT e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode,e.bank_name, e.blood_group, e.city, e.country, e.created_by, e.created_on, e.date_of_birth,\n"
				// 		+ "e.date_of_joining, e.email, e.emergency_contact_mobile, e.emergency_contact_person,\n"
				// 		+ "e.employmentstatus, e.esic_number, e.father_name, e.gender, e.graduation_type, e.pursuing,\n"
				// 		+ "e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, e.mother_tongue, e.name,\n"
				// 		+ "e.notice_period, e.alternate_mobile_no,  e.pan_number, e.passport_number,\n"
				// 		+ "e.permanent_address, e.pf_account_number, e.pincode, e.place_of_birth, e.passing_grade,\n"
				// 		+ "e.previous_pf_account_number, e.relation, e.state, e.uan,\n"
				// 		+ "e.views_on_organisation, e.year_of_passing,\n" + "jr.dept_id, jr.name as jobrolename,\n"
				// 		+ "d.name as departmentname, e.work_location, e.probation_period, e.emp_id, e2.name as manager, e.experience,\n"
				// 		+ "e.billable,e.child1,e.child2,e.child3,e.mothers_name,e.spouse,e.total_experience,emp_proj_client.project_name, emp_proj_client.client_name, emp_proj_client.project_id, e.updated_on,e4.name as createdByName, e3.name as updatedByName,\n"
				// 		+ "e.designation_id,de.designation_name,e.updated_by,e.billable_type,emp_proj_client.team_name, e.is_consultant, e.is_apprenticeship, \n"
				// 		+ "(SELECT COUNT(*) * 100.0 / NULLIF(COUNT(*), 0) FROM\n"
				// 		+ "employee e_profile WHERE\n"
				// 		+ "e_profile.emp_id = e.emp_id) AS profile_completion_percentage \n" + "FROM employee e\n"
//				String q = "SELECT e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode,e.bank_name, e.blood_group, e.city, e.country, e.created_by, e.created_on, e.date_of_birth,\n"
//						+ "e.date_of_joining, e.email, e.emergency_contact_mobile, e.emergency_contact_person,\n"
//						+ "e.employmentstatus, e.esic_number, e.father_name, e.gender, e.graduation_type, e.pursuing,\n"
//						+ "e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, e.mother_tongue, e.name,\n"
//						+ "e.notice_period, e.alternate_mobile_no,  e.pan_number, e.passport_number,\n"
//						+ "e.permanent_address, e.pf_account_number, e.pincode, e.place_of_birth, e.passing_grade,\n"
//						+ "e.previous_pf_account_number, e.relation, e.state, e.uan,\n"
//						+ "e.views_on_organisation, e.year_of_passing,\n" + "jr.dept_id, jr.name as jobrolename,\n"
//						+ "d.name as departmentname, e.work_location, e.probation_period, e.emp_id, e2.name as manager, e.experience,\n"
//						+ "e.billable,e.child1,e.child2,e.child3,e.mothers_name,e.spouse,e.total_experience,emp_proj_client.project_name, emp_proj_client.client_name, e.updated_on,e4.name as createdByName, e3.name as updatedByName,\n"
//						+ "e.designation_id,de.designation_name,e.updated_by,e.billable_type,emp_proj_client.team_name, e.is_consultant, e.is_apprenticeship,emp_proj_client.project_id,emp_proj_client.project_name, \n"
//						+ "       emp_proj_client.po_no, emp_proj_client.po_start_date, emp_proj_client.po_end_date, \n"
//						+ "       emp_proj_client.po_project_type, \n"
//						+ "(SELECT COUNT(*) * 100.0 / NULLIF(COUNT(*), 0) FROM\n"
//						+ "employee e_profile WHERE\n"
//						+ "e_profile.emp_id = e.emp_id) AS profile_completion_percentage \n" + "FROM employee e\n"
//						+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id\n"
//						+ "INNER JOIN department d ON d.dept_id = jr.dept_id\n"
//						+ "INNER JOIN employee e2 ON e.manager_id = e2.emp_id\n"
//						+ "LEFT JOIN employee e3 ON e.updated_by = e3.emp_id\n"
//						+ "LEFT JOIN designation de ON de.designation_id = e.designation_id \n"
//						+ "LEFT JOIN employee e4 on e.created_by = e4.emp_id\n"
//						+ "LEFT JOIN (SELECT etm.emp_id,GROUP_CONCAT(DISTINCT pr.project_id ORDER BY pr.project_id SEPARATOR ',') AS project_id,GROUP_CONCAT(DISTINCT pr.project_name ORDER BY pr.project_id SEPARATOR ',') AS project_name, GROUP_CONCAT(DISTINCT cl.client_name ORDER BY pr.project_id SEPARATOR ',') AS client_name, GROUP_CONCAT(DISTINCT t.team_name ORDER BY pr.project_id SEPARATOR ',') AS team_name,GROUP_CONCAT(DISTINCT pr.po_no ORDER BY pr.project_id SEPARATOR ',') AS po_no,GROUP_CONCAT(DISTINCT pr.po_start_date ORDER BY pr.project_id SEPARATOR ',') AS po_start_date,GROUP_CONCAT(DISTINCT pr.po_end_date ORDER BY pr.project_id SEPARATOR ',') AS po_end_date,GROUP_CONCAT(DISTINCT pr.po_project_type ORDER BY pr.project_id SEPARATOR ',') AS po_project_type \n"
//						+ "FROM employee_team_mapping etm \n"
//						+ "LEFT JOIN teams t ON t.team_id = etm.team_id \n"
//						+ "LEFT JOIN projects_temp pr ON pr.project_id = t.project_id \n"
//						+ "LEFT JOIN clients cl ON cl.client_id = pr.client_id \n"
//						+ "WHERE etm.active != 0 "
//						+ "AND t.is_active != 'N' \n"
//						+ "AND pr.active != 'false'\n"
//						+ "GROUP BY etm.emp_id) emp_proj_client ON emp_proj_client.emp_id = e.emp_id  where " + customQuery;
					if (empId != null) {
						String deptList = departmentRepository.findAccessibleDeptIdsForEmp(empId);
						if (subFeatureName != null) {
							List<GetDeptIdByRoleDTO> overrideDepts = employeeAccessOverrideRepository.findActiveDeptIdsAndSubFeatureNameByEmpId(empId, subFeatureName);
							
							if (overrideDepts != null && !overrideDepts.isEmpty()) {
								List<Long> overrideDeptIds = overrideDepts.stream().map(GetDeptIdByRoleDTO::getDeptId)
										.filter(Objects::nonNull).collect(Collectors.toList());
								if (deptList != null && !deptList.isEmpty()) {
									deptList = deptList + "," + overrideDeptIds.stream().map(String::valueOf).collect(Collectors.joining(","));
								} else {
									deptList = overrideDeptIds.stream().map(String::valueOf).collect(Collectors.joining(","));
								}
							}
						}

				 q = "SELECT \n"
				 		+ "    e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode, \n"
						+ "    e.bank_name, e.blood_group, e.city, e.country, e.created_by, e.created_on, e.date_of_birth,\n"
						+ "    e.date_of_joining, e.email, e.emergency_contact_mobile, e.emergency_contact_person,\n"
						+ "    e.employmentstatus, e.esic_number, e.father_name, e.gender, e.graduation_type, e.pursuing,\n"
						+ "    e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, e.mother_tongue, e.name,\n"
						+ "    e.notice_period, e.alternate_mobile_no, e.pan_number, e.passport_number,\n"
						+ "    e.permanent_address, e.pf_account_number, e.pincode, e.place_of_birth, e.passing_grade,\n"
						+ "    e.previous_pf_account_number, e.relation, e.state, e.uan,\n"
						+ "    e.views_on_organisation, e.year_of_passing, jr.dept_id, jr.name AS jobrolename,\n"
						+ "    d.name AS departmentname, e.work_location, e.probation_period, e.emp_id, e2.name AS manager, \n"
						+ "    e.experience, e.billable, e.child1, e.child2, e.child3, e.mothers_name, e.spouse, \n"
						+ "    e.total_experience, emp_proj_client.project_name, emp_proj_client.client_name, emp_proj_client.project_id, e.updated_on, \n"
						+ "    e4.name AS createdByName, e3.name AS updatedByName, e.designation_id, de.designation_name, \n"
						+ "    e.updated_by, e.billable_type, emp_proj_client.team_name,e.is_consultant, e.is_apprenticeship, \n"  // Added comma here
						+ "    emp_proj_client.po_no, \n"
						+ "    emp_proj_client.start_date, emp_proj_client.end_date, emp_proj_client.po_project_type, \n"
						+ " \n"
						+ "    (SELECT COUNT(*) * 100.0 / NULLIF(COUNT(*), 0) \n"
						+ "     FROM employee e_profile \n"
						+ "     WHERE e_profile.emp_id = e.emp_id) AS profile_completion_percentage,e.is_apmosys_product \n"
						+ "\n"
						+ "FROM employee e\n"
						+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id\n"
						+ "INNER JOIN department d ON d.dept_id = jr.dept_id\n"
						+ "INNER JOIN employee e2 ON e.manager_id = e2.emp_id\n"
						+ "LEFT JOIN employee e3 ON e.updated_by = e3.emp_id\n"
						+ "LEFT JOIN designation de ON de.designation_id = e.designation_id \n"
						+ "LEFT JOIN employee e4 ON e.created_by = e4.emp_id\n"
						+ "LEFT JOIN (\n"
						+ "    SELECT \n"
						+ "        etm.emp_id,\n"
						+ "        GROUP_CONCAT(DISTINCT pr.project_id ORDER BY pr.project_id SEPARATOR ',') AS project_id,\n"
						+ "        GROUP_CONCAT(DISTINCT pr.project_name ORDER BY pr.project_id SEPARATOR ',') AS project_name, \n"
						+ "        GROUP_CONCAT(DISTINCT cl.client_name ORDER BY pr.project_id SEPARATOR ',') AS client_name,  \n"
						+ "        GROUP_CONCAT(DISTINCT t.team_name ORDER BY pr.project_id SEPARATOR ',') AS team_name,\n"
						+ "        GROUP_CONCAT(DISTINCT ppd.po_no ORDER BY pr.project_id SEPARATOR ',') AS po_no,\n"
						+ "        GROUP_CONCAT(DISTINCT pr.start_date ORDER BY pr.project_id SEPARATOR ',') AS start_date,\n"
						+ "        GROUP_CONCAT(DISTINCT pr.end_date ORDER BY pr.project_id SEPARATOR ',') AS end_date,\n"
						+ "        GROUP_CONCAT(DISTINCT pr.po_project_type ORDER BY pr.project_id SEPARATOR ',') AS po_project_type \n"
						+ "    FROM employee_team_mapping etm \n"
						+ "    LEFT JOIN teams t ON t.team_id = etm.team_id \n"
						+ "    LEFT JOIN projects pr ON pr.project_id = t.project_id \n"
						+ "LEFT JOIN project_po_details ppd \n"
						+ "ON ppd.project_id = pr.project_id and ppd.active = true \n"
   						+ "AND ppd.po_start_date <= current_timestamp \n" 
						+ "AND (ppd.po_end_date IS NULL OR ppd.po_end_date >= current_timestamp)\n"
						+ "    LEFT JOIN clients cl ON cl.client_id = pr.client_id \n"
						+ "    WHERE etm.active != 0 \n"
						+ "      AND t.is_active != 'N' \n"
						+ "      AND pr.active != 'false'\n"
						+ "    GROUP BY etm.emp_id\n"
						+ ") emp_proj_client ON emp_proj_client.emp_id = e.emp_id where " + customQuery +"AND jr.dept_id IN (" + deptList + ")";
				}else {
					 q = "SELECT \n"
								+ "    e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode, \n"
								+ "    e.bank_name, e.blood_group, e.city, e.country, e.created_by, e.created_on, e.date_of_birth,\n"
								+ "    e.date_of_joining, e.email, e.emergency_contact_mobile, e.emergency_contact_person,\n"
								+ "    e.employmentstatus, e.esic_number, e.father_name, e.gender, e.graduation_type, e.pursuing,\n"
								+ "    e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, e.mother_tongue, e.name,\n"
								+ "    e.notice_period, e.alternate_mobile_no, e.pan_number, e.passport_number,\n"
								+ "    e.permanent_address, e.pf_account_number, e.pincode, e.place_of_birth, e.passing_grade,\n"
								+ "    e.previous_pf_account_number, e.relation, e.state, e.uan,\n"
								+ "    e.views_on_organisation, e.year_of_passing, jr.dept_id, jr.name AS jobrolename,\n"
								+ "    d.name AS departmentname, e.work_location, e.probation_period, e.emp_id, e2.name AS manager, \n"
								+ "    e.experience, e.billable, e.child1, e.child2, e.child3, e.mothers_name, e.spouse, \n"
								+ "    e.total_experience, emp_proj_client.project_name, emp_proj_client.client_name,emp_proj_client.project_id, e.updated_on, \n"
								+ "    e4.name AS createdByName, e3.name AS updatedByName, e.designation_id, de.designation_name, \n"
								+ "    e.updated_by, e.billable_type, emp_proj_client.team_name, e.is_consultant, e.is_apprenticeship, \n"
								+ "    emp_proj_client.po_no, \n"
								+ "    emp_proj_client.start_date, emp_proj_client.end_date, emp_proj_client.po_project_type, \n"
								+ "\n"
								+ "    (SELECT COUNT(*) * 100.0 / NULLIF(COUNT(*), 0) \n"
								+ "     FROM employee e_profile \n"
								+ "     WHERE e_profile.emp_id = e.emp_id) AS profile_completion_percentage ,e.is_apmosys_product\n"
								+ "\n"
								+ "FROM employee e\n"
								+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id\n"
								+ "INNER JOIN department d ON d.dept_id = jr.dept_id\n"
								+ "INNER JOIN employee e2 ON e.manager_id = e2.emp_id\n"
								+ "LEFT JOIN employee e3 ON e.updated_by = e3.emp_id\n"
								+ "LEFT JOIN designation de ON de.designation_id = e.designation_id \n"
								+ "LEFT JOIN employee e4 ON e.created_by = e4.emp_id\n"
								+ "LEFT JOIN (\n"
								+ "    SELECT \n"
								+ "        etm.emp_id,\n"
								+ "        GROUP_CONCAT(DISTINCT pr.project_id ORDER BY pr.project_id SEPARATOR ',') AS project_id,\n"
								+ "        GROUP_CONCAT(DISTINCT pr.project_name ORDER BY pr.project_id SEPARATOR ',') AS project_name, \n"
								+ "        GROUP_CONCAT(DISTINCT cl.client_name ORDER BY pr.project_id SEPARATOR ',') AS client_name,  \n"
								+ "        GROUP_CONCAT(DISTINCT t.team_name ORDER BY pr.project_id SEPARATOR ',') AS team_name,\n"
								+ "        GROUP_CONCAT(DISTINCT pr.po_no ORDER BY pr.project_id SEPARATOR ',') AS po_no,\n"
								+ "        GROUP_CONCAT(DISTINCT pr.start_date ORDER BY pr.project_id SEPARATOR ',') AS start_date,\n"
								+ "        GROUP_CONCAT(DISTINCT pr.end_date ORDER BY pr.project_id SEPARATOR ',') AS end_date,\n"
								+ "        GROUP_CONCAT(DISTINCT pr.po_project_type ORDER BY pr.project_id SEPARATOR ',') AS po_project_type \n"
								+ "    FROM employee_team_mapping etm \n"
								+ "    LEFT JOIN teams t ON t.team_id = etm.team_id \n"
								+ "    LEFT JOIN projects pr ON pr.project_id = t.project_id \n"
								+ "LEFT JOIN project_po_details ppd \n"
								+ "ON ppd.project_id = pr.project_id and ppd.active = true \n"
   								+ "AND ppd.po_start_date <= current_timestamp \n" 
								+ "AND (ppd.po_end_date IS NULL OR ppd.po_end_date >= current_timestamp ) \n"
								+ "    LEFT JOIN clients cl ON cl.client_id = pr.client_id \n"
								+ "    WHERE etm.active != 0 \n"
								+ "      AND t.is_active != 'N' \n"
								+ "      AND pr.active != 'false'\n"
								+ "    GROUP BY etm.emp_id\n"
								+ ") emp_proj_client ON emp_proj_client.emp_id = e.emp_id where " + customQuery ;
				}
				
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
	
/*
 * added by anurag dashboard for billable/non-billable
 */
	
	List<Object[]> getDepartmentWiseBillableEmployeeReport(List<Long> customQuery) {
		try {
			Session session = entityManager.unwrap(Session.class);
			
			System.out.println(" customQuery list  "+customQuery);
			String ids = customQuery.toString();
			String deptIds = ids.substring(1,ids.length()-1);
			try {


//				String q = "SELECT e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode,e.bank_name, e.blood_group, e.city, e.country, e.created_by, e.created_on, e.date_of_birth,\n"
//						+ "e.date_of_joining, e.email, e.emergency_contact_mobile, e.emergency_contact_person,\n"
//						+ "e.employmentstatus, e.esic_number, e.father_name, e.gender, e.graduation_type, e.pursuing,\n"
//						+ "e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, e.mother_tongue, e.name,\n"
//						+ "e.notice_period, e.alternate_mobile_no,  e.pan_number, e.passport_number,\n"
//						+ "e.permanent_address, e.pf_account_number, e.pincode, e.place_of_birth, e.passing_grade,\n"
//						+ "e.previous_pf_account_number, e.relation, e.state, e.uan,\n"
//						+ "e.views_on_organisation, e.year_of_passing,\n" + "jr.dept_id, jr.name as jobrolename,\n"
//						+ "d.name as departmentname, e.work_location, e.probation_period, e.emp_id, e2.name as manager, e.experience,\n"
//						+ "e.billable,e.child1,e.child2,e.child3,e.mothers_name,e.spouse,e.total_experience,emp_proj_client.project_name, emp_proj_client.client_name, e.updated_on,e4.name as createdByName, e3.name as updatedByName,\n"
//						+ "e.designation_id,de.designation_name,e.updated_by,e.billable_type,e.is_consultant,is_apprenticeship \n"
//						+ "(SELECT COUNT(*) * 100.0 / NULLIF(COUNT(*), 0) FROM\n"
//						+ "employee e_profile WHERE\n"
//						+ "e_profile.emp_id = e.emp_id) AS profile_completion_percentage \n" + "FROM employee e\n"
//						+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id\n"
//						+ "INNER JOIN department d ON d.dept_id = jr.dept_id\n"
//						+ "INNER JOIN employee e2 ON e.manager_id = e2.emp_id\n"
//						+ "LEFT JOIN employee e3 ON e.updated_by = e3.emp_id\n"
//						+ "LEFT JOIN designation de ON de.designation_id = e.designation_id \n"
//						+ "LEFT JOIN employee e4 on e.created_by = e4.emp_id\n"
//						+ "LEFT JOIN (SELECT etm.emp_id, GROUP_CONCAT(DISTINCT pr.project_name) AS project_name, GROUP_CONCAT(DISTINCT cl.client_name) AS client_name \n"
//						+ "FROM employee_team_mapping etm \n"
//						+ "LEFT JOIN teams t ON t.team_id = etm.team_id \n"
//						+ "LEFT JOIN projects pr ON pr.project_id = t.project_id \n"
//						+ "LEFT JOIN clients cl ON cl.client_id = pr.client_id \n"
//						+ "GROUP BY etm.emp_id) emp_proj_client ON emp_proj_client.emp_id = e.emp_id  where d.dept_id IN (" + deptIds + ") GROUP BY \n"
//								+ "    e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode, e.bank_name, e.blood_group, \n"
//								+ "    e.city, e.country, e.created_by, e.created_on, e.date_of_birth, e.date_of_joining, e.email, \n"
//								+ "    e.emergency_contact_mobile, e.emergency_contact_person, e.employmentstatus, e.esic_number, e.father_name, \n"
//								+ "    e.gender, e.graduation_type, e.pursuing, e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, \n"
//								+ "    e.mother_tongue, e.name, e.notice_period, e.alternate_mobile_no, e.pan_number, e.passport_number, \n"
//								+ "    e.permanent_address, e.pf_account_number, e.pincode, e.place_of_birth, e.passing_grade, e.previous_pf_account_number, \n"
//								+ "    e.relation, e.state, e.uan, e.views_on_organisation, e.year_of_passing, jr.dept_id, jr.name, \n"
//								+ "    d.name, e.work_location, e.probation_period, e.emp_id, e2.name, e.experience, \n"
//								+ "    e.billable, e.child1, e.child2, e.child3, e.mothers_name, e.spouse, e.total_experience, emp_proj_client.project_name, \n"
//								+ "    emp_proj_client.client_name, e.updated_on, e4.name, e3.name, \n"
//								+ "    e.designation_id, de.designation_name, e.updated_by, e.billable_type,e.is_consultant,is_apprenticeship";
//
				  
				
				String q = 
					    "SELECT e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode, e.bank_name, e.blood_group, " +
					    "e.city, e.country, e.created_by, e.created_on, e.date_of_birth, e.date_of_joining, e.email, e.emergency_contact_mobile, " +
					    "e.emergency_contact_person, e.employmentstatus, e.esic_number, e.father_name, e.gender, e.graduation_type, e.pursuing, " +
					    "e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, e.mother_tongue, e.name, e.notice_period, " +
					    "e.alternate_mobile_no, e.pan_number, e.passport_number, e.permanent_address, e.pf_account_number, e.pincode, " +
					    "e.place_of_birth, e.passing_grade, e.previous_pf_account_number, e.relation, e.state, e.uan, e.views_on_organisation, " +
					    "e.year_of_passing, jr.dept_id, jr.name AS jobrolename, d.name AS departmentname, e.work_location, e.probation_period, " +
					    "e.emp_id, e2.name AS manager, e.experience, e.billable, e.child1, e.child2, e.child3, e.mothers_name, e.spouse, " +
					    "e.total_experience, emp_proj_client.project_name, emp_proj_client.client_name, e.updated_on, e4.name AS createdByName, " +
					    "e3.name AS updatedByName, e.designation_id, de.designation_name, e.updated_by, e.billable_type, e.is_consultant, " +
					    "e.is_apprenticeship, " +
					    "(SELECT COUNT(*) * 100.0 / NULLIF(COUNT(*), 0) FROM employee e_profile WHERE e_profile.emp_id = e.emp_id) " +
					    "AS profile_completion_percentage " +
					    "FROM employee e " +
					    "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id " +
					    "INNER JOIN department d ON d.dept_id = jr.dept_id " +
					    "INNER JOIN employee e2 ON e.manager_id = e2.emp_id " +
					    "LEFT JOIN employee e3 ON e.updated_by = e3.emp_id " +
					    "LEFT JOIN designation de ON de.designation_id = e.designation_id " +
					    "LEFT JOIN employee e4 ON e.created_by = e4.emp_id " +
					    "LEFT JOIN (SELECT etm.emp_id, GROUP_CONCAT(DISTINCT pr.project_name) AS project_name, " +
					    "GROUP_CONCAT(DISTINCT cl.client_name) AS client_name FROM employee_team_mapping etm " +
					    "LEFT JOIN teams t ON t.team_id = etm.team_id " +
					    "LEFT JOIN projects pr ON pr.project_id = t.project_id " +
					    "LEFT JOIN clients cl ON cl.client_id = pr.client_id GROUP BY etm.emp_id) emp_proj_client " +
					    "ON emp_proj_client.emp_id = e.emp_id " +
					    "WHERE d.dept_id IN (" + deptIds + ") " +
					    "GROUP BY e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode, e.bank_name, " +
					    "e.blood_group, e.city, e.country, e.created_by, e.created_on, e.date_of_birth, e.date_of_joining, e.email, " +
					    "e.emergency_contact_mobile, e.emergency_contact_person, e.employmentstatus, e.esic_number, e.father_name, " +
					    "e.gender, e.graduation_type, e.pursuing, e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, " +
					    "e.mother_tongue, e.name, e.notice_period, e.alternate_mobile_no, e.pan_number, e.passport_number, " +
					    "e.permanent_address, e.pf_account_number, e.pincode, e.place_of_birth, e.passing_grade, e.previous_pf_account_number, " +
					    "e.relation, e.state, e.uan, e.views_on_organisation, e.year_of_passing, jr.dept_id, jr.name, d.name, e.work_location, " +
					    "e.probation_period, e.emp_id, e2.name, e.experience, e.billable, e.child1, e.child2, e.child3, e.mothers_name, e.spouse, " +
					    "e.total_experience, emp_proj_client.project_name, emp_proj_client.client_name, e.updated_on, e4.name, e3.name, " +
					    "e.designation_id, de.designation_name, e.updated_by, e.billable_type, e.is_consultant, e.is_apprenticeship";

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
	
	public ServiceResponse customQueryForDepartmentWiseBillableEmployeeReport(List<Long> ids) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("CustomQueryForEmployeeReport");
		apiLogInfo.setApiUrl("/api/customQueryForEmployeeReport");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("QueryList : " + employeeDTO.getQueryList().size());

		try {

			System.err.println(
					" createQueryForEmployeeReport  :: employeeDTO.getQueryList()     " + ids);
//			StringBuilder subQuery = createQueryForDepartmentWiseBillableEmployeeReport(ids);
			List<Object[]> list = getDepartmentWiseBillableEmployeeReport(ids);
			
//			System.out.println(list);

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
					empDTO.setTotalExperience(object[59] != null ? Float.parseFloat(object[59].toString()) : null);
					empDTO.setProjectName(object[60] != null ? object[60].toString() : null);
					empDTO.setClientName(object[61] != null ? object[61].toString() : null);
					empDTO.setUpdatedOn(object[62] != null ? (object[62].toString()) : null);
					empDTO.setCreatedByName(object[63] != null ? (object[63].toString()) : null);
					empDTO.setUpdatedByName(object[64] != null ? (object[64].toString()) : null);
//					empDTO.setSpecializationName(object[65] != null ? (object[65].toString()) : null);
//					empDTO.setDomainName(object[66] != null ? (object[66].toString()) : null);
					empDTO.setDesignationId(object[65] != null ? Long.parseLong(object[65].toString()) : null);
					empDTO.setDesignationName(object[66] != null ? (object[66].toString()) : null);
					empDTO.setUpdatedBy(object[67] != null ? Long.parseLong(object[67].toString()) : null);
					empDTO.setBillableType(object[68] != null ? object[68].toString() : null);
					empDTO.setIsConsultant(object[69] != null ? object[69].toString() : null);
					empDTO.setIsApprenticeship(object[70] != null ? object[70].toString() : null);
					


//					empDTO.setAge(object[70] != null ? Long.parseLong(object[70].toString()) : null);
//					empDTO.setTotalExperience(object[69] != null ? Float.parseFloat(object[69].toString()) : null);		
					
					
					ServiceResponse completionResponse = employeeService.getEmployeeProfileCompletion(empDTO);
					EmployeeDTO emp = (EmployeeDTO) completionResponse.getServiceResponse();

					empDTO.setProfileCompletedPercent(emp != null ? emp.getProfileCompletedPercent() : 0.00);

					dtoList.add(empDTO);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("dtoList :" + dtoList);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee list is empty.");
				apiLogInfo.setApiResponse("Employee List is empty");
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
	
	public ServiceResponse customQueryForEmployeeReport(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("CustomQueryForEmployeeReport");
		apiLogInfo.setApiUrl("/api/customQueryForEmployeeReport");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("QueryList : " + employeeDTO.getQueryList().size());

		try {

			System.err.println(
					" createQueryForEmployeeReport  :: employeeDTO.getQueryList()     " + employeeDTO.getQueryList());
			StringBuilder subQuery = createQueryForEmployeeReport(employeeDTO.getQueryList());
			List<Object[]> list = getCustomEmployeeReport(subQuery.toString(),employeeDTO.getEmpId(), employeeDTO.getSubFeatureName());
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
					empDTO.setProjectName(object[60] != null ? object[60].toString() : null);
					empDTO.setClientName(object[61] != null ? object[61].toString() : null);
//					empDTO.setProjectId(object[62] != null ? Integer.parseInt((object[62].toString())) : null);
					empDTO.setUpdatedOn(object[63] != null ? (object[63].toString()) : null);
					empDTO.setCreatedByName(object[64] != null ? (object[64].toString()) : null);
					empDTO.setUpdatedByName(object[65] != null ? (object[65].toString()) : null);
					
//					empDTO.setSpecializationName(object[66] != null ? (object[66].toString()) : null);
//					empDTO.setDomainName(object[67] != null ? (object[67].toString()) : null);
					//added bu rahul sir
					empDTO.setDesignationId(object[66] != null ? Long.parseLong(object[66].toString()) : null);
					empDTO.setDesignationName(object[67] != null ? (object[67].toString()) : null);
					 empDTO.setUpdatedBy(object[68] != null ? Long.parseLong(object[68].toString()) : null);
					empDTO.setBillableType(object[69] != null ? object[69].toString() : null);
					empDTO.setTeamName(object[70] != null ? object[70].toString() : null);	
					empDTO.setIsConsultant(object[71] != null ? object[71].toString() : null);
					empDTO.setIsApprenticeship(object[72] != null ? object[72].toString() : null);
					empDTO.setPoNo(object[73] != null ? object[73].toString() : null);
                    empDTO.setProjectStartDate(object[74] != null ? object[74].toString() : null);
					empDTO.setProjectEndDate(object[75] != null ? object[75].toString() : null);
					empDTO.setPoProjectType(object[76] != null ? object[76].toString() : null);
					empDTO.setIsApmosysProduct(object[78] != null ? object[78].toString() : null);
					applyEmployeeReportDisplay(empDTO);

					if (object[60] != null && object[62] != null) {
		                String projectIdStr = object[62].toString().trim();
		                String projectNameStr = object[60].toString().trim();

		                
		                if (!projectIdStr.isEmpty() && !projectNameStr.isEmpty()) {
		                    String[] projectIds = projectIdStr.split(",");
		                    String[] projectNames = projectNameStr.split(",");

		                    
		                    List<ProjectDTO> projectList = new ArrayList<>();
		                    int length = Math.min(projectIds.length, projectNames.length);
		                    
		                    for (int i = 0; i < length; i++) {
		                        try {
		                            ProjectDTO projectDTO = new ProjectDTO();
		                            projectDTO.setProjectId(Integer.parseInt(projectIds[i].trim()));
		                            projectDTO.setProjectName(projectNames[i].trim());
		                            projectList.add(projectDTO);
		                        } catch (NumberFormatException e) {
		                            System.err.println("Invalid projectId: " + projectIds[i]);
		                        }
		                    }
		                    empDTO.setProjectList(projectList);
		                }
		            }
					//added by me 
					// empDTO.setDesignationId(object[65] != null ? Long.parseLong(object[65].toString()) : null);
					// empDTO.setDesignationName(object[66] != null ? (object[66].toString()) : null);
					// //updated by
					// empDTO.setUpdatedBy(object[67] != null ? Long.parseLong(object[67].toString()) : null);
					// empDTO.setBillableType(object[68] != null ? object[68].toString() : null);
					// empDTO.setTeamName(object[69] != null ? object[69].toString() : null);	
					// empDTO.setIsConsultant(object[70] != null ? object[70].toString() : null);
					// empDTO.setIsApprenticeship(object[71] != null ? object[71].toString() : null);
					
					ServiceResponse completionResponse = employeeService.getEmployeeProfileCompletion(empDTO);
					EmployeeDTO emp = (EmployeeDTO) completionResponse.getServiceResponse();

					empDTO.setProfileCompletedPercent(emp != null ? emp.getProfileCompletedPercent() : 0.00);

					dtoList.add(empDTO);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("dtoList :" + dtoList);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee list is empty.");
				apiLogInfo.setApiResponse("Employee List is empty");
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

	/**
	 * For sub query creation
	 */
	public StringBuilder createQueryForTimesheetReport(List<CustomFilterDTO> queryList) {
		StringBuilder query = new StringBuilder();
		boolean hasCondition = false;
		boolean hasDateFilter = false;

		for (CustomFilterDTO dto : queryList) {
			String column = dto.getColumn();
			String operator = dto.getOperator();
			String value = dto.getValue();

			if (column == null || operator == null || value == null)
				continue;
			value = value.trim();
			if (value.isEmpty())
				continue;

			boolean isDate = isDateField(column);
			if (isDate)
				hasDateFilter = true;

			if (isDate) {
				String normalizedDate = normalizeDate(value);
				if (normalizedDate == null)
					continue;
				value = normalizedDate;
			}

			if (hasCondition)
				query.append(" AND ");
			hasCondition = true;

			switch (column) {
				case "Employment Status":
					query.append("LOWER(e1.employmentstatus) ");
					if (operator.equalsIgnoreCase("like")) {
						query.append("LIKE LOWER('%").append(value).append("%')");
					} else if (operator.equals("=")) {
						query.append("= LOWER('").append(value).append("')");
					} else {
						query.append(operator).append(" '").append(value).append("'");
					}
					break;
				case "Employee Id":
					if (operator.equals("=")) {
						if (value.startsWith("AP-")) {
							query.append("(e1.employeement_id = '")
									.append(value.substring(3))
									.append("' AND e1.is_apmosys_product = 'true')");
						} else if (value.startsWith("A-")) {
							query.append("(e1.employeement_id = '")
									.append(value.substring(2))
									.append("' AND (e1.is_apmosys_product = 'false' OR e1.is_apmosys_product IS NULL))");
						} else {
							query.append("e1.employeement_id = '").append(value).append("'");
						}
					}
					else if (operator.equalsIgnoreCase("LIKE")) {
				        query.append("e1.employeement_id LIKE '%").append(value).append("%'");
				    }
					break;

				case "Full Name":
				    query.append("LOWER(e1.name) ");
				    if (operator.equalsIgnoreCase("like")) {
				        query.append("LIKE LOWER('%").append(value).append("%')");
				    } else if (operator.equals("=")) {
				        query.append("= LOWER('").append(value).append("')");
				    } else {
				        query.append(operator).append(" '").append(value).append("'");
				    }
				    break;


				case "Date":
				case "From Date":
				case "To Date":
					if (operator.equalsIgnoreCase("LIKE")) {
			            query.append("CAST(et.date AS CHAR) LIKE '%").append(value).append("%'");
			        } else {
			            query.append("DATE(et.date) ").append(operator).append(" '").append(value).append("'");
			        }
					break;
				case "Day Type": 
			        query.append("LOWER(dtm.day_type) LIKE LOWER('%").append(value).append("%')");
			        break;

			    case "Description": 
			        query.append("LOWER(et.description) LIKE LOWER('%").append(value).append("%')");
			        break;

				case "Created On":
					if (operator.equalsIgnoreCase("LIKE")) {
			            query.append("CAST(et.created_on AS CHAR) LIKE '%").append(value).append("%'");
			        } else {
			            appendDateFilter(query, "et.created_on", operator, value);
			        }
					break;

				case "Updated On":
					appendDateFilter(query, "et.updated_on", operator, value);
					break;

				case "Status":
					query.append("sm.status ").append(operator.equalsIgnoreCase("like") ? "LIKE" : operator)
							.append(" '%").append(value).append("%'");
					break;

				case "Department":
				    query.append("LOWER(d.name) ");
				    if (operator.equalsIgnoreCase("like")) {
				        query.append("LIKE LOWER('%").append(value).append("%')");
				    } else if (operator.equals("=")) {
				        query.append("= LOWER('").append(value).append("')");
				    } else {
				        query.append(operator).append(" '").append(value).append("'");
				    }
				    break;


				case "Leave Type":
				    query.append("LOWER(ltm.leave_type) ");
				    if (operator.equalsIgnoreCase("like")) {
				        query.append("LIKE LOWER('%").append(value).append("%')");
				    } else if (operator.equals("=")) {
				        query.append("= LOWER('").append(value).append("')");
				    } else {
				        query.append(operator).append(" '").append(value).append("'");
				    }
				    break;
				case "Total Working Hour":
					query.append("(et.total_working_minutes / 60) ")
						.append(operator)
						.append(" ")
						.append(value);
					break;
				case "Team Name":
					query.append("LOWER(t.team_name) ");
					if (operator.equalsIgnoreCase("like")) {
						query.append("LIKE LOWER('%").append(value).append("%')");
					} else {
						query.append(operator).append(" LOWER('").append(value).append("')");
					}
					break;
				case "Project Name":
					query.append("LOWER(p.project_name) ");
					if (operator.equalsIgnoreCase("like")) {
						query.append("LIKE LOWER('%").append(value).append("%')");
					} else {
						query.append(operator).append(" LOWER('").append(value).append("')");
					}
					break;
				case "Client Name":
					query.append("LOWER(c.client_name) ");
					if (operator.equalsIgnoreCase("like")) {
						query.append("LIKE LOWER('%").append(value).append("%')");
					} else {
						query.append(operator).append(" LOWER('").append(value).append("')");
					}
					break;
				case "Updated By":
					query.append("LOWER(e2.name) ");
					if (operator.equalsIgnoreCase("like")) {
						query.append("LIKE LOWER('%").append(value).append("%')");
					} else {
						query.append(operator).append(" LOWER('").append(value).append("')");
					}
					break;


				default:
					query.append("1=1");
					break;
			}
		}

		if (!hasCondition)
			query.append("1=1");

		if (!hasDateFilter) {
		    query.append(" AND et.date BETWEEN DATE_FORMAT(CURRENT_DATE, '%Y-%m-01') AND CURRENT_DATE ");
		}


		return query;
	}
	/**
	 * To check  date columns like created on,Updated On,From Date,To Date.
	 */
	private boolean isDateField(String column) {
		return column != null && (column.equalsIgnoreCase("Date") ||
				column.equalsIgnoreCase("From Date") ||
				column.equalsIgnoreCase("To Date") ||
				column.equalsIgnoreCase("Created On") ||
				column.equalsIgnoreCase("Updated On"));
	}
	/**
	 * To Achieve like feature in date search parse DD/MM/YYYY OR YYYY/MM/DD.
	 */
	private String normalizeDate(String value) {
		if (value == null)
			return null;
		value = value.trim();
		if (value.isEmpty())
			return null;

		if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
			return value;
		}

		if (value.matches("\\d{2}-\\d{2}-\\d{4}")) {
			String[] parts = value.split("-");
			return parts[2] + "-" + parts[1] + "-" + parts[0];
		}

		return null;
	}

	private void appendDateFilter(StringBuilder query, String dbColumn, String operator, String value) {
		if ("like".equalsIgnoreCase(operator)) {
			query.append("DATE(").append(dbColumn).append(") = '").append(value).append("'");
		} else {
			query.append("DATE(").append(dbColumn).append(") ").append(operator)
					.append(" '").append(value).append("'");
		}
	}
	/**
	 * Query Generation.
	 */
	@Transactional(readOnly = true)
	public Page<CustomTimesheetReportDTO> getCustomTimesheetReport(String filterQuery, Long empId, Pageable pageable) {
		Session session = entityManager.unwrap(Session.class);
		String deptList = departmentRepository.findAccessibleDeptIdsForEmp(empId);
// in the where clause the conditions are temporarily removed for timesheet report
		String whereClause = " WHERE " + filterQuery +
				" AND jr.dept_id IN (" + deptList + ")" ;
				// " AND etm.active != 0 " +
				// " AND t.is_active != 'N' " +
				// " AND p.active != 'false' ";
//		this below old query is replaced with the new table structure of timesheet
		
//		String countQueryStr = "SELECT COUNT(DISTINCT e1.employeement_id, et.date) " +
//				"FROM employee_timesheets et " +
//				"INNER JOIN employee e1 ON et.emp_id = e1.emp_id " +
//				"LEFT JOIN job_role jr ON e1.job_role_id = jr.job_role_id " +
//				"LEFT JOIN department d ON jr.dept_id = d.dept_id " +
//				"LEFT JOIN employee_team_mapping etm ON etm.emp_id = et.emp_id " +
//				"LEFT JOIN teams t ON t.team_id = etm.team_id " +
//				"LEFT JOIN leave_type_master ltm ON ltm.leave_type_master_id = et.leave_type_master_id " +
//				"LEFT JOIN projects p ON p.project_id = t.project_id " +
//				whereClause;
		
		String countQueryStr = "SELECT COUNT(DISTINCT e1.employeement_id, et.date)  \n"
				+ "FROM employee_timesheets_new et  \n"
				+ "INNER JOIN employee e1 ON et.emp_id = e1.emp_id  \n"
				+ "LEFT JOIN status_master_new sm ON et.status = sm.status_id \n"
				+ "LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id \n"
				+ "LEFT JOIN job_role jr ON e1.job_role_id = jr.job_role_id  \n"
				+ "LEFT JOIN department d ON jr.dept_id = d.dept_id  \n"
				+ "LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id \n"
			    + "LEFT JOIN projects p ON p.project_id = pts.project_id \n"
			    + "LEFT JOIN employee_team_mapping etm ON etm.emp_id = et.emp_id \n"
				+ "LEFT JOIN teams t ON t.team_id = etm.team_id  \n"
				+ "LEFT JOIN leave_type_master ltm ON ltm.leave_type_master_id = et.leave_type_master_id  \n"
				+ whereClause;
		
		// System.out.println("=====query of count" + countQueryStr);
		Number totalElements = ((Number) session.createNativeQuery(countQueryStr).getSingleResult());

		String orderBy = pageable.getSort().isSorted()
				? pageable.getSort().stream()
						.map(order -> mapSortColumn(order.getProperty()) + " " + order.getDirection().name())
						.collect(Collectors.joining(", "))
				: "et.date DESC";
//		String dataQueryStr = "SELECT " +
//				"e1.employeement_id AS employeementId, " + "e1.name AS employeeName, " +
//				"et.date AS date, " +
//				"et.day_type AS dayType, " +
//				"et.description AS description, " +
//				"et.status AS status, " +
//				"et.total_time AS totalTime, " +
//				"DATE_FORMAT(et.created_on, '%Y-%m-%d %H:%i:%s') AS createdOn, " +
//				"DATE_FORMAT(et.updated_on, '%Y-%m-%d %H:%i:%s') AS updatedOn, " +
//				"e2.name AS statusUpdatedBy, " +
//				"t.team_name AS teamName, " +
//				"p.project_name AS projectName, " +
//				"p.client_name AS clientName, " +
//				"ltm.leave_type AS leaveType, " +
//				"e1.is_apmosys_product AS isApmosysProduct " +
//				"FROM employee_timesheets et " +
//				"INNER JOIN employee e1 ON et.emp_id = e1.emp_id " +
//				"LEFT JOIN employee e2 ON et.timesheet_status_updated_by = e2.emp_id " +
//				"LEFT JOIN job_role jr ON e1.job_role_id = jr.job_role_id " +
//				"LEFT JOIN department d ON jr.dept_id = d.dept_id " +
//				"LEFT JOIN employee_team_mapping etm ON etm.emp_id = et.emp_id " +
//				"LEFT JOIN teams t ON t.team_id = etm.team_id " +
//				"LEFT JOIN projects p ON p.project_id = t.project_id " +
//				"LEFT JOIN leave_type_master ltm ON ltm.leave_type_master_id = et.leave_type_master_id " +
//				whereClause +
//				" GROUP BY e1.employeement_id, et.date " +
//				" ORDER BY " + orderBy;
		
		
		String dataQueryStr ="SELECT DISTINCT \n"
				+ "e1.employeement_id AS employeementId,  \n"
				+ "e1.name AS employeeName,  \n"
				+ "et.date AS date,  \n"
				+ "dtm.day_type AS dayType,  \n"
				+ "GROUP_CONCAT(DISTINCT TRIM(REPLACE(REPLACE(pts.description, '<br>', ''), '<br/>', '')) SEPARATOR ' | ') AS description,  \n"
				+ "sm.status AS status,  \n"
				+ "ROUND(et.total_working_minutes /60 , 2 ) AS totalTime,  \n"
                + "DATE_FORMAT(et.work_in_time, '%Y-%m-%d %H:%i:%s') as officeInTime,\n" 
                + "DATE_FORMAT(et.work_out_time,'%Y-%m-%d %H:%i:%s') as officeOutTime, \n" 
                + "CONCAT(LPAD(FLOOR(pts.total_client_working_minutes / 60), 2, '0'), ':', "
                +"LPAD(pts.total_client_working_minutes % 60, 2, '0')) AS totalWorkingHours, "        
				+ "DATE_FORMAT(et.created_on, '%Y-%m-%d %H:%i:%s') AS createdOn,  \n"
				+ "DATE_FORMAT(et.updated_on, '%Y-%m-%d %H:%i:%s') AS updatedOn,  \n"
				+ "e2.name AS statusUpdatedBy,  \n"
				+ "GROUP_CONCAT(DISTINCT t.team_name SEPARATOR ', ') AS teamName,  \n"
				+ "GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS projectName,  \n"
				+ "GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS clientName,  \n"
				+ "ltm.leave_type AS leaveType,  \n"
				+ "e1.is_apmosys_product AS isApmosysProduct  \n"
				+ "FROM employee_timesheets_new et  \n"
				+ "INNER JOIN employee e1 ON et.emp_id = e1.emp_id  \n"
				+ "LEFT JOIN status_master_new sm ON et.status = sm.status_id \n"
				+ "LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id \n"
				+ "LEFT JOIN timesheet_action_audit taa ON et.timesheet_id = taa.timesheet_id AND taa.audit_id = (select max(taa1.audit_id) from timesheet_action_audit taa1 where taa.timesheet_id = taa1.timesheet_id) \n"
				+ "LEFT JOIN employee e2 ON taa.action_by = e2.emp_id  \n"
				+ "LEFT JOIN job_role jr ON e1.job_role_id = jr.job_role_id  \n"
				+ "LEFT JOIN department d ON jr.dept_id = d.dept_id  \n"
				+ "LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id\n"
				+ "LEFT JOIN projects p ON p.project_id = pts.project_id\n"
				+ "LEFT JOIN clients c ON c.client_id = p.client_id\n"
				+ "LEFT JOIN employee_team_mapping etm ON etm.emp_id = et.emp_id \n"
				+ "AND (etm.end_date IS NULL OR etm.end_date >= et.date)\n"
				+ "LEFT JOIN teams t ON t.team_id = etm.team_id AND t.project_id = p.project_id\n"
				+ "LEFT JOIN leave_type_master ltm ON ltm.leave_type_master_id = et.leave_type_master_id  \n"
				+  whereClause 
				+ "GROUP BY \n"
				+ "    e1.employeement_id, et.date, dtm.day_type, \n"
				+ "    sm.status, et.total_working_minutes, et.created_on, \n"
				+ "    et.updated_on, e2.name, ltm.leave_type, e1.is_apmosys_product\n"
				+ "ORDER BY " + orderBy ;
		
		@SuppressWarnings("unchecked")
		NativeQuery<CustomTimesheetReportDTO> query = (NativeQuery<CustomTimesheetReportDTO>) session
				.createNativeQuery(dataQueryStr)
				.addScalar("employeementId", StandardBasicTypes.LONG)
				.addScalar("employeeName", StandardBasicTypes.STRING)
				.addScalar("date", StandardBasicTypes.DATE)
				.addScalar("dayType", StandardBasicTypes.STRING)
				.addScalar("description", StandardBasicTypes.STRING)
				.addScalar("status", StandardBasicTypes.STRING)
				.addScalar("totalTime", StandardBasicTypes.STRING)
				.addScalar("officeInTime", StandardBasicTypes.STRING)
				.addScalar("officeOutTime", StandardBasicTypes.STRING)
				.addScalar("totalWorkingHours", StandardBasicTypes.STRING)
				.addScalar("createdOn", StandardBasicTypes.STRING)
				.addScalar("updatedOn", StandardBasicTypes.STRING)
				.addScalar("statusUpdatedBy", StandardBasicTypes.STRING)
				.addScalar("teamName", StandardBasicTypes.STRING)
				.addScalar("projectName", StandardBasicTypes.STRING)
				.addScalar("clientName", StandardBasicTypes.STRING)
				.addScalar("leaveType", StandardBasicTypes.STRING)
				.addScalar("isApmosysProduct", StandardBasicTypes.STRING)
				.setResultTransformer(Transformers.aliasToBean(CustomTimesheetReportDTO.class));

		query.setFirstResult((int) pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());

		List<CustomTimesheetReportDTO> results = query.list();

		for (CustomTimesheetReportDTO dto : results) {
			if (dto.getEmployeementId() != null) {
				String prefix = "true".equalsIgnoreCase(dto.getIsApmosysProduct()) ? "AP-" : "A-";
				dto.setEmploymentIdAcToET(prefix + dto.getEmployeementId());
			}
		}

		return new PageImpl<>(results, pageable, totalElements.longValue());
	}
    
	/**
	 * Method to Map frontend column to db column for search in query.
	 */
	private String mapSortColumn(String property) {
		switch (property) {
			case "employeeName":
				return "e1.name";
			case "employeementId":
				return "e1.employeement_id";
			case "dayType":
				return "et.day_type";
			case "totalTime":
				return "et.total_time ";
			case "description":
				return "et.description";
			case "statusUpdatedBy":
				return "e2.name";
			case "date":
				return "et.date";
			case "status":
				return "et.status";
			case "createdOn":
				return "et.created_on";
			case "updatedOn":
				return "et.updated_on";
			case "teamName":
				return "t.team_name";
			case "projectName":
				return "p.project_name";
			case "clientName":
				return "p.client_name";
			default:
				return "et.date";
		}
	}
    
	
	/**
	 * Query to generate Excel .
	 */
	@Transactional(readOnly = true)
	public List<CustomTimesheetReportDTO> getAllCustomTimesheetReport(String filterQuery, Long empId, Sort sort) {
		Session session = entityManager.unwrap(Session.class);
		String deptList = departmentRepository.findAccessibleDeptIdsForEmp(empId);

		String whereClause = " WHERE " + filterQuery +
				" AND jr.dept_id IN (" + deptList + ")" ;
				// " AND etm.active != 0 " +
				// " AND t.is_active != 'N' " +
				// " AND p.active != 'false' ";

		String orderBy = (sort != null && sort.isSorted())
				? sort.stream()
						.map(order -> mapSortColumn(order.getProperty()) + " " + order.getDirection().name())
						.collect(Collectors.joining(", "))
				: "et.date DESC";

//		String queryStr = "SELECT " +
//				"e1.employeement_id AS employeementId, " +
//				"e1.name AS employeeName, " +
//				"et.date AS date, " +
//				"et.day_type AS dayType, " +
//				"et.description AS description, " +
//				"et.status AS status, " +
//				"et.total_time AS totalTime, " +
//				"DATE_FORMAT(et.created_on, '%Y-%m-%d %H:%i:%s') AS createdOn, " +
//				"DATE_FORMAT(et.updated_on, '%Y-%m-%d %H:%i:%s') AS updatedOn, " +
//				"e2.name AS statusUpdatedBy, " +
//				"t.team_name AS teamName, " +
//				"p.project_name AS projectName, " +
//				"p.client_name AS clientName, " +
//				"ltm.leave_type AS leaveType, " +
//				"e1.is_apmosys_product AS isApmosysProduct " +
//				"FROM employee_timesheets et " +
//				"INNER JOIN employee e1 ON et.emp_id = e1.emp_id " +
//				"LEFT JOIN employee e2 ON et.timesheet_status_updated_by = e2.emp_id " +
//				"LEFT JOIN job_role jr ON e1.job_role_id = jr.job_role_id " +
//				"LEFT JOIN department d ON jr.dept_id = d.dept_id " +
//				"LEFT JOIN employee_team_mapping etm ON etm.emp_id = et.emp_id " +
//				"LEFT JOIN teams t ON t.team_id = etm.team_id " +
//				"LEFT JOIN projects p ON p.project_id = t.project_id " +
//				"LEFT JOIN leave_type_master ltm ON ltm.leave_type_master_id = et.leave_type_master_id " +
//				whereClause +
//				" GROUP BY e1.employeement_id, et.date " +
//				" ORDER BY " + orderBy;
		
		String queryStr ="SELECT DISTINCT \n"
				+ "e1.employeement_id AS employeementId,  \n"
				+ "e1.name AS employeeName,  \n"
				+ "et.date AS date,  \n"
				+ "dtm.day_type AS dayType,  \n"
				// + "pts.description AS description,  \n"
				+ "GROUP_CONCAT(DISTINCT TRIM(REPLACE(REPLACE(pts.description, '<br>', ''), '<br/>', '')) SEPARATOR ' | ') AS description, "
				+ "sm.status AS status,  \n"
				+ "ROUND(et.total_working_minutes /60 , 2 ) AS totalTime,  \n"
				+ "DATE_FORMAT(et.created_on, '%Y-%m-%d %H:%i:%s') AS createdOn,  \n"
				+ "DATE_FORMAT(et.updated_on, '%Y-%m-%d %H:%i:%s') AS updatedOn,  \n"
				+ "e2.name AS statusUpdatedBy,  \n"
				+ "GROUP_CONCAT(DISTINCT t.team_name SEPARATOR ', ') AS teamName,  \n"
				+ "GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS projectName,  \n"
				+ "GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS clientName,  \n"
				+ "ltm.leave_type AS leaveType,  \n"
				+ "e1.is_apmosys_product AS isApmosysProduct  \n"
				+ "FROM employee_timesheets_new et  \n"
				+ "INNER JOIN employee e1 ON et.emp_id = e1.emp_id  \n"
				+ "LEFT JOIN status_master_new sm ON et.status = sm.status_id \n"
				+ "LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id \n"
				+ "LEFT JOIN timesheet_action_audit taa ON et.timesheet_id = taa.timesheet_id AND taa.audit_id = (select max(taa1.audit_id) from timesheet_action_audit taa1 where taa.timesheet_id = taa1.timesheet_id) \n"
				+ "LEFT JOIN employee e2 ON taa.action_by = e2.emp_id  \n"
				+ "LEFT JOIN job_role jr ON e1.job_role_id = jr.job_role_id  \n"
				+ "LEFT JOIN department d ON jr.dept_id = d.dept_id  \n"
				+ "LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id\n"
				+ "LEFT JOIN projects p ON p.project_id = pts.project_id\n"
				+ "LEFT JOIN clients c ON c.client_id = p.client_id\n"
				+ "LEFT JOIN employee_team_mapping etm ON etm.emp_id = et.emp_id \n"
				+ "AND (etm.end_date IS NULL OR etm.end_date >= et.date)\n"
				+ "LEFT JOIN teams t ON t.team_id = etm.team_id AND t.project_id = p.project_id\n"
				+ "LEFT JOIN leave_type_master ltm ON ltm.leave_type_master_id = et.leave_type_master_id  \n"
				+  whereClause 
				+ "GROUP BY \n"
				+ "    e1.employeement_id, et.date, dtm.day_type, \n"
				+ "    sm.status, et.total_working_minutes, et.created_on, \n"
				+ "    et.updated_on, e2.name, ltm.leave_type, e1.is_apmosys_product\n"
				+ "ORDER BY " + orderBy ;
		
		@SuppressWarnings("unchecked")
		NativeQuery<CustomTimesheetReportDTO> query = (NativeQuery<CustomTimesheetReportDTO>) session
				.createNativeQuery(queryStr)
				.addScalar("employeementId", StandardBasicTypes.LONG)
				.addScalar("employeeName", StandardBasicTypes.STRING)
				.addScalar("date", StandardBasicTypes.DATE)
				.addScalar("dayType", StandardBasicTypes.STRING)
				.addScalar("description", StandardBasicTypes.STRING)
				.addScalar("status", StandardBasicTypes.STRING)
				.addScalar("totalTime", StandardBasicTypes.STRING)
				.addScalar("createdOn", StandardBasicTypes.STRING)
				.addScalar("updatedOn", StandardBasicTypes.STRING)
				.addScalar("statusUpdatedBy", StandardBasicTypes.STRING)
				.addScalar("teamName", StandardBasicTypes.STRING)
				.addScalar("projectName", StandardBasicTypes.STRING)
				.addScalar("clientName", StandardBasicTypes.STRING)
				.addScalar("leaveType", StandardBasicTypes.STRING)
				.addScalar("isApmosysProduct", StandardBasicTypes.STRING)
				.setResultTransformer(Transformers.aliasToBean(CustomTimesheetReportDTO.class));

		List<CustomTimesheetReportDTO> results = query.list();

		for (CustomTimesheetReportDTO dto : results) {
			if (dto.getEmployeementId() != null) {
				String prefix = "true".equalsIgnoreCase(dto.getIsApmosysProduct()) ? "AP-" : "A-";
				dto.setEmploymentIdAcToET(prefix + dto.getEmployeementId());
			}
		}

		return results;
	}
	/**
	 * service for customTimesheetApplicationReport.
	 */
	public ServiceResponse customTimesheetApplicationReport(TimesheetDTO timesheetDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("customTimesheetApplicationReport");
	    apiLogInfo.setApiUrl("/api/customTimesheetApplicationReport");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        if (timesheetDTO == null) {
	            throw new IllegalArgumentException("Request body cannot be null");
	        }
	        if (timesheetDTO.getEmpId() == null) {
	            throw new IllegalArgumentException("Employee ID is required");
	        }
	        if (timesheetDTO.getPage() == null || timesheetDTO.getPage() < 0) {
	            timesheetDTO.setPage(0);
	        }
	        if (timesheetDTO.getSize() == null || timesheetDTO.getSize() <= 0) {
	            timesheetDTO.setSize(10);
	        }

	        StringBuilder logBuilder = new StringBuilder("Received request for Custom Timesheet Report")
	                .append(" | EmpId: ").append(timesheetDTO.getEmpId())
	                .append(" | QueryList Size: ")
	                .append(timesheetDTO.getQueryList() != null ? timesheetDTO.getQueryList().size() : 0);

	        Sort sort = Sort.unsorted();
	        if (timesheetDTO.getSortColumn() != null && !timesheetDTO.getSortColumn().isEmpty()) {
	            List<Sort.Order> orders = new ArrayList<>();
	            for (String column : timesheetDTO.getSortColumn()) {
	                Sort.Direction direction = "desc".equalsIgnoreCase(timesheetDTO.getSortDirection())
	                        ? Sort.Direction.DESC : Sort.Direction.ASC;
	                orders.add(new Sort.Order(direction, column));
	            }
	            sort = Sort.by(orders);
	        }

	        StringBuilder subQuery = createQueryForTimesheetReport(timesheetDTO.getQueryList());
	        logBuilder.append(" | Query: ").append(subQuery);
	        // System.out.println("===================="+subQuery);
	        Page<CustomTimesheetReportDTO> page;
	        Pageable pageable;

	        if (Boolean.TRUE.equals(timesheetDTO.getExportAll())) {
	            logBuilder.append(" | ExportAll = true");
	            List<CustomTimesheetReportDTO> allRecords = getAllCustomTimesheetReport(
	                    subQuery.toString(), timesheetDTO.getEmpId(), sort);
	            page = new PageImpl<>(allRecords);
	        } else {
	            logBuilder.append(" | ExportAll = false | Page: ")
	                    .append(timesheetDTO.getPage())
	                    .append(", Size: ").append(timesheetDTO.getSize());
	            pageable = PageRequest.of(timesheetDTO.getPage(), timesheetDTO.getSize(), sort);
	            page = getCustomTimesheetReport(subQuery.toString(), timesheetDTO.getEmpId(), pageable);
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(page);
	        apiLogInfo.setApiResponse("Total Records: " + page.getTotalElements());
	        response.setServiceMessage("Data fetched successfully");

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Total Records: " + page.getTotalElements());

	    } 
	    catch (IllegalArgumentException ex) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceMessage("Invalid request: " + ex.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(ex.getMessage());
	        apiLogInfo.setLogLevel("WARN");
	    } 
	    catch (DataAccessException ex) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceMessage("Database error occurred");
	        response.setServiceError(ex.getMostSpecificCause().getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse("Database exception: " + ex.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	    } 
	    catch (SQLGrammarException ex) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceMessage("Query syntax issue detected");
	        response.setServiceError(ex.getSQL());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse("SQL Error: " + ex.getSQL());
	        apiLogInfo.setLogLevel("ERROR");
	    } 
	    catch (Exception ex) {
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceMessage("Unexpected error occurred");
	        response.setServiceError(ex.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse("Unexpected exception: " + ex.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}


	
	public StringBuilder createQueryForTimesheetSummaryChart(List<CustomFilterDTO> queryList) {
		StringBuilder query = new StringBuilder("");

		for (CustomFilterDTO dto : queryList) {
			if (dto.getOperator() != null && dto.getOperator().equals("like")) {
				dto.setValue("%" + dto.getValue() + "%");
			}

			switch (dto.getColumn()) {
//			case "Employee Id": {
//				query = query.append(" e.employeement_id ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction());
//				break;
//			}
			
			case "Employee Id": {
			    String value = dto.getValue();
			    String operator = dto.getOperator();
			    String conjunction = dto.getConjunction();

			    if (value != null && operator.equals("=")) {
			        if (value.startsWith("AP-")) {
			            String id = value.substring(3);
			            query.append(" e.employeement_id = '").append(id).append("' ")
			                 .append("AND e.is_apmosys_product = 'true' ")
			                 .append(conjunction);
			        } else if (value.startsWith("A-")) {
			            String id = value.substring(2);
			            query.append(" e.employeement_id = '").append(id).append("' ")
			                 .append("AND (e.is_apmosys_product = 'false' OR e.is_apmosys_product IS NULL) ")
			                 .append(conjunction);
			        } else {
			          
			            query.append(" e.employeement_id ").append(operator).append(" '")
			                 .append(value).append("' ").append(conjunction);
			        }
			    } else {
			      
			        query.append(" e.employeement_id ").append(operator).append(" '")
			             .append(value).append("' ").append(conjunction);
			    }
			    break;
			}
			case "Full Name": {
				query = query.append(" e.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Date": {
				query = query.append(" et.date ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Day Type": {
				query = query.append(" et.day_type ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Status": {
				query = query.append(" et.status ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Total Working Hour": {
				query = query.append(" et.total_time ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Team Name": {
				query = query.append(" t.team_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Project Name": {
				query = query.append(" p.project_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Client Name": {
				query = query.append(" p.client_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "From Date": {
				query = query.append(" et.date ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "To Date": {
				query = query.append(" et.date ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Created On": {
				query = query.append(" et.created_on ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Updated On": {
				query = query.append(" et.updated_on ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Updated By": {
				query = query.append(" e2.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Department": {
				query = query.append(" d.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
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

		for (CustomFilterDTO dto : queryList) {
			if (dto.getOperator() != null && dto.getOperator().equals("like")) {
				dto.setValue("%" + dto.getValue() + "%");
			}

			switch (dto.getColumn()) {
//			case "Employee Id": {
//				query = query.append(" e.employeement_id ").append(dto.getOperator() + " '")
//						.append(dto.getValue() + "' ").append(dto.getConjunction());
//				break;
//			}
			case "Employee Id": {
			    String value = dto.getValue();
			    String operator = dto.getOperator();
			    String conjunction = dto.getConjunction();

			    if (value != null && operator.equals("=")) {
			        if (value.startsWith("AP-")) {
			            String id = value.substring(3);
			            query.append(" e.employeement_id = '").append(id).append("' ")
			                 .append("AND e.is_apmosys_product = 'true' ")
			                 .append(conjunction);
			        } else if (value.startsWith("A-")) {
			            String id = value.substring(2);
			            query.append(" e.employeement_id = '").append(id).append("' ")
			                 .append("AND (e.is_apmosys_product = 'false' OR e.is_apmosys_product IS NULL) ")
			                 .append(conjunction);
			        } else {
			          
			            query.append(" e.employeement_id ").append(operator).append(" '")
			                 .append(value).append("' ").append(conjunction);
			        }
			    } else {
			      
			        query.append(" e.employeement_id ").append(operator).append(" '")
			             .append(value).append("' ").append(conjunction);
			    }
			    break;
			}
			case "Full Name": {
				query = query.append(" e.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}

			case "Team Name": {
				query = query.append(" t.team_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Project Name": {
				query = query.append(" p.project_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Client Name": {
				query = query.append(" p.client_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}

			case "Updated By": {
				query = query.append(" e2.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Department": {
				query = query.append(" d.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			default:
				break;
			}
		}
		return query;
	}

	public ServiceResponse getCustomTimesheetSummaryChart(String customQuery, String customQuery1,
			List<CustomFilterDTO> queryList) {
		ServiceResponse response = new ServiceResponse();

		Session session = entityManager.unwrap(Session.class);
		String fromDate = null;
		String toDate = null;

		for (CustomFilterDTO dto : queryList) {
			if (dto.getColumn().equals("From Date")) {
				fromDate = dto.getValue();
			}
			if (dto.getColumn().equals("To Date")) {
				toDate = dto.getValue();
			}
		}

		try {

			String q1 = "SELECT e.emp_id,count(*) filled_eod FROM employee_timesheets et "
					+ "INNER JOIN employee e ON e.emp_id = et.emp_id " + "WHERE date between '" + fromDate + "' and '"
					+ toDate + "' " + " group by e.emp_id";
			Query query1 = session.createSQLQuery(q1);
			List<Object[]> timesheetList = query1.getResultList();

			String q2 = "SELECT et.status,e.employeement_id,e.name,d.name dept,e.email,e.mobile_no,date,total_time working_hours, "
					+ "et.day_type, e2.name manager, t.team_name,p.project_name,p.client_name "
					+ "FROM employee_timesheets et " + "INNER JOIN employee e ON e.emp_id = et.emp_id "
					+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id "
					+ "INNER JOIN department d ON d.dept_id = jr.dept_id "
					+ "INNER JOIN employee e2 on e2.emp_id = e.manager_id "
					+ "LEFT JOIN employee_team_mapping etm on etm.emp_id = et.emp_id "
					+ "LEFT JOIN teams t on t.team_id = etm.team_id "
					+ "LEFT JOIN projects p on p.project_id = t.project_id where " + customQuery;

			System.out.println(q2);
			Query query2 = session.createSQLQuery(q2);
			List<Object[]> filledTimesheetList = query2.getResultList();

			LocalDate startDate = LocalDate.parse(fromDate);
			LocalDate endDate = LocalDate.parse(toDate);
			Long pendingEOdNumber = ChronoUnit.DAYS.between(startDate, endDate);

			List<TimesheetDTO> dtoList = new ArrayList<>();

			if (timesheetList != null) {
				if (!customQuery1.equalsIgnoreCase("")) {
					String empId = " SELECT distinct(e.emp_id),e.employeement_id,e.name,d.name as dept,e.email,e.mobile_no, e2.name as managerName,e.employmentstatus "
							+ " FROM employee_timesheets et " + " INNER JOIN employee e ON e.emp_id = et.emp_id "
							+ " INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id "
							+ " INNER JOIN department d ON d.dept_id = jr.dept_id "
							+ " INNER JOIN employee e2 on e2.emp_id = e.manager_id "
							+ " LEFT JOIN employee_team_mapping etm on etm.emp_id = et.emp_id "
							+ " LEFT JOIN teams t on t.team_id = etm.team_id "
							+ " LEFT JOIN projects p on p.project_id = t.project_id "
							+ "where e.employmentstatus not like 'InActive' and " + customQuery1;

					Query EmployeeId = session.createSQLQuery(empId);
					List<Object[]> employeeList = EmployeeId.getResultList();

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

								Long filledEodCount = timesheet[1] != null ? Long.parseLong(timesheet[1].toString())
										: 0L;

								Long pendingEodCount = pendingEOdNumber - filledEodCount;

								dto.setPendingEodCount(pendingEodCount);

							}

						});
						dtoList.add(dto);
					});

				} else {
					List<EmployeeProjection> employeeList = employeeRepository.getAllEmployees();

					employeeList.forEach(employee -> {
					    TimesheetDTO dto = new TimesheetDTO();

					    dto.setEmployeementId(employee.getEmployeementId());
					    dto.setEmployeeName(employee.getName());
					    dto.setDepartmentName(employee.getDepartmentName());
					    dto.setEmail(employee.getEmail());
					    dto.setMobileNo(employee.getMobileNo());
					    dto.setManagerName(employee.getManager());
					    dto.setEmpId(employee.getEmpId());
					    dto.setPendingEodCount(8L);
					    dto.setLegend("Pending By User");
					    dto.setEmploymentstatus(employee.getEmploymentstatus());
					    dto.setIsConsultant(employee.getIsConsultant());
					    dto.setManagerId(employee.getManagerId());
					    dto.setIsApmosysProduct(employee.getIsApmosysProduct());

					    String employmentId = dto.getEmployeementId() != null ? dto.getEmployeementId().toString() : null;
					    String isApmosysProduct = dto.getIsApmosysProduct();
					    if (employmentId != null) {
					        dto.setEmploymentIdAcToET(
					            "true".equalsIgnoreCase(isApmosysProduct) ? "AP-" + employmentId : "A-" + employmentId
					        );
					    }

					    timesheetList.forEach(timesheet -> {
					        Long timesheetEmpId = timesheet[0] != null ? Long.parseLong(timesheet[0].toString()) : null;
					        Long employeeEmpId = employee.getEmpId();

					        if (Objects.equals(timesheetEmpId, employeeEmpId)) {
					            Long filledEodCount = timesheet[1] != null ? Long.parseLong(timesheet[1].toString()) : 0L;
					            dto.setPendingEodCount(8 - filledEodCount);
					        }
					    });

					    dtoList.add(dto);
					});

				}

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found. Kindly check date range.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return response;
	}

	public ServiceResponse customQueryForTimesheetSummaryChart(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("CustomQueryForTimesheetSummaryChart");
		apiLogInfo.setApiUrl("/api/customQueryForTimesheetSummaryChart");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("QueryList : " + timesheetDTO.getQueryList().size() + "  ,QueryList1 :"
				+ timesheetDTO.getQueryList().size());

		try {

			StringBuilder subQuery = createQueryForTimesheetSummaryChart(timesheetDTO.getQueryList());

			StringBuilder subQuery1 = createQueryForTimesheetSummaryChart1(timesheetDTO.getQueryList1());

			ServiceResponse timesheetSummaryLeaveResposne = getCustomTimesheetSummaryChart(subQuery.toString(),
					subQuery1.toString(), timesheetDTO.getQueryList());

			if (timesheetSummaryLeaveResposne.getServiceStatus().equals("Success")) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(timesheetSummaryLeaveResposne.getServiceResponse());
				apiLogInfo.setApiResponse("Service_reponse :" + timesheetSummaryLeaveResposne.getServiceResponse());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(timesheetSummaryLeaveResposne.getServiceResponse());
				apiLogInfo.setApiResponse("Service_reponse :" + timesheetSummaryLeaveResposne.getServiceResponse());
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

	private StringBuilder createQueryForViewTimesheet(List<CustomFilterDTO> queryList) {
		StringBuilder query = new StringBuilder("");

		for (CustomFilterDTO dto : queryList) {
			if (dto.getOperator() != null && dto.getOperator().equals("like")) {
				dto.setValue("%" + dto.getValue() + "%");
			}

			switch (dto.getColumn()) {
			case "Project Name": {
				query = query.append(" p.project_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Client Name": {
				query = query.append(" p.client_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "From Date": {
				query = query.append(" et.date ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "To Date": {
				query = query.append(" et.date ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
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
						+ "WHERE et.emp_id =" + empId + " AND" + customQuery;

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
		LogDTO apiLogInfo = new LogDTO();
		// apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getCustomFilteredTimesheet");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + timesheetDTO.getEmpId() + " , QueryList: " + timesheetDTO.getQueryList().size());

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
					dto.setTotalTime(object[3] != null ? Float.parseFloat(object[3].toString()) : null);
					dto.setStatus(object[4] != null ? object[4].toString() : null);
					dto.setEmployeeName(object[5] != null ? object[5].toString() : null);
					dto.setCreatedOn(object[6] != null ? object[6].toString() : null);
					dto.setRemarks(object[7] != null ? object[7].toString() : null);
					dto.setDescription(object[8] != null ? object[8].toString() : null);

					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("dtoList :" + dtoList);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet list is empty.");
				apiLogInfo.setApiResponse("Timesheet list is empty");
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

	public ServiceResponse getValueOptionData(CustomFilterDTO customFilterDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getValueOptionData");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			if (customFilterDTO == null || customFilterDTO.getColumn() == null
					|| customFilterDTO.getColumn().trim().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Filter column is required");
				return response;
			}

			final String column = customFilterDTO.getColumn().trim();
			logBuilder.append(" column: ").append(column).append(" empId: ").append(customFilterDTO.getEmpId());

			List<EmployeeDTO> staticOptions = buildStaticValueOptionList(column);
			if (staticOptions != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(staticOptions);
				apiLogInfo.setApiResponse("Static options fetched of size : " + staticOptions.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}

			if(customFilterDTO.getEmpId()!= null) {

			   String deptList = departmentRepository.findAccessibleDeptIdsForEmp(customFilterDTO.getEmpId());
			   if (deptList == null || deptList.isBlank()) {
				   response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				   response.setServiceResponse("No accessible departments for user");
				   apiLogInfo.setApiRequest(logBuilder.toString());
				   logService.logMyInfo(httpRequest, apiLogInfo);
				   return response;
			   }
						 List<Integer> deptIds = Arrays.stream(deptList.split(","))
								    .map(String::trim)
								    .map(Integer::parseInt)
								    .collect(Collectors.toList());
						 List<Long> deptIdLongs = Arrays.stream(deptList.split(","))
								    .map(String::trim)
								    .map(Long::parseLong)    
								    .collect(Collectors.toList());
		 List<Object[]> allEmployeeList = employeeRepository.getAllEmployeesBasedOnUserLogined(deptIds);
//			List<Object[]> allEmployeeList = employeeRepository.getAllEmployees();
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();

			switch (customFilterDTO.getColumn()) {
			case "Employee Id":
			case "employmentID": {
				if (!allEmployeeList.isEmpty()) {
					addEmployeeIdFilterOptionsFromRows(allEmployeeList, dtoList);
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			case "employeeType":
			case "Employee Type": {
				addEmployeeTypeFilterOptions(dtoList);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				break;
			}
			case "Full Name": {
				if (!allEmployeeList.isEmpty()) {
					allEmployeeList.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object[29] != null ? object[29].toString() : null);
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			case "Leave Type": {
				List<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findAll();
				if (!leaveType.isEmpty()) {
					leaveType.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getLeaveType());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			case "Team Name": {
				List<Team> teamObj = teamRepository.findAll();
				if (!teamObj.isEmpty()) {
					teamObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getTeamName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			case "Project Name": {
				List<Project> projectObj = projectRepository.findAll();
				if (!projectObj.isEmpty()) {
					projectObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getProjectName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			case "Po No":{
				List<Project> projObj = projectRepository.findAll();
				if(!projObj.isEmpty()) {
					projObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getPoNo());
						dtoList.add(dto);				
						});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			
			case "Po Project Type":{
				List<String> projObj = projectRepository.finddistinctPoProjectType();
				if(!projObj.isEmpty()) {
					projObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object);
						dtoList.add(dto);				
						});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			case "Client Name": {
				List<Client> clientObj = clientsRepository.findAll();
				if (!clientObj.isEmpty()) {
					clientObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getClientName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			case "Department": {
				List<Department> departmentObj = departmentRepository.findByDeptIdIn(deptIdLongs);
				if (!departmentObj.isEmpty()) {
					departmentObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			// timesheet / leave status
			case "Status": {
				String[] status = new String[] { "Pending", "Approved", "Rejected" };
				for (String object : status) {
					EmployeeDTO dto = new EmployeeDTO();
					dto.setName(object);
					dtoList.add(dto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				break;
			}
			case "Manager Name": {
				List<Object[]> empObj = employeeRepository.getAllManagers();
				if (!empObj.isEmpty()) {
					empObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object[1] != null ? object[1].toString() : null);
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			case "Job Role": {
				List<JobRole> jobRoleObj = jobRoleRepository.findAll();
				if (!jobRoleObj.isEmpty()) {
					jobRoleObj.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			case "Employment Status": {
//				String[] status = new String[] { "Probation", "Confirmed", "Resigned", "InActive", "Reinstate" };
				String[] status = new String[] { "Probation", "Confirmed", "Resigned", "InActive" };
				for (String object : status) {
					EmployeeDTO dto = new EmployeeDTO();
					dto.setName(object);
					dtoList.add(dto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				break;
			}
			case "Gender": {
				String[] status = new String[] { "male", "female", "other" };
				for (String object : status) {
					EmployeeDTO dto = new EmployeeDTO();
					dto.setName(object);
					dtoList.add(dto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				break;
			}
			case "Day Type": {
				String[] status = new String[] { "Working", "Holiday", "Non-working", "Public Holiday", "Leave",
						"Week Off","Comp Off" };
				for (String object : status) {
					EmployeeDTO dto = new EmployeeDTO();
					dto.setName(object);
					dtoList.add(dto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				break;
			}
			case "Work Location": {
				String[] state = new String[] { "Andaman & Nicobar Islands", "Andhra Pradesh", "Arunachal Pradesh",
						"Assam", "BiNohar", "Chandigarh", "Chhattisgarh", "Dadra and Nagar Haveli and  Daman & Diu",
						"Delhi", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jammu & Kashmir", "Jharkhand",
						"Karnataka", "Kerala", "Ladakh", "Lakshadweep", "Madhya Pradesh", "Maharashtra", "Manipur",
						"Meghalaya", "Mizoram", "Nagaland", "Odisha", "Puducherry", "Punjab", "Rajasthan", "Sikkim",
						"Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal" };
				for (String object : state) {
					EmployeeDTO dto = new EmployeeDTO();
					dto.setName(object);
					dtoList.add(dto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}
			case "State": {
				String[] state = new String[] { "Andaman & Nicobar Islands", "Andhra Pradesh", "Arunachal Pradesh",
						"Assam", "Bihar", "Chandigarh", "Chhattisgarh", "Dadra and Nagar Haveli and  Daman & Diu",
						"Delhi", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jammu & Kashmir", "Jharkhand",
						"Karnataka", "Kerala", "Ladakh", "Lakshadweep", "Madhya Pradesh", "Maharashtra", "Manipur",
						"Meghalaya", "Mizoram", "Nagaland", "Odisha", "Puducherry", "Punjab", "Rajasthan", "Sikkim",
						"Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal" };
				for (String object : state) {
					EmployeeDTO dto = new EmployeeDTO();
					dto.setName(object);
					dtoList.add(dto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("dto list fetched.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}
			case "Specialization": {
				List<Specialization> allSpecialization = specializationRepository.findByIsActive("true");
				if (!allSpecialization.isEmpty()) {
					allSpecialization.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getSpecializationName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			case "Domain": {
				List<Domain> allDomain = domainRepository.findByIsActive("true");
				if (!allDomain.isEmpty()) {
					allDomain.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getDomainName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			case "Designation": {
				List<Designation> allDesignation = designationRepository.findAll();
				if (!allDesignation.isEmpty()) {
					allDesignation.forEach((object) -> {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object.getDesignationName());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				break;
			}
			default:
				break;
			}
			}else {
				List<EmployeeProjection> allEmployeeList = employeeRepository.getAllEmployees();
				List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();

				switch (customFilterDTO.getColumn()) {
				case "Employee Id":
				case "employmentID": {
					if (!allEmployeeList.isEmpty()) {
						java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>();
						allEmployeeList.forEach(employee -> {
							String formatted = formatFilterDropdownEmploymentId(
									employee.getEmployeementId(),
									employee.getIsConsultant(),
									employee.getIsApmosysProduct());
							if (formatted != null && seen.add(formatted)) {
								EmployeeDTO dto = new EmployeeDTO();
								dto.setName(formatted);
								dtoList.add(dto);
							}
						});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				case "employeeType":
				case "Employee Type": {
					addEmployeeTypeFilterOptions(dtoList);
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					break;
				}
				case "Full Name": {
					if (!allEmployeeList.isEmpty()) {
						allEmployeeList.forEach((employee) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(employee.getName() != null ? employee.getName() : null);
							dtoList.add(dto);
						});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				case "Leave Type": {
					List<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findAll();
					if (!leaveType.isEmpty()) {
						leaveType.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(object.getLeaveType());
							dtoList.add(dto);
						});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				case "Team Name": {
					List<Team> teamObj = teamRepository.findAll();
					if (!teamObj.isEmpty()) {
						teamObj.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(object.getTeamName());
							dtoList.add(dto);
						});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				case "Project Name": {
					List<Project> projectObj = projectRepository.findAll();
					if (!projectObj.isEmpty()) {
						projectObj.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(object.getProjectName());
							dtoList.add(dto);
						});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				case "Po No":{
					List<Project> projObj = projectRepository.findAll();
					if(!projObj.isEmpty()) {
						projObj.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(object.getPoNo());
							dtoList.add(dto);				
							});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				
				case "Po Project Type":{
					List<String> projObj = projectRepository.finddistinctPoProjectType();
					if(!projObj.isEmpty()) {
						projObj.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(object);
							dtoList.add(dto);				
							});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				case "Client Name": {
					List<Client> clientObj = clientsRepository.findAll();
					if (!clientObj.isEmpty()) {
						clientObj.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(object.getClientName());
							dtoList.add(dto);
						});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				case "Department": {
					 List<Department> departmentObj = departmentRepository.findAll();
					 if (!departmentObj.isEmpty()) {
						departmentObj.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(object.getName());
							dtoList.add(dto);
						});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				// timesheet / leave status
				case "Status": {
					String[] status = new String[] { "Pending", "Approved", "Rejected" };
					for (String object : status) {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object);
						dtoList.add(dto);
					}
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					break;
				}
				case "Manager Name": {
					List<Object[]> empObj = employeeRepository.getAllManagers();
					if (!empObj.isEmpty()) {
						empObj.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(object[1] != null ? object[1].toString() : null);
							dtoList.add(dto);
						});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				case "Job Role": {
					List<JobRole> jobRoleObj = jobRoleRepository.findAll();
					if (!jobRoleObj.isEmpty()) {
						jobRoleObj.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(object.getName());
							dtoList.add(dto);
						});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				case "Employment Status": {
//					String[] status = new String[] { "Probation", "Confirmed", "Resigned", "InActive", "Reinstate" };
					String[] status = new String[] { "Probation", "Confirmed", "Resigned", "InActive" };
					for (String object : status) {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object);
						dtoList.add(dto);
					}
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					break;
				}
				case "Gender": {
					String[] status = new String[] { "male", "female", "other" };
					for (String object : status) {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object);
						dtoList.add(dto);
					}
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					break;
				}
				case "Day Type": {
					String[] status = new String[] { "Working", "Holiday", "Non-working", "Public Holiday", "Leave",
							"Week Off","Comp Off" };
					for (String object : status) {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object);
						dtoList.add(dto);
					}
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					break;
				}
				case "Work Location": {
					String[] state = new String[] { "Andaman & Nicobar Islands", "Andhra Pradesh", "Arunachal Pradesh",
							"Assam", "BiNohar", "Chandigarh", "Chhattisgarh", "Dadra and Nagar Haveli and  Daman & Diu",
							"Delhi", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jammu & Kashmir", "Jharkhand",
							"Karnataka", "Kerala", "Ladakh", "Lakshadweep", "Madhya Pradesh", "Maharashtra", "Manipur",
							"Meghalaya", "Mizoram", "Nagaland", "Odisha", "Puducherry", "Punjab", "Rajasthan", "Sikkim",
							"Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal" };
					for (String object : state) {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object);
						dtoList.add(dto);
					}
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				case "State": {
					String[] state = new String[] { "Andaman & Nicobar Islands", "Andhra Pradesh", "Arunachal Pradesh",
							"Assam", "Bihar", "Chandigarh", "Chhattisgarh", "Dadra and Nagar Haveli and  Daman & Diu",
							"Delhi", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jammu & Kashmir", "Jharkhand",
							"Karnataka", "Kerala", "Ladakh", "Lakshadweep", "Madhya Pradesh", "Maharashtra", "Manipur",
							"Meghalaya", "Mizoram", "Nagaland", "Odisha", "Puducherry", "Punjab", "Rajasthan", "Sikkim",
							"Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal" };
					for (String object : state) {
						EmployeeDTO dto = new EmployeeDTO();
						dto.setName(object);
						dtoList.add(dto);
					}
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("dto list fetched.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				case "Specialization": {
					List<Specialization> allSpecialization = specializationRepository.findByIsActive("true");
					if (!allSpecialization.isEmpty()) {
						allSpecialization.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(object.getSpecializationName());
							dtoList.add(dto);
						});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				case "Domain": {
					List<Domain> allDomain = domainRepository.findByIsActive("true");
					if (!allDomain.isEmpty()) {
						allDomain.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(object.getDomainName());
							dtoList.add(dto);
						});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				case "Designation": {
					List<Designation> allDesignation = designationRepository.findAll();
					if (!allDesignation.isEmpty()) {
						allDesignation.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setName(object.getDesignationName());
							dtoList.add(dto);
						});
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						apiLogInfo.setApiResponse("List fetched of size : " + dtoList.size());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					break;
				}
				default:
					break;
				}

				
			}
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
			logBuilder.append(" error: ").append(e.getMessage());
		}
		if (response.getServiceStatus() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("No options found for column: "
					+ (customFilterDTO != null ? customFilterDTO.getColumn() : "unknown"));
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getCustomQueryData(CustomFilterDTO customFilterDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getCustomQueryData");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		List<Object[]> list = new ArrayList<Object[]>();
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {

			if (customFilterDTO.getCustomQuery() != null) {

//				Session session = entityManager.unwrap(Session.class);
				try {
					String q = customFilterDTO.getCustomQuery();
					logBuilder.append("custom query : " + q);

//					Query query = session.createSQLQuery(q);
//					System.out.println("\n\n query : "+ query);
//					System.out.println("\n\n Result Set : "+ query.getResultList());
//					list = query.getResultList();

					Class.forName("com.mysql.cj.jdbc.Driver");
					con = DriverManager.getConnection(dbURL, dbUsername, dbPassword);

					stmt = con.prepareStatement(q);

					if (stmt != null) {
						rs = stmt.executeQuery();
						ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();

						int columnsNumber = rsmd.getColumnCount();
						System.out.println("column size  : " + columnsNumber);

						Object[] headers = new Object[columnsNumber];

						for (int i = 1; i <= columnsNumber; i++) {
							headers[i - 1] = rsmd.getColumnLabel(i);
						}

						list.add(headers);

						while (rs.next()) {
							Object[] dataArr = new Object[columnsNumber];

							for (int i = 1; i <= columnsNumber; i++) {
								String data = rs.getString(i);
								dataArr[i - 1] = data;

							}

							list.add(dataArr);
						}

						if (!list.isEmpty()) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse(list);
							apiLogInfo.setApiResponse("list size : " + list.size());
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Result set is empty.");
							apiLogInfo.setApiResponse("Result set is empty.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Invalid Query.");
						apiLogInfo.setApiResponse("Invalid Query.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}

				} catch (SQLException e) {
					e.printStackTrace();
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Invalid Query, Please Check entered query.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setLogLevel("ERROR");
					response.setServiceError(e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
					response.setServiceResponse("Something Went Wrong.");
					apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
					apiLogInfo.setLogLevel("ERROR");
					response.setServiceError(e.getMessage());
				} finally {
					try {
						if (stmt != null)
							stmt.close();
						if (rs != null)
							rs.close();
						if (con != null)
							con.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Custom Query Not Found.");
				apiLogInfo.setApiResponse("Custom Query Not Found.");
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

	// ===== Custom Query: server-side paging/sort/search =====
	public ServiceResponse getCustomQueryDataPaged(QueryRequestDTO requestDTO) {
		return executePagedQuery(requestDTO, false);
	}

	public ServiceResponse getFilteredQueryDataPaged(QueryRequestDTO requestDTO) {
		return executePagedQuery(requestDTO, true);
	}

	public ServiceResponse getCustomQueryDistinctValues(QueryDistinctValuesRequestDTO requestDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getCustomQueryDistinctValues");
		apiLogInfo.setLogLevel("INFO");

		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String baseQuery = requestDTO.getCustomQuery();
			if (baseQuery == null || !baseQuery.trim().toLowerCase().startsWith("select")) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Only SELECT query allowed.");
				return response;
			}

			String column = requestDTO.getColumn();
			if (column == null || !column.matches("^[a-zA-Z0-9_]+$")) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid column.");
				return response;
			}

			int limit = requestDTO.getLimit() != null && requestDTO.getLimit() > 0 ? requestDTO.getLimit() : 2000;
			limit = Math.min(limit, 5000);

			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT temp.").append(column)
					.append(" FROM (").append(baseQuery).append(") AS temp WHERE 1=1 ");

			// optional filters (same shape as custom filter)
			if (requestDTO.getCustomQueryFilters() != null && !requestDTO.getCustomQueryFilters().isEmpty()) {
				sql.append(buildWhereClauseFromFilters(requestDTO.getCustomQueryFilters()));
			}

			sql.append(" AND temp.").append(column).append(" IS NOT NULL ");
			sql.append(" ORDER BY temp.").append(column).append(" ASC ");
			sql.append(" LIMIT ").append(limit);

			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
			stmt = con.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			List<String> values = new ArrayList<>();
			while (rs.next()) {
				Object v = rs.getObject(1);
				if (v == null) continue;
				String s = String.valueOf(v).trim();
				if (s.isEmpty()) continue;
				values.add(s);
			}

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(values);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("distinct values size : " + values.size());
			return response;

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong while fetching distinct values.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			return response;
		} finally {
			try {
				if (stmt != null) stmt.close();
				if (rs != null) rs.close();
				if (con != null) con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			logService.logMyInfo(httpRequest, apiLogInfo);
		}
	}

	private ServiceResponse executePagedQuery(QueryRequestDTO requestDTO, boolean applyFilters) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl(applyFilters ? "/api/getFilteredQueryDataPaged" : "/api/getCustomQueryDataPaged");
		apiLogInfo.setLogLevel("INFO");

		Connection con = null;
		PreparedStatement stmt = null;
		PreparedStatement stmtCount = null;
		ResultSet rs = null;
		ResultSet rsCount = null;

		try {
			String baseQuery = requestDTO.getCustomQuery();
			if (baseQuery == null || !baseQuery.trim().toLowerCase().startsWith("select")) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Only SELECT query allowed.");
				return response;
			}

			int page = requestDTO.getPage() != null && requestDTO.getPage() > 0 ? requestDTO.getPage() : 1;
			int size = requestDTO.getSize() != null && requestDTO.getSize() > 0 ? requestDTO.getSize() : 10;
			size = Math.min(size, 5000);
			int offset = (page - 1) * size;

			String selectedCols = requestDTO.getSelectedColumns();
			selectedCols = (selectedCols != null && !selectedCols.trim().isEmpty()) ? selectedCols.trim() : "*";

			String sortColumn = requestDTO.getSortColumn();
			String sortDirection = requestDTO.getSortDirection();
			sortDirection = (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) ? "DESC" : "ASC";
			if (sortColumn != null && !sortColumn.matches("^[a-zA-Z0-9_]+$")) {
				sortColumn = null;
			}

			String searchText = requestDTO.getSearchText();
			searchText = (searchText != null) ? searchText.trim() : "";
			java.util.Map<String, String> columnSearch = requestDTO.getColumnSearch();

			// Discover available columns from metadata (for search across columns and to validate sort column)
			List<String> availableColumns = discoverColumns(baseQuery);
			if (availableColumns.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No columns found for the query.");
				return response;
			}
			if (sortColumn != null && !availableColumns.contains(sortColumn)) {
				sortColumn = null;
			}

			// Build WHERE clause: filters + search
			StringBuilder where = new StringBuilder(" WHERE 1=1 ");
			if (applyFilters && requestDTO.getCustomQueryFilters() != null && !requestDTO.getCustomQueryFilters().isEmpty()) {
				where.append(buildWhereClauseFromFilters(requestDTO.getCustomQueryFilters()));
			}
			if (!searchText.isEmpty()) {
				String escaped = escapeSqlLiteral(searchText);
				List<String> searchCols = selectedCols.equals("*") ? availableColumns : parseSelectedColumns(selectedCols, availableColumns);
				if (!searchCols.isEmpty()) {
					where.append(" AND (");
					for (int i = 0; i < searchCols.size(); i++) {
						if (i > 0) where.append(" OR ");
						where.append("CAST(temp.").append(searchCols.get(i)).append(" AS CHAR) LIKE '%").append(escaped).append("%'");
					}
					where.append(") ");
				}
			}

			// Column-wise search (same as column filter bar)
			if (columnSearch != null && !columnSearch.isEmpty()) {
				for (java.util.Map.Entry<String, String> e : columnSearch.entrySet()) {
					if (e == null) continue;
					String k = e.getKey();
					String v = e.getValue();
					if (k == null || v == null) continue;
					k = k.trim();
					v = v.trim();
					if (k.isEmpty() || v.isEmpty()) continue;
					if (!k.matches("^[a-zA-Z0-9_]+$")) continue;
					if (!availableColumns.contains(k)) continue;
					String escaped = escapeSqlLiteral(v);
					where.append(" AND CAST(temp.").append(k).append(" AS CHAR) LIKE '%").append(escaped).append("%'");
				}
			}

			String from = " FROM (" + baseQuery + ") AS temp ";

			String countSql = "SELECT COUNT(1) " + from + where;

			StringBuilder dataSql = new StringBuilder();
			dataSql.append("SELECT ").append(selectedCols).append(from).append(where);
			if (sortColumn != null) {
				dataSql.append(" ORDER BY temp.").append(sortColumn).append(" ").append(sortDirection);
			}
			dataSql.append(" LIMIT ").append(size).append(" OFFSET ").append(offset);

			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(dbURL, dbUsername, dbPassword);

			stmtCount = con.prepareStatement(countSql);
			rsCount = stmtCount.executeQuery();
			long total = 0;
			if (rsCount.next()) {
				total = rsCount.getLong(1);
			}

			stmt = con.prepareStatement(dataSql.toString());
			rs = stmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();

			List<String> headers = new ArrayList<>();
			for (int i = 1; i <= columnsNumber; i++) {
				headers.add(rsmd.getColumnLabel(i));
			}

			List<List<Object>> rows = new ArrayList<>();
			while (rs.next()) {
				List<Object> row = new ArrayList<>();
				for (int i = 1; i <= columnsNumber; i++) {
					row.add(rs.getObject(i));
				}
				rows.add(row);
			}

			QueryPageResponseDTO out = new QueryPageResponseDTO();
			out.setHeaders(headers);
			out.setRows(rows);
			out.setTotalElements(total);
			out.setPage(page);
			out.setSize(size);

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(out);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("paged query ok, total=" + total + ", rows=" + rows.size());
			return response;

		} catch (SQLException e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Invalid Query, Please Check entered query.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong while executing query.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			return response;
		} finally {
			try {
				if (rs != null) rs.close();
				if (rsCount != null) rsCount.close();
				if (stmt != null) stmt.close();
				if (stmtCount != null) stmtCount.close();
				if (con != null) con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			logService.logMyInfo(httpRequest, apiLogInfo);
		}
	}

	private List<String> discoverColumns(String baseQuery) {
		List<String> cols = new ArrayList<>();
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
			String sql = "SELECT * FROM (" + baseQuery + ") AS temp WHERE 1=0";
			stmt = con.prepareStatement(sql);
			rs = stmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int n = rsmd.getColumnCount();
			for (int i = 1; i <= n; i++) {
				String label = rsmd.getColumnLabel(i);
				if (label != null && label.matches("^[a-zA-Z0-9_]+$")) {
					cols.add(label);
				}
			}
		} catch (Exception ignored) {
			// best-effort; fall back to empty
		} finally {
			try {
				if (rs != null) rs.close();
				if (stmt != null) stmt.close();
				if (con != null) con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return cols;
	}

	private List<String> parseSelectedColumns(String selectedColumns, List<String> availableColumns) {
		if (selectedColumns == null || selectedColumns.trim().isEmpty() || selectedColumns.trim().equals("*")) {
			return availableColumns;
		}
		String[] parts = selectedColumns.split(",");
		List<String> out = new ArrayList<>();
		for (String p : parts) {
			String c = p.trim();
			if (c.startsWith("temp.")) {
				c = c.substring(5);
			}
			if (availableColumns.contains(c)) {
				out.add(c);
			}
		}
		return out;
	}

	private String escapeSqlLiteral(String val) {
		if (val == null) return "";
		return val.replace("'", "''");
	}

	private String buildWhereClauseFromFilters(List<QueryFilterDTO> filters) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (QueryFilterDTO filter : filters) {
			if (filter == null) continue;
			if (filter.getColumn() == null || !filter.getColumn().matches("^[a-zA-Z0-9_]+$")) {
				continue;
			}
			String conj = filter.getConjunction();
			conj = (conj != null && conj.equalsIgnoreCase("OR")) ? "OR" : "AND";
			if (first) {
				conj = "AND";
				first = false;
			}
			String col = filter.getColumn();
			String op = filter.getOperator();
			String val = escapeSqlLiteral(filter.getValue());

			switch (op) {
				case "equals":
					sb.append(" ").append(conj).append(" temp.").append(col).append(" = '").append(val).append("'");
					break;
				case "not_equals":
					sb.append(" ").append(conj).append(" temp.").append(col).append(" != '").append(val).append("'");
					break;
				case "contains":
					sb.append(" ").append(conj).append(" CAST(temp.").append(col).append(" AS CHAR) LIKE '%").append(val).append("%'");
					break;
				case "not_contains":
					sb.append(" ").append(conj).append(" CAST(temp.").append(col).append(" AS CHAR) NOT LIKE '%").append(val).append("%'");
					break;
				case "starts_with":
					sb.append(" ").append(conj).append(" CAST(temp.").append(col).append(" AS CHAR) LIKE '").append(val).append("%'");
					break;
				case "ends_with":
					sb.append(" ").append(conj).append(" CAST(temp.").append(col).append(" AS CHAR) LIKE '%").append(val).append("'");
					break;
				case "gt":
					sb.append(" ").append(conj).append(" temp.").append(col).append(" > '").append(val).append("'");
					break;
				case "gte":
					sb.append(" ").append(conj).append(" temp.").append(col).append(" >= '").append(val).append("'");
					break;
				case "lt":
					sb.append(" ").append(conj).append(" temp.").append(col).append(" < '").append(val).append("'");
					break;
				case "lte":
					sb.append(" ").append(conj).append(" temp.").append(col).append(" <= '").append(val).append("'");
					break;
				case "between":
					String v2 = escapeSqlLiteral(filter.getValueTo());
					if (filter.getValue() != null && filter.getValueTo() != null) {
						sb.append(" ").append(conj).append(" temp.").append(col).append(" BETWEEN '").append(val).append("' AND '").append(v2).append("'");
					}
					break;
				case "in":
					sb.append(" ").append(conj).append(" temp.").append(col).append(" IN (").append(formatInClause(escapeSqlLiteral(filter.getValue()))).append(")");
					break;
				case "not_in":
					sb.append(" ").append(conj).append(" temp.").append(col).append(" NOT IN (").append(formatInClause(escapeSqlLiteral(filter.getValue()))).append(")");
					break;
				case "is_null":
					sb.append(" ").append(conj).append(" temp.").append(col).append(" IS NULL");
					break;
				case "is_not_null":
					sb.append(" ").append(conj).append(" temp.").append(col).append(" IS NOT NULL");
					break;
				default:
					break;
			}
		}
		return sb.toString();
	}

	//get Filtered Query Data
	public ServiceResponse getFilteredQueryData(QueryRequestDTO requestDTO) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getFilteredQueryData");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();

		try {
			String baseQuery = requestDTO.getCustomQuery();

			// Validate (only SELECT allowed)
			if (baseQuery == null || !baseQuery.trim().toLowerCase().startsWith("select")) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Only SELECT query allowed.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			// Wrap query
			StringBuilder finalQuery = new StringBuilder();
			if (requestDTO.getSelectedColumns()!=null && !requestDTO.getSelectedColumns().isEmpty()){
				finalQuery.append("SELECT "+requestDTO.getSelectedColumns()+" FROM (");
			}else {
				finalQuery.append("SELECT * FROM (");
			}
			finalQuery.append(baseQuery);
			finalQuery.append(") AS temp WHERE 1=1 ");

			// Apply filters
			if (requestDTO.getCustomQueryFilters() != null) {

				for (QueryFilterDTO filter : requestDTO.getCustomQueryFilters()) {

					if (filter.getColumn() == null || !filter.getColumn().matches("^[a-zA-Z0-9_]+$")) {
						continue;
					}

					String val = filter.getValue();

					switch (filter.getOperator()) {

						case "equals":
							finalQuery.append(" AND temp.")
									.append(filter.getColumn()).append(" = '").append(val).append("'");
							break;

						case "not_equals":
							finalQuery.append(" AND temp.")
									.append(filter.getColumn()).append(" != '").append(val).append("'");
							break;

						case "contains":
							finalQuery.append(" AND temp.")
									.append(filter.getColumn()).append(" LIKE '%").append(val).append("%'");
							break;

						case "not_contains":
							finalQuery.append(" AND temp.")
									.append(filter.getColumn()).append(" NOT LIKE '%").append(val).append("%'");
							break;

						case "starts_with":
							finalQuery.append(" AND temp.")
									.append(filter.getColumn()).append(" LIKE '").append(val).append("%'");
							break;

						case "ends_with":
							finalQuery.append(" AND temp.")
									.append(filter.getColumn()).append(" LIKE '%").append(val).append("'");
							break;

						case "gt":
							finalQuery.append(" AND temp.")
									.append(filter.getColumn()).append(" > '").append(val).append("'");
							break;

						case "gte":
							finalQuery.append(" AND temp.")
									.append(filter.getColumn()).append(" >= '").append(val).append("'");
							break;

						case "lt":
							finalQuery.append(" AND temp.")
									.append(filter.getColumn()).append(" < '").append(val).append("'");
							break;

						case "lte":
							finalQuery.append(" AND temp.")
									.append(filter.getColumn()).append(" <= '").append(val).append("'");
							break;

						case "between":
							if (filter.getValue() != null && filter.getValueTo() != null) {
								finalQuery.append(" AND temp.").append(filter.getColumn())
										.append(" BETWEEN '")
										.append(filter.getValue())
										.append("' AND '")
										.append(filter.getValueTo())
										.append("'");
							}
							break;

						case "in":
							finalQuery.append(" AND temp.").append(filter.getColumn())
									.append(" IN (").append(formatInClause(val)).append(")");
							break;

						case "not_in":
							finalQuery.append(" AND temp.").append(filter.getColumn())
									.append(" NOT IN (").append(formatInClause(val)).append(")");
							break;

						case "is_null":
							finalQuery.append(" AND temp.").append(filter.getColumn())
									.append(" IS NULL");
							break;

						case "is_not_null":
							finalQuery.append(" AND temp.").append(filter.getColumn())
									.append(" IS NOT NULL");
							break;
						case "group":
							finalQuery.append(" GROUP BY temp.").append(filter.getColumn());
							break;
						case "order":
							finalQuery.append(" ORDER BY temp.").append(filter.getColumn())
									.append(" ").append(filter.getValue());
							break;
						default:
							break;
					}
				}
			}

			String finalSql = finalQuery.toString();
			logBuilder.append("Final Query: ").append(finalSql);

			// use your existing method to execute query
			CustomFilterDTO customDTO = new CustomFilterDTO();
			//pass the final SQL Query to get filtered data
			customDTO.setCustomQuery(finalSql);
			response = getCustomQueryData(customDTO);

			apiLogInfo.setApiStatus(response.getServiceStatus());
			apiLogInfo.setApiResponse("Filtered query executed");
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong while filtering data.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	private String formatInClause(String value) {
		if (value == null || value.isEmpty()) return "";

		String[] values = value.split(",");

		return Arrays.stream(values)
				.map(v -> "'" + v.trim() + "'")
				.collect(Collectors.joining(","));
	}



	public ServiceResponse customQueryForDocument(NewsletterDTO newsletterDto) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("customQueryForDocument");
		apiLogInfo.setApiUrl("/api/customQueryForDocument");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("QueryList : " + newsletterDto.getQueryList().size());

		try {

			StringBuilder subQuery = createQueryForDocument(newsletterDto.getQueryList());
			List<Object[]> listOfDoc = getCustomDocuments(subQuery.toString());

			System.err.println(" query   " + subQuery);

			List<NewsletterDTO> dtoList = new ArrayList<NewsletterDTO>();

			if (listOfDoc != null) {
				listOfDoc.forEach((object) -> {
					NewsletterDTO dto = new NewsletterDTO();

					dto.setDisplayName(object[0] != null ? object[0].toString() : null);
					dto.setCreatedOn(object[1] != null ? object[1].toString() : null);
					dto.setCreatedBy(object[2] != null ? Integer.parseInt(object[2].toString()) : null);
					dto.setName(object[3] != null ? object[3].toString() : null);
					dto.setTypeName(object[4] != null ? object[4].toString() : null);
					dto.setFileName(object[5] != null ? object[5].toString() : null);
					dto.setDocumentId(object[6] != null ? Long.parseLong(object[6].toString()) : null);

					dtoList.add(dto);

				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse(" dtoList :" + dtoList);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Document list is Empty !! ");
				apiLogInfo.setApiResponse("Document list is Empty !! ");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

//	public StringBuilder createQueryForDocument(List<CustomFilterDTO> queryList) {
//		StringBuilder query = new StringBuilder("");
//		System.out.println(" after query List   " + queryList);
//		for (CustomFilterDTO dto : queryList) {
//			if (dto.getOperator() != null && dto.getOperator().equals("like")) {
//				dto.setValue("%" + dto.getValue() + "%");
//			}
//
//			switch (dto.getColumn()) {
//			case "Document Name": {
//				query = query.append(" d.display_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction());
//				break;
//			}
//			case "Created On": {
//				query = query.append(" d.created_on ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction());
//				break;
//			}
//			case "Created By": {
//				query = query.append(" d.created_by ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction());
//				break;
//			}
//			case "Full Name": {
//				query = query.append(" e.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction());
//				break;
//			}
//			case "Type": {
//				query = query.append(" td.type_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction());
//				break;
//			}
//			case "File Name": {
//				query = query.append(" d.file_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
//						.append(dto.getConjunction());
//				break;
//			}
//
//			default:
//				break;
//
//			}
//		}
//
//		return query;
//	}
	public StringBuilder createQueryForDocument(List<CustomFilterDTO> queryList) {
	    StringBuilder query = new StringBuilder("");
	    System.out.println("After query List: " + queryList);

	    for (CustomFilterDTO dto : queryList) {
	        if (dto.getOperator() != null && dto.getOperator().equalsIgnoreCase("like")) {
	            dto.setValue("%" + dto.getValue() + "%");
	        }

	        String column = dto.getColumn();
	        if (column == null) continue;

	        switch (column.trim()) { 
	            case "displayName": 
	                query.append(" d.display_name ").append(dto.getOperator()).append(" '")
	                        .append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
//	            case "createdOn":
//	                query.append(" d.created_on ").append(dto.getOperator()).append(" '")
//	                        .append(dto.getValue()).append("' ").append(dto.getConjunction());
//	                break;
	            case "createdOn": {
	                String formattedDate = formatDate(dto.getValue());
	                query.append(" d.created_on ").append(dto.getOperator()).append(" '")
	                     .append(formattedDate).append("' ").append(dto.getConjunction());
	                break;
	            }
	            case "createdBy":
	                query.append(" d.created_by ").append(dto.getOperator()).append(" '")
	                        .append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "fullName":
	                query.append(" e.name ").append(dto.getOperator()).append(" '")
	                        .append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "type":
	                query.append(" td.type_name ").append(dto.getOperator()).append(" '")
	                        .append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            case "fileName":
	                query.append(" d.file_name ").append(dto.getOperator()).append(" '")
	                        .append(dto.getValue()).append("' ").append(dto.getConjunction());
	                break;
	            default:
	                System.out.println("Unexpected column: " + column);
	                break;
	        }
	    }
	    return query;
	}
	private String formatDate(String inputDate) {
	    SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); 
	    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    try {
	        Date date = inputFormat.parse(inputDate);
	        return outputFormat.format(date);
	    } catch (ParseException e) {
	        e.printStackTrace();
	        return inputDate;
	    }
	}



	public List<Object[]> getCustomDocuments(String customQuery) {
		try {
			
			System.err.println(" custom query    "+customQuery);

			Session session = entityManager.unwrap(Session.class);

			try {

				String q = "SELECT d.display_name, d.created_on,d.created_by, e.name, td.type_name , d.file_name, d.document_id FROM documents d "
						+ "INNER JOIN employee e ON e.emp_id=d.created_by "
						+ "INNER JOIN type_document td ON td.type_id=d.type_id  WHERE " + customQuery;

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
	
	public StringBuilder createQueryForAttendanceReport(List<CustomFilterDTO> queryList) {
		StringBuilder query = new StringBuilder("");

		for (CustomFilterDTO dto : queryList) {
			if (dto.getOperator() != null && dto.getOperator().equals("like")) {
				dto.setValue("%" + dto.getValue() + "%");
			}

			switch (dto.getColumn()) {
			case "Employee Id": {
				query = query.append(" e.employeement_id ").append(dto.getOperator() + " '")
						.append(dto.getValue() + "' ").append(dto.getConjunction());
				break;
			}
			case "Full Name": {
				query = query.append(" e.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Leave Type": {
				query = query.append(" ltm.leave_type ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}

			case "From Date": {
				query = query.append(" el.from_date ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "To Date": {
				query = query.append(" el.to_date ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "No. of Days": {
				query = query.append(" el.no_of_days ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Reason": {
				query = query.append(" el.reason ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Status": {
				query = query.append(" ls.status ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Manager Name": {
				query = query.append(" e2.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Created On": {
				query = query.append(" el.created_on ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Updated On": {
				query = query.append(" el.updated_on ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Updated By": {
				query = query.append(" e3.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Team Name": {
				query = query.append(" t.team_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Project Name": {
				query = query.append(" p.project_name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Client Name": {
				query = query.append(" p.client ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
				break;
			}
			case "Department": {
				query = query.append(" d.name ").append(dto.getOperator() + " '").append(dto.getValue() + "' ")
						.append(dto.getConjunction());
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
	
	public List<Object[]> getAttendanceReport(String customQuery) {
		try {

			Session session = entityManager.unwrap(Session.class);

			try {

				String q = "SELECT e.employeement_id, e.name AS employee, et.date, et.day_type, et.description, et.status, "
				        + "et.total_time, et.created_on, et.updated_on, e2.name AS statusUpdatedBy, t.team_name, "
				        + "p.project_name, p.client_name, et.office_in_time, et.office_out_time, et.total_working_hours, "
				        + "e.is_consultant, e.is_apprenticeship "
				        + "FROM employee_timesheets et "
				        + "INNER JOIN employee e ON et.emp_id = e.emp_id "
				        + "LEFT JOIN employee_leave el on el.emp_id = e.emp_id "
				        + "LEFT JOIN leave_type_master ltm on el.leave_type_master_id = el.leave_type_master_id "
				        + "LEFT JOIN employee e2 ON et.timesheet_status_updated_by = e2.emp_id "
				        + "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id "
				        + "LEFT JOIN department d ON jr.dept_id = d.dept_id "
				        + "LEFT JOIN employee_team_mapping etm ON etm.emp_id = et.emp_id "
				        + "LEFT JOIN teams t ON t.team_id = etm.team_id "
				        + "LEFT JOIN projects p ON p.project_id = t.project_id "
				        + "WHERE "
				        + customQuery
				        + "AND t.is_active != 'N' AND p.active != 'false' "
				        + "GROUP BY e.employeement_id, et.date";
				
//				String q = "SELECT distinct "
//		                + "e.employeement_id, e.name AS employee, e2.name AS statusUpdatedBy, "
//		                + "t.team_name, p.project_name, p.client_name, e.is_consultant, e.is_apprenticeship "
//		                + "FROM employee e "
//		                + "LEFT JOIN employee_leave el ON el.emp_id = e.emp_id "
//		                + "LEFT JOIN leave_type_master ltm ON el.leave_type_master_id = el.leave_type_master_id "
//		                + "LEFT JOIN employee e2 ON e2.emp_id = e.emp_id "
//		                + "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id "
//		                + "LEFT JOIN department d ON jr.dept_id = d.dept_id "
//		                + "LEFT JOIN employee_team_mapping etm ON etm.emp_id = e.emp_id "
//		                + "LEFT JOIN teams t ON t.team_id = etm.team_id "
//		                + "LEFT JOIN projects p ON p.project_id = t.project_id "
//		                + "WHERE "
//		                + customQuery // Use the custom query parameter here
//		                + "AND t.is_active != 'N' AND p.active != 'false';";



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
	
//	public ServiceResponse getCustomLAttendanceApplicationsList(LeaveDTO leaveDTO) {
//	    ServiceResponse response = new ServiceResponse();
//	    System.out.println(leaveDTO.getQueryList());
//	    LogDTO apiLogInfo = new LogDTO();
//	    apiLogInfo.setSubFeatureName("getCustomLAttendanceApplicationsList");
//	    apiLogInfo.setApiUrl("/api/getCustomLAttendanceApplicationsList");
//	    apiLogInfo.setLogLevel("INFO");
//	    StringBuilder logBuilder = new StringBuilder();
//	    logBuilder.append("QueryList : " + leaveDTO.getQueryList().size());
//	    String sDate = "";
//	    String eDate = "";
//
//	    try {
//	        StringBuilder subQuery = createQueryForAttendanceReport(leaveDTO.getQueryList());
//	        List<Object[]> list = getAttendanceReport(subQuery.toString());
//	        System.out.println(leaveDTO.getQueryList());
//
//	        for (CustomFilterDTO customFilterDTO : leaveDTO.getQueryList()) {
//	            if (customFilterDTO.getStartDate() != null) {
//	                sDate += customFilterDTO.getStartDate();
//	            }
//	            if (customFilterDTO.getEndDate() != null) {
//	                eDate += customFilterDTO.getEndDate();
//	            }
//	        }
//
//	        // Call getEmpBioData to fetch biometric data
//	        BioMaxService bioMaxService = new BioMaxService();
//	        ServiceResponse bioDataResponse = bioMaxService.getEmpBioData(sDate, eDate);
//	        List<BioMaTO> bioDataList = bioDataResponse.getServiceResponse() != null
//	            ? (List<BioMaTO>) bioDataResponse.getServiceResponse()
//	            : new ArrayList<>();
//
//	        System.out.println("SubQuery : " + subQuery);
//
//	        List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
//	        List<BioMaTO> biolist = new ArrayList<BioMaTO>();
//
//	        if (!bioDataList.isEmpty() && !list.isEmpty()) {
//	            Set<Long> bioDataEmploymentIds = bioDataList.stream()
//	                .map(bioMaTO -> {
//	                    String employeeCode = bioMaTO.getEmployeeCode();
//	                    if (employeeCode != null) {
//	                        // Extract numeric part after the prefix
//	                        String numericPart = employeeCode.replaceAll("\\D", ""); // Remove all non-digit characters
//	                        if (!numericPart.isEmpty()) {
//	                            return Long.parseLong(numericPart);
//	                        } else {
//	                            System.out.println("No numeric part found in employee code: " + employeeCode);
//	                            return null;
//	                        }
//	                    } else {
//	                        System.out.println("Null employee code found");
//	                        return null;
//	                    }
//	                })
//	                .filter(Objects::nonNull)
//	                .collect(Collectors.toSet());
//	            
//	            // Check if any ID in the list matches with bioDataEmploymentIds
//	            List<Object[]> matchedRecords = list.stream()
//	                .filter(object -> {
//	                    if (object[0] != null) {
//	                        try {
//	                            String objectCode = object[0].toString();
//	                            if (!objectCode.isEmpty()) {
//	                                return bioDataEmploymentIds.equals(Long.parseLong(objectCode));
//	                            }
//	                        } catch (NumberFormatException e) {
//	                            System.out.println("Error parsing numeric part of object[0]: " + object[0]);
//	                        }
//	                    }
//	                    return false;
//	                })
//	                .collect(Collectors.toList());
//	            
//	            if(!matchedRecords.isEmpty()) {
//	            	for (Object[] matchedRecord : matchedRecords) {
//	                    if (matchedRecord != null && matchedRecord.length > 0) {
//	                        String employeeCodeFromRecord = matchedRecord[0].toString(); // Extracting the employee code or matching key
//	                        
//	                        Optional<BioMaTO> matchingBioData = bioDataList.stream()
//	                            .filter(bioMaTO -> bioMaTO.getEmployeeCode() != null && bioMaTO.getEmployeeCode().equals(employeeCodeFromRecord))
//	                            .findAny();
//	                        
//	                        if (matchingBioData.isPresent()) {
//	                            BioMaTO bioMaTO = new BioMaTO();
//	                            
//	                            bioMaTO.setLogDate(bioDataList.get(0).getLogDate() != null ? bioDataList.get(0).getLogDate() : null); // Assuming first element is log date
//	                            bioMaTO.setEmployeeCode(bioDataList.get(0).getEmployeeCode() != null ? bioDataList.get(0).getEmployeeCode() : null); // Assuming second element is employeeCode
//	                            bioMaTO.setEmployeeName(bioDataList.get(0).getEmployeeName() != null ? bioDataList.get(0).getEmployeeName() : null); // Assuming third element is employeeName
//	                            bioMaTO.setTotalDuration(bioDataList.get(0).getTotalDuration() != null ? bioDataList.get(0).getTotalDuration() : null); // Assuming fourth element is totalDuration
//	                            bioMaTO.setShiftName(bioDataList.get(0).getShiftName() != null ? bioDataList.get(0).getShiftName() : null); // Assuming fifth element is shiftName
//	                            bioMaTO.setBeginTime(bioDataList.get(0).getBeginTime() != null ? bioDataList.get(0).getBeginTime() : null); // and so on...
//	                            bioMaTO.setEndTime(bioDataList.get(0).getEndTime() != null ? bioDataList.get(0).getEndTime() : null);
//	                            bioMaTO.setStatus(bioDataList.get(0).getStatus() != null ? bioDataList.get(0).getStatus() : null);
//	                            bioMaTO.setPunchRecords(bioDataList.get(0).getPunchRecords() != null ? bioDataList.get(0).getPunchRecords() : null);
//	                            bioMaTO.setEarlyBy(bioDataList.get(0).getEarlyBy() != null ? bioDataList.get(0).getEarlyBy() : null);
//	                            bioMaTO.setLateBy(bioDataList.get(0).getEarlyBy() != null ? bioDataList.get(0).getEarlyBy() : null);
//	                            bioMaTO.setDuration(bioDataList.get(0).getDuration() != null ? bioDataList.get(0).getDuration() : null);
//	                            bioMaTO.setInTime(bioDataList.get(0).getInTime() != null ? bioDataList.get(0).getInTime() : null);
//	                            bioMaTO.setOutTime(bioDataList.get(0).getOutTime() != null ? bioDataList.get(0).getOutTime() : null);
//	                            bioMaTO.setShiftDuration(bioDataList.get(0).getShiftDuration() != null ? bioDataList.get(0).getShiftDuration() : null);
//
//	                            // Add the populated BioMaTO object to the biolist
//	                            biolist.add(bioMaTO);
//	                        }
//	                    }
//	                }
//	            }
//
//	            // Update the response with the populated biolist
//	            if (!biolist.isEmpty()) {
//	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	                response.setServiceResponse(biolist);
//
//	                // Log the successful API response
//	                apiLogInfo.setApiResponse("dtoList: " + biolist);
//	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//	            } else {
//	                // Handle the no-match scenario
//	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	                response.setServiceResponse("Attendance list is empty.");
//
//	                // Log the failure
//	                apiLogInfo.setApiResponse("Attendance list is empty");
//	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	            }
//	        }
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	        response.setServiceResponse("Something Went Wrong.");
//	        response.setServiceError(e.getMessage());
//	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	        apiLogInfo.setLogLevel("ERROR");
//	    }
//
//	    apiLogInfo.setApiRequest(logBuilder.toString());
//	    logService.logMyInfo(httpRequest, apiLogInfo);
//	    return response;
//	}
	
	public ServiceResponse getCustomLAttendanceApplicationsList(LeaveDTO leaveDTO) {
	    ServiceResponse response = new ServiceResponse();
	    System.out.println(leaveDTO.getQueryList());
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getCustomLAttendanceApplicationsList");
	    apiLogInfo.setApiUrl("/api/getCustomLAttendanceApplicationsList");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("QueryList : " + leaveDTO.getQueryList().size());
	    String sDate = "";
	    String eDate = "";

	    try {
	        StringBuilder subQuery = createQueryForAttendanceReport(leaveDTO.getQueryList());
	        List<Object[]> list = getAttendanceReport(subQuery.toString());
	        System.out.println(leaveDTO.getQueryList());

	        for (CustomFilterDTO customFilterDTO : leaveDTO.getQueryList()) {
	            if (customFilterDTO.getStartDate() != null) {
	                sDate += customFilterDTO.getStartDate();
	            }
	            if (customFilterDTO.getEndDate() != null) {
	                eDate += customFilterDTO.getEndDate();
	            }
	        }

	        // Call getEmpBioData to fetch biometric data
	        BioMaxService bioMaxService = new BioMaxService();
	        ServiceResponse bioDataResponse = bioMaxService.getEmpBioDataFromIshine(sDate, eDate, null, null);
	        List<BioMaTO> bioDataList = bioDataResponse.getServiceResponse() != null
	            ? (List<BioMaTO>) bioDataResponse.getServiceResponse()
	            : new ArrayList<>();

	        System.out.println("SubQuery : " + subQuery);

	        List<BioMaTO> biolist = new ArrayList<BioMaTO>();

	        if (!bioDataList.isEmpty() && !list.isEmpty()) {
	            // Stream through the list to get employment IDs
	            Set<Long> bioDataEmploymentIds = bioDataList.stream()
	                .map(bioMaTO -> {
	                    String employeeCode = bioMaTO.getEmployeeCode();
	                    if (employeeCode != null) {
	                        // Extract numeric part after the prefix
	                        String numericPart = employeeCode.replaceAll("\\D", ""); // Remove all non-digit characters
	                        if (!numericPart.isEmpty()) {
	                            return Long.parseLong(numericPart);
	                        } else {
	                            System.out.println("No numeric part found in employee code: " + employeeCode);
	                            return null;
	                        }
	                    } else {
	                        System.out.println("Null employee code found");
	                        return null;
	                    }
	                })
	                .filter(Objects::nonNull)
	                .collect(Collectors.toSet());

	            // Stream through the list and match with bioDataList
	            list.stream()
	                .filter(object -> object[0] != null) // Filter non-null objects in list
	                .map(object -> {
	                    String objectCode = object[0].toString();
	                    Long employmentId = null;
	                    try {
	                        employmentId = Long.parseLong(objectCode);
	                    } catch (NumberFormatException e) {
	                        System.out.println("Error parsing employment ID: " + objectCode);
	                    }
	                    return employmentId;
	                })
	                .filter(Objects::nonNull) // Filter non-null employmentIds
	                .forEach(employmentId -> {
	                    // For each employmentId, find matching BioMaTO from bioDataList
	                    bioDataList.stream()
	                        .filter(bioMaTO -> {
	                            String employeeCode = bioMaTO.getEmployeeCode();
	                            if (employeeCode != null) {
	                                // Extract numeric part from employeeCode and compare with employmentId
	                                String numericPart = employeeCode.replaceAll("\\D", "");
	                                return !numericPart.isEmpty() && Long.parseLong(numericPart)== employmentId;
	                            }
	                            return false;
	                        })
	                        .findFirst() // Find the first match
	                        .ifPresent(matchingBioData -> {
	                            BioMaTO bioMaTO = new BioMaTO();
	                            bioMaTO.setLogDate(matchingBioData.getLogDate() != null ? matchingBioData.getLogDate() : null);
	                            bioMaTO.setEmployeeCode(matchingBioData.getEmployeeCode() != null ? matchingBioData.getEmployeeCode() : null);
	                            bioMaTO.setEmployeeName(matchingBioData.getEmployeeName() != null ? matchingBioData.getEmployeeName() : null);
	                            bioMaTO.setTotalDuration(matchingBioData.getTotalDuration() != null ? matchingBioData.getTotalDuration() : null);
	                            bioMaTO.setShiftName(matchingBioData.getShiftName() != null ? matchingBioData.getShiftName() : null);
	                            bioMaTO.setBeginTime(matchingBioData.getBeginTime() != null ? matchingBioData.getBeginTime() : null);
	                            bioMaTO.setEndTime(matchingBioData.getEndTime() != null ? matchingBioData.getEndTime() : null);
	                            bioMaTO.setStatus(matchingBioData.getStatus() != null ? matchingBioData.getStatus() : null);
	                            bioMaTO.setPunchRecords(matchingBioData.getPunchRecords() != null ? matchingBioData.getPunchRecords() : null);
	                            bioMaTO.setEarlyBy(matchingBioData.getEarlyBy() != null ? matchingBioData.getEarlyBy() : null);
	                            bioMaTO.setLateBy(matchingBioData.getLateBy() != null ? matchingBioData.getLateBy() : null);
	                            bioMaTO.setDuration(matchingBioData.getDuration() != null ? matchingBioData.getDuration() : null);
	                            bioMaTO.setInTime(matchingBioData.getInTime() != null ? matchingBioData.getInTime() : null);
	                            bioMaTO.setOutTime(matchingBioData.getOutTime() != null ? matchingBioData.getOutTime() : null);
	                            bioMaTO.setShiftDuration(matchingBioData.getShiftDuration() != null ? matchingBioData.getShiftDuration() : null);

	                            // Add the populated BioMaTO object to the biolist
	                            biolist.add(bioMaTO);
	                        });
	                });

	            // Update the response with the populated biolist
	            if (!biolist.isEmpty()) {
	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(biolist);

	                // Log the successful API response
	                apiLogInfo.setApiResponse("dtoList: " + biolist);
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            } else {
	                // Handle the no-match scenario
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Attendance list is empty.");

	                // Log the failure
	                apiLogInfo.setApiResponse("Attendance list is empty");
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
	
	
    
	public List<Object[]> getCustomLeaveTrendAnalysis(LocalDate fetchDate, String typeOfLeave, List<CustomFilterDTO> queryList) {
		Session session = entityManager.unwrap(Session.class);
		try {
			String filterConditions = createQueryForLeaveTrend(queryList).toString();
			String q = "SELECT "
					+ "    ld.emp_id, ld.department, ld.employee_name, ld.from_date, ld.to_date, ld.status, "
					+ "    ld.from_date_day_type, ld.to_date_day_type, ld.employment_type, ld.manager_name, "
					+ "    ld.type_of_leave, ld.leave_date "
					+ "FROM ( "
					+ "    SELECT "
					+ "        " + EmployeeEmploymentIdUtil.sqlCaseFormattedEmploymentId("e", "employeement_id") + " AS emp_id, "
					+ "        d.name AS department, "
					+ "        e.name AS employee_name, "
					+ "        el.from_date, el.to_date, ls.status, "
					+ "        el.from_date_day_type, el.to_date_day_type, "
					+ "        CASE "
					+ "            WHEN e.is_apmosys_product = 'true' AND e.is_consultant = 'true' THEN 'Apmosys Product Consultant' "
					+ "            WHEN e.is_apmosys_product = 'true' THEN 'Apmosys Product' "
					+ "            WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' "
					+ "            WHEN e.is_consultant = 'true' THEN 'Consultant' "
					+ "            ELSE 'Regular' "
					+ "        END AS employment_type, "
					+ "        e2.name AS manager_name, "
					+ "        DATE(:fetch_date) AS leave_date, "
					+ "        CASE "
					+ "            WHEN el.leave_type_master_id = 1 THEN 'Paid Leave' "
					+ "            WHEN el.leave_type_master_id = 2 THEN 'Casual Leave' "
					+ "            WHEN el.leave_type_master_id = 3 THEN 'Leave Without Pay' "
					+ "            WHEN el.leave_type_master_id = 4 THEN 'Compensatory Off' "
					+ "            WHEN el.leave_type_master_id = 5 THEN 'Maternity Leave' "
					+ "        END AS type_of_leave, "
					+ "        CASE "
					+ "            WHEN el.from_date <= DATE(:fetch_date) AND el.to_date >= DATE(:fetch_date) THEN 1 "
					+ "            ELSE 0 "
					+ "        END AS is_on_leave "
					+ "    FROM employee_leave el "
					+ "    INNER JOIN employee e ON e.emp_id = el.emp_id "
					+ "    INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id "
					+ "    INNER JOIN department d ON d.dept_id = jr.dept_id "
					+ "    INNER JOIN leave_status ls ON ls.leave_status_id = el.leave_status_id "
					+ "    LEFT JOIN employee e2 ON e2.emp_id = e.manager_id " 
					+ "    WHERE el.leave_status_id = 2 "
					+ filterConditions
					+ ") AS ld "
					+ "WHERE ld.is_on_leave > 0 "
					+ "AND ld.type_of_leave = :type_of_leave;";

			System.out.println("Executing Leave Trend Analysis Query: " + q);
			Query query = session.createSQLQuery(q);
			query.setParameter("fetch_date", fetchDate);
			query.setParameter("type_of_leave", typeOfLeave);
			return query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
	
	//String Query Builder for Employee Dashboard
	
	public StringBuilder createQueryForEmployeeDashboard(List<CustomFilterDTO> queryList) {
		StringBuilder query = new StringBuilder("");
		if (queryList == null || queryList.isEmpty()) {
			return query;
		}

		for (CustomFilterDTO dto : queryList) {
			
			if ("Employee Id".equals(dto.getColumn()) || "employmentID".equals(dto.getColumn())) {
				if (dto.getOperator() != null && dto.getOperator().equalsIgnoreCase("like")) {
					String likeValue = "%" + dto.getValue() + "%";
					query.append(" AND ");
					appendFormattedEmploymentIdLikeFilter(query, likeValue, "e");
				} else {
					appendPrefixedEmployeeIdFilter(query, dto.getValue(), dto.getOperator(), dto.getConjunction(), true);
				}
				continue;
			}

			if ("employeeType".equals(dto.getColumn()) || "Employee Type".equals(dto.getColumn())) {
				query.append(" AND ");
				if (dto.getOperator() != null && dto.getOperator().equalsIgnoreCase("like")) {
					String likeValue = "%" + dto.getValue() + "%";
					query.append(buildEmployeeTypeLikeFilterSql(likeValue, "e"));
				} else {
					query.append(buildEmployeeTypeFilterSql(dto.getValue(), "e"));
				}
				continue;
			}
			
			if (dto.getOperator() != null && dto.getOperator().equalsIgnoreCase("like")) {
				dto.setValue("%" + dto.getValue() + "%");
			}

			String columnAlias = "";
			switch (dto.getColumn()) {
//				case "Employee Id":       columnAlias = "e.employeement_id"; break;
				case "Full Name":         columnAlias = "e.name"; break;
				case "Employment Status": columnAlias = "e.employmentstatus"; break;
				case "Department":        columnAlias = "d.name"; break;
				case "Manager Name":      columnAlias = "m.name"; break;
				case "Project Name":      columnAlias = "p.project_name"; break;
				case "Client Name":       columnAlias = "c.client_name"; break;
				case "Gender":            columnAlias = "e.gender"; break;
				case "Work Location":     columnAlias = "e.work_location"; break;
				case "Job Role":  		  columnAlias = "jr.name" ; break;//employeeType, city,marital status,bankName
				case "Date of Joining":	  columnAlias = "e.date_of_joining";break;
				case "Probation Period":   columnAlias = "e.probation_period";break;
				case "State": 			columnAlias = "e.state";break;
				case "Experience": 			columnAlias = "e.experience";break;
				case "Marital Status": 			columnAlias = "e.marital_status";break;
				case "Notice Period": 			columnAlias = "e.notice_period";break;
				
				
				default: continue; 
			}

			query.append(" AND ").append(columnAlias).append(" ").append(dto.getOperator()).append(" '")
				 .append(dto.getValue()).append("' ");
		}
		return query;
	}
	
	
	// Get Count from the employee Dashboard
	
	public List<Object[]> getCustomEmployeesByEmploymentType(ReportsQueryDTO request) {
		Session session = entityManager.unwrap(Session.class);
		try {
			// 1. Create the custom filter condition string
			String customFilterConditions = createQueryForEmployeeDashboard(request.getQueryList()).toString();

			// 2. Build the base native SQL query
			String q = "SELECT distinct " +
					EmployeeEmploymentIdUtil.sqlCaseFormattedEmploymentId("e", "employeement_id") + " as EMP_ID, " +
					"CASE WHEN e.is_apmosys_product = 'true' AND e.is_consultant = 'true' THEN 'Apmosys Product Consultant' " +
					"     WHEN e.is_apmosys_product = 'true' THEN 'Apmosys Product' " +
					"     WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' " +
					"     WHEN e.is_consultant = 'true' THEN 'Consultant' " +
					"     ELSE 'Regular' " +
					"END AS EMPLOYMENT_TYPE, " +
					"e.name AS NAME, " +
					"e.experience AS EXPERIENCE, " +
					"d.name AS DEPARTMENT_NAME, " +
					"e.email AS EMAIL_ID, " +
					"m.name AS MANAGER_NAME, " +
					"e.billable AS BILLABLE, " +
					"e.billable_type AS BILLABLE_TYPE, " +
					"GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES, " +
					"GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES, " +
					"e.date_of_joining AS DATE_OF_JOINING, " +
					"e.mobile_no AS MOBILE_NO, " +
					"e.employmentstatus AS STATUS, " +
					"e.total_experience AS TOTAL_EXPERIENCE, " +
					"e.gender AS GENDER, " +
					"e.work_location AS WORK_LOCATION, " +
					"TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) as age, " +
					"e.is_user_info_updated AS KYC " +
					"FROM employee e " +
					"LEFT JOIN job_role jr on e.job_role_id = jr.job_role_id " +
					"LEFT JOIN department d on jr.dept_id = d.dept_id " +
					"LEFT JOIN employee m on m.emp_id = e.manager_id " + // Alias 'm' for manager
					"LEFT JOIN employee_team_mapping etm on e.emp_id = etm.emp_id AND etm.active != '0' " +
					"LEFT JOIN teams t on etm.team_id = t.team_id AND t.is_active = 'Y' " +
					"LEFT JOIN projects p on t.project_id = p.project_id AND p.active = 'true' " +
					"LEFT JOIN clients c on p.client_id = c.client_id " +
					"WHERE e.employmentstatus != 'InActive' AND e.emp_id NOT BETWEEN 1 AND 6 " +
					"AND ((:apprentice = true AND e.is_apprenticeship = 'true') " +
					"     OR (:consultant = true AND e.is_consultant = 'true' AND COALESCE(e.is_apmosys_product, 'false') != 'true') " +
					"     OR (:regular = true AND ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false' AND COALESCE(e.is_apmosys_product, 'false') != 'true') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = ''))) " +
					"     OR (:apmosysProduct = 'true' AND e.is_apmosys_product = 'true' AND COALESCE(e.is_consultant, 'false') != 'true') " +
					"     OR (:apmosysProductConsultant = true AND e.is_apmosys_product = 'true' AND e.is_consultant = 'true') " +
					"     OR (:probation = true AND e.employmentstatus = 'Probation' AND TIMESTAMPDIFF(DAY, e.date_of_joining, current_date()) > 180) " +
					"     OR (:allEmp = true)) " +
					// 3. Append the dynamic filter conditions
					customFilterConditions +
					"GROUP BY e.emp_id";

			System.out.println("Executing Employee by Type Query: " + q);
			Query query = session.createSQLQuery(q);
			
			// 4. Set the named parameters for employment types
			query.setParameter("apprentice", request.isApprentice());
			query.setParameter("consultant", request.isConsultant());
			query.setParameter("regular", request.isRegular());
			query.setParameter("probation", request.isProbation());
			query.setParameter("apmosysProduct", request.getIsApmosysProduct() != null ? request.getIsApmosysProduct() : "false");
			query.setParameter("apmosysProductConsultant", request.isApmosysProductConsultant());
			query.setParameter("allEmp", request.isAllEmp());

			return query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
	
	
	//+++++++++++++++++++COUNT FOR REPORT DASHBOARD++++++++++++++++++++++++++++++++++//
	
	
	
	//1 - LEAVE TREND ANALYSIS
	
	public StringBuilder createQueryForLeaveTrend(List<CustomFilterDTO> queryList) {
	    StringBuilder query = new StringBuilder("");
	    if (queryList == null || queryList.isEmpty()) {
	        return query;
	    }

	    for (CustomFilterDTO dto : queryList) {
	        if (dto.getOperator() != null && dto.getOperator().equalsIgnoreCase("like")) {
	            dto.setValue("%" + dto.getValue() + "%");
	        }

	        switch (dto.getColumn()) {
	            case "Department": {
	                query.append(" AND d.name ").append(dto.getOperator()).append(" '")
	                        .append(dto.getValue()).append("' ");
	                break;
	            }
	            case "Manager Name": {
	                // Note: The main query must have a join to employee table with alias e2 for manager
	                query.append(" AND e2.name ").append(dto.getOperator()).append(" '")
	                        .append(dto.getValue()).append("' ");
	                break;
	            }
	            case "Employment Status": {
	                 query.append(" AND e.employmentstatus ").append(dto.getOperator()).append(" '")
	                        .append(dto.getValue()).append("' ");
	                break;
	            }
	            case "Full Name": {
	                 query.append(" AND e.name ").append(dto.getOperator()).append(" '")
	                        .append(dto.getValue()).append("' ");
	                break;
	            }
//	             case "Employee Id": {
//	                query.append(" AND e.employeement_id ").append(dto.getOperator()).append(" '")
//	                        .append(dto.getValue()).append("' ");
//	                break;
//	            }
	             case "Employee Id": {
	 			    String value = dto.getValue();
	 			    String operator = dto.getOperator();
	 			    String conjunction = dto.getConjunction();

	 			    if (value != null && operator.equals("=")) {
	 			        if (value.toUpperCase().startsWith("APCS-")) {
	 			            String id = value.substring(5);
	 			            query.append(" AND e.employeement_id = '").append(id).append("' ")
	 			                 .append("AND e.is_apmosys_product = 'true' AND e.is_consultant = 'true' ")
	 			                 .append(conjunction);
	 			        } else if (value.toUpperCase().startsWith("AP-")) {
	 			            String id = value.substring(3);
	 			            query.append(" AND e.employeement_id = '").append(id).append("' ")
	 			                 .append("AND e.is_apmosys_product = 'true' AND COALESCE(e.is_consultant, 'false') != 'true' ")
	 			                 .append(conjunction);
	 			        } else if (value.toUpperCase().startsWith("CS-")) {
	 			            String id = value.substring(3);
	 			            query.append(" AND e.employeement_id = '").append(id).append("' ")
	 			                 .append("AND e.is_consultant = 'true' AND COALESCE(e.is_apmosys_product, 'false') != 'true' ")
	 			                 .append(conjunction);
	 			        } else if (value.toUpperCase().startsWith("A-")) {
	 			            String id = value.substring(2);
	 			            query.append(" AND e.employeement_id = '").append(id).append("' ")
	 			                 .append("AND COALESCE(e.is_consultant, 'false') != 'true' ")
	 			                 .append("AND COALESCE(e.is_apmosys_product, 'false') != 'true' ")
	 			                 .append(conjunction);
	 			        } else {
	 			            query.append(" AND e.employeement_id ").append(operator).append(" '")
	 			                 .append(value).append("' ").append(conjunction);
	 			        }
	 			    } else {
	 			        query.append(" AND e.employeement_id ").append(operator).append(" '")
	 			             .append(value).append("' ").append(conjunction);
	 			    }
	 			    break;
	 			}
	            // Add other filterable cases as needed, ensuring they use the correct table aliases (e, d, e2, etc.)
	            default:
	                break;
	        }
	    }
	    return query;
	}
	
	public List<Object[]> getCustomLeaveTrendDetails(List<CustomFilterDTO> queryList) {
		Session session = entityManager.unwrap(Session.class);
		try {
			String filterConditions = createQueryForLeaveTrend(queryList).toString();
			String q = "SELECT "
					+ "    ld.type_of_leave, "
					+ "    ld.leave_date,   "
					+ "    SUM(ld.no_of_days) AS total_leave_days "
					+ "FROM ( "
					+ "    SELECT  "
					+ "        DATE_ADD(CURDATE(), INTERVAL -n DAY) AS leave_date, "
					+ "        CASE WHEN el.leave_type_master_id = 1 THEN 'Paid Leave' "
					+ "             WHEN el.leave_type_master_id = 2 THEN 'Casual Leave' "
					+ "             WHEN el.leave_type_master_id = 3 THEN 'Leave Without Pay' "
					+ "             WHEN el.leave_type_master_id = 4 THEN 'Compensatory Off' "
					+ "             WHEN el.leave_type_master_id = 5 THEN 'Maternity Leave' "
					+ "        END AS type_of_leave, "
					+ "        CASE  "
					+ "            WHEN WEEKDAY(DATE_ADD(CURDATE(), INTERVAL -n DAY)) = 6 " 
					+ "                 OR (DAYOFWEEK(DATE_ADD(CURDATE(), INTERVAL -n DAY)) = 7  "
					+ "                     AND (DAY(DATE_ADD(CURDATE(), INTERVAL -n DAY)) BETWEEN 8 AND 14  "
					+ "                          OR DAY(DATE_ADD(CURDATE(), INTERVAL -n DAY)) BETWEEN 22 AND 28)) " 
					+ "            THEN 0 "
					+ "            WHEN el.from_date <= DATE_ADD(CURDATE(), INTERVAL -n DAY)  "
					+ "                 AND el.to_date >= DATE_ADD(CURDATE(), INTERVAL -n DAY)  "
					+ "            THEN 1 " 
					+ "            ELSE 0 "
					+ "        END AS no_of_days "
					+ "    FROM employee_leave el "
				
					+ "    INNER JOIN employee e ON e.emp_id = el.emp_id "
					+ "    INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id "
					+ "    INNER JOIN department d ON d.dept_id = jr.dept_id "
					+ "    LEFT JOIN employee e2 ON e2.emp_id = e.manager_id, "
					+ "    (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4  "
					+ "     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7) AS days_range " 
					+ "    WHERE el.from_date <= CURDATE() AND el.leave_status_id = 2 "
					+ filterConditions
					+ ") AS ld "
					+ "WHERE ld.no_of_days > 0 "
					+ "GROUP BY ld.type_of_leave, ld.leave_date "
					+ "ORDER BY ld.leave_date;";

			System.out.println("Executing Leave Trend Query: " + q);
			Query query = session.createSQLQuery(q);
			return query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
	
	//2 - EMPLOYEE GRAPH SUMMARY
	
	public List<Object[]> getCustomGraphEmployeeSummary(ReportsQueryDTO request) {
		Session session = entityManager.unwrap(Session.class);
		try {
			// 1. Reuse the filter builder to create the custom WHERE clause conditions
			String customFilterConditions = createQueryForEmployeeDashboard(request.getQueryList()).toString();

			// 2. Build the full native SQL query
			String q = "WITH employee_data AS (\n"
					+ "    SELECT distinct \n"
					+ "        e.gender, e.employmentstatus, e.experience, e.billable, e.date_of_birth, \n"
					+ "        e.date_of_joining, e.is_apprenticeship, e.is_consultant, e.billable_type, e.emp_id, e.is_apmosys_product \n"
					+ "    FROM employee e \n"
					+ "    LEFT JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
					+ "	   LEFT JOIN department d on jr.dept_id = d.dept_id\n"
					+ "	   LEFT JOIN employee m on m.emp_id = e.manager_id \n" // Alias 'm' for manager
					+ "	   LEFT JOIN employee_team_mapping etm on e.emp_id = etm.emp_id and etm.active != '0'\n"
					+ "	   LEFT JOIN teams t on etm.team_id = t.team_id and t.is_active = 'Y'\n"
					+ "	   LEFT JOIN projects p on t.project_id = p.project_id and p.active = 'true'\n"
                    + "    LEFT JOIN clients c on p.client_id = c.client_id \n" // Added clients join
					+ "    WHERE e.emp_id NOT BETWEEN 1 AND 6\n"
					// 3. Inject the dynamic filter conditions here
					+ customFilterConditions
					+ ")\n"
					// The rest of the query remains the same as it aggregates the filtered data
					+ "SELECT\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable = 'Yes' THEN 1 ELSE 0 END) AS Yes,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable = 'No' THEN 1 ELSE 0 END) AS No,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable = 'Other' THEN 1 ELSE 0 END) AS Billable_Other,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND TIMESTAMPDIFF(YEAR, date_of_birth, CURRENT_DATE()) BETWEEN 18 AND 25 THEN 1 ELSE 0 END) AS 'Years_18_25',\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND TIMESTAMPDIFF(YEAR, date_of_birth, CURRENT_DATE()) BETWEEN 26 AND 35 THEN 1 ELSE 0 END) AS 'Years_26_35',\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND TIMESTAMPDIFF(YEAR, date_of_birth, CURRENT_DATE()) BETWEEN 36 AND 45 THEN 1 ELSE 0 END) AS 'Years_36_45',\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND TIMESTAMPDIFF(YEAR, date_of_birth, CURRENT_DATE()) >= 46 THEN 1 ELSE 0 END) AS 'Years_above_45',\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND gender = 'Male' THEN 1 ELSE 0 END) AS Male,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND gender = 'Female' THEN 1 ELSE 0 END) AS Female,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND gender = 'Other' THEN 1 ELSE 0 END) AS Gender_Other,\n"
					+ "    SUM(CASE WHEN employmentstatus = 'Confirmed' THEN 1 ELSE 0 END) AS Confirmed,\n"
					+ "    SUM(CASE WHEN employmentstatus = 'Resigned' THEN 1 ELSE 0 END) AS Resigned,\n"
					+ "    SUM(CASE WHEN employmentstatus = 'Probation' THEN 1 ELSE 0 END) AS Probation,\n"
					+ "    SUM(CASE WHEN employmentstatus = 'Retain' THEN 1 ELSE 0 END) AS Retain,\n"
					+ "    SUM(CASE WHEN employmentstatus = 'InActive' THEN 1 ELSE 0 END) AS InActive,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND experience = 'Experienced' THEN 1 ELSE 0 END) AS Experienced,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND experience = 'Fresher' THEN 1 ELSE 0 END) AS Fresher,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 1 THEN 1 ELSE 0 END) AS Apprentice_Years_0_1,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 1 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 2 THEN 1 ELSE 0 END) AS Apprentice_Years_1_2,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 2 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 5 THEN 1 ELSE 0 END) AS Apprentice_Years_2_5,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 5 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 10 THEN 1 ELSE 0 END) AS Apprentice_Years_5_10,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 10 THEN 1 ELSE 0 END) AS Apprentice_Years_Above_10,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 1 THEN 1 ELSE 0 END) AS Employees_Years_0_1,\n" // -- CORRECTED LOGIC
			        + "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 1 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 2 THEN 1 ELSE 0 END) AS Employees_Years_1_2,\n" // -- CORRECTED LOGIC
			        + "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 2 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 5 THEN 1 ELSE 0 END) AS Employees_Years_2_5,\n" // -- CORRECTED LOGIC
			        + "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 5 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 10 THEN 1 ELSE 0 END) AS Employees_Years_5_10,\n" // -- CORRECTED LOGIC
			        + "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 10 THEN 1 ELSE 0 END) AS Employees_Years_Above_10,\n" // -- CORRECTED LOGIC
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' AND COALESCE(is_apmosys_product, 'false') != 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 1 THEN 1 ELSE 0 END) AS Consultant_Years_0_1,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' AND COALESCE(is_apmosys_product, 'false') != 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 1 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 2 THEN 1 ELSE 0 END) AS Consultant_Years_1_2,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' AND COALESCE(is_apmosys_product, 'false') != 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 2 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 5 THEN 1 ELSE 0 END) AS Consultant_Years_2_5,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' AND COALESCE(is_apmosys_product, 'false') != 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 5 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 10 THEN 1 ELSE 0 END) AS Consultant_Years_5_10,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' AND COALESCE(is_apmosys_product, 'false') != 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 10 THEN 1 ELSE 0 END) AS Consultant_Years_Above_10,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable_type = 'Fixed Cost' THEN 1 ELSE 0 END) AS Fixed_Cost,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable_type = 'TNM' THEN 1 ELSE 0 END) AS TNM,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable_type = 'Bench' THEN 1 ELSE 0 END) AS Bench,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable_type = 'Shadow' THEN 1 ELSE 0 END) AS Shadow,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND billable_type = 'InternalRNDProducts' THEN 1 ELSE 0 END) AS InternalRNDProducts,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' THEN 1 ELSE 0 END) AS Total_Employee,\n"
					+ "    SUM(CASE WHEN employmentstatus = 'Probation' and TIMESTAMPDIFF(DAY, date_of_joining, CURRENT_DATE()) > 180 then 1 ELSE 0 end) as probation_count,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apprenticeship = 'true' THEN 1 ELSE 0 END) as apprentice_count,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_consultant = 'true' THEN 1 ELSE 0 END) as consultant_count,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND ((is_consultant = 'false' AND is_apprenticeship = 'false' AND is_apmosys_product = 'false') OR (COALESCE(is_consultant, '') = '' AND COALESCE(is_apprenticeship, '') = '')) THEN 1 ELSE 0 END) as regular_count,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' THEN 1 ELSE 0 END) as apmosys_product_count,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND is_consultant = 'true' THEN 1 ELSE 0 END) as apmosys_product_consultant_count,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND COALESCE(is_consultant, 'false') != 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 >= 0 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 1 THEN 1 ELSE 0 END) AS Apmosys_Product_Years_0_1,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND COALESCE(is_consultant, 'false') != 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 1 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 2 THEN 1 ELSE 0 END) AS Apmosys_Product_Years_1_2,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND COALESCE(is_consultant, 'false') != 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 2 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 5 THEN 1 ELSE 0 END) AS Apmosys_Product_Years_2_5,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND COALESCE(is_consultant, 'false') != 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 5 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 10 THEN 1 ELSE 0 END) AS Apmosys_Product_Years_5_10,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND COALESCE(is_consultant, 'false') != 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 10 THEN 1 ELSE 0 END) AS Apmosys_Product_Years_Above_10,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND is_consultant = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 >= 0 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 1 THEN 1 ELSE 0 END) AS Apmosys_Product_Consultant_Years_0_1,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND is_consultant = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 1 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 2 THEN 1 ELSE 0 END) AS Apmosys_Product_Consultant_Years_1_2,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND is_consultant = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 2 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 5 THEN 1 ELSE 0 END) AS Apmosys_Product_Consultant_Years_2_5,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND is_consultant = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 5 AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 <= 10 THEN 1 ELSE 0 END) AS Apmosys_Product_Consultant_Years_5_10,\n"
					+ "    SUM(CASE WHEN employmentstatus != 'InActive' AND is_apmosys_product = 'true' AND is_consultant = 'true' AND TIMESTAMPDIFF(MONTH, date_of_joining, CURRENT_DATE()) / 12 > 10 THEN 1 ELSE 0 END) AS Apmosys_Product_Consultant_Years_Above_10\n"
					+ "FROM employee_data";

			System.out.println("Executing Graph Summary Query: " + q);
			Query query = session.createSQLQuery(q);
			return query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
	//3 - Work Location Summary
	
	public List<Object[]> getCustomWorkLocationDetails(ReportsQueryDTO request) {
		Session session = entityManager.unwrap(Session.class);
		try {
			// 1. Build the dynamic WHERE clause from the filters.
			// We can reuse the same filter builder.
			String customFilterConditions = createQueryForEmployeeDashboard(request.getQueryList()).toString();

			// 2. Construct the full native SQL query
			// String q = "SELECT distinct cl.client_location, count(distinct e.emp_id) "
			// 		+ "FROM employee e "
			// 		+ "INNER JOIN employee_team_mapping etm on etm.emp_id = e.emp_id "
			// 		+ "INNER JOIN employee_timesheets et ON et.emp_id = etm.emp_id "
			// 		+ "INNER JOIN employee_timesheet_activities_mapping etam ON etam.timesheet_id = et.timesheet_id "
			// 		+ "INNER JOIN activities a ON a.activity_id = etam.activity_id "
			// 		+ "INNER JOIN teams t on etm.team_id = t.team_id and etm.team_id = a.team_id "
			// 		+ "INNER JOIN projects p on p.project_id = t.project_id "
			// 		+ "INNER JOIN client_locations cl on cl.client_location_id = etam.client_location_id "
			// 		// Other necessary joins for filtering
			// 		+ "LEFT JOIN job_role jr on e.job_role_id = jr.job_role_id "
			// 		+ "LEFT JOIN department d on jr.dept_id = d.dept_id "
			// 		+ "LEFT JOIN employee m on m.emp_id = e.manager_id "
			// 		+ "LEFT JOIN clients c on p.client_id = c.client_id "
			// 		+ "WHERE e.employmentstatus != 'InActive' and etm.active != 0 "
			// 		+ "AND t.is_active = 'Y' and p.active = 'true' "
			// 		+ "AND e.emp_id not between 1 and 6 "
			// 		// 3. Inject the dynamic filter conditions here.
			// 		+ customFilterConditions
			// 		+ "GROUP BY cl.client_location";

			String q = 	"SELECT cl.client_location, COUNT(DISTINCT e.emp_id) "
						+ "FROM employee e "
						+ "INNER JOIN employee_team_mapping etm ON etm.emp_id = e.emp_id "
						+ "INNER JOIN employee_timesheets_new et ON et.emp_id = e.emp_id  "
						+ "INNER JOIN employee_timesheet_location_mapping etlm ON etlm.timesheet_id = et.timesheet_id "
						+ "INNER JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.location_mapping_id = etlm.location_mapping_id "
						+ "INNER JOIN employee_timesheet_activities_mapping_new etamn ON etamn.timesheet_id = et.timesheet_id "
						+ "    AND etamn.location_mapping_id = etlm.location_mapping_id AND etamn.project_id = pts.project_id "
						+ "INNER JOIN activities a ON a.activity_id = etamn.activity_id "
						+ "INNER JOIN teams t ON etm.team_id = t.team_id AND a.team_id = t.team_id "
						+ " INNER JOIN projects p ON p.project_id = t.project_id AND pts.project_id = p.project_id "
  						+ " INNER JOIN client_locations cl ON cl.client_location_id = pts.client_location_id "
  						+ " LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id "
  						+ " LEFT JOIN department d ON jr.dept_id = d.dept_id "
  						+ " LEFT JOIN employee m ON m.emp_id = e.manager_id "
  						+ " LEFT JOIN clients c ON p.client_id = c.client_id "
						+ " WHERE e.employmentstatus != 'InActive' "
						+ "   AND etm.active != 0 "
						+ "   AND t.is_active = 'Y' " 
						+ "   AND p.active = 'true' "
 						+ "  AND e.emp_id NOT BETWEEN 1 AND 6 "
						+ 	 customFilterConditions
						+ " GROUP BY cl.client_location ";

			System.out.println("Executing Work Location Query: " + q);
			Query query = session.createSQLQuery(q);
			return query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
	
	//----------------------MODAL TABLE ------------------------------//
	
	public List<Object[]> getCustomPieGraphListSummary(PieParamDTO pieParamDto, List<CustomFilterDTO> customFilters) {
	    Session session = entityManager.unwrap(Session.class);
	    try {
	        // 1. Build the dynamic WHERE clause from the general custom filters
	        String customFilterConditions = createQueryForEmployeeDashboard(customFilters).toString();
	        
	        // Debug logging
	        System.out.println("Custom Filter Conditions: " + customFilterConditions);
	        System.out.println("Number of custom filters: " + (customFilters != null ? customFilters.size() : 0));
	        
	        // 2. Build the base native SQL query
	        String q = "SELECT DISTINCT "
	        		+ EmployeeEmploymentIdUtil.sqlCaseFormattedEmploymentIdWithAp2lEmail("e", "employeement_id") + " AS Employeement_Id, "
	        		+ "CASE "
	        		+ "    WHEN e.is_apmosys_product = 'true' AND e.is_consultant = 'true' THEN 'Apmosys Product Consultant' "
	        		+ "    WHEN e.is_apmosys_product = 'true' OR e.email LIKE '%ap2l.ai%' THEN 'ApmosysProduct' "
	        		+ "    WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' "
	        		+ "    WHEN ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) THEN 'Regular' "
	        		+ "    WHEN e.is_consultant = 'true' THEN 'Consultant' "
	        		+ "END AS EMPLOYMENT_TYPE, "
	        		+ "e.name AS NAME, "
	        		+ "e.experience AS EXPERIENCE, "
	        		+ "d.name AS DEPARTMENT_NAME, "
	        		+ "e.email AS EMAIL_ID, "
	        		+ "m.name AS MANAGER_NAME, "
	        		+ "e.billable AS BILLABLE, "
	        		+ "e.billable_type AS BILLABLE_TYPE, "
	        		+ "GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES, "
	        		+ "GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES, "
	        		+ "e.date_of_joining AS DATE_OF_JOINING, "
	        		+ "e.mobile_no AS MOBILE_NO, "
	        		+ "e.employmentstatus AS STATUS, "
	        		+ "e.total_experience AS TOTAL_EXPERIENCE, "
	        		+ "e.gender AS GENDER, "
	        		+ "e.work_location AS WORK_LOCATION, "
	        		+ "TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) AS age, "
	        		+ "e.is_user_info_updated AS KYC, "
	        		+ "m.emp_id AS MANAGER_ID, "
	        		+ "e.emp_id AS Emp_Id "
	        		+ "FROM employee e "
	        		+ "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id "
	        		+ "LEFT JOIN department d ON jr.dept_id = d.dept_id "
	        		+ "LEFT JOIN employee m ON m.emp_id = e.manager_id "
	        		+ "LEFT JOIN employee_team_mapping etm ON e.emp_id = etm.emp_id AND etm.active != '0' "
	        		+ "LEFT JOIN teams t ON etm.team_id = t.team_id AND t.is_active = 'Y' "
	        		+ "LEFT JOIN projects p ON t.project_id = p.project_id AND p.active = 'true' "
	        		+ "LEFT JOIN clients c ON p.client_id = c.client_id "
	        		+ "WHERE 1=1 "
	        		+ "AND ((:in_active_flag = 1 AND e.employmentstatus = 'InActive') OR (:in_active_flag = 0 AND e.employmentstatus != 'InActive')) "
	        		+ "AND e.emp_id NOT BETWEEN 1 AND 6 "
	        		+ "AND (:billable IS NULL OR e.billable = :billable) "
	        		+ "AND (:billable_type IS NULL OR e.billable_type = :billable_type) "
	        		+ "AND (:employmentstatus IS NULL OR e.employmentstatus = :employmentstatus) "
	        		+ "AND (:gender IS NULL OR e.gender = :gender) "
	        		+ "AND (:experience IS NULL OR e.experience = :experience) "
	        		+ "AND ((:lower_age IS NULL AND :upper_age IS NULL) OR TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) BETWEEN :lower_age AND :upper_age) "
	        		+ customFilterConditions
	        		+ " GROUP BY e.emp_id";

	        System.out.println("Final Query: " + q);
	        
	        Query query = session.createSQLQuery(q);

	        // 4. Set the named parameters from the PieParamDTO
	        query.setParameter("in_active_flag", pieParamDto.getInActiveFlag());
	        query.setParameter("billable", pieParamDto.getBillable());
	        query.setParameter("billable_type", pieParamDto.getBillableType());
	        query.setParameter("employmentstatus", pieParamDto.getEmploymentstatus());
	        query.setParameter("gender", pieParamDto.getGender());
	        query.setParameter("experience", pieParamDto.getExperience());
	        query.setParameter("lower_age", pieParamDto.getLowerAge());
	        query.setParameter("upper_age", pieParamDto.getUpperAge());

	        // Debug: Log parameter values
	        System.out.println("Parameters - Gender: " + pieParamDto.getGender() + 
	                          ", InActiveFlag: " + pieParamDto.getInActiveFlag() +
	                          ", Billable: " + pieParamDto.getBillable());

	        List<Object[]> results = query.getResultList();
	        System.out.println("Query returned " + results.size() + " results");
	        
	        return results;

	    } catch (Exception e) {
	        System.err.println("Error in getCustomPieGraphListSummary: " + e.getMessage());
	        e.printStackTrace();
	        return new ArrayList<>();
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	}
	

	
	public List<Object[]> getCustomEmployeesByExperience(ReportsQueryDTO request) {
		Session session = entityManager.unwrap(Session.class);
		try {
		    // 1. Build the dynamic WHERE clause from the general custom filters.
		    String customFilterConditions = createQueryForEmployeeDashboard(request.getQueryList()).toString();

		    // 2. Build the base native SQL query
		    String q = "SELECT distinct "
		    		+ EmployeeEmploymentIdUtil.sqlCaseFormattedEmploymentId("e", "employeement_id") + " as EMPLOYEEMENT_ID, "
		    		+ " CASE WHEN e.is_apmosys_product = 'true' AND e.is_consultant = 'true' THEN 'Apmosys Product Consultant' "
		    		+ "      WHEN e.is_apmosys_product = 'true' THEN 'Apmosys Product' "
		    		+ "      WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' "
		    		+ "      WHEN e.is_consultant = 'true' THEN 'Consultant' "
		    		+ "      ELSE 'Regular' "
		    		+ " END AS EMPLOYMENT_TYPE, "
		    		+ " e.name AS NAME, "
		    		+ " e.experience AS EXPERIENCE, "
		    		+ " d.name AS DEPARTMENT_NAME, "
		    		+ " e.email AS EMAIL_ID, "
		    		+ " m.name AS MANAGER_NAME, "
		    		+ " e.billable AS BILLABLE, "
		    		+ " e.billable_type AS BILLABLE_TYPE, "
		    		+ " GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES, "
		    		+ " GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES, "
		    		+ " e.date_of_joining AS DATE_OF_JOINING, "
		    		+ " e.mobile_no AS MOBILE_NO, "
		    		+ " e.employmentstatus AS STATUS, "
		    		+ " e.total_experience AS TOTAL_EXPERIENCE, "
		    		+ " e.gender AS GENDER, "
		    		+ " e.work_location AS WORK_LOCATION, "
		    		+ " TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) as age, "
		    		+ " e.is_user_info_updated AS KYC, "
		    		+ " m.emp_id AS MANAGER_ID, "
		    		+ " e.emp_id AS EMP_ID "
		    		+ "FROM employee e "
		    		+ "LEFT JOIN job_role jr on e.job_role_id = jr.job_role_id "
		    		+ "LEFT JOIN department d on jr.dept_id = d.dept_id "
		    		+ "LEFT JOIN employee m on m.emp_id = e.manager_id "
		    		+ "LEFT JOIN employee_team_mapping etm on e.emp_id = etm.emp_id and etm.active != '0' "
		    		+ "LEFT JOIN teams t on etm.team_id = t.team_id and t.is_active = 'Y' "
		    		+ "LEFT JOIN projects p on t.project_id = p.project_id and p.active = 'true' "
		    		+ "LEFT JOIN clients c on p.client_id = c.client_id "
		    		+ "WHERE 1=1 AND e.employmentstatus != 'InActive' "
		    		+ " AND e.emp_id NOT BETWEEN 1 AND 6 "
		    		// ***** UPDATED LOGIC WITH APMOSYS PRODUCT SUPPORT *****
		    		+ " AND ("
		    		+ " (:employee_type = 'apmosys_product_consultant' AND e.is_apmosys_product = 'true' AND e.is_consultant = 'true') "
		    		+ " OR "
		    		+ " (:employee_type = 'apmosys_product' AND e.is_apmosys_product = 'true' AND COALESCE(e.is_consultant, 'false') != 'true') "
		    		+ " OR "
		    		+ " (:employee_type = 'apprentice' AND e.is_apprenticeship = 'true') "
		    		+ " OR "
		    		+ " (:employee_type = 'consultant' AND e.is_consultant = 'true' AND COALESCE(e.is_apmosys_product, 'false') != 'true') "
		    		+ " OR "
		    		+ " (:employee_type = 'regular' AND ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = ''))) "
		    		+ " ) "
		    		// *****************************************
		    		+ " AND (TIMESTAMPDIFF(MONTH, e.date_of_joining, CURRENT_DATE())/12 > :lower_value AND TIMESTAMPDIFF(MONTH, e.date_of_joining, CURRENT_DATE())/12 <= :upper_value) "
		    		// 3. Inject the dynamic filter conditions
		    		+ customFilterConditions
		    		+ "GROUP BY e.emp_id";

		    System.out.println("Executing Experience Drill-down Query: " + q);
		    Query query = session.createSQLQuery(q);
		    
		    // 4. Set the named parameters
		    query.setParameter("employee_type", request.getEmployeeType());
		    query.setParameter("lower_value", request.getLowerValue());
		    query.setParameter("upper_value", request.getUpperValue());

		    return query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
	public List<Object[]> getCustomWorkLocationSummaryDetails(ReportsQueryDTO request) {
		Session session = entityManager.unwrap(Session.class);
		try {
			// 1. Build the dynamic WHERE clause from the general custom filters.
			String customFilterConditions = createQueryForEmployeeDashboard(request.getQueryList()).toString();

			// 2. Build the base native SQL query
			String q = "SELECT "
					+ " CASE WHEN e.is_apmosys_product = 'true' AND e.is_consultant = 'true' THEN CONCAT('APCS-', REPLACE(e.employeement_id, '-', '')) "
					+ "      WHEN e.is_consultant = 'true' THEN CONCAT('CS-', REPLACE(e.employeement_id, '-', '')) "
					+ "      WHEN e.is_apmosys_product = 'true' OR e.email LIKE '%ap2l.ai%' THEN CONCAT('AP-', REPLACE(e.employeement_id, '-', '')) "
					+ "      ELSE CONCAT('A-', REPLACE(e.employeement_id, '-', '')) "
					+ " END as EMPLOYEEMENT_ID, "
					+ " CASE WHEN e.is_apmosys_product = 'true' AND e.is_consultant = 'true' THEN 'Apmosys Product Consultant' "
					+ "      WHEN e.is_apmosys_product = 'true' OR e.email LIKE '%ap2l.ai%' THEN 'ApmosysProduct' "
					+ "      WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' "
					+ "      WHEN e.is_consultant = 'true' THEN 'Consultant' "
					+ "      ELSE 'Regular' "
					+ " END AS EMPLOYMENT_TYPE, "
					+ " e.name AS NAME, "
					// + " e.experience AS EXPERIENCE, "
					+ " d.name AS DEPARTMENT_NAME, "
					+ " e.email AS EMAIL_ID, "
					+ " m.name AS MANAGER_NAME, "
					+ " e.billable AS BILLABLE, "
					+ " e.billable_type AS BILLABLE_TYPE, "
					+ " GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES, "
					+ " GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES, "
					// + " e.date_of_joining AS DATE_OF_JOINING, "
					// + " e.mobile_no AS MOBILE_NO, "
					// + " e.employmentstatus AS STATUS, "
					+ " e.total_experience AS TOTAL_EXPERIENCE, "
					+ " e.gender AS GENDER, "
					+ " e.work_location AS WORK_LOCATION, "
					// + " cl.client_location, "
					// + " TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) as age, "
					// + " e.is_user_info_updated AS KYC, "
					+ " m.emp_id AS MANAGER_ID, "
					+ " e.emp_id AS EMP_ID,e.date_of_joining AS DATE_OF_JOINING "
					+ "FROM employee e "
					+ "INNER JOIN employee_team_mapping etm on etm.emp_id = e.emp_id "
					+ "INNER JOIN employee_timesheets et ON et.emp_id = etm.emp_id "
					+ "INNER JOIN employee_timesheet_activities_mapping etam ON etam.timesheet_id = et.timesheet_id "
					+ "INNER JOIN activities a ON a.activity_id = etam.activity_id "
					+ "INNER JOIN teams t on etm.team_id = t.team_id and etm.team_id = a.team_id "
					+ "INNER JOIN projects p on p.project_id = t.project_id "
					+ "INNER JOIN client_locations cl on cl.client_location_id = etam.client_location_id "
					// Other necessary joins for filtering
					+ "INNER JOIN job_role jr on e.job_role_id = jr.job_role_id "
					+ "INNER JOIN department d on jr.dept_id = d.dept_id "
					+ "INNER JOIN employee m on m.emp_id = e.manager_id "
					+ "INNER JOIN clients c on p.client_id = c.client_id "
					+ "WHERE e.employmentstatus != 'InActive' AND etm.active != 0 "
					+ "AND t.is_active = 'Y' AND p.active = 'true' "
					+ "AND e.emp_id NOT BETWEEN 1 AND 6 "
					+ "AND cl.client_location = :work_location " // Specific drill-down filter
					// 3. Inject the dynamic general filter conditions
					+ customFilterConditions
					+ "GROUP BY e.emp_id";

			System.out.println("Executing Work Location Drill-down Query: " + q);
			Query query = session.createSQLQuery(q);
			
			// 4. Set the named parameter for the specific work location
			query.setParameter("work_location", request.getWorkLocation());

			return query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	// --------------------------Leave Trend Analysis-----------------------------
	public List<Object[]> getCustomLeaveTrendAnalysis(ReportsQueryDTO request) {
		Session session = entityManager.unwrap(Session.class);
		try {
			// 1. Build the dynamic WHERE clause from the general custom filters.
			// Reusing createQueryForLeaveTrend, which is suitable for employee/department filters.
			String customFilterConditions = createQueryForLeaveTrend(request.getQueryList()).toString();

			// 2. Build the base native SQL query
			String q = "SELECT "
					+ "    ld.employeement_id, ld.department, ld.employee_name, ld.from_date, ld.to_date, ld.status, "
					+ "    ld.from_date_day_type, ld.to_date_day_type, ld.employment_type, ld.manager_name, "
					+ "    ld.type_of_leave, ld.leave_date, ld.manager_id, ld.emp_id, "
					+ "    ld.is_consultant_flag, ld.is_apmosys_product_flag, ld.is_apprenticeship_flag "
					+ "FROM ( "
					+ "    SELECT "
					+ "        e.employeement_id AS employeement_id, "
					+ "        d.name AS department, "
					+ "        e.name AS employee_name, "
					+ "        el.from_date, el.to_date, ls.status, "
					+ "        el.from_date_day_type, el.to_date_day_type, "
					+ "        CASE "
					+ "            WHEN e.is_apmosys_product = 'true' AND e.is_consultant = 'true' THEN 'Apmosys Product Consultant' "
					+ "            WHEN e.is_apmosys_product = 'true' THEN 'Apmosys Product' "
					+ "            WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' "
					+ "            WHEN e.is_consultant = 'true' THEN 'Consultant' "
					+ "            ELSE 'Regular' "
					+ "        END AS employment_type, "
					+ "        e2.name AS manager_name, "
					+ "        DATE(:fetch_date) AS leave_date, "
					+ "        CASE "
					+ "            WHEN el.leave_type_master_id = 1 THEN 'Paid Leave' "
					+ "            WHEN el.leave_type_master_id = 2 THEN 'Casual Leave' "
					+ "            WHEN el.leave_type_master_id = 3 THEN 'Leave Without Pay' "
					+ "            WHEN el.leave_type_master_id = 4 THEN 'Compensatory Off' "
					+ "            WHEN el.leave_type_master_id = 5 THEN 'Maternity Leave' "
					+ "        END AS type_of_leave, "
					+ "        CASE " // Simplified logic to check if on leave on the specific date
					+ "            WHEN el.from_date <= DATE(:fetch_date) AND el.to_date >= DATE(:fetch_date) THEN 1 "
					+ "            ELSE 0 "
					+ "        END AS is_on_leave, "
					+ "        e2.emp_id AS manager_id, "
					+ "        e.emp_id AS emp_id, "
					+ "        e.is_consultant AS is_consultant_flag, "
					+ "        e.is_apmosys_product AS is_apmosys_product_flag, "
					+ "        e.is_apprenticeship AS is_apprenticeship_flag "
					+ "    FROM employee_leave el "
					+ "    INNER JOIN employee e ON e.emp_id = el.emp_id "
					+ "    INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id "
					+ "    INNER JOIN department d ON d.dept_id = jr.dept_id "
					+ "    INNER JOIN leave_status ls ON ls.leave_status_id = el.leave_status_id "
					+ "    LEFT JOIN employee e2 ON e2.emp_id = e.manager_id " // Join for manager filter
					+ "    WHERE el.leave_status_id = 2 "
					// 3. Inject the dynamic general filter conditions
					+ customFilterConditions
					+ ") AS ld "
					+ "WHERE ld.is_on_leave > 0 "
					+ "AND ld.type_of_leave = :type_of_leave";

			System.out.println("Executing Leave Trend Drill-down Query: " + q);
			Query query = session.createSQLQuery(q);

			// 4. Set the named parameters
			query.setParameter("fetch_date", request.getFetchDate());
			query.setParameter("type_of_leave", request.getTypeOfLeave());

			return query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
	
	
//	--------------------Leave Trend Analysis Table--------------------------------//
	public List<Object[]> getCustomJoinVsResignEmployeeDetails(ReportsQueryDTO request) {
		Session session = entityManager.unwrap(Session.class);
		try {
			// 1. Build the dynamic WHERE clause from the general custom filters.
			String customFilterConditions = createQueryForEmployeeDashboard(request.getQueryList()).toString();

			// 2. Build the base native SQL query
			String q = "SELECT DISTINCT "
			        + EmployeeEmploymentIdUtil.sqlCaseFormattedEmploymentIdWithAp2lEmail("e", "employeement_id") + " AS EMPLOYEEMENT_ID, "
			        + "    CASE "
			        + "        WHEN e.is_apmosys_product = 'true' AND e.is_consultant = 'true' THEN 'Apmosys Product Consultant' "
			        + "        WHEN e.is_apmosys_product = 'true' OR e.email LIKE '%ap2l.ai%' THEN 'ApmosysProduct' "
			        + "        WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' "
			        + "        WHEN e.is_consultant = 'true' THEN 'Consultant' "
			        + "        ELSE 'Regular' "
			        + "    END AS EMPLOYMENT_TYPE, "
			        + "    e.name AS NAME, "
			        + "    e.experience AS EXPERIENCE, "
			        + "    d.name AS DEPARTMENT_NAME, "
			        + "    e.email AS EMAIL_ID, "
			        + "    m.name AS MANAGER_NAME, "
			        + "    e.billable AS BILLABLE, "
			        + "    e.billable_type AS BILLABLE_TYPE, "
			        + "    GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES, "
			        + "    GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES, "
			        + "    e.date_of_joining AS DATE_OF_JOINING, "
			        + "    e.mobile_no AS MOBILE_NO, "
			        + "    e.employmentstatus AS STATUS, "
			        + "    e.total_experience AS TOTAL_EXPERIENCE, "
			        + "    e.gender AS GENDER, "
			        + "    e.work_location AS WORK_LOCATION, "
			        + "    TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) AS AGE, "
			        + "    e.is_user_info_updated AS KYC, "
			        + "    m.emp_id AS MANAGER_ID, "
			        + "    e.emp_id AS EMP_ID "
			        + "FROM employee e "
			        + "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id "
			        + "LEFT JOIN department d ON jr.dept_id = d.dept_id "
			        + "LEFT JOIN employee m ON m.emp_id = e.manager_id "
			        + "LEFT JOIN employee_team_mapping etm ON e.emp_id = etm.emp_id AND etm.active != '0' "
			        + "LEFT JOIN teams t ON etm.team_id = t.team_id AND t.is_active = 'Y' "
			        + "LEFT JOIN projects p ON t.project_id = p.project_id AND p.active = 'true' "
			        + "LEFT JOIN clients c ON p.client_id = c.client_id "
			        + "WHERE e.emp_id NOT BETWEEN 1 AND 6 "
			        + "AND ( "
			        + "   (:type = 'apmosys product consultant' AND e.is_apmosys_product = 'true' AND e.is_consultant = 'true' AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) "
			        + "   OR (:type = 'apmosysproduct' AND (e.is_apmosys_product = 'true' OR e.email LIKE '%ap2l.ai%') AND COALESCE(e.is_consultant, 'false') != 'true' AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) "
			        + "   OR (:type = 'apprenticeship' AND e.is_apprenticeship = 'true' AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) "
			        + "   OR (:type = 'consultant' AND e.is_consultant = 'true' AND COALESCE(e.is_apmosys_product, 'false') != 'true' AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) "
			        + "   OR (:type = 'apmosys product' AND e.is_apmosys_product = 'true' AND COALESCE(e.is_consultant, 'false') != 'true' AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) "
			        + "   OR (:type = 'regular' AND ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false' AND e.is_apmosys_product = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = '')) AND MONTHNAME(e.date_of_joining) = :month_name AND YEAR(e.date_of_joining) = :YEAR_VALUE) "
			        + "   OR (:type = 'resign' AND e.date_of_resign IS NOT NULL AND MONTHNAME(e.date_of_resign) = :month_name AND YEAR(e.date_of_resign) = :YEAR_VALUE) "
			        + ") "
			        // 3. Inject the dynamic general filter conditions
			        + customFilterConditions
			        + "GROUP BY e.emp_id";

			System.out.println("Executing Join/Resign Drill-down Query: " + q);
			Query query = session.createSQLQuery(q);

			// 4. Set the named parameters
			query.setParameter("type", request.getEmployeeType());
			query.setParameter("month_name", request.getMonthName());
			query.setParameter("YEAR_VALUE", request.getYear());

			return query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
	
	
	//------------------------All employee box---------------------------------//
	public List<Object[]> getCustomEmployeesByEmploymentTypeList(ReportsQueryDTO request) {
	    Session session = entityManager.unwrap(Session.class);
	    try {
	        // 1. Build the dynamic part of the WHERE clause from the filter list
	        String customFilterConditions = createQueryForEmployeeDashboard(request.getQueryList()).toString();

	        // 2. Build the full native SQL query by combining the static parts with the dynamic part
	        String q = "SELECT distinct " +
	        EmployeeEmploymentIdUtil.sqlCaseFormattedEmploymentId("e", "employeement_id") + " as EMPLOYEEMENT_ID, " +
	        "CASE " +
	        " WHEN e.is_apmosys_product = 'true' AND e.is_consultant = 'true' THEN 'Apmosys Product Consultant' " +
	        " WHEN e.is_apmosys_product = 'true' THEN 'ApmosysProduct' " +
	        " WHEN e.is_apprenticeship = 'true' THEN 'Apprentice' " +
	        " WHEN e.is_consultant = 'true' THEN 'Consultant' " +
	        " ELSE 'Regular' " +
	        "END AS EMPLOYMENT_TYPE, " +
	        "e.name AS NAME, " +
	        "e.experience AS EXPERIENCE, " +
	        "d.name AS DEPARTMENT_NAME, " +
	        "e.email AS EMAIL_ID, " +
	        "m.name AS MANAGER_NAME, " +
	        "e.billable AS BILLABLE, " +
	        "e.billable_type AS BILLABLE_TYPE, " +
	        "GROUP_CONCAT(DISTINCT p.project_name SEPARATOR ', ') AS PROJECT_NAMES, " +
	        "GROUP_CONCAT(DISTINCT c.client_name SEPARATOR ', ') AS CLIENT_NAMES, " +
	        "e.date_of_joining AS DATE_OF_JOINING, " +
	        "e.mobile_no AS MOBILE_NO, " +
	        "e.employmentstatus AS STATUS, " +
	        "e.total_experience AS TOTAL_EXPERIENCE, " +
	        "e.gender AS GENDER, " +
	        "e.work_location AS WORK_LOCATION, " +
	        "TIMESTAMPDIFF(YEAR, e.date_of_birth, CURRENT_DATE()) as age, " +
	        "e.is_user_info_updated AS KYC, " +
	        "m.emp_id AS MANAGER_ID, " +
	        "e.emp_id AS EMP_ID " +
	        "FROM employee e " +
	        "LEFT JOIN job_role jr on e.job_role_id = jr.job_role_id " +
	        "LEFT JOIN department d on jr.dept_id = d.dept_id " +
	        "LEFT JOIN employee m on m.emp_id = e.manager_id " +
	        "LEFT JOIN employee_team_mapping etm on e.emp_id = etm.emp_id AND etm.active != '0' " +
	        "LEFT JOIN teams t on etm.team_id = t.team_id AND t.is_active = 'Y' " +
	        "LEFT JOIN projects p on t.project_id = p.project_id AND p.active = 'true' " +
	        "LEFT JOIN clients c on p.client_id = c.client_id " +
	        "WHERE e.employmentstatus != 'InActive' AND e.emp_id NOT BETWEEN 1 AND 6 " +
	        "AND ((:apprentice = true AND e.is_apprenticeship = 'true') " +
	        " OR (:consultant = true AND e.is_consultant = 'true') " +
	        " OR (:regular = true AND ((e.is_consultant = 'false' AND e.is_apprenticeship = 'false' AND e.is_apmosys_product = 'false') OR (COALESCE(e.is_consultant, '') = '' AND COALESCE(e.is_apprenticeship, '') = ''))) " +
	        " OR (:probation = true AND e.employmentstatus = 'Probation' AND TIMESTAMPDIFF(DAY, e.date_of_joining, current_date()) > 180) " +
	        " OR (:apmosysProduct = 'true' AND e.is_apmosys_product = 'true') " +
	        " OR (:apmosysProductConsultant = true AND e.is_apmosys_product = 'true' AND e.is_consultant = 'true') " +
	        " OR (:allEmp = true)) " +
	        customFilterConditions +
	        "GROUP BY e.emp_id";

	        System.out.println("Executing dynamic findEmployeesByEmploymentType Query: " + q);
	        Query query = session.createSQLQuery(q);

	        // 3. Set the named parameters for the static part of the query
	        query.setParameter("apprentice", request.isApprentice());
	        query.setParameter("consultant", request.isConsultant());
	        query.setParameter("regular", request.isRegular());
	        query.setParameter("probation", request.isProbation());
	        query.setParameter("apmosysProduct", request.getIsApmosysProduct());
	        query.setParameter("apmosysProductConsultant", request.isApmosysProductConsultant());
	        query.setParameter("allEmp", request.isAllEmp());

	        // 4. Execute and return the results
	        return query.getResultList();

	    } catch (Exception e) {
	        e.printStackTrace();
	        // Return an empty list in case of an error to prevent crashes
	        return new ArrayList<>();
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	}

	private void applyEmployeeReportDisplay(EmployeeDTO dto) {
		if (dto == null) {
			return;
		}
		dto.setEmployeeType(EmployeeEmploymentIdUtil.resolveEmployeeType(dto.getIsConsultant(),
				dto.getIsApmosysProduct(), dto.getIsApprenticeship()));
		if (dto.getEmployeementId() != null) {
			String numericId = dto.getEmployeementId().toString().replaceFirst("(?i)^(APCS-|AP-|CS-|A-)", "");
			dto.setEmploymentIdAcToET(EmployeeEmploymentIdUtil.formatEmploymentId(numericId, dto.getIsConsultant(),
					dto.getIsApmosysProduct()));
		}
	}

	private void applyLeaveDtoEmployeeDisplay(LeaveDTO dto) {
		if (dto == null) {
			return;
		}
		dto.setEmployeeType(EmployeeEmploymentIdUtil.resolveEmployeeType(dto.getIsConsultant(),
				dto.getIsApmosysProduct(), dto.getIsApprenticeship()));
		if (dto.getEmployeementId() != null) {
			String numericId = dto.getEmployeementId().toString().replaceFirst("(?i)^(APCS-|AP-|CS-|A-)", "");
			dto.setEmploymentIdAcToET(EmployeeEmploymentIdUtil.formatEmploymentId(numericId, dto.getIsConsultant(),
					dto.getIsApmosysProduct()));
		}
	}

	private String formatFilterDropdownEmploymentId(Object rawEmploymentId, Object isConsultant, Object isApmosysProduct) {
		if (rawEmploymentId == null) {
			return null;
		}
		String numeric = rawEmploymentId.toString().replaceFirst("(?i)^(APCS-|AP-|CS-|A-)", "");
		return EmployeeEmploymentIdUtil.formatEmploymentId(numeric,
				isConsultant != null ? isConsultant.toString() : "false",
				isApmosysProduct != null ? isApmosysProduct.toString() : "false");
	}

	private void addEmployeeIdFilterOptionsFromRows(List<Object[]> allEmployeeList, List<EmployeeDTO> dtoList) {
		java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>();
		allEmployeeList.forEach((object) -> {
			String formatted = formatFilterDropdownEmploymentId(object[0],
					object.length > 76 ? object[76] : null,
					object.length > 89 ? object[89] : null);
			if (formatted != null && seen.add(formatted)) {
				EmployeeDTO dto = new EmployeeDTO();
				dto.setName(formatted);
				dtoList.add(dto);
			}
		});
	}

	private List<EmployeeDTO> buildStaticValueOptionList(String column) {
		List<EmployeeDTO> dtoList = new ArrayList<>();
		switch (column) {
		case "employeeType":
		case "Employee Type":
			addEmployeeTypeFilterOptions(dtoList);
			return dtoList;
		case "Employment Status": {
			String[] status = new String[] { "Probation", "Confirmed", "Resigned", "InActive" };
			for (String value : status) {
				EmployeeDTO dto = new EmployeeDTO();
				dto.setName(value);
				dtoList.add(dto);
			}
			return dtoList;
		}
		case "Gender": {
			String[] values = new String[] { "male", "female", "other" };
			for (String value : values) {
				EmployeeDTO dto = new EmployeeDTO();
				dto.setName(value);
				dtoList.add(dto);
			}
			return dtoList;
		}
		case "Status": {
			String[] values = new String[] { "Pending", "Approved", "Rejected" };
			for (String value : values) {
				EmployeeDTO dto = new EmployeeDTO();
				dto.setName(value);
				dtoList.add(dto);
			}
			return dtoList;
		}
		case "Day Type": {
			String[] values = new String[] { "Working", "Holiday", "Non-working", "Public Holiday", "Leave",
					"Week Off", "Comp Off" };
			for (String value : values) {
				EmployeeDTO dto = new EmployeeDTO();
				dto.setName(value);
				dtoList.add(dto);
			}
			return dtoList;
		}
		default:
			return null;
		}
	}

	private void addEmployeeTypeFilterOptions(List<EmployeeDTO> dtoList) {
		String[] types = { "Regular", "Consultant", "ApMoSys Product", "ApMoSys Product Consultant", "Apprentice" };
		for (String type : types) {
			EmployeeDTO dto = new EmployeeDTO();
			dto.setName(type);
			dtoList.add(dto);
		}
	}

	private void appendPrefixedEmployeeIdFilter(StringBuilder query, String value, String operator, String conjunction,
			boolean leadingAnd) {
		if (value == null) {
			return;
		}
		String prefix = leadingAnd ? " AND " : " ";
		if ("=".equals(operator)) {
			String upper = value.toUpperCase();
			if (upper.startsWith("APCS-")) {
				query.append(prefix).append("e.employeement_id = '").append(value.substring(5))
						.append("' AND e.is_apmosys_product = 'true' AND e.is_consultant = 'true' ");
			} else if (upper.startsWith("CS-")) {
				query.append(prefix).append("e.employeement_id = '").append(value.substring(3))
						.append("' AND e.is_consultant = 'true' AND COALESCE(e.is_apmosys_product, 'false') != 'true' ");
			} else if (upper.startsWith("AP-")) {
				query.append(prefix).append("e.employeement_id = '").append(value.substring(3))
						.append("' AND e.is_apmosys_product = 'true' AND COALESCE(e.is_consultant, 'false') != 'true' ");
			} else if (upper.startsWith("A-")) {
				query.append(prefix).append("e.employeement_id = '").append(value.substring(2))
						.append("' AND COALESCE(e.is_consultant, 'false') != 'true' ")
						.append("AND COALESCE(e.is_apmosys_product, 'false') != 'true' ");
			} else {
				query.append(prefix).append("e.employeement_id ").append(operator).append(" '").append(value)
						.append("' ");
			}
		} else {
			query.append(prefix).append("e.employeement_id ").append(operator).append(" '").append(value)
					.append("' ");
		}
		if (conjunction != null) {
			query.append(conjunction);
		}
	}

	private void appendFormattedEmploymentIdLikeFilter(StringBuilder query, String likeValue, String tableAlias) {
		if (likeValue == null) {
			return;
		}
		String escaped = likeValue.replace("'", "''");
		query.append(EmployeeEmploymentIdUtil.sqlCaseFormattedEmploymentId(tableAlias, "employeement_id"))
				.append(" LIKE '").append(escaped).append("' ");
	}

	private String buildEmployeeTypeFilterSql(String employeeTypeValue) {
		return buildEmployeeTypeFilterSql(employeeTypeValue, "e");
	}

	private String buildEmployeeTypeFilterSql(String employeeTypeValue, String tableAlias) {
		if (employeeTypeValue == null || employeeTypeValue.isBlank()) {
			return "1=1";
		}
		String v = employeeTypeValue.replace("%", "").trim();
		if (matchesEmployeeTypeLabel(v, "Apmosys Product Consultant", "ApMoSys Product Consultant")) {
			return "(" + tableAlias + ".is_apmosys_product = 'true' AND " + tableAlias + ".is_consultant = 'true')";
		}
		if (matchesEmployeeTypeLabel(v, "Apmosys Product", "ApMoSys Product")) {
			return "(" + tableAlias + ".is_apmosys_product = 'true' AND COALESCE(" + tableAlias
					+ ".is_consultant, 'false') != 'true')";
		}
		if (matchesEmployeeTypeLabel(v, "Consultant")) {
			return "(" + tableAlias + ".is_consultant = 'true' AND COALESCE(" + tableAlias
					+ ".is_apmosys_product, 'false') != 'true')";
		}
		if (matchesEmployeeTypeLabel(v, "Apprentice")) {
			return "(" + tableAlias + ".is_apprenticeship = 'true')";
		}
		if (matchesEmployeeTypeLabel(v, "Regular")) {
			return "((" + tableAlias + ".is_consultant = 'false' AND COALESCE(" + tableAlias
					+ ".is_apmosys_product, 'false') != 'true' AND COALESCE(" + tableAlias
					+ ".is_apprenticeship, 'false') != 'true') "
					+ "OR (COALESCE(" + tableAlias + ".is_consultant, '') = '' AND COALESCE(" + tableAlias
					+ ".is_apprenticeship, '') = ''))";
		}
		return "1=1";
	}

	private String buildEmployeeTypeLikeFilterSql(String likeValue, String tableAlias) {
		if (likeValue == null || likeValue.isBlank()) {
			return "1=1";
		}
		String escaped = likeValue.replace("'", "''");
		return "(" + sqlEmployeeTypeDisplayCase(tableAlias) + ") LIKE '" + escaped + "'";
	}

	private String sqlEmployeeTypeDisplayCase(String tableAlias) {
		return "CASE "
				+ "WHEN " + tableAlias + ".is_apmosys_product = 'true' AND " + tableAlias
				+ ".is_consultant = 'true' THEN 'Apmosys Product Consultant' "
				+ "WHEN " + tableAlias + ".is_consultant = 'true' THEN 'Consultant' "
				+ "WHEN " + tableAlias + ".is_apprenticeship = 'true' THEN 'Apprentice' "
				+ "WHEN " + tableAlias + ".is_apmosys_product = 'true' THEN 'Apmosys Product' "
				+ "ELSE 'Regular' "
				+ "END";
	}

	private boolean matchesEmployeeTypeLabel(String value, String... candidates) {
		for (String candidate : candidates) {
			if (candidate.equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

}

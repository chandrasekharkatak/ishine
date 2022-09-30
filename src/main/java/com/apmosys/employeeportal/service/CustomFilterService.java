package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.CustomFilterDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class CustomFilterService {
	
	@PersistenceContext
    private EntityManager entityManager;

	public StringBuilder createQuery(List<CustomFilterDTO> queryList) {
			StringBuilder query = new StringBuilder("");
			
				for(CustomFilterDTO dto: queryList) {
					if(dto.getOperator()!=null && dto.getOperator().equals("like")) {
						dto.setValue("%"+dto.getValue()+"%");
					}
					
					switch (dto.getColumn()) {
					case "employeementId": {
						query = query.append(" e.employeement_id ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "employeeName": {
						query = query.append(" e.name ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "leaveType": {
						query = query.append(" ltm.leave_type ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}

					case "fromDate": {
						query = query.append(" el.from_date ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "toDate": {
						query = query.append(" el.to_date ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "noOfDays": {
						query = query.append(" el.no_of_days ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "reason": {
						query = query.append(" el.reason ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "status": {
						query = query.append(" ls.status ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "managerName": {
						query = query.append(" e2.name ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "createdOn": {
						query = query.append(" el.created_on ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "updatedOn": {
						query = query.append(" el.updated_on ").append(dto.getOperator() + " '")
								.append(dto.getValue() + "' ").append(dto.getConjunction());
						break;
					}
					case "leaveStatusUpdatedByName": {
						query = query.append(" e3.leave_status_updated_by_name ").append(dto.getOperator() + " '")
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
				String q="select e.employeement_id, e.name as employee, ltm.leave_type, el.from_date, el.to_date, "
						+ "el.no_of_days,el.reason, ls.status, e2.name as manager, el.created_on, "
						+ "el.updated_on, e3.name as statusUpdateBy from employee_leave el "
						+ "inner join employee e on el.emp_id = e.emp_id "
						+ "inner join leave_type_master ltm on el.leave_type_master_id = ltm.leave_type_master_id "
						+ "inner join leave_status ls on el.leave_status_id = ls.leave_status_id "
						+ "inner join employee e2 on el.manager_id = e2.emp_id "
						+ "inner join employee e3 on el.leave_status_updated_by = e3.emp_id where "+customQuery;
				
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
			
			StringBuilder subQuery = createQuery(leaveDTO.getQueryList());
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
		}
		return response;
	}
	
}

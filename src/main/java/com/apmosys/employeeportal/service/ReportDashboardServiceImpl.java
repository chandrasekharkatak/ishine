package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.ReportDTO;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.serviceInterface.ReportDashboardService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ReportDashboardServiceImpl implements ReportDashboardService {

	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;

	@Override
	public ServiceResponse getLast8DaysLeaveReport(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			LocalDate start = LocalDate.now().minusDays(8);

			LocalDate end = LocalDate.now();

			List<Object[]> list = employeeLeaveRepository.getLast8DaysLeaveReport(start, end);

			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave history not found. Kindly check date range.");
			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setDepartmentName(object[1] != null ? object[1].toString() : null);
					dto.setEmployeeName(object[2] != null ? object[2].toString() : null);
					dto.setFromDate(object[3] != null ? object[3].toString() : null);
					dto.setToDate(object[4] != null ? object[4].toString() : null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
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

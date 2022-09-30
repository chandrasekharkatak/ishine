package com.apmosys.employeeportal.serviceInterface;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;

public interface ReportDashboardService {

	ServiceResponse getLast8DaysLeaveReport(LeaveDTO leaveDTO);

}

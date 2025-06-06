package com.apmosys.employeeportal.serviceInterface;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;

public interface ReportDashboardService {

	ServiceResponse getLast8DaysLeaveReport(LeaveDTO leaveDto);

	ServiceResponse getLast9DaysTimesheetReport();

	ServiceResponse getLeaveTrendAnalysisReport(LeaveDTO leaveDto);

	ServiceResponse getEmployeeWorkLocationForSummary();

	ServiceResponse getDepartmentWiseBillableData(LeaveDTO leaveDto);
	
	//for employee status summary , gender summary , and fresher-lateral summary 
	ServiceResponse getAllGraphEmployeeSummary();
}

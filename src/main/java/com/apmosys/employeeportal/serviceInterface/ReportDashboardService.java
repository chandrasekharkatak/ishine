package com.apmosys.employeeportal.serviceInterface;

import java.util.List;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.PieChartListDTO;
import com.apmosys.employeeportal.dto.PieParamDTO;
import com.apmosys.employeeportal.dto.ReportsQueryDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;

public interface ReportDashboardService {

	ServiceResponse getLast8DaysLeaveReport(LeaveDTO leaveDto);

	ServiceResponse getLast9DaysTimesheetReport();

	ServiceResponse getLeaveTrendAnalysisReport(LeaveDTO leaveDto);

	ServiceResponse getEmployeeWorkLocationForSummary();

	ServiceResponse getDepartmentWiseBillableData(LeaveDTO leaveDto);
		
	ServiceResponse getEmployeeDetailsByDepartmentAndBillableType(ReportsQueryDTO request);
	//for employee status summary , gender summary , and fresher-lateral summary 
	ServiceResponse getAllGraphEmployeeSummary();

	ServiceResponse getAllPieGraphListSummary(PieParamDTO pieParamDto);
	ServiceResponse getJoiningVsResignationCount(ReportsQueryDTO request) ;
	ServiceResponse getDepartmentWiseKycCount() ;
	
	
	ServiceResponse  getAllEmployeeCountDepartmentWise();
	
	ServiceResponse getDepartmentWiseBillableNonBillableSummary(ReportsQueryDTO request);
	ServiceResponse getEmployeeDetailsByEmploymentType(ReportsQueryDTO request);
	ServiceResponse getEmployeesByExperience(ReportsQueryDTO request);
	ServiceResponse getEmployeeDetailsByDepartmentAndKyc(ReportsQueryDTO request);

	ServiceResponse getDepartmentwiseEmployeesByType(ReportsQueryDTO request);
	ServiceResponse getJoinVsResignEmployeeDetails(ReportsQueryDTO request);
	ServiceResponse getLeaveTrendDetails();
	
	ServiceResponse getLeaveTrendAnalysis(ReportsQueryDTO request);
	ServiceResponse getWorkLocationDetails();
	ServiceResponse getWorkLocationSummaryDetails(ReportsQueryDTO reportQueryDTO);
		}
	

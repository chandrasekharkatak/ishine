package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BioMaTO {
	private String logDate;
    private String employeeCode;
    private String employeeName;
    private String totalDuration;
    private String shiftName;
    private String beginTime;
    private String endTime;
    private String status;
    private String punchRecords;
    private String earlyBy;
    private String lateBy;
    private String duration;
    private String inTime;
    private String outTime;
    private String shiftDuration;
    private String totalOverTime;
    private String totalUnderTimeE;
    private String attendanceDate;
    private String deduct;
    private Long employementId;
    private Long departmentId;
    private Long empId;
	private String departmentName;
	private String reportingManagerName;
    
    
    public void setDeduct(String deduct) {
    	this.deduct = deduct;
    }
    public String getDeduct() {
    	return deduct;
    }
    public void setAttendanceDate(String attendanceDate) {
    	this.attendanceDate = attendanceDate;
    }
    public String getAttendanceDate() {
    	return attendanceDate;
    }
	public String getLogDate() {
		return logDate;
	}
	public void setLogDate(String logDate) {
		this.logDate = logDate;
	}
	public String getEmployeeCode() {
		return employeeCode;
	}
	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}
	public String getEmployeeName() {
		return employeeName;
	}
	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}
	public String getTotalDuration() {
		return totalDuration;
	}
	public void setTotalDuration(String totalDuration) {
		this.totalDuration = totalDuration;
	}
	public String getShiftName() {
		return shiftName;
	}
	public void setShiftName(String shiftName) {
		this.shiftName = shiftName;
	}
	public String getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPunchRecords() {
		return punchRecords;
	}
	public void setPunchRecords(String punchRecords) {
		this.punchRecords = punchRecords;
	}
	public String getEarlyBy() {
		return earlyBy;
	}
	public void setEarlyBy(String earlyBy) {
		this.earlyBy = earlyBy;
	}
	public String getLateBy() {
		return lateBy;
	}
	public void setLateBy(String lateBy) {
		this.lateBy = lateBy;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getInTime() {
		return inTime;
	}
	public void setInTime(String inTime) {
		this.inTime = inTime;
	}
	public String getOutTime() {
		return outTime;
	}
	public void setOutTime(String outTime) {
		this.outTime = outTime;
	}
	public String getShiftDuration() {
		return shiftDuration;
	}
	public void setShiftDuration(String shiftDuration) {
		this.shiftDuration = shiftDuration;
	}
	public String getTotalOverTime() {
		return totalOverTime;
	}
	public void setTotalOverTime(String totalOverTime) {
		this.totalOverTime = totalOverTime;
	}
	public String getTotalUnderTimeE() {
		return totalUnderTimeE;
	}
	public void setTotalUnderTimeE(String totalUnderTimeE) {
		this.totalUnderTimeE = totalUnderTimeE;
	}
	public Long getEmployementId() {
		return employementId;
	}
	public void setEmployementId(Long employementId) {
		this.employementId = employementId;
	}
	public Long getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}
	public Long getEmpId() {
		return empId;
	}
	public void setEmpId(Long empId) {
		this.empId = empId;
	}
	
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	public String getReportingManagerName() {
		return reportingManagerName;
	}
	public void setReportingManagerName(String reportingManagerName) {
		this.reportingManagerName = reportingManagerName;
	}
    

    
}
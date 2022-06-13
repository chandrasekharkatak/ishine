package com.apmosys.employeeportal.projections;

import org.springframework.beans.factory.annotation.Value;

public interface Leave {
	
	@Value("#{target.leave_type}")
	public String getLeaveType();
	
	public Short getBalance();
	
	@Value("#{target.pending_for_approval}")
	public Short getPendingForApproval();

}

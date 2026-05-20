package com.apmosys.employeeportal.dto;

public interface PendingCompOffDTO {

    Long getCompOffLeaveId();
    String getCompOffReasons();
    String getDescription();
    Object getCreatedOn();
    String getCreatedByName();
    String getStatus();
    Object getFromDate();
    Object getToDate();
    Double getNoOfDays();
    Long getEmpId();
    String getEmail();
    Long getEmployeementId();
    Long getManagerId();
    String getManagerEmail();
    Integer getFinalApprovalLevel();
    Long getLevel2ApproverId();
    String getManagerApprovalStatus();
    Long getReportingManagerId();
    Integer getCurrentApprovalLevel();
    String getLevel2ApprovalStatus();
    String getLevel2ApproverName();
    String getApproverName();
}
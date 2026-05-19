package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;

import lombok.Data;

public interface ExpiredProjectDTOForNotification {
    Integer getProjectId();
    String getClientName();
    String getClientLocation();
    String getState();
    String getProjectName();
    String getDescription();
    Long getProjectManagerId();
    Long getEmpId();
    Timestamp getApprovedOn();
    Timestamp getCreatedOn();
    Integer getClientId();
    Long getPoClientId();
    String getDepartmentName();
    Long getPoProjectId();
    String getActive();
    String getSyncProject();
    String getIsDraftProject();
    Long getCreatedBy();
    Long getUpdatedBy();
    LocalDateTime getUpdatedOn();
    String getRole();
    Integer getCount();
    String getExperience();
    String getPoNo();
    String getPoProjectType();
    String getApmosysRM();
    String getClientRM();
    Boolean getIsRenewable();  // Changed to Boolean wrapper
    String getDeptId();
    String getStatus();
    String getApmosysRmEmail();
    String getProjectCompletionDate();
    String getProjectStatus();
    String getInternalProjectType();
    Boolean getHasClientSideId();  // Changed to Boolean wrapper
    Boolean getClientFlag();  // Changed to Boolean wrapper
    String getStartDate();
    String getEndDate();
}

package com.apmosys.employeeportal.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectPo {
	
    private String poNo;
    private String department;
    private String projectName;
    private Date projectStartDate;
    private Date projectEndDate;
    private String apmosysRm;
    private boolean isRenewable;
    private String poProjectType;
    private String hodEmail;
    private String poProjectId;
    
}

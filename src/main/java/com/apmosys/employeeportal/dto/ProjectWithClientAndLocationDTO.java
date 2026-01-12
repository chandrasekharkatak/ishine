package com.apmosys.employeeportal.dto;


import java.util.List;

import com.apmosys.employeeportal.model.PortalConfig;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectWithClientAndLocationDTO {
	
	  // ===== Client Fields =====
    private Integer clientId;
    private String clientName;

    // ===== Client Location Fields =====
    private Integer clientLocationId;
    private String clientLocation;

    // ===== Project Fields =====
    private Integer projectId;
    private String projectName;

    // ===== Team Fields =====
    private Long teamId;
    private String teamName;
    
    // ===== Client Flags ====
    private Boolean hasClientSideId;
    private Boolean hasClientFlag;
    
	

}

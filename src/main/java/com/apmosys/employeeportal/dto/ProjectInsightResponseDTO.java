package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ProjectInsightResponseDTO {
	
	private Map<String, Object> option;
    private List<Map<String, Object>> optionsList;
    private boolean showDocDiv;
    private String isDraft; // "Y" or "N"
    private List<Object> projectInsightResponsePointList;
    private List<String> tags;
    private Long responseBy;
    private String responseByEmpName;
    private LocalDateTime assignedOn;
    private String newTag;
    private String response;
    private List<ReviewerInfoDTO> reviewerInfo;

}

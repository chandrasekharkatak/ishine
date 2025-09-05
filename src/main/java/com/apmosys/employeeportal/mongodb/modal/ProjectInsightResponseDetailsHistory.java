package com.apmosys.employeeportal.mongodb.modal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import com.apmosys.employeeportal.dto.ReviewerInfoDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "project_insight_response_details_history")
public class ProjectInsightResponseDetailsHistory {
	@Id
	private String id;
	private Map<String, Object> option;
    private List<Map<String, Object>> optionsList;
    private boolean showDocDiv;
    private Boolean isDraft;
    private List<String> tags;
    private String quesId;
    private Long responseBy;
    private String responseByEmpName;
    private LocalDateTime lastSavedOn;
    private String response;
    private List<ReviewerInfoDTO> reviewerInfo;
    Integer version;
}
